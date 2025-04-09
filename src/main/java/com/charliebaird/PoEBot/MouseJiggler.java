package com.charliebaird.PoEBot;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class MouseJiggler implements Runnable
{
    private volatile boolean running = true;
    private final PoEBot bot;

    public MouseJiggler(PoEBot bot)
    {
        this.bot = bot;
    }

    @Override
    public void run()
    {
        while (running) {
            try {
                int dx;
                int dy;

                int rand = ThreadLocalRandom.current().nextInt(0, 10);
                if (rand >= 8)
                {
                    dx = ThreadLocalRandom.current().nextInt(-8, 9); // -1, 0, or 1
                    dy = ThreadLocalRandom.current().nextInt(-8, 9);
                }
                else {
                    dx = ThreadLocalRandom.current().nextInt(-1, 2); // -1, 0, or 1
                    dy = ThreadLocalRandom.current().nextInt(-1, 2);
                }

                bot.mouseMoveRelative(new Point(dx, dy));

                Thread.sleep(ThreadLocalRandom.current().nextInt(20, 1200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop()
    {
        running = false;
    }
}
