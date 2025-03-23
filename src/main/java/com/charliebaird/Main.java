package com.charliebaird;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class Main
{
    static { nu.pattern.OpenCV.loadLocally(); }

    public static void main(String[] args)
    {
        String imagePath = "C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/complex.png";
        Mat original = Imgcodecs.imread(imagePath);

        Mat blackMat = Mat.zeros(original.size(), original.type());

        long startTime = System.nanoTime();

        MinimapExtractor minimap = new MinimapExtractor();
        original = minimap.drawWalls(original);
        original = minimap.drawBlue(original);

        long endTime = System.nanoTime();
        long durationInNanoseconds = endTime - startTime;
        double durationInMilliseconds = durationInNanoseconds / 1_000_000.0;

        System.out.println("Execution time: " + durationInMilliseconds + " ms");


        Imgcodecs.imwrite("final.png", original);
    }
}