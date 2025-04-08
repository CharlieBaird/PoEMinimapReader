package com.charliebaird.utility;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class MatIO
{
    public static Robot robot;

    static {
        try {
            robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[1]);
        } catch (AWTException e) {
            throw new RuntimeException(e);
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
