package com.charliebaird.Minimap;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.charliebaird.Minimap.MinimapVisuals.*;

public class MinimapExtractor
{
    // Load OpenCV library
    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public Mat fullMinimap;
    public Legend legend;
    private final boolean writeToDisk;

    // Wall and blue colors
    private final Scalar wallColor = new Scalar(180, 180, 180);
    private final Scalar blueColor = new Scalar(255, 100, 100);

    // Sprites list
    private final Mat sulphiteMat = Imgcodecs.imread("C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/src/resources/sulphite.png", Imgcodecs.IMREAD_COLOR);
    private final Mat itemMat = Imgcodecs.imread("C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/src/resources/greensquare.png", Imgcodecs.IMREAD_COLOR);
    private final Mat doorMat = Imgcodecs.imread("C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/src/resources/door.png", Imgcodecs.IMREAD_COLOR);
    private final Mat portalMat = Imgcodecs.imread("C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/src/resources/portal.png", Imgcodecs.IMREAD_COLOR);

    // Constructor
    // writeToDisk is a debug flag to write intermediary output files to disk as it executes
    public MinimapExtractor(boolean writeToDisk)
    {
        this.writeToDisk = writeToDisk;
    }

    // Main method to create a resolved minimap from a given 1920x1080 screenshot of game
    // Params: Mat original is the 1920x1080 screenshot encoded in BGR
    public void resolve(Mat original)
    {
        // Crop original mat, removing outer UI elements
//        original = original.submat(new Rect(259, 125, original.width() - 259 - 259, original.height() - 145 - 145));

        writeMatToDisk("original.png", original, writeToDisk);

        // Create a black minimap of same size as cropped mat
        Mat minimap = Mat.zeros(original.size(), original.type());

        // Initialize legend. The legend stores discovered information such as
        // item drop locations, portal locations, door locations, sulphite locations
        legend = new Legend();

        // Detect unrevealed (blue lines) patches on the minimap
        // Stores these areas in the legend
        drawBlue(original, minimap, writeToDisk);

        // Detect walls in image (gray lines)
        // Stores these in the legend
        drawWalls(original, minimap, writeToDisk);

        // Find sprites if they exist (items, portals, etc.)
        // Stores these in the legend
//        findSprites(original, minimap, writeToDisk);

        // Only worry about the player icon if debug flag is enabled
        if (writeToDisk)
            drawPlayer(minimap);

        fullMinimap = minimap;
    }

    public List<Point> findRevealPoints()
    {
        List<Point> points = legend.findRevealPoints(fullMinimap);

        // Circles the optimal reveal point
//        if (writeToDisk && p != null)
//        {
//            Imgproc.circle(fullMinimap, p, 16, new Scalar(203, 192, 255), -1);
//        }

        return points;
    }

    // Finds all sprite locations in the mat and optionally marks them on the output minimap
    // Puts all discovered sprites in the legend
    public void findSprites(Mat original, Mat output, boolean writeToDisk)
    {
        legend.sulphitePoints = findSpriteLocations(original, sulphiteMat, 0.75);
        legend.portalPoints = findSpriteLocations(original, portalMat, 0.75);
        legend.itemPoints = findSpriteLocations(original, itemMat, 0.6);
        legend.doorPoints = findSpriteLocations(original, doorMat, 0.65);

        if (writeToDisk)
        {
            drawSprites(output, legend);
        }
    }

    // Uses OpenCV templating to find given spriteTemplate on screen
    // Returns a list of all points they occur at
    public static ArrayList<Point> findSpriteLocations(Mat grayScreen, Mat grayTemplate, double threshold) {
        ArrayList<Point> foundPoints = new ArrayList<>();

//        Imgproc.GaussianBlur(spriteTemplate, grayScreen, new Size(3, 3), 0);

        double[] scales = {1.0};

        for (double scale : scales) {
            // Resize the template
            Size scaledSize = new Size(
                    grayTemplate.cols() * scale,
                    grayTemplate.rows() * scale
            );

            if (scaledSize.width < 1 || scaledSize.height < 1 ||
                    scaledSize.width > grayScreen.cols() ||
                    scaledSize.height > grayScreen.rows()) {
                continue; // Skip invalid sizes
            }

            Mat scaledTemplate = new Mat();
            Imgproc.resize(grayTemplate, scaledTemplate, scaledSize);

            // Create result matrix
            int resultCols = grayScreen.cols() - scaledTemplate.cols() + 1;
            int resultRows = grayScreen.rows() - scaledTemplate.rows() + 1;
            Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);

            // Match
            Imgproc.matchTemplate(grayScreen, scaledTemplate, result, Imgproc.TM_CCOEFF_NORMED);

            // Scan for points over threshold
            for (int y = 0; y < result.rows(); y++) {
                for (int x = 0; x < result.cols(); x++) {
                    double matchVal = result.get(y, x)[0];
                    if (matchVal >= threshold) {
                        Point matchPoint = new Point(x, y);
                        foundPoints.add(matchPoint);
                    }
                }
            }
        }

        return foundPoints;
    }

    // Find unrevealed segments on minimap
    public void drawBlue(Mat original, Mat output, boolean writeToDisk)
    {
        // Convert color encoding to HSV
        Mat hsv = new Mat();
        Imgproc.cvtColor(original, hsv, Imgproc.COLOR_BGR2HSV);

        // Optionally save hsv mask to disk
        writeMatToDisk("debug_mask.png", hsv, writeToDisk);

        // Use HSV in range openCV method to filter the majority of mat
        Scalar lowerBound = new Scalar(98, 135, 172);  // H, S, V
        Scalar upperBound = new Scalar(103, 245, 201);
        Mat mask = new Mat();
        Core.inRange(hsv, lowerBound, upperBound, mask);

        // Clean up some noise
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2, 2));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);

        // Optionally save intermediary form of mask
        writeMatToDisk("blue.png", mask, writeToDisk);

        // Find contours (segments of blue essentially)
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Filter by larger ones
        List<MatOfPoint> filteredContours = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            if (Imgproc.contourArea(contour) >= 10) {
                filteredContours.add(contour);
            }
        }

        // Build lines connecting "close enough" blue segments
//        List<ContourEdge> edges = new ArrayList<>();
//        double maxConnectDistance = 150;
//        for (int i = 0; i < filteredContours.size(); i++) {
//            for (int j = i + 1; j < filteredContours.size(); j++) {
//                MatOfPoint c1 = filteredContours.get(i);
//                MatOfPoint c2 = filteredContours.get(j);
//
//                double bestDist = Double.MAX_VALUE;
//                Point closest1 = null, closest2 = null;
//
//                for (Point p1 : c1.toArray()) {
//                    for (Point p2 : c2.toArray()) {
//                        double dist = Math.hypot(p1.x - p2.x, p1.y - p2.y);
//                        if (dist < bestDist) {
//                            bestDist = dist;
//                            closest1 = p1;
//                            closest2 = p2;
//                        }
//                    }
//                }
//
//                if (bestDist <= maxConnectDistance) {
//                    edges.add(new ContourEdge(i, j, bestDist, closest1, closest2));
//                }
//            }
//        }
//
//        Collections.sort(edges);
//        UnionFind uf = new UnionFind(filteredContours.size());
//
//        for (ContourEdge edge : edges) {
//            if (uf.union(edge.i, edge.j)) {
//                // Get direction vector from p1 to p2
//                double dx = edge.p2.x - edge.p1.x;
//                double dy = edge.p2.y - edge.p1.y;
//                double length = Math.sqrt(dx * dx + dy * dy);
//
//                // Normalize and scale by 4
//                double scale = 4.0;
//                double offsetX = dx / length * scale;
//                double offsetY = dy / length * scale;
//
//                // Extend both ends
//                Point extendedP1 = new Point(edge.p1.x - offsetX, edge.p1.y - offsetY);
//                Point extendedP2 = new Point(edge.p2.x + offsetX, edge.p2.y + offsetY);
//
//                Imgproc.line(mask, extendedP1, extendedP2, new Scalar(255), 4);
//            }
//        }
//
//        contours.clear();
//        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        filteredContours.clear();
//        for (MatOfPoint contour : contours) {
//            if (Imgproc.contourArea(contour) >= 20) {
//                filteredContours.add(contour);
//            }
//        }
//
//        writeMatToDisk("blueMerged.png", mask);

        // Sort the discovered contours by total area
        filteredContours.sort((c1, c2) -> {
            double area1 = Imgproc.contourArea(c1);
            double area2 = Imgproc.contourArea(c2);
            return Double.compare(area2, area1);
        });

        // Create clone for debug disk writing purposes
        Mat clone = null;
        if (writeToDisk)
        {
            clone = original.clone();
        }

        // For each contour, find it's "center"
        // (a point visualizing its center, or its "focus" if it were an ellipse
        // Put this found list of points in the legend
        for (MatOfPoint contour : filteredContours) {
            Moments moments = Imgproc.moments(contour);
            int cx = (int)(moments.get_m10() / moments.get_m00());
            int cy = (int)(moments.get_m01() / moments.get_m00());
            Point center = new Point(cx, cy);

            if (writeToDisk)
            {
                Imgproc.circle(clone, center, 8, new Scalar(0, 255, 0), -1); // filled blue circle
                Imgproc.circle(output, center, 8, new Scalar(0, 255, 0), -1); // filled blue circle
            }

            legend.revealPoints.add(center);
        }

        // Save debug images if necessary
        if (writeToDisk)
        {
            Imgproc.drawContours(clone, filteredContours, -1, blueColor, 2);
            Imgproc.drawContours(output, filteredContours, -1, blueColor, 2);
            writeMatToDisk("blueFinal.png", clone);
        }
    }

    // Find walls on minimap
    public void drawWalls(Mat original, Mat output, boolean writeToDisk)
    {
        // Convert color encoding to HSV
        Mat hsv = new Mat();
        Imgproc.cvtColor(original, hsv, Imgproc.COLOR_BGR2HSV);

        // Use HSV in range openCV method to filter the majority of mat
        Scalar lowerBound1 = new Scalar(114, 50, 112);
        Scalar upperBound1 = new Scalar(120, 91, 203);
        Mat mask = new Mat();
        Core.inRange(hsv, lowerBound1, upperBound1, mask);

        // Optionally save intermediary form of mask
        writeMatToDisk("walls.png", mask, writeToDisk);

        // Find contours (segments of walls)
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Filter contours by minimum size
        double minContourArea = 4;
        List<MatOfPoint> filteredContours = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            if (Imgproc.contourArea(contour) >= minContourArea) {
                filteredContours.add(contour);
            }
        }

//        ArrayList<ContourEdge> edges = new ArrayList<>();
//        double maxConnectDistance = 60;
//
//        // Build lines connecting "close enough" wall segments
//        for (int i = 0; i < filteredContours.size(); i++) {
//            for (int j = i + 1; j < filteredContours.size(); j++) {
//                MatOfPoint c1 = filteredContours.get(i);
//                MatOfPoint c2 = filteredContours.get(j);
//
//                double bestDist = Double.MAX_VALUE;
//                Point closest1 = null, closest2 = null;
//
//                for (Point p1 : c1.toArray()) {
//                    for (Point p2 : c2.toArray()) {
//                        double dist = Math.hypot(p1.x - p2.x, p1.y - p2.y);
//                        if (dist < bestDist) {
//                            bestDist = dist;
//                            closest1 = p1;
//                            closest2 = p2;
//                        }
//                    }
//                }
//
//                if (bestDist <= maxConnectDistance) {
//                    edges.add(new ContourEdge(i, j, bestDist, closest1, closest2));
//                }
//            }
//        }
//
//        Collections.sort(edges);
//        UnionFind uf = new UnionFind(filteredContours.size());
//
//        for (ContourEdge edge : edges) {
//            if (uf.union(edge.i, edge.j)) {
//                Imgproc.line(mask, edge.p1, edge.p2, new Scalar(255), 2);
//            }
//        }

//        contours.clear();
//        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        filteredContours.clear();
//        for (MatOfPoint contour : contours) {
//            if (Imgproc.contourArea(contour) >= minContourArea) {
//                filteredContours.add(contour);
//            }
//        }

        legend.wallContours = contours;

        // Save debug images if necessary
        Mat clone = null;
        if (writeToDisk)
        {
            clone = original.clone();
            Imgproc.drawContours(clone, filteredContours, -1, wallColor, 2);
            Imgproc.drawContours(output, filteredContours, -1, wallColor, 2);
            writeMatToDisk("finalWalls.png", clone, writeToDisk);
        }
    }

    // Write final minimap to disk
    public void saveFinalMinimap(String path)
    {
        writeMatToDisk(path, fullMinimap, writeToDisk);
    }

    public void debugCircle(String path, Scalar scalar, Point p)
    {
        if (writeToDisk && p != null)
        {
            Imgproc.circle(fullMinimap, p, 16, scalar, -1);
        }
    }

    class ContourEdge implements Comparable<ContourEdge>
    {
        int i, j;
        double dist;
        Point p1, p2;

        public ContourEdge(int i, int j, double dist, Point p1, Point p2)
        {
            this.i = i;
            this.j = j;
            this.dist = dist;
            this.p1 = p1;
            this.p2 = p2;
        }

        public int compareTo(ContourEdge other)
        {
            return Double.compare(this.dist, other.dist);
        }
    }

    class UnionFind
    {
        int[] parent;

        public UnionFind(int size)
        {
            parent = new int[size];
            for (int i = 0; i < size; i++) parent[i] = i;
        }

        public int find(int x)
        {
            if (parent[x] != x) parent[x] = find(parent[x]);
            return parent[x];
        }

        public boolean union(int x, int y)
        {
            int rx = find(x);
            int ry = find(y);
            if (rx == ry) return false;
            parent[ry] = rx;
            return true;
        }
    }


}
