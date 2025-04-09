package com.charliebaird.PoEBot;

import com.TeensyBottingLib.InputCodes.MouseCode;
import com.TeensyBottingLib.Utility.SleepUtils;
import com.charliebaird.utility.ScreenCapture;
import org.opencv.core.Mat;

import static com.charliebaird.Minimap.MinimapVisuals.writeMatToDisk;

public class ScreenScanner implements Runnable
{
    private volatile boolean running = true;
    private final PoEBot bot;

    public ScreenScanner(PoEBot bot)
    {
        this.bot = bot;
    }

    @Override
    public void run()
    {
        int iteration = 0;
        while (running) {
            iteration++;
            System.out.println("Scanning");

            Mat mat = ScreenCapture.captureScreenMat();

            writeMatToDisk("scanner" + iteration + ".png", mat, true);
        }
    }

    public void stop()
    {
        running = false;
    }
}
