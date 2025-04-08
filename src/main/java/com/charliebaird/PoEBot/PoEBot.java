package com.charliebaird.PoEBot;

import com.charliebaird.Minimap.Legend;
import com.charliebaird.Minimap.MinimapExtractor;
import com.charliebaird.teensybottinglib.TeensyBot;
import org.opencv.core.Point;

public class PoEBot extends TeensyBot
{
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
