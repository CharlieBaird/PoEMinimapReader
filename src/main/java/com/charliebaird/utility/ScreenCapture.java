package com.charliebaird.utility;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ScreenCapture {
    public static Robot robot;

    static {
        try {
            robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[1]);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

//    public static Mat captureScreenMat()
//    {
//        // Capture screenshot from middle monitor
//
//        BufferedImage screenshot = robot.createScreenCapture(captureRect);
//
//        return bufferedImageToMat(screenshot);
//    }
//
//    public static Mat bufferedImageToMat(BufferedImage bi) {
//        // Convert to a byte-based format first (BGR)
//        BufferedImage convertedImg = new BufferedImage(
//                bi.getWidth(), bi.getHeight(),
//                BufferedImage.TYPE_3BYTE_BGR
//        );
//        convertedImg.getGraphics().drawImage(bi, 0, 0, null);
//
//        byte[] data = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();
//        Mat mat = new Mat(convertedImg.getHeight(), convertedImg.getWidth(), CvType.CV_8UC3);
//        mat.put(0, 0, data);
//        return mat;
//    }


    private final static Rectangle captureRect = new Rectangle(259, 125, 1920 - 259 - 259, 1080 - 145 - 145);

    public static Mat captureScreenMat() {
        BufferedImage image = robot.createScreenCapture(captureRect);

        // Fastest path: TYPE_3BYTE_BGR is most compatible with OpenCV
        if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = converted.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = converted;
        }

        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);

        return mat;
    }
}
