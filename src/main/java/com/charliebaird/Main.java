package com.charliebaird;

import com.charliebaird.Minimap.Legend;
import com.charliebaird.Minimap.MinimapExtractor;
import com.charliebaird.Robot.SmartBot;
import com.charliebaird.utility.Timer;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Main
{
    static { nu.pattern.OpenCV.loadLocally(); }
    static SmartBot robot;

    private static void init()
    {
        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

        try {
            robot = new SmartBot(screens[1]); // Use Robot for the second monitor
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    static final boolean writeToDisk = true;

    public static void main(String[] args)
    {
        init();

        try {
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
                Timer.start();
                Mat original = getScreenshot();

                MinimapExtractor minimap = new MinimapExtractor(writeToDisk);
                minimap.resolve(original);
                Timer.stop();
                Point p = minimap.findOptimalRevealAngle();
                if (p != null)
                {
                    Point screenPoint = Legend.convertMinimapPointToScreen(p);

//                    robot.mouseMoveGeneralLocation(screenPoint);
                }

                minimap.saveFinalMinimap("final.png");
            }

            else if (args[0].equals("-r"))
            {
                for (int i=0; i<30; i++)
                {
                    Timer.start();
                    Mat original = getScreenshot();

                    MinimapExtractor minimap = new MinimapExtractor(writeToDisk);
                    minimap.resolve(original);
                    Timer.stop();

                    Point p = minimap.findOptimalRevealAngle();
                    if (p != null)
                    {
                        Point screenPoint = Legend.convertMinimapPointToScreen(p);

                        robot.mouseMoveGeneralLocation(screenPoint);
                        robot.holdRightClick();

                    }
                }
                robot.releaseRightClick();
            }
        } catch (Exception e) {

        } finally {
            robot.releaseRightClick();
        }
    }

    public static Mat getScreenshot()
    {
        // Capture screenshot from middle monitor

        BufferedImage screenshot = robot.createScreenCapture(new Rectangle(0, 0, 1920, 1080));

        return bufferedImageToMat(screenshot);
    }

    public static Mat bufferedImageToMat(BufferedImage bi) {
        // Convert to a byte-based format first (BGR)
        BufferedImage convertedImg = new BufferedImage(
                bi.getWidth(), bi.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR
        );
        convertedImg.getGraphics().drawImage(bi, 0, 0, null);

        byte[] data = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(convertedImg.getHeight(), convertedImg.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

}