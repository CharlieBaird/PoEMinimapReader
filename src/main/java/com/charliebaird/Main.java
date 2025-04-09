package com.charliebaird;

import com.charliebaird.PoEBot.MouseJiggler;
import com.charliebaird.PoEBot.PoEBot;

public class Main
{
    static { nu.pattern.OpenCV.loadLocally(); }

    static final boolean writeToDisk = true;

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
//            String imagePath = "C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/samples/minimap1.png";
//            Mat original = Imgcodecs.imread(imagePath);
//
//            Timer.start();
//            MinimapExtractor minimap = new MinimapExtractor(writeToDisk);
//            minimap.resolve(original);
//            Timer.stop();
//            minimap.saveFinalMinimap("final.png");
        }

        else if (args[0].equals("-l"))
        {
//            Timer.start();
//            Mat original = ScreenCapture.captureScreenMat();
//            Timer.stop();
//
//            Timer.start();
//            MinimapExtractor minimap = new MinimapExtractor(writeToDisk);
//            minimap.resolve(original);
//            Timer.stop();
//            minimap.saveFinalMinimap("final.png");
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
//            MapRunner runner = new MapRunner();
//            for (int i=0; i<20; i++)
//            {
//                runner.runIteration();
//            }
//
//            runner.exitMap();

            PoEBot bot = new PoEBot();
            MouseJiggler jiggler = new MouseJiggler(bot);
            Thread jiggleThread = new Thread(jiggler);
            jiggleThread.start();
            bot.delayMS(3000);
            jiggler.stop();
            try {
                jiggleThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}