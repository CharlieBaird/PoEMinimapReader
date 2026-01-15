package com.charliebaird.utility;

import com.charliebaird.Minimap.MinimapVisuals;
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

    private final static Rectangle centerRect = new Rectangle(259, 125, 1920 - 259 - 259, 1080 - 145 - 145);
    private final static Rectangle fullscreenRect = new Rectangle(0, 0, 1920, 1080);

    public static Mat captureFullscreenMat()
    {
        return captureMat(fullscreenRect);
    }

    public static Mat captureScreenMat() {
        return captureMat(centerRect);
    }

    private static Mat captureMat(Rectangle rect) {
        BufferedImage image = robot.createScreenCapture(rect);

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

    public static Mat captureInventoryMat()
    {
        Rectangle rect = new Rectangle(1273, 590, 1911-1273-8, 857-590-8);

        BufferedImage image = robot.createScreenCapture(rect);

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

    public static void saveFullScreenshot(String path)
    {
        Mat fullscreenMat = captureFullscreenMat();

        MinimapVisuals.writeMatToDisk(path, fullscreenMat);
    }
}
