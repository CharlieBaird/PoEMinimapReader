package com.charliebaird.Minimap;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import com.charliebaird.utility.Timer;

import java.util.*;

/*

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

public class MinimapExtractor
{
    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public Mat fullMinimap;
    public Legend legend;

    private final Mat sulphiteMat = Imgcodecs.imread("C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/src/resources/sulphite.png", Imgcodecs.IMREAD_COLOR);
    private final Mat itemMat = Imgcodecs.imread("C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/src/resources/greensquare.png", Imgcodecs.IMREAD_COLOR);
    private final Mat doorMat = Imgcodecs.imread("C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/src/resources/door.png", Imgcodecs.IMREAD_COLOR);
    private final Mat portalMat = Imgcodecs.imread("C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/src/resources/portal.png", Imgcodecs.IMREAD_COLOR);
    private final Timer timer = new Timer();

    public MinimapExtractor()
    {
    }

    public void resolve(Mat original, boolean writeToDisk)
    {
        original = original.submat(new Rect(259, 125, original.width() - 259 - 259, original.height() - 145 - 145));

        System.out.println(original.size());

        Mat minimap = Mat.zeros(original.size(), original.type());

        legend = new Legend();

        // Detect blue in image
        drawBlue(original, minimap, writeToDisk);

        // Detect walls in image
        drawWalls(original, minimap, writeToDisk);

        // Find sprites if they exist
        timer.start();
        drawSprites(original, minimap, writeToDisk);
        timer.stop();

        if (writeToDisk)
            drawPlayer(minimap);

        fullMinimap = minimap;
    }

    public Point findOptimalRevealAngle()
    {
        Point p = legend.findOptimalPoint(fullMinimap);

        if (p != null)
        {
            Imgproc.circle(fullMinimap, p, 16, new Scalar(203, 192, 255), -1);
        }

        return p;
    }

    private final Scalar wallColor = new Scalar(180, 180, 180);
    private final Scalar blueColor = new Scalar(255, 100, 100);

    private void drawPlayer(Mat output)
    {
        int centerX = output.cols() / 2;
        int centerY = output.rows() / 2;
        int halfSize = 6; // Half the length of the X (10px each direction for a 20px total)

        Point p1 = new Point(centerX - halfSize, centerY - halfSize);
        Point p2 = new Point(centerX + halfSize, centerY + halfSize);

        Point p3 = new Point(centerX - halfSize, centerY + halfSize);
        Point p4 = new Point(centerX + halfSize, centerY - halfSize);

        Scalar red = new Scalar(0, 0, 255); // BGR format, so red is (0,0,255)
        int thickness = 2;

        Imgproc.line(output, p1, p2, red, thickness);
        Imgproc.line(output, p3, p4, red, thickness);
    }

    private void writeMatToDisk(String filename, Mat mat, boolean writeToDisk)
    {
        if (!writeToDisk) return;

        Imgcodecs.imwrite(filename, mat);
    }

    public void drawSprites(Mat original, Mat output, boolean writeToDisk)
    {
        ArrayList<Point> matchPoints = findSpriteLocations(original, sulphiteMat, 0.75);

        for (Point p : matchPoints) {
            Imgproc.circle(output, p, 8, new Scalar(0, 255, 255), -1);
        }

        matchPoints = findSpriteLocations(original, portalMat, 0.75);

        for (Point p : matchPoints) {
            Imgproc.circle(output, p, 8, new Scalar(255, 213, 144), -1);
        }

        matchPoints = findSpriteLocations(original, itemMat, 0.6);

        for (Point p : matchPoints) {
            Imgproc.circle(output, p, 8, new Scalar(200, 255, 200), -1);
        }

        matchPoints = findSpriteLocations(original, doorMat, 0.65);

        for (Point p : matchPoints) {
            Imgproc.circle(output, p, 8, new Scalar(0, 165, 255), -1);
            System.out.println(p);
            Imgcodecs.imwrite("final.png", output);
        }
    }

    public static ArrayList<Point> findSpriteLocations(Mat screen, Mat spriteTemplate, double threshold) {
        ArrayList<Point> foundPoints = new ArrayList<>();

        // Convert to grayscale for performance
        Mat grayScreen = new Mat();
        Mat grayTemplate = new Mat();

        grayScreen = screen;
        grayTemplate = spriteTemplate;

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

    public void drawBlue(Mat original, Mat output, boolean writeToDisk)
    {
        // Convert to HSV
        Mat hsv = new Mat();
        Imgproc.cvtColor(original, hsv, Imgproc.COLOR_BGR2HSV);

        writeMatToDisk("debug_mask.png", hsv, writeToDisk);

        // BLUE UNREVEALED
        Scalar lowerBound = new Scalar(98, 135, 172);  // H, S, V
        Scalar upperBound = new Scalar(103, 245, 201);
        Mat mask = new Mat();
        Core.inRange(hsv, lowerBound, upperBound, mask);

        Mat kernel;

        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2, 2));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);

        writeMatToDisk("blue.png", mask, writeToDisk);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> filteredContours = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            if (Imgproc.contourArea(contour) >= 10) {
                filteredContours.add(contour);
            }
        }

        List<ContourEdge> edges = new ArrayList<>();
        double maxConnectDistance = 35;

        // Build all valid edges between contours
        for (int i = 0; i < filteredContours.size(); i++) {
            for (int j = i + 1; j < filteredContours.size(); j++) {
                MatOfPoint c1 = filteredContours.get(i);
                MatOfPoint c2 = filteredContours.get(j);

                double bestDist = Double.MAX_VALUE;
                Point closest1 = null, closest2 = null;

                for (Point p1 : c1.toArray()) {
                    for (Point p2 : c2.toArray()) {
                        double dist = Math.hypot(p1.x - p2.x, p1.y - p2.y);
                        if (dist < bestDist) {
                            bestDist = dist;
                            closest1 = p1;
                            closest2 = p2;
                        }
                    }
                }

                if (bestDist <= maxConnectDistance) {
                    edges.add(new ContourEdge(i, j, bestDist, closest1, closest2));
                }
            }
        }

        Collections.sort(edges);
        UnionFind uf = new UnionFind(filteredContours.size());

        for (ContourEdge edge : edges) {
            if (uf.union(edge.i, edge.j)) {
                // Get direction vector from p1 to p2
                double dx = edge.p2.x - edge.p1.x;
                double dy = edge.p2.y - edge.p1.y;
                double length = Math.sqrt(dx * dx + dy * dy);

                // Normalize and scale by 4
                double scale = 4.0;
                double offsetX = dx / length * scale;
                double offsetY = dy / length * scale;

                // Extend both ends
                Point extendedP1 = new Point(edge.p1.x - offsetX, edge.p1.y - offsetY);
                Point extendedP2 = new Point(edge.p2.x + offsetX, edge.p2.y + offsetY);

//                Imgproc.line(mask, extendedP1, extendedP2, new Scalar(255), 4);
            }
        }

        contours.clear();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        filteredContours.clear();
        for (MatOfPoint contour : contours) {
            if (Imgproc.contourArea(contour) >= 20) {
                filteredContours.add(contour);
            }
        }

        filteredContours.sort((c1, c2) -> {
            double area1 = Imgproc.contourArea(c1);
            double area2 = Imgproc.contourArea(c2);
            return Double.compare(area2, area1);
        });

        Mat clone = original.clone();

        for (MatOfPoint contour : filteredContours) {
            Moments moments = Imgproc.moments(contour);
            int cx = (int)(moments.get_m10() / moments.get_m00());
            int cy = (int)(moments.get_m01() / moments.get_m00());
            Point center = new Point(cx, cy);

            Imgproc.circle(clone, center, 8, new Scalar(0, 255, 0), -1); // filled blue circle
            Imgproc.circle(output, center, 8, new Scalar(0, 255, 0), -1); // filled blue circle

            legend.revealPoints.add(center);
        }

        Imgproc.drawContours(clone, filteredContours, -1, blueColor, 2);
        Imgproc.drawContours(output, filteredContours, -1, blueColor, 2);

        writeMatToDisk("blueFinal.png", clone, writeToDisk);
    }

    public void drawWalls(Mat original, Mat output, boolean writeToDisk)
    {
        // Convert to HSV
        Mat hsv = new Mat();
        Imgproc.cvtColor(original, hsv, Imgproc.COLOR_BGR2HSV);

        writeMatToDisk("debug_mask.png", hsv, writeToDisk);

        // Define HSV range for bright walls
        Scalar lowerBound1 = new Scalar(114, 50, 112);
        Scalar upperBound1 = new Scalar(120, 91, 203);
        Mat mask = new Mat();
        Core.inRange(hsv, lowerBound1, upperBound1, mask);

        writeMatToDisk("walls.png", mask, writeToDisk);

        // Find contours
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double minContourArea = 3; // Adjust depending on your image

        List<MatOfPoint> filteredContours = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            if (Imgproc.contourArea(contour) >= minContourArea) {
                filteredContours.add(contour);
            }
        }

        List<ContourEdge> edges = new ArrayList<>();
        double maxConnectDistance = 60;

        // Build all valid edges between contours
        for (int i = 0; i < filteredContours.size(); i++) {
            for (int j = i + 1; j < filteredContours.size(); j++) {
                MatOfPoint c1 = filteredContours.get(i);
                MatOfPoint c2 = filteredContours.get(j);

                double bestDist = Double.MAX_VALUE;
                Point closest1 = null, closest2 = null;

                for (Point p1 : c1.toArray()) {
                    for (Point p2 : c2.toArray()) {
                        double dist = Math.hypot(p1.x - p2.x, p1.y - p2.y);
                        if (dist < bestDist) {
                            bestDist = dist;
                            closest1 = p1;
                            closest2 = p2;
                        }
                    }
                }

                if (bestDist <= maxConnectDistance) {
                    edges.add(new ContourEdge(i, j, bestDist, closest1, closest2));
                }
            }
        }

        Collections.sort(edges);
        UnionFind uf = new UnionFind(filteredContours.size());

        for (ContourEdge edge : edges) {
            if (uf.union(edge.i, edge.j)) {
                Imgproc.line(mask, edge.p1, edge.p2, new Scalar(255), 2);
            }
        }

        contours.clear();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat clone = original.clone();

        filteredContours.clear();
        for (MatOfPoint contour : contours) {
            if (Imgproc.contourArea(contour) >= minContourArea) {
                filteredContours.add(contour);
            }
        }

        legend.contours = contours;

        Imgproc.drawContours(clone, filteredContours, -1, wallColor, 2);
        Imgproc.drawContours(output, filteredContours, -1, wallColor, 2);

        writeMatToDisk("finalWalls.png", clone, writeToDisk);
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
