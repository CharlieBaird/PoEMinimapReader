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
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

import static com.charliebaird.PoEBot.MapRunner.findPortal;

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
            String imagePath = "C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/samples/portalsample2.png";
            Mat original = Imgcodecs.imread(imagePath);

//            Timer.start();
//            boolean influenceProc = ScreenScanner.scanForInfluenceProc(original);
//            Timer.stop();

            Point portal = findPortal(original);
            System.out.println(portal.x + " " + portal.y);
        }

        else if (args[0].equals("-r"))
        {
            MapRunner runner = new MapRunner();

            runner.openMap();

            runner.executiveLoop(20);

            runner.exitMap();
        }
    }

}