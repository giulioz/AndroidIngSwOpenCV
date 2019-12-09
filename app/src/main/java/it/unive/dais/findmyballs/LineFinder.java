package it.unive.dais.findmyballs;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;

public class LineFinder {
    private int thresh_lower;
    private int thresh_upper;

    private boolean debug = false;
    private String orientation = "portrait";

    private Mat frame;

    public LineFinder(Mat frame) {
        this.frame = frame.clone();
    }

    public LineFinder(Mat frame, boolean debug) {
        this(frame);

        if (debug) {
            this.frame = frame;
            this.debug = true;
        }
    }

    public void setOrientation(String orientation) {
        if (orientation == "landscape" || orientation == "portrait")
            this.orientation = orientation;
        else
            throw new IllegalArgumentException("Invalid orientation, only \"portrait\" or \"landscape\" are allowed");
    }

    public void setThreshold(int center, int size) {
        this.thresh_lower = center - size / 2;
        this.thresh_upper = center + size / 2;
    }

    private double getLine(Mat frame, Mat lines) {
        for (int x = 0; x < lines.rows(); x++) {
            double rho = lines.get(x, 0)[0], theta = lines.get(x, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;

            Point p1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
            Point p2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));

            boolean cond = thresh_lower <= p1.x && p1.x <= thresh_upper && thresh_lower <= p2.x && p2.x <= thresh_upper;

            if (orientation == "landscape")
                cond = thresh_lower <= p1.y && p1.y <= thresh_upper && thresh_lower <= p2.y && p2.y <= thresh_upper;

            if (cond) {
                if (debug)
                    Imgproc.line(frame, p1, p2, new Scalar(255, 0, 0), 2);

                return Math.toDegrees(theta);
            }
        }

        return Double.NaN;
    }

    public double findLine() {
        Mat frame_gray = new Mat();
        Imgproc.cvtColor(frame, frame_gray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.medianBlur(frame_gray, frame_gray, 3);

        Mat edges = new Mat();
        Mat lines = new Mat();
        Imgproc.Canny(frame_gray, edges, 20, 50);
        Imgproc.HoughLines(edges, lines, 1, Math.PI/180, 150);

        if (debug) {
            Point p1 = new Point(thresh_lower, 0);
            Point p2 = new Point(thresh_lower, frame.height());
            Point p3 = new Point(thresh_upper, 0);
            Point p4 = new Point(thresh_upper, frame.height());

            if (orientation == "landscape") {
                p1 = new Point(0, thresh_lower);
                p2 = new Point(frame.width(), thresh_lower);
                p3 = new Point(0, thresh_upper);
                p4 = new Point(frame.width(), thresh_upper);
            }

            Imgproc.line(frame, p1, p2, new Scalar(0, 255, 0), 2);
            Imgproc.line(frame, p3, p4, new Scalar(0, 255, 0), 2);
        }

        return getLine(frame, lines);
    }
}