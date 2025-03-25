package com.charliebaird.Robot;

import java.awt.*;
import java.awt.event.InputEvent;
import org.opencv.core.Point;

public class SmartBot extends Robot
{
    private MouseMotion motion;

    public SmartBot(GraphicsDevice screen) throws AWTException
    {
        super(screen);
        motion = new MouseMotion();
    }

    public void delayMS(int ms)
    {
        int min = ms;
        int max = (int) (ms * 2);

        int value = (int) Math.floor(Math.random()*(max-min+1)+min);

        try
        {
            Thread.sleep(value);
        }
        catch (InterruptedException ex)
        {

        }
    }

    public void mouseMoveGeneralLocation(Point p)
    {
        motion.move((int) Math.round(p.x), (int) Math.round(p.y));
    }

    public void leftClick()
    {
        this.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        delayMS(85);
        this.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void rightClick()
    {
        this.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        delayMS(85);
        this.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }

    public void holdRightClick()
    {
        this.mousePress(InputEvent.BUTTON3_DOWN_MASK);
    }

    public void releaseRightClick()
    {
        this.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }

    class Location {
        public int x;
        public int y;

        public Location(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Location Add(Location that)
        {
            return new Location(this.x + that.x, this.y + that.y);
        }

        @Override
        public String toString()
        {
            return "[" + x + " " + y + "]";
        }
    }
}
