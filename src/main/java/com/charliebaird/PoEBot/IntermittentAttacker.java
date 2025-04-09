package com.charliebaird.PoEBot;

import com.charliebaird.TeensyBottingLib.InputCodes.MouseCode;

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
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(2523, 4623));
            while (running) {
                System.out.println("Attacking");
                bot.mouseClick(MouseCode.RIGHT);
                Thread.sleep(ThreadLocalRandom.current().nextInt(1423, 2344));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }

    public void stop()
    {
        running = false;
    }
}
