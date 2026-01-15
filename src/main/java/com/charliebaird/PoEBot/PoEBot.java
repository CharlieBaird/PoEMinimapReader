package com.charliebaird.PoEBot;

import com.TeensyBottingLib.AsyncTeensyBot;

import java.awt.*;

public class PoEBot extends AsyncTeensyBot
{
    public PoEBot()
    {
        super();
    }

    public void mouseMoveGeneralLocation(org.opencv.core.Point p, boolean async)
    {
        super.mouseMoveGeneralLocation(new Point((int) Math.round(p.x), (int) Math.round(p.y)), async);
    }

    public void mouseMoveGeneralLocation(org.opencv.core.Point p, int proximity, boolean async)
    {
        super.mouseMoveGeneralLocation(new Point((int) Math.round(p.x), (int) Math.round(p.y)), proximity, async);
    }

    public void mouseMoveExactLocation(org.opencv.core.Point p, boolean async)
    {
        super.mouseMoveExactLocation(new Point((int) Math.round(p.x), (int) Math.round(p.y)), async);
    }
}
