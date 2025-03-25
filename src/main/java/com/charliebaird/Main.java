package com.charliebaird;

import com.charliebaird.Minimap.MinimapExtractor;
import com.charliebaird.utility.Timer;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Main
{
    static { nu.pattern.OpenCV.loadLocally(); }
    static Timer timer = new Timer();

    public static void main(String[] args)
    {
        String imagePath = "C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/minimap1.png";
        Mat original = Imgcodecs.imread(imagePath);

        timer.start();

        MinimapExtractor minimap = new MinimapExtractor();

        minimap.resolve(original, true);

        timer.stop();

        Imgcodecs.imwrite("final.png", minimap.fullMinimap);
    }
}