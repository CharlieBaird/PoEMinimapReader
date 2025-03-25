package com.charliebaird.Minimap;

import org.opencv.core.Point;

import java.util.ArrayList;

// Generated structure from minimap of detected objects in map (sulphite, dropped items, reveal points)
public class Legend
{
    // Optimal places to walk to reveal minimap
    public ArrayList<Point> revealPoints;

    // Sulphite found in map
    public Point[] sulphitePoints;

    // Item dropped in map
    public Point[] itemPoints;

    public Legend()
    {
        revealPoints = new ArrayList<>();
    }
}
