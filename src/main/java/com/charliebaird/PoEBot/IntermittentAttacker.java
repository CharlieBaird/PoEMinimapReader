package com.charliebaird.PoEBot;

import com.TeensyBottingLib.InputCodes.MouseCode;
import com.TeensyBottingLib.Utility.SleepUtils;

import java.util.concurrent.ThreadLocalRandom;

public class IntermittentAttacker implements Runnable
{
    private volatile boolean running = true;
    private final PoEBot bot;

    public IntermittentAttacker(PoEBot bot)
    {
        this.bot = bot;
    }

    @Override
    public void run()
    {
        SleepUtils.sleep(800, 3500, SleepUtils.BiasType.GAUSSIAN, 0.5, 0.7, false);
        while (running) {
//            System.out.println("Attacking");

            bot.mouseClickForDuration(MouseCode.RIGHT, 60, 500);

//            SleepUtils.sleep(500, 2000, SleepUtils.BiasType.GAUSSIAN, 0.5, 0.7, false);
            SleepUtils.sleep(500, 2000, SleepUtils.BiasType.EXPONENTIAL, 6, 0, true);
        }
    }

    public void stop()
    {
        running = false;
    }
}
