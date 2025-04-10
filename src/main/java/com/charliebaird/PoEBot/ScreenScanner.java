package com.charliebaird.PoEBot;

import com.charliebaird.Minimap.MinimapVisuals;
import com.charliebaird.utility.ScreenCapture;
import com.charliebaird.utility.Timer;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static com.charliebaird.Minimap.MinimapVisuals.writeMatToDisk;

public class ScreenScanner implements Runnable
{
    private volatile boolean running = true;
    private final MapRunner mapRunner;

    public ScreenScanner(MapRunner mapRunner)
    {
        this.mapRunner = mapRunner;
    }

    @Override
    public void run()
    {
        int iteration = 0;
        while (running) {
            iteration++;

            Mat mat = ScreenCapture.captureScreenMat();

            Timer.start();
            boolean influenceProc = scanForInfluenceProc(mat);
            Timer.stop();

            if (influenceProc)
            {
                System.out.println("\tInfluence procced in iteration " + iteration);
                mapRunner.influenceDetected();

            }

            writeMatToDisk("scanner" + iteration + ".png", mat, true);
        }
    }

    public void stop()
    {
        running = false;
    }

    public static boolean scanForInfluenceProc(Mat original)
    {
        Imgproc.resize(original, original, new Size(original.width() / 4, original.height() / 4));

        Mat eaterInfluenceFilter = applyHSVFilter(original, 90, 111, 159, 97, 231, 255);
        double eaterPercent = getNonZeroPercent(original, eaterInfluenceFilter);
        MinimapVisuals.writeMatToDisk("scanner718mask.png", eaterInfluenceFilter);

        if (eaterPercent > 7) return true;

        Mat exarchInfluenceFilter = applyHSVFilter(original, 0, 64, 85, 11, 165, 255);
        double exarchPercent = getNonZeroPercent(original, exarchInfluenceFilter);
        return exarchPercent > 7;
    }

    public static Mat applyHSVFilter(Mat original, int hMin, int sMin, int vMin, int hMax, int sMax, int vMax)
    {
        Mat hsv = new Mat();
        Imgproc.cvtColor(original, hsv, Imgproc.COLOR_BGR2HSV);

        Scalar lowerBound = new Scalar(hMin, sMin, vMin);
        Scalar upperBound = new Scalar(hMax, sMax, vMax);

        // Create mask for the color filter
        Mat colorMask = new Mat();
        Core.inRange(hsv, lowerBound, upperBound, colorMask);

        // Create mask for non-black pixels in the original BGR image
        Mat blackMask = new Mat();
        Core.inRange(original, new Scalar(1, 1, 1), new Scalar(255, 255, 255), blackMask); // non-black

        // Combine the two masks
        Mat combinedMask = new Mat();
        Core.bitwise_and(colorMask, blackMask, combinedMask);

        return combinedMask;
    }

    // Gets the percentage of non-zero pixels in the MASKED mat out of the total
    // number of colorful pixels in the ORIGINAL mat (filtering out rgb < 20)
    // Essentially, this filters out areas behind walls in places like Toxic Sewer map
    private static double getNonZeroPercent(Mat original, Mat hsvMask)
    {
        // Create mask of non-black pixels in original BGR image
        Mat nonBlackMask = new Mat();
        Core.inRange(original, new Scalar(20, 20, 20), new Scalar(255, 255, 255), nonBlackMask);

        // Apply that non-black mask to the HSV mask (bitwise AND)
        Mat validRegionMask = new Mat();
        Core.bitwise_and(hsvMask, nonBlackMask, validRegionMask);

        int matchingPixels = Core.countNonZero(validRegionMask);
        int totalNonBlackPixels = Core.countNonZero(nonBlackMask);

        if (totalNonBlackPixels == 0) return 0.0;

        return (matchingPixels / (double) totalNonBlackPixels) * 100.0;
    }
}
