package com.charliebaird.PoEBot;

import com.TeensyBottingLib.Utility.SleepUtils;
import com.charliebaird.Minimap.Legend;
import com.charliebaird.Minimap.MinimapExtractor;
import com.TeensyBottingLib.InputCodes.KeyCode;
import com.TeensyBottingLib.InputCodes.MouseCode;
import com.charliebaird.Minimap.MinimapVisuals;
import com.charliebaird.utility.ScreenCapture;
import com.charliebaird.utility.Timer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MapRunner
{
    private final Robot utilRobot;
    private final PoEBot bot;

    private MouseJiggler mouseJiggler;
    private Thread mouseJigglerThread;

    private IntermittentAttacker intermittentAttacker;
    private Thread intermittentAttackerThread;

    private ScreenScanner screenScanner;
    private Thread screenScannerThread;

    public static MapRunner runnerSingleton;

    public static void cleanExit()
    {
        boolean lShiftDown =
                (User32.INSTANCE.GetAsyncKeyState(0xA0) & 0x8000) != 0; // VK_LSHIFT

        if (lShiftDown) {
            runnerSingleton.bot.keyRelease(KeyCode.LSHIFT, false);
        }

        boolean lCtrlDown =
                (User32.INSTANCE.GetAsyncKeyState(0xA2) & 0x8000) != 0; // VK_LCONTROL

        if (lCtrlDown) {
            runnerSingleton.bot.keyRelease(KeyCode.LCTRL, false);
        }

        Robot r = null;
        try {
            r = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        r.keyRelease(KeyEvent.VK_F4);
    }

    public MapRunner()
    {
        bot = new PoEBot();
        try {
            utilRobot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        // Init mouse jiggler background thread, mimics human-like constant mouse jiggle
        mouseJiggler = new MouseJiggler(bot);
        mouseJigglerThread = new Thread(mouseJiggler);
        mouseJigglerThread.start();

        runnerSingleton = this;
    }

    public void test()
    {
        Mat original = ScreenCapture.captureScreenMat();
        Timer.start();
        MinimapExtractor minimap = new MinimapExtractor(true);
        minimap.resolve(original);
        minimap.saveFinalMinimap("test.png");
        Timer.stop();
    }

    public void openMap()
    {
        bot.mouseMoveGeneralLocation(new Point(46, 37), 10, false);
        SleepUtils.delayAround(300);
        bot.mouseClick(MouseCode.LEFT, false);
        SleepUtils.delayAround(300);
    }

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
            influenceDetected = false;
            recentSelections.clear();

            intermittentAttacker = new IntermittentAttacker(bot);
            intermittentAttackerThread = new Thread(intermittentAttacker);
            intermittentAttackerThread.start();

            screenScanner = new ScreenScanner(this);
            screenScannerThread = new Thread(screenScanner);
            screenScannerThread.start();

            boolean success = outMapLoop(50);
            if (!success) return;
        }
    }

    // Starts inside a map, ends inside new map (just portaled in)
    public boolean outMapLoop(int iterations)
    {
        System.out.println("Starting new map");
        boolean exit = false;

        try {
            for (int i = 0; i < iterations; i++) {
                boolean success = inMapLoop(i);
                if (!success) {
                    exit = true;
                    break;
                }

                if (influenceDetected) {
                    break;
                }
            }
        } finally {
            bot.keyRelease(KeyCode.F4, true);
            intermittentAttacker.stop();
            screenScanner.stop();
        }

        // blocking calls go AFTER finally
        try {
            intermittentAttackerThread.join();
            screenScannerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread join interrupted", e);
        }

        if (exit)
        {
            return false;
        }


        SleepUtils.delayAround(250);

        portalOut();

        // Wait for character to be on screen (no loading screen)
        while (true)
        {
            if (utilRobot.getPixelColor(230, 1021).equals(new Color(36, 36, 39)))
            {
                break;
            }

            SleepUtils.delayAround(250);
        }

        System.out.println("End of loading screen");
        bot.mouseMoveGeneralLocation(new Point(950, 350), true);
        SleepUtils.delayAround(800);
        bot.mouseClick(MouseCode.LEFT, true);
        SleepUtils.delayAround(500);

        // Safety check map device is open
        if (!utilRobot.getPixelColor(627, 625).equals(new Color(86, 81, 65)))
        {
            System.out.println("Map device open safety check failed");
            return false;
        }

        Point mapInInventoryPoint = ScreenScanner.findMapInInventory();

        if (mapInInventoryPoint == null)
        {
            System.out.println("Inventory out of maps");
            return false;
        }

        bot.mouseMoveGeneralLocation(mapInInventoryPoint, false);

        SleepUtils.delayAround(50);
        bot.keyPress(KeyCode.LCTRL, false);
        SleepUtils.delayAround(50);
        bot.mouseClick(MouseCode.LEFT, false);
        SleepUtils.delayAround(50);
        bot.keyRelease(KeyCode.LCTRL, false);
        SleepUtils.delayAround(50);

        bot.mouseMoveGeneralLocation(new Point(620, 848), false);
        bot.mouseClick(MouseCode.LEFT, false);

        SleepUtils.delayAround(500);
        bot.mouseMoveGeneralLocation(new Point(993, 474), false);
        SleepUtils.delayAround(1750);

        // Click portal to enter map
        bot.mouseClick(MouseCode.LEFT, false);

        // Sleep until map is entered
        SleepUtils.delayAround(2000);
        while (true)
        {
            if (utilRobot.getPixelColor(230, 1021).equals(new Color(36, 36, 39)))
            {
                break;
            }

            SleepUtils.delayAround(250);
        }

        // Repeat cycle
        return true;
    }

    public boolean portalOut()
    {
        LogTailer.observe();

        bot.keyClick(KeyCode.N, false);

        bot.mouseMoveGeneralLocation(new Point(956, 320), 20, false);

        SleepUtils.delayAround(300);

        bot.mouseClick(MouseCode.LEFT, false);

        // Check success?
        // Wait for character to be on screen (no loading screen)
        for (int i=0; i<100; i++)
        {
            if (!utilRobot.getPixelColor(230, 1021).equals(new Color(36, 36, 39)))
            {
                System.out.println("Quick portal worked, exited map");
                return true;
            }

            SleepUtils.delayAround(30);
        }

        List<String> messages = LogTailer.getLast();
        LogTailer.printMessages(messages);

        // If here, failed to quick portal.
        bot.mouseMoveGeneralLocation(new Point(1099, 405), false);

        // Scan screen for red portal
        Point portalPoint = findPortal();

        if (portalPoint == null)
        {
            for (int i = 0; i < 5; i++)
            {
                System.out.println("Couldn't find portal. " + i + "-th iteration");
                bot.mouseClickForDuration(MouseCode.RIGHT, 700 + 300 * i, 2000 + 300 * i, false);

                SleepUtils.delayAround(500);

                bot.keyClick(KeyCode.N, true);

                portalPoint = findPortal();

                if (portalPoint != null) break;
            }

            if (portalPoint == null)
            {
                System.out.println("Couldn't open a portal. Breaking");
                return false;
            }
        }

        System.out.println("Portal found at " + portalPoint.x + ", " + portalPoint.y);

        bot.mouseMoveGeneralLocation(portalPoint, false);

        SleepUtils.delayAround(80);

        bot.mouseClick(MouseCode.LEFT, true);

        for (int i=0; i<5; i++)
        {
            portalPoint = findPortal();

            if (portalPoint == null) return true;

            bot.mouseMoveGeneralLocation(portalPoint, false);

            SleepUtils.delayAround(80);

            bot.mouseClick(MouseCode.LEFT, true);

            SleepUtils.delayAround(1500);
        }

        return true;
    }

    public static Point findPortal()
    {
        Mat screen = ScreenCapture.captureFullscreenMat();
        return findPortal(screen);
    }

    public static Point findPortal(Mat mat)
    {
        mat = new Mat(mat, new Rect(160, 160, mat.cols() - 160, mat.rows() - 160 - 200));

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

        if (centerPoint != null) {
            return new Point(centerPoint.x + 160, centerPoint.y + 160);
        }

        return null;
    }

    public boolean inMapLoop(int iteration)
    {
        System.out.println("(" + iteration + "): In map loop");

        List<Point> revealPoints = new ArrayList<>();

        for (int i=0; i<5; i++)
        {
            // Safety check, is escape open? Or not in instance?
            if (!utilRobot.getPixelColor(230, 1021).equals(new Color(36, 36, 39)))
            {
                System.out.println("Escape open, quitting");
                return false;
            }

            Mat original = ScreenCapture.captureScreenMat();
            Timer.start();
            MinimapExtractor minimap = new MinimapExtractor(true);
            minimap.resolve(original);
            minimap.saveFinalMinimap("iteration " + iteration + ".png");
            Timer.stop(iteration);

            revealPoints = minimap.findRevealPoints();

            minimap.saveFinalMinimap("iteration.png");

            if (revealPoints == null || revealPoints.isEmpty())
            {
                System.out.println("No reveal points found in sub-iteration " + i);
                SleepUtils.delayAround(300);
            }
            else
            {
                break;
            }
        }

        Point point = findBestRevealPoint(revealPoints, recentSelections);
        recentSelections.add(point);
        if (recentSelections.size() > 10)
        {
            recentSelections.removeFirst();
        }

        if (point != null)
        {
            Point screenPoint = Legend.convertMinimapPointToScreen(point);
            bot.mouseMoveGeneralLocation(screenPoint, false);
        }

        if (iteration == 0)
        {
            bot.keyPress(KeyCode.F4, true);
        }

        return true;
    }

    private final List<Point> recentSelections = new ArrayList<Point>();
    public static Point findBestRevealPoint(List<Point> revealPoints, List<Point> recentSelections) {
        if (recentSelections == null || recentSelections.isEmpty())
        {
            // Initialize recently selected point as up and to the right
            // Usually safe for map
            recentSelections = new ArrayList<>();
            recentSelections.add(new Point(30, 40));
        }

        Point bestPoint = revealPoints.getFirst();
        double bestScore = -1000;

        Point mostRecentPoint = recentSelections.getLast();
        double previousAngle = Math.atan2(mostRecentPoint.y - 395, mostRecentPoint.x - 701); // -pi to pi

        for (Point point : revealPoints)
        {
            double newAngle = Math.atan2(point.y - 395, point.x - 701);
            double score;

            try {
                score = 1 / Math.abs(newAngle - previousAngle);
            }
            catch (Exception e) {
                score = -1000;
            }

            // Punish it if recent selections contains the point
            if (recentSelections.contains(point))
            {
                score = -1000;
            }

            System.out.println("\tPoint has score " + score + " at loc " + point.x + ", " + point.y);
            if (score > bestScore)
            {
                bestPoint = point;
                bestScore = score;
            }
        }

        System.out.println("Previous points:");
        for (Point point : recentSelections)
        {
            System.out.println("\t" + point.x + ", " + point.y);
        }
        System.out.println("Selected point " + bestPoint.x + ", " + bestPoint.y);

        return bestPoint;
    }
}
