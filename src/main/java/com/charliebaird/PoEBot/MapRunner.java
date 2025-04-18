package com.charliebaird.PoEBot;

import com.charliebaird.Minimap.Legend;
import com.charliebaird.Minimap.MinimapExtractor;
import com.TeensyBottingLib.InputCodes.KeyCode;
import com.TeensyBottingLib.InputCodes.MouseCode;
import com.charliebaird.utility.ScreenCapture;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MapRunner
{
    private final PoEBot bot;

    private final MouseJiggler mouseJiggler;
    private final Thread mouseJigglerThread;

    private final IntermittentAttacker intermittentAttacker;
    private final Thread intermittentAttackerThread;

    private final ScreenScanner screenScanner;
    private final Thread screenScannerThread;

    public MapRunner()
    {
        bot = new PoEBot();

        mouseJiggler = new MouseJiggler(bot);
        mouseJigglerThread = new Thread(mouseJiggler);
        mouseJigglerThread.start();

        intermittentAttacker = new IntermittentAttacker(bot);
        intermittentAttackerThread = new Thread(intermittentAttacker);
        intermittentAttackerThread.start();

        screenScanner = new ScreenScanner(bot);
        screenScannerThread = new Thread(screenScanner);
        screenScannerThread.start();
    }

    public void openMap() {}

    public void exitMap()
    {
        bot.mouseRelease(MouseCode.LEFT);

        mouseJiggler.stop();
        intermittentAttacker.stop();
        screenScanner.stop();

        try {
            mouseJigglerThread.join();
            intermittentAttackerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private final List<Point> recentSelections = new ArrayList<Point>();
    public void runIteration()
    {
        Mat original = ScreenCapture.captureScreenMat();
        MinimapExtractor minimap = new MinimapExtractor(true);
        minimap.resolve(original);

        List<Point> revealPoints = minimap.findRevealPoints();

        if (revealPoints == null || revealPoints.isEmpty())
        {
            System.out.println("No reveal points found");
            return;
        }

        Point point = findBestRevealPoint(revealPoints, recentSelections);
        recentSelections.add(point);

        if (point != null)
        {
            Point screenPoint = Legend.convertMinimapPointToScreen(point);

            // todo move this to a separate thread?
            bot.mouseMoveGeneralLocation(screenPoint);

            // 1 in 5 chance
            if (ThreadLocalRandom.current().nextInt(1, 6) > 4)
            {
                bot.keyClick(KeyCode.SPACE);
            }
        }

        bot.mousePress(MouseCode.LEFT);
    }

    private static final double LIST_POSITION_WEIGHT = 1.0;
    private static final double DISTANCE_WEIGHT = 100.0;
    public static Point findBestRevealPoint(List<Point> revealPoints, List<Point> recentSelections) {
        Point bestPoint = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < revealPoints.size(); i++) {
            Point p = revealPoints.get(i);

            // Score for being early in the list (higher score for earlier points)
            double listScore = LIST_POSITION_WEIGHT * (revealPoints.size() - i);

            // Score based on proximity to recent selections
            double proximityScore = 0.0;
            int count = Math.min(6, recentSelections.size());

            for (int j = 0; j < count; j++) {
                Point recent = recentSelections.get(recentSelections.size() - 1 - j);
                double distance = Legend.euclideanDistance(p, recent);

                // If distance is too close, we might be stuck, so punish this point
                if (distance < 10) {
                    proximityScore = -100;
                    continue;
                }

                // Inverse distance: closer = higher score (avoid div by zero)
                proximityScore += 1.0 / (distance + 1e-5);
            }

            double totalScore = listScore + (DISTANCE_WEIGHT * proximityScore);
//            System.out.printf("  List Score: %.2f, Proximity Score: %.5f, Total Score: %.5f%n",
//                    listScore, proximityScore * DISTANCE_WEIGHT, totalScore);
            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestPoint = p;
            }
        }

        return bestPoint;
    }
}
