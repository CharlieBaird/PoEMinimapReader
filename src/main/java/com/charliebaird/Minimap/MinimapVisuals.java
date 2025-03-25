package com.charliebaird.Minimap;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

// Class to handle visualization of generated minimap.
// Separated from MinimapExtractor for readability
public class MinimapVisuals
{
    private static final Scalar sulphiteColor = new Scalar(0, 255, 255);
    private static final Scalar portalColor = new Scalar(255, 213, 144);
    private static final Scalar itemColor = new Scalar(200, 255, 200);
    private static final Scalar doorColor = new Scalar(0, 165, 255);

    // Given filename and mat, writes that image to disk.
    public static void writeMatToDisk(String filename, Mat mat, boolean writeToDisk)
    {
        if (!writeToDisk) return;

        Imgcodecs.imwrite("samples/output/" + filename, mat);
    }

    public static void writeMatToDisk(String filename, Mat mat)
    {
        writeMatToDisk(filename, mat, true);
    }

    // Draws red X in center of mat where the player is.
    public static void drawPlayer(Mat output)
    {
        int centerX = output.cols() / 2;
        int centerY = output.rows() / 2;
        int halfSize = 6; // Half the length of the X (10px each direction for a 20px total)

        Point p1 = new Point(centerX - halfSize, centerY - halfSize);
        Point p2 = new Point(centerX + halfSize, centerY + halfSize);

        Point p3 = new Point(centerX - halfSize, centerY + halfSize);
        Point p4 = new Point(centerX + halfSize, centerY - halfSize);

        Scalar red = new Scalar(0, 0, 255); // BGR format, so red is (0,0,255)
        int thickness = 2;

        Imgproc.line(output, p1, p2, red, thickness);
        Imgproc.line(output, p3, p4, red, thickness);
    }

    // Draws all sprites stored in legend on mat
    public static void drawSprites(Mat output, Legend legend)
    {
        for (Point p : legend.sulphitePoints) {
            Imgproc.circle(output, p, 8, sulphiteColor, -1);
        }

        for (Point p : legend.portalPoints) {
            Imgproc.circle(output, p, 8, portalColor, -1);
        }

        for (Point p : legend.itemPoints) {
            Imgproc.circle(output, p, 8, itemColor, -1);
        }

        for (Point p : legend.doorPoints) {
            Imgproc.circle(output, p, 8, doorColor, -1);
        }
    }
}
