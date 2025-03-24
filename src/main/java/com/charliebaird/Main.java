package com.charliebaird;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main
{
    static { nu.pattern.OpenCV.loadLocally(); }

    public static void main(String[] args)
    {
        String imagePath = "C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/complex.png";
        Mat original = Imgcodecs.imread(imagePath);

        long startTime = System.nanoTime();

        MinimapExtractor minimap = new MinimapExtractor();

        minimap.resolve(original);

        long endTime = System.nanoTime();
        long durationInNanoseconds = endTime - startTime;
        double durationInMilliseconds = durationInNanoseconds / 1_000_000.0;

        System.out.println("Execution time: " + durationInMilliseconds + " ms");


        Imgcodecs.imwrite("final.png", minimap.fullMinimap);
    }
}