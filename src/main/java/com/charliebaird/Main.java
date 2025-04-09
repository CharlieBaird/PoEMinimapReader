package com.charliebaird;

import com.charliebaird.Minimap.MinimapExtractor;
import com.charliebaird.Minimap.MinimapVisuals;
import com.charliebaird.PoEBot.MapRunner;
import com.charliebaird.PoEBot.ScreenScanner;
import com.charliebaird.utility.ScreenCapture;
import com.charliebaird.utility.Timer;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Main
{
    static { nu.pattern.OpenCV.loadLocally(); }

    static final boolean writeToDisk = true;

    public static void main(String[] args)
    {
//        SleepUtils.testDistributions();

        if (args.length != 1)
        {
            String imagePath = "C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/samples/minimap1.png";
            Mat original = Imgcodecs.imread(imagePath);

            Timer.start();
            MinimapExtractor minimap = new MinimapExtractor(writeToDisk);
            minimap.resolve(original);
            Timer.stop();
            minimap.saveFinalMinimap("final.png");
        }

//        else if (args[0].equals("-l"))
//        {
////            Timer.start();
//            Mat original = ScreenCapture.captureScreenMat();
////            Timer.stop();
////
////            Timer.start();
//            MinimapExtractor minimap = new MinimapExtractor(writeToDisk);
//            minimap.resolve(original);
////            Timer.stop();
//            minimap.saveFinalMinimap("final.png");
////
//        }

        else if (args[0].equals("-l"))
        {
            String imagePath = "C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/samples/output/scanner523.png";
            Mat original = Imgcodecs.imread(imagePath);

//            Imgproc.resize(original, original, new Size(original.width() / 4, original.height() / 4));

            Timer.start();

            // Convert color encoding to HSV
            Mat hsv = new Mat();
            Imgproc.cvtColor(original, hsv, Imgproc.COLOR_BGR2HSV);

            // Use HSV in range openCV method to filter the majority of mat
//            Scalar lowerBound = new Scalar(0, 64, 85);
//            Scalar upperBound = new Scalar(11, 165, 255);
//            Mat mask = new Mat();
//            Core.inRange(hsv, lowerBound, upperBound, mask);

            Mat mask = ScreenScanner.applyHSVFilter(original, 0, 64, 85, 11, 165, 255);

            MinimapVisuals.writeMatToDisk("scanner84mask.png", mask);

            int nonZero = Core.countNonZero(mask);
            int totalPixels = mask.rows() * mask.cols();
            double percent = (nonZero / (double) totalPixels) * 100.0;
            Timer.stop();

            System.out.printf("Non-zero pixels: %d / %d (%.2f%%)%n", nonZero, totalPixels, percent);
        }

        else if (args[0].equals("-r"))
        {
            MapRunner runner = new MapRunner();

            runner.openMap();

            for (int i=0; i<20   ; i++)
            {
                runner.runIteration();
            }

            runner.exitMap();
        }
    }

}