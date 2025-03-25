package com.charliebaird.Minimap;

import org.opencv.core.Point;

import java.util.ArrayList;

// Generated structure from minimap of detected objects in map (sulphite, dropped items, reveal points)
public class Legend
{
    // Optimal places to walk to reveal minimap
    public ArrayList<Point> revealPoints;

    // Sulphite found in map
    public ArrayList<Point> sulphitePoints;

    // Item dropped in map
    public ArrayList<Point> itemPoints;

    public Legend()
    {
        revealPoints = new ArrayList<>();
        sulphitePoints = new ArrayList<>();
        itemPoints = new ArrayList<>();
    }
}
