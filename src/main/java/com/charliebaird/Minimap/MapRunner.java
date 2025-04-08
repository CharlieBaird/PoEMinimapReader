package com.charliebaird.Minimap;

import com.charliebaird.PoEBot.PoEBot;
import com.charliebaird.teensybottinglib.InputCodes.MouseCode;
import com.charliebaird.utility.ScreenCapture;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class MapRunner
{
    private PoEBot bot;

    public MapRunner()
    {
        bot = new PoEBot();
    }

    public void openMap() {}

    public void exitMap()
    {
        bot.mouseRelease(MouseCode.LEFT);
    }

    public void runIteration()
    {
        Mat original = ScreenCapture.captureScreenMat();
        MinimapExtractor minimap = new MinimapExtractor(false);
        minimap.resolve(original);

        Point p = minimap.findOptimalRevealAngle(0);
        if (p != null)
        {
            Point screenPoint = Legend.convertMinimapPointToScreen(p);

            bot.mouseMoveGeneralLocation(screenPoint);
        }

        bot.mousePress(MouseCode.LEFT);

        if (Math.random() < 0.45)
            bot.mouseClick(MouseCode.RIGHT);
    }
}
