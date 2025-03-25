package com.charliebaird;

import com.charliebaird.Minimap.MinimapExtractor;
import com.charliebaird.utility.Timer;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Main
{
    static { nu.pattern.OpenCV.loadLocally(); }
    static Timer timer = new Timer();
    static Robot robot;

    private static void init()
    {
        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

        try {
            robot = new Robot(screens[1]); // Use Robot for the second monitor
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            String imagePath = "C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/minimap3.png";
            Mat original = Imgcodecs.imread(imagePath);

            timer.start();

            MinimapExtractor minimap = new MinimapExtractor();

            minimap.resolve(original, true);

            timer.stop();

            Imgcodecs.imwrite("final.png", minimap.fullMinimap);
        }

        else if (args[0].equals("-l"))
        {
            init();

            Mat original = getScreenshot();

            MinimapExtractor minimap = new MinimapExtractor();
            minimap.resolve(original, true);
            Imgcodecs.imwrite("final.png", minimap.fullMinimap);
        }
    }

    public static Mat getScreenshot()
    {
        // Capture screenshot from middle monitor
        timer.start();
        BufferedImage screenshot = robot.createScreenCapture(new Rectangle(0, 0, 1920, 1080));
        timer.stop();

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