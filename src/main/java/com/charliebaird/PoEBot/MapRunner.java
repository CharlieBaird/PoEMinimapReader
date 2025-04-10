package com.charliebaird.PoEBot;

import com.TeensyBottingLib.Utility.SleepUtils;
import com.charliebaird.Minimap.Legend;
import com.charliebaird.Minimap.MinimapExtractor;
import com.TeensyBottingLib.InputCodes.KeyCode;
import com.TeensyBottingLib.InputCodes.MouseCode;
import com.charliebaird.Minimap.MinimapVisuals;
import com.charliebaird.utility.ScreenCapture;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

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

        // Init mouse jiggler background thread, mimics human-like constant mouse jiggle
        mouseJiggler = new MouseJiggler(bot);
        mouseJigglerThread = new Thread(mouseJiggler);
        mouseJigglerThread.start();

        intermittentAttacker = new IntermittentAttacker(bot);
        intermittentAttackerThread = new Thread(intermittentAttacker);
        intermittentAttackerThread.start();

        screenScanner = new ScreenScanner(this);
        screenScannerThread = new Thread(screenScanner);
        screenScannerThread.start();
    }

    public void openMap() {}

    public void exitMap()
    {
        mouseJiggler.stop();

        try {
            mouseJigglerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    boolean influenceDetected = false;
    public void influenceDetected()
    {
        influenceDetected = true;
    }

    public void executiveLoop(int iterations)
    {
        for (int i = 0; i < iterations; i++)
        {
            runMapLoop();

            if (influenceDetected)
            {
                break;
            }
        }

        bot.mouseRelease(MouseCode.LEFT);
        intermittentAttacker.stop();
        screenScanner.stop();
        try {
            intermittentAttackerThread.join();
            screenScannerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        SleepUtils.delayAround(500);

        portalOut();
    }

    public boolean portalOut()
    {
        SleepUtils.delayAround(200);

        bot.mouseClickForDuration(MouseCode.RIGHT, 700, 2000);

        SleepUtils.delayAround(200);

        bot.keyClick(KeyCode.R);

        SleepUtils.delayAround(400);

        // Scan screen for green portal
        Point portalPoint = findPortal();

        // If not found, hold right click again and cast portal again
        if (portalPoint == null)
        {
            for (int i=0; i<5; i++)
            {
                System.out.println("In for loop");
                bot.mouseClickForDuration(MouseCode.RIGHT, 800, 1800);
                SleepUtils.delayAround(200);
                bot.keyClick(KeyCode.R);
                SleepUtils.delayAround(400);
                portalPoint = findPortal();
                if (portalPoint != null)
                {
                    break;
                }
            }

            if (portalPoint == null)
            {
                System.out.println("Couldn't open a portal. Breaking");
                return false;
            }
        }

        System.out.println("Portal found at " + portalPoint.x + ", " + portalPoint.y);

        bot.mouseMoveGeneralLocation(portalPoint);

        SleepUtils.delayAround(80);

        bot.mouseClickOnceOrTwice(MouseCode.LEFT);

        return true;
    }

    public static Point findPortal()
    {
        Mat screen = ScreenCapture.captureFullscreenMat();
        return findPortal(screen);
    }

    public static Point findPortal(Mat mat)
    {
        // Crop the life circle out
        int cropHeight = 200;
        int newHeight = mat.rows() - cropHeight;

        // Crop the top part (excluding the bottom 200 pixels)
        mat = new Mat(mat, new Rect(0, 0, mat.cols(), newHeight));

        Mat mask1 = ScreenScanner.applyHSVFilter(mat, 0, 160, 75, 1, 255, 255);
        Mat mask2 = ScreenScanner.applyHSVFilter(mat, 178, 150, 84, 179, 211, 255);
        Mat mask = new Mat();
        Core.bitwise_or(mask1, mask2, mask);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15));

        // Perform morphological closing (dilate, then erode)
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel, new Point(-1, -1), 1);

        MinimapVisuals.writeMatToDisk("_test.png", mask);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask.clone(), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Point centerPoint = null;
        double maxArea = 0;
        double minAreaThreshold = 3000; // arbitrary minimum size

        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > minAreaThreshold && area > maxArea) {
                Moments moments = Imgproc.moments(contour);
                if (moments.m00 != 0) {
                    centerPoint = new Point(moments.m10 / moments.m00, moments.m01 / moments.m00);
                    maxArea = area;
                }
            }
        }

        return centerPoint;
    }

    public void runMapLoop()
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

    private final List<Point> recentSelections = new ArrayList<Point>();
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
