package com.charliebaird.Minimap;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.ArrayList;

// Generated structure from minimap of detected objects in map (sulphite, dropped items, reveal points)
public class Legend
{
    public ArrayList<MatOfPoint> contours;

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

    public Point findOptimalPoint(Mat minimap) {
        if (revealPoints == null || revealPoints.isEmpty() || contours == null || contours.isEmpty() || minimap == null) {
            return null;
        }

        Point center = new Point(minimap.cols() / 2.0, minimap.rows() / 2.0);
        Point bestPoint = null;
        double bestScore = Double.MAX_VALUE;

        for (Point revealPoint : revealPoints) {
            double centerDist = euclideanDistance(revealPoint, center);
            double contourDist = distanceToNearestContour(revealPoint, contours);
            double score = centerDist - contourDist;

            if (score < bestScore) {
                bestScore = score;
                bestPoint = revealPoint;
            }
        }

        return bestPoint;
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

    public static Point convertMinimapPointToScreen(Point point) {
        int screenX = (int) Math.round(0.1078 * (point.x - 701) + -5.6513) + 1920/2;
        int screenY = (int) Math.round(0.1041 * (point.y - 345) + 48.9066) + 1080/2;
        return new Point(screenX, screenY);
    }
}
