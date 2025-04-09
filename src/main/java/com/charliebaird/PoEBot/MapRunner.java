package com.charliebaird.PoEBot;

import com.charliebaird.Minimap.Legend;
import com.charliebaird.Minimap.MinimapExtractor;
import com.charliebaird.TeensyBottingLib.InputCodes.MouseCode;
import com.charliebaird.utility.ScreenCapture;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class MapRunner
{
    private PoEBot bot;

    public MapRunner()
    {
        bot = new PoEBot();
    }

    public void openMap() {}

    public void exitMap()
    {
        bot.mouseRelease(MouseCode.LEFT);
    }

    private final List<Point> recentSelections = new ArrayList<Point>();
    public void runIteration()
    {
        Mat original = ScreenCapture.captureScreenMat();
        MinimapExtractor minimap = new MinimapExtractor(false);
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

            bot.mouseMoveGeneralLocation(screenPoint);
        }

        bot.mousePress(MouseCode.LEFT);

        if (Math.random() < 0.45)
        {
            bot.mouseClick(MouseCode.RIGHT);
        }
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
                // Inverse distance: closer = higher score (avoid div by zero)
                proximityScore += 1.0 / (distance + 1e-5);
            }

            double totalScore = listScore + (DISTANCE_WEIGHT * proximityScore);
            System.out.printf("  List Score: %.2f, Proximity Score: %.5f, Total Score: %.5f%n",
                    listScore, proximityScore * DISTANCE_WEIGHT, totalScore);
            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestPoint = p;
            }
        }

        return bestPoint;
    }
}
