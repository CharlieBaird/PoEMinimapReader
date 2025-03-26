package com.charliebaird.Robot;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.LinkedList;

import com.charliebaird.Minimap.Legend;
import com.charliebaird.Minimap.MinimapExtractor;
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

    private boolean holdingRightClick = false;
    public void holdRightClick()
    {
        if (holdingRightClick) return;
        holdingRightClick = true;

        delayMS(40);
        this.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        delayMS(40);
    }

    public void releaseRightClick()
    {
        if (!holdingRightClick) return;
        holdingRightClick = false;
        delayMS(40);
        this.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        delayMS(40);
    }

    public void moveCharacter(MinimapExtractor minimap)
    {
        int index = 0;
        Point point;
        boolean wasStuck = false;
        while (true)
        {
            point = minimap.findOptimalRevealAngle(index);
            System.out.println("Found " + point);

            if (point == null) return;
            boolean stuck = addPoint(point);

            if (!stuck) break;

            wasStuck = true;

            System.out.println(index);
            index++;
        }

        Point screenPoint = Legend.convertMinimapPointToScreen(point);

        mouseMoveGeneralLocation(screenPoint);

        if (wasStuck)
            delayMS(1000);
    }

    private static final int MAX_POINTS = 10;
    private static final int MIN_CLUSTER = 5;
    private static final double DIST_THRESHOLD = 30;

    private LinkedList<Point> pointHistory = new LinkedList<>();

    public boolean addPoint(Point newPoint) {
        pointHistory.addLast(newPoint);

        if (pointHistory.size() > MAX_POINTS) {
            pointHistory.removeFirst();
        }

        return checkCluster();
    }

    private boolean checkCluster() {
        int count = 1;

        for (int i = pointHistory.size() - 2; i >= 0; i--) {
            Point current = pointHistory.get(i);
            Point next = pointHistory.get(i + 1);

            if (euclideanDistance(current, next) <= DIST_THRESHOLD) {
                count++;
                if (count >= MIN_CLUSTER) {
                    return true;
                }
            } else {
                count = 1;
            }
        }

        return false;
    }

    public static double euclideanDistance(Point p1, Point p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }
}
