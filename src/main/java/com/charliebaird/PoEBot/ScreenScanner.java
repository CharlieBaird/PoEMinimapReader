package com.charliebaird.PoEBot;

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
    private final PoEBot bot;

    public ScreenScanner(PoEBot bot)
    {
        this.bot = bot;
    }

    @Override
    public void run()
    {
        int iteration = 0;
        while (running) {
            iteration++;

            Mat mat = ScreenCapture.captureScreenMat();

            Timer.start();
            System.out.println(iteration);
            boolean influenceProc = scanForInfluenceProc(mat);
            Timer.stop();

            if (influenceProc)
            {
                System.out.println("\tInfluence procced in iteration " + iteration);
            }

            writeMatToDisk("scanner" + iteration + ".png", mat, true);
        }
    }

    public void stop()
    {
        running = false;
    }

    public boolean scanForInfluenceProc(Mat original)
    {
        Imgproc.resize(original, original, new Size(original.width() / 4, original.height() / 4));

        Mat eaterInfluenceFilter = applyHSVFilter(original, 90, 111, 159, 97, 231, 255);
        double eaterPercent = getNonZeroPercent(eaterInfluenceFilter);
        System.out.println("\tEater percent: " + eaterPercent);
        if (eaterPercent > 7) return true;

        Mat exarchInfluenceFilter = applyHSVFilter(original, 0, 64, 85, 11, 165, 255);
        double exarchPercent = getNonZeroPercent(exarchInfluenceFilter);
        System.out.println("\tExarch percent: " + exarchPercent);
        return exarchPercent > 7;
    }

    public static Mat applyHSVFilter(Mat original, int hMin, int sMin, int vMin, int hMax, int sMax, int vMax)
    {
        Mat hsv = new Mat();
        Imgproc.cvtColor(original, hsv, Imgproc.COLOR_BGR2HSV);

        Scalar lowerBound = new Scalar(hMin, sMin, vMin);
        Scalar upperBound = new Scalar(hMax, sMax, vMax);
        Mat mask = new Mat();
        Core.inRange(hsv, lowerBound, upperBound, mask);

        return mask;
    }

    private double getNonZeroPercent(Mat mat)
    {
        int nonZero = Core.countNonZero(mat);
        int totalPixels = mat.rows() * mat.cols();
        double percent = (nonZero / (double) totalPixels) * 100.0;

        return percent;
    }
}
