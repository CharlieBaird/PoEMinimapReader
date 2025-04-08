package com.charliebaird.utility;

import java.awt.*;
import java.awt.image.*;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class ScreenCapture {
    public static Robot robot;

    static {
        try {
            robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[1]);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

//    private final static Rectangle captureRect = new Rectangle(0, 0, 1920, 1080);

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
