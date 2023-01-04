package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.ExportToBlocks;
import org.openftc.apriltag.AprilTagDetection;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvInternalCamera2;

import java.util.ArrayList;

public class AprilTagBlocksBridge
{
    public static class BlocksContext
    {
        AprilTagDetectionPipeline pipeline;
        OpenCvCamera camera;
    }

    @ExportToBlocks(parameterLabels = {"hardwareMap", "tagsize", "fx", "fy", "cx", "cy"})
    public static BlocksContext createAprilTagDetector(HardwareMap hardwareMap, double tagsize, double fx, double fy, double cx, double cy)
    {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());

        BlocksContext ctx = new BlocksContext();
        ctx.camera = OpenCvCameraFactory.getInstance().createInternalCamera2(OpenCvInternalCamera2.CameraDirection.BACK, cameraMonitorViewId);
        ctx.pipeline = new AprilTagDetectionPipeline(tagsize, fx, fy, cx, cy);

        return ctx;
    }

    @ExportToBlocks(parameterLabels = {"AprilTagDetector", "width", "height"})
    public static void startAprilTagDetector(BlocksContext ctx, int width, int height)
    {
        ctx.camera.setPipeline(ctx.pipeline);
        ctx.camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                ctx.camera.startStreaming(width, height);
            }

            @Override
            public void onError(int errorCode) {

            }
        });
    }

    @ExportToBlocks(parameterLabels = {"AprilTagDetector"})
    public static ArrayList<AprilTagDetection> getDetections(BlocksContext ctx)
    {
        return ctx.pipeline.getLatestDetections();
    }

    @ExportToBlocks(parameterLabels = {"AprilTagDetections"})
    public static int getNumDetections(ArrayList<AprilTagDetection> detections)
    {
        if(detections == null)
        {
            return 0;
        }
        return detections.size();
    }

    @ExportToBlocks(parameterLabels = {"AprilTagDetections", "idx"})
    public static AprilTagDetection getDetection(ArrayList<AprilTagDetection> detections, int idx)
    {
        return detections.get(idx);
    }

    @ExportToBlocks(parameterLabels = {"Detection"})
    public static double getId(AprilTagDetection detection)
    {
        return detection.id;
    }

    @ExportToBlocks(parameterLabels = {"Detection"})
    public static double getX(AprilTagDetection detection)
    {
        return detection.pose.x;
    }

    @ExportToBlocks(parameterLabels = {"Detection"})
    public static double getY(AprilTagDetection detection)
    {
        return detection.pose.y;
    }

    @ExportToBlocks(parameterLabels = {"Detection"})
    public static double getZ(AprilTagDetection detection)
    {
        return detection.pose.z;
    }

    @ExportToBlocks(parameterLabels = {"Detection"})
    public static double getYaw(AprilTagDetection detection)
    {
        return detection.pose.yaw;
    }

    @ExportToBlocks(parameterLabels = {"Detection"})
    public static double getPitch(AprilTagDetection detection)
    {
        return detection.pose.pitch;
    }

    @ExportToBlocks(parameterLabels = {"Detection"})
    public static double getRoll(AprilTagDetection detection)
    {
        return detection.pose.roll;
    }

    @ExportToBlocks(parameterLabels = {"AprilTagDetector", "decimation"})
    public static void setDetectorDecimation(BlocksContext context, float decimation)
    {
        context.pipeline.setDecimation(decimation);
    }
}
