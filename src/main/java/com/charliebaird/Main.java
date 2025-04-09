package com.charliebaird;

import com.charliebaird.Minimap.MinimapExtractor;
import com.charliebaird.PoEBot.MapRunner;
import com.charliebaird.utility.ScreenCapture;
import com.charliebaird.utility.Timer;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

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

        else if (args[0].equals("-l"))
        {
//            Timer.start();
            Mat original = ScreenCapture.captureScreenMat();
//            Timer.stop();
//
//            Timer.start();
            MinimapExtractor minimap = new MinimapExtractor(writeToDisk);
            minimap.resolve(original);
//            Timer.stop();
            minimap.saveFinalMinimap("final.png");
//
//            Point p = minimap.findOptimalRevealAngle(0);
//            if (p != null)
//            {
//                Point screenPoint = Legend.convertMinimapPointToScreen(p);
//
//                bot.mouseMoveGeneralLocation(screenPoint);
//            }
//
//            minimap.saveFinalMinimap("final.png");
        }

        else if (args[0].equals("-r"))
        {
            MapRunner runner = new MapRunner();
            for (int i=0; i<20; i++)
            {
                runner.runIteration();
            }

            runner.exitMap();
        }
    }

}