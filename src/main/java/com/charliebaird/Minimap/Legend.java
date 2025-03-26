package com.charliebaird.Minimap;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.*;
import java.util.stream.Collectors;

// Generated structure from minimap of detected objects in map (sulphite, dropped items, reveal points)
public class Legend
{
    // Wall contours
    public ArrayList<MatOfPoint> wallContours;

    // Optimal places to walk to reveal minimap
    public ArrayList<Point> revealPoints;

    // Sulphite found in map
    public ArrayList<Point> sulphitePoints;

    // Item dropped in map
    public ArrayList<Point> itemPoints;

    // Portal locations
    public ArrayList<Point> portalPoints;

    // Door locations
    public ArrayList<Point> doorPoints;

    public Legend()
    {
        revealPoints = new ArrayList<>();
        sulphitePoints = new ArrayList<>();
        itemPoints = new ArrayList<>();
        portalPoints = new ArrayList<>();
        doorPoints = new ArrayList<>();
    }

    public Point findOptimalPoint(Mat minimap, int n) {
        if (revealPoints == null || revealPoints.isEmpty() || wallContours == null || wallContours.isEmpty() || minimap == null) {
            return null;
        }

        Point center = new Point(minimap.cols() / 2.0, minimap.rows() / 2.0);

        List<Point> sortedPoints = revealPoints.stream()
                .map(p -> new AbstractMap.SimpleEntry<>(p, euclideanDistance(p, center) - distanceToNearestContour(p, wallContours)))
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (n < 0 || n >= sortedPoints.size()) {
            return null;
        }

        return sortedPoints.get(n);
    }


    private static double euclideanDistance(Point p1, Point p2) {
        return Math.hypot(p1.x - p2.x, p1.y - p2.y);
    }

    private static double distanceToNearestContour(Point point, ArrayList<MatOfPoint> contours) {
        double minDist = Double.MAX_VALUE;

        for (MatOfPoint contour : contours) {
            Point[] contourPoints = contour.toArray();
            for (Point contourPoint : contourPoints) {
                double dist = euclideanDistance(point, contourPoint);
                if (dist < minDist) {
                    minDist = dist;
                }
            }
        }

        return minDist;
    }

    /*

FIRST COORDINATE: SCREEN
SECOND COORDINATE: MINIMAP

POINTS
X: 690 Y: 98 {657.0, 341.0}
X: 704 Y: 434 {666.0, 386.0}
X: 611 Y: 652 {660.0, 408.0}
X: 795 Y: 965 {681.0, 432.0}
X: 1007 Y: 251 {699.0, 363.0}
X: 1060 Y: 452 {705.0, 388.0}
X: 1279 Y: 300 {733.0, 369.0}
X: 1345 Y: 714 {731.0, 413.0}
X: 1587 Y: 433 {768.0, 386.0}
DOOR
X: 271 Y: 113 {626.0, 352.0}
PORTAL
X: 1027 Y: 10 {703.0, 337.0}
 */
    
/*
(-270.0, -442.0, -44.0, -4.0)
(-256.0, -106.0, -35.0, 41.0)
(-349.0, 112.0, -41.0, 63.0)
(-165.0, 425.0, -20.0, 87.0)
(47.0, -289.0, -2.0, 18.0)
(100.0, -88.0, 4.0, 43.0)
(319.0, -240.0, 32.0, 24.0)
(385.0, 174.0, 30.0, 68.0)
(627.0, -107.0, 67.0, 41.0)
(-689.0, -427.0, -75.0, 7.0)
(67.0, -530.0, 2.0, -8.0)
 */

    // Returns null if boolean is true and point is off screen. Otherwise scales mouse back onto screen
    public static Point convertMinimapPointToScreen(Point point) {
        int screenWidth = 1920;
        int screenHeight = 1080;

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int minX = 350;
        int maxX = screenWidth - 350;
        int minY = 250;
        int maxY = screenHeight - 250;

        // Convert using the regression formula
        double rawX = 9.1632 * (point.x - 701) + 51.5805;
        double rawY = 9.4712 * (point.y - 345) - 465.1877;

        double screenX = rawX + centerX;
        double screenY = rawY + centerY;

        // Vector from center to point
        double dx = screenX - centerX;
        double dy = screenY - centerY;

        // Compute max scaling factor to stay within margins
        double scaleX = dx != 0 ? (dx > 0 ? (maxX - centerX) / dx : (minX - centerX) / dx) : Double.MAX_VALUE;
        double scaleY = dy != 0 ? (dy > 0 ? (maxY - centerY) / dy : (minY - centerY) / dy) : Double.MAX_VALUE;

        double scale = Math.min(1.0, Math.min(scaleX, scaleY));

        // Apply scaling if needed
        screenX = centerX + dx * scale;
        screenY = centerY + dy * scale;

        return new Point((int) Math.round(screenX), (int) Math.round(screenY));
    }

}
