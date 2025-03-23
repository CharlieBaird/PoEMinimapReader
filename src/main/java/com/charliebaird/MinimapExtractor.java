package com.charliebaird;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.util.*;

public class MinimapExtractor {
    static { nu.pattern.OpenCV.loadLocally(); }

    public Mat drawBlue(Mat original)
    {
        String imagePath = "C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/image.png";
        if (original.empty()) {
            System.out.println("Could not load image.");
            return null;
        }

        // Convert to HSV
        Mat hsv = new Mat();
        Imgproc.cvtColor(original, hsv, Imgproc.COLOR_RGB2HSV);

        Imgcodecs.imwrite("debug_mask.png", hsv);

        // BLUE UNREVEALED
        Scalar lowerBound = new Scalar(15, 80, 155);  // H, S, V
        Scalar upperBound = new Scalar(25, 240, 255);
        Mat mask = new Mat();
        Core.inRange(hsv, lowerBound, upperBound, mask);

        Imgcodecs.imwrite("blue.png", mask);

        Mat kernel;

        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(4, 4));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);

        Imgcodecs.imwrite("blue.png", mask);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Imgcodecs.imwrite("hierarchy.png", mask);

//        // Draw all contours with area >= 100 on a copy of the original image
        Mat clone = original.clone();

        List<MatOfPoint> filteredContours = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            if (Imgproc.contourArea(contour) >= 10) {
                filteredContours.add(contour);
            }
        }

        Imgproc.drawContours(clone, filteredContours, -1, new Scalar(255, 140, 140), -1);

        Imgcodecs.imwrite("blueFinal.png", clone);

        return clone;

    }

    public Mat drawWalls(Mat original) {
        String imagePath = "C:/Users/charl/Documents/dev/CB/PoE/MinimapReader/image.png";
        if (original.empty()) {
            System.out.println("Could not load image.");
            return null;
        }

        // Convert to HSV
        Mat hsv = new Mat();
        Imgproc.cvtColor(original, hsv, Imgproc.COLOR_RGB2HSV);

        Imgcodecs.imwrite("debug_mask.png", hsv);

        // WALLS

        // Define HSV range for purple (adjust these values as needed)
        Scalar lowerBound = new Scalar(0, 45, 120);  // H, S, V
        Scalar upperBound = new Scalar(5, 80, 200);
        Mat mask = new Mat();
        Core.inRange(hsv, lowerBound, upperBound, mask);

        Imgcodecs.imwrite("walls.png", mask);

        Mat kernel;

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
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
        double maxConnectDistance = 100.0;

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

        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat clone = original.clone();

        filteredContours.clear();
        for (MatOfPoint contour : contours) {
            if (Imgproc.contourArea(contour) >= minContourArea) {
                filteredContours.add(contour);
            }
        }

        Imgproc.drawContours(clone, filteredContours, -1, new Scalar(0, 255, 255), 2);

        Imgcodecs.imwrite("finalWalls.png", clone);

        return clone;
    }

    public static double contourDistance(MatOfPoint c1, MatOfPoint c2) {
        double minDist = Double.MAX_VALUE;
        for (Point p1 : c1.toArray()) {
            for (Point p2 : c2.toArray()) {
                double dist = Math.hypot(p1.x - p2.x, p1.y - p2.y);
                if (dist < minDist) {
                    minDist = dist;
                }
            }
        }
        return minDist;
    }

    class ContourEdge implements Comparable<ContourEdge> {
        int i, j;
        double dist;
        Point p1, p2;

        public ContourEdge(int i, int j, double dist, Point p1, Point p2) {
            this.i = i;
            this.j = j;
            this.dist = dist;
            this.p1 = p1;
            this.p2 = p2;
        }

        public int compareTo(ContourEdge other) {
            return Double.compare(this.dist, other.dist);
        }
    }

    class UnionFind {
        int[] parent;

        public UnionFind(int size) {
            parent = new int[size];
            for (int i = 0; i < size; i++) parent[i] = i;
        }

        public int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]);
            return parent[x];
        }

        public boolean union(int x, int y) {
            int rx = find(x);
            int ry = find(y);
            if (rx == ry) return false;
            parent[ry] = rx;
            return true;
        }
    }


}
