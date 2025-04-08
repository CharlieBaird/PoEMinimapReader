package com.charliebaird;

import com.charliebaird.Minimap.MinimapExtractor;
import com.charliebaird.PoEBot.PoEBot;
import com.charliebaird.utility.Timer;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Main
{
    static { nu.pattern.OpenCV.loadLocally(); }

    static final boolean writeToDisk = true;

    public static void main(String[] args)
    {
        PoEBot teensyBot = new PoEBot();


//        try {
//            if (args.length != 1)
//            {
//                String imagePath = "C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/samples/minimap1.png";
//                Mat original = Imgcodecs.imread(imagePath);
//
//                Timer.start();
//                MinimapExtractor minimap = new MinimapExtractor(writeToDisk);
//                minimap.resolve(original);
//                Timer.stop();
//                minimap.saveFinalMinimap("final.png");
//            }
//
//            else if (args[0].equals("-l"))
//            {
//                Timer.start();
//                Mat original = getScreenshot();
//
//                MinimapExtractor minimap = new MinimapExtractor(writeToDisk);
//                minimap.resolve(original);
//                Timer.stop();
////                Point p = minimap.findOptimalRevealAngle();
////                if (p != null)
////                {
////                    Point screenPoint = Legend.convertMinimapPointToScreen(p);
//
////                    robot.mouseMoveGeneralLocation(screenPoint);
////                }
//
////                minimap.saveFinalMinimap("final.png");
//            }
//
//            else if (args[0].equals("-r"))
//            {
//                for (int i=0; i<30; i++)
//                {
//                    Timer.start();
//                    Mat original = getScreenshot();
//
//                    MinimapExtractor minimap = new MinimapExtractor(writeToDisk);
//                    minimap.resolve(original);
//                    robot.moveCharacter(minimap);
//                    robot.holdRightClick();
//                    Timer.stop();
//
//
////                    if (p != null)
////                    {
//
////                        robot.moveCharacter(minimap);
//
//
////                    }
//                }
//                robot.releaseRightClick();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            robot.releaseRightClick();
//        }
    }

}