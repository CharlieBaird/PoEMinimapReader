package com.charliebaird.PoEBot;

import com.TeensyBottingLib.TeensyBot;

import java.awt.*;

public class PoEBot extends TeensyBot
{
    public PoEBot()
    {
        super();
    }

    public void mouseMoveGeneralLocation(org.opencv.core.Point p)
    {
        super.mouseMoveGeneralLocation(new Point((int) Math.round(p.x), (int) Math.round(p.y)));
    }

    public void mouseMoveExactLocation(org.opencv.core.Point p)
    {
        super.mouseMoveExactLocation(new Point((int) Math.round(p.x), (int) Math.round(p.y)));
    }
}
