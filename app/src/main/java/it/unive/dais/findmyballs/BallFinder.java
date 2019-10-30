package it.unive.dais.findmyballs;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class BallFinder {
    private float view_ratio = 0.0f;
    private int min_area = 250;

    private int sat_lower = 96;
    private int sat_upper = 255;
    private int red_lower = 160;
    private int red_upper = 180;
    private int blue_lower = 110;
    private int blue_upper = 120;
    private int yellow_lower = 16;
    private int yellow_upper = 25;

    private Mat frame;

    public BallFinder(Mat frame) {
        this.frame = frame.clone();
    }

    public Mat findBalls() {
        Mat hsv = new Mat();
        List <Mat> split_hsv = new ArrayList<>();

        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);
        Core.split(hsv, split_hsv);

        Mat mask_sat = new Mat();
        Imgproc.threshold(split_hsv.get(1), mask_sat, sat_lower, sat_upper, Imgproc.THRESH_BINARY);

        // TODO: add morphing to mask_sat

        Mat hue = split_hsv.get(0);
        Mat mask_red = new Mat();
        Mat mask_blue = new Mat();
        Mat mask_yellow = new Mat();

        Core.inRange(hsv, new Scalar(red_lower, 0, 0), new Scalar(red_upper, 255, 255), mask_red);
        Core.inRange(hsv, new Scalar(blue_lower, 0, 0), new Scalar(blue_upper, 255, 255), mask_blue);
        Core.inRange(hsv, new Scalar(yellow_lower, 0, 0), new Scalar(yellow_upper, 255, 255), mask_yellow);

        Mat mask_hue = new Mat();
        Mat mask = new Mat();

        Core.bitwise_or(mask_red, mask_blue, mask_hue);
        Core.bitwise_or(mask_hue, mask_yellow, mask_hue);
        Core.bitwise_and(mask_sat, mask_hue, mask);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int i = 0; i < contours.size(); i++)
            Imgproc.drawContours(frame, contours, i, new Scalar(255, 0, 0), 1);

        float[] radius = new float[1];
        Point center = new Point();

        for (MatOfPoint c : contours) {

            Imgproc.minEnclosingCircle(new MatOfPoint2f(c.toArray()), center, radius);

            if (center.y > frame.height() * view_ratio && Imgproc.contourArea(c) > min_area) {
                int area_hue = (int) hue.get((int) center.y, (int) center.x)[0];
                Scalar color;

                if (area_hue >= red_lower && area_hue <= red_upper)
                    color = new Scalar(0, 0, 255);
                else if (area_hue >= blue_lower && area_hue <= blue_upper)
                    color = new Scalar(255, 0, 0);
                else if (area_hue >= yellow_lower && area_hue <= yellow_upper)
                    color = new Scalar(0, 255, 255);
                else
                    color = new Scalar(0, 0, 0);

                Imgproc.circle(frame, center, (int) radius[0], color, 2);
            }
        }

        return frame;

    }
}