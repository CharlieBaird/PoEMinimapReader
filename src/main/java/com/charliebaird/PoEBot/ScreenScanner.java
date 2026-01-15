package com.charliebaird.PoEBot;

import com.charliebaird.Minimap.MinimapVisuals;
import com.charliebaird.utility.ScreenCapture;
import com.charliebaird.utility.Timer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static com.charliebaird.Minimap.MinimapVisuals.writeMatToDisk;

public class ScreenScanner implements Runnable
{
    private volatile boolean running = true;
    private final MapRunner mapRunner;
    private boolean scanningForInfluence = true;

    public ScreenScanner(MapRunner mapRunner)
    {
        this.mapRunner = mapRunner;
    }

    @Override
    public void run()
    {
        int iteration = 0;
        while (running) {
            iteration++;

            Mat mat = ScreenCapture.captureScreenMat();

            if (scanningForInfluence)
            {
                boolean influenceProc = scanForInfluenceProc(mat, iteration);
                if (influenceProc)
                {
                    System.out.println("\tInfluence procced in iteration " + iteration);
                    scanningForInfluence = false;
                    mapRunner.influenceDetected();
                }

                writeMatToDisk("influence_scan/scanner" + iteration + ".png", mat, true);
            }
        }
    }

    public void stop()
    {
        running = false;
    }

    public static final boolean CHECK_FOR_EATER = true;
    public static boolean scanForInfluenceProc(Mat original, int iteration)
    {
        Imgproc.resize(original, original, new Size(original.width() / 4, original.height() / 4));

        if (CHECK_FOR_EATER)
        {
            Mat eaterInfluenceFilter = applyHSVFilter(original, 90, 111, 159, 97, 231, 255);
            double eaterPercent = getNonZeroPercent(original, eaterInfluenceFilter);
            MinimapVisuals.writeMatToDisk("influence_scan/scanner" + iteration + "_filtered.png", eaterInfluenceFilter);

            return eaterPercent > 7;
        }

        else
        {
            Mat exarchInfluenceFilter = applyHSVFilter(original, 0, 64, 85, 11, 165, 255);
            double exarchPercent = getNonZeroPercent(original, exarchInfluenceFilter);

            return exarchPercent > 7;
        }
    }

    public static Mat applyHSVFilter(Mat original, int hMin, int sMin, int vMin, int hMax, int sMax, int vMax)
    {
        Mat hsv = new Mat();
        Imgproc.cvtColor(original, hsv, Imgproc.COLOR_BGR2HSV);

        Scalar lowerBound = new Scalar(hMin, sMin, vMin);
        Scalar upperBound = new Scalar(hMax, sMax, vMax);

        // Create mask for the color filter
        Mat colorMask = new Mat();
        Core.inRange(hsv, lowerBound, upperBound, colorMask);

        // Create mask for non-black pixels in the original BGR image
        Mat blackMask = new Mat();
        Core.inRange(original, new Scalar(1, 1, 1), new Scalar(255, 255, 255), blackMask); // non-black

        // Combine the two masks
        Mat combinedMask = new Mat();
        Core.bitwise_and(colorMask, blackMask, combinedMask);

        return combinedMask;
    }

    // Gets the percentage of non-zero pixels in the MASKED mat out of the total
    // number of colorful pixels in the ORIGINAL mat (filtering out rgb < 20)
    // Essentially, this filters out areas behind walls in places like Toxic Sewer map
    private static double getNonZeroPercent(Mat original, Mat hsvMask)
    {
        // Create mask of non-black pixels in original BGR image
        Mat nonBlackMask = new Mat();
        Core.inRange(original, new Scalar(20, 20, 20), new Scalar(255, 255, 255), nonBlackMask);

        // Apply that non-black mask to the HSV mask (bitwise AND)
        Mat validRegionMask = new Mat();
        Core.bitwise_and(hsvMask, nonBlackMask, validRegionMask);

        int matchingPixels = Core.countNonZero(validRegionMask);
        int totalNonBlackPixels = Core.countNonZero(nonBlackMask);

        if (totalNonBlackPixels == 0) return 0.0;

        return (matchingPixels / (double) totalNonBlackPixels) * 100.0;
    }

    public static Point findMapInInventory()
    {
        Mat original = ScreenCapture.captureInventoryMat();

        // Apply hsv filter to highlight map color (current)
        Mat hsvApplied = applyHSVFilter(original, 43, 0, 25, 70, 36, 142);

        // Apply hsv filter to highlight map color (legacy)
        Mat hsvApplied2 = applyHSVFilter(original, 8, 0, 32, 56, 73, 127);

        Core.bitwise_or(hsvApplied, hsvApplied2, hsvApplied);

        // Clean up noise
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(hsvApplied, hsvApplied, Imgproc.MORPH_OPEN, kernel);

        ArrayList<Point> mapLocs = findOccupiedCells(hsvApplied);

        mapLocs.sort((a, b) -> {
            int colA = (int) a.y;
            int colB = (int) b.y;

            if (colA != colB)
                return Integer.compare(colA, colB);

            int rowA = (int) a.x;
            int rowB = (int) b.x;

            return Integer.compare(rowA, rowB);
        });

        MinimapVisuals.writeMatToDisk("inventory.png", original);

        if (mapLocs.isEmpty()) return null;

        Point selectedMap = mapLocs.getLast();

        // Convert inventory coordinate to pixel on screen
        int baseX = 1296;
        int baseY = 614;

        int calcX = (int) Math.floor(baseX + selectedMap.y * 52.5);
        int calcY = (int) Math.floor(baseY + selectedMap.x * 52.5);

        return new Point(calcX, calcY);
    }

    // Given a binary, cleaned, filtered mat, find all maps in inventory.
    private static ArrayList<Point> findOccupiedCells(Mat filtered) {

        if (filtered.empty())
            throw new IllegalArgumentException("Input Mat is empty");

        int rows = 5;
        int cols = 12;

        int imgW = filtered.cols();
        int imgH = filtered.rows();

        double cellW = (double) imgW / cols;
        double cellH = (double) imgH / rows;

        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();

        Imgproc.connectedComponentsWithStats(
                filtered, labels, stats, centroids, 8, CvType.CV_32S
        );

        boolean[][] seen = new boolean[rows][cols];
        ArrayList<Point> results = new ArrayList<>();

        for (int i = 1; i < stats.rows(); i++) { // skip background
            int area = (int) stats.get(i, Imgproc.CC_STAT_AREA)[0];
            if (area < 50) continue; // noise threshold — tune if needed

            double cx = centroids.get(i, 0)[0];
            double cy = centroids.get(i, 1)[0];

            int col = (int) (cx / cellW);
            int row = (int) (cy / cellH);

            if (row < 0 || row >= rows || col < 0 || col >= cols)
                continue;

            if (!seen[row][col]) {
                seen[row][col] = true;
                results.add(new Point(row, col));
            }
        }

        labels.release();
        stats.release();
        centroids.release();

        return results;
    }
}
