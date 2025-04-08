package com.charliebaird.PoEBot;

import com.charliebaird.Minimap.Legend;
import com.charliebaird.Minimap.MinimapExtractor;
import com.charliebaird.teensybottinglib.TeensyBot;

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


//    public void moveCharacter(MinimapExtractor minimap)
//    {
//        int index = 0;
//        Point point;
//        boolean wasStuck = false;
//        while (true) {
//            point = minimap.findOptimalRevealAngle(index);
//            System.out.println("Found " + point);
//
//            if (point == null) return;
//            boolean stuck = addPoint(point);
//
//            if (!stuck) break;
//
//            wasStuck = true;
//
//            System.out.println(index);
//            index++;
//        }
//
//        Point screenPoint = Legend.convertMinimapPointToScreen(point);
//    }
}
