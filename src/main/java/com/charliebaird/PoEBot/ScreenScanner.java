package com.charliebaird.PoEBot;

import com.TeensyBottingLib.InputCodes.MouseCode;
import com.TeensyBottingLib.Utility.SleepUtils;
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

            boolean eaterInfluenceProcced = scanForEaterInfluence(mat);

            if (eaterInfluenceProcced)
            {
                System.out.println("Eater influence in iteration " + iteration);
            }

            writeMatToDisk("scanner" + iteration + ".png", mat, true);
        }
    }

    public void stop()
    {
        running = false;
    }

    public boolean scanForEaterInfluence(Mat original)
    {
        Imgproc.resize(original, original, new Size(original.width() / 4, original.height() / 4));

        Timer.start();

        // Convert color encoding to HSV
        Mat hsv = new Mat();
        Imgproc.cvtColor(original, hsv, Imgproc.COLOR_BGR2HSV);

        // Use HSV in range openCV method to filter the majority of mat
        Scalar lowerBound = new Scalar(90, 111, 159);
        Scalar upperBound = new Scalar(97, 231, 255);
        Mat mask = new Mat();
        Core.inRange(hsv, lowerBound, upperBound, mask);

        MinimapVisuals.writeMatToDisk("scanner84mask.png", mask);

        int nonZero = Core.countNonZero(mask);
        int totalPixels = mask.rows() * mask.cols();
        double percent = (nonZero / (double) totalPixels) * 100.0;
        Timer.stop();

        return percent > 10;
    }
}
