/*
This sample FTC OpMode uses methods of the Datalogger class to specify and
collect robot data to be logged in a CSV file, ready for download and charting.

For instructions, see the **Part 2** tutorial at the FTC Wiki:
https://github.com/FIRST-Tech-Challenge/FtcRobotController/wiki/Datalogging


The Datalogger class is suitable for FTC OnBot Java (OBJ) programmers.
Its methods can be made available for FTC Blocks, by creating myBlocks in OBJ.

Android Studio programmers can see instructions in the Datalogger class notes.

Credit to @Windwoes (https://github.com/Windwoes).

*/

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Datalog Example v01", group = "Datalogging")
public class DatalogExample_v01 extends LinearOpMode
{
    Datalog datalog;

    DcMotor lifterMotor;
    Servo grabberServo;
    TouchSensor myTouchSensor;
    AnalogInput myPotSensor;
    ColorSensor myColorSensor;

    @Override
    public void runOpMode() throws InterruptedException
    {
        // Get devices from the hardwareMap.
        lifterMotor = hardwareMap.get(DcMotor.class, "lifter");
        grabberServo = hardwareMap.get(Servo.class, "grabber");
        myTouchSensor = hardwareMap.get(TouchSensor.class, "sensorTouch");
        myPotSensor = hardwareMap.get(AnalogInput.class, "sensorPot");
        myColorSensor = hardwareMap.get(ColorSensor.class, "sensorColor");
    
        // Initialize the datalog.
        datalog = new Datalog("datalog_02");

        // You do not need to fill every field of the datalog
        // every time you call writeLine(); those fields will simply
        // contain the last value.
        datalog.opModeStatus.set("INIT");
        datalog.writeLine();

        telemetry.setMsTransmissionInterval(50);

        waitForStart();

        datalog.opModeStatus.set("RUNNING");

        for (int i = 0; opModeIsActive(); i++)
        {
            // Move the motor and servo.
            lifterMotor.setPower(-gamepad1.left_stick_y);
            grabberServo.setPosition(gamepad1.left_trigger);
    
            // Note that the order in which we set datalog fields
            // does *not* matter! The order is configured inside
            // the Datalog class constructor.

            datalog.loopCounter.set(i);
        
            // Optional to format numeric values of type float and double.
            // Their default is "%.3f", can edit in Datalogger.java file.
            datalog.motorEncoder.set(lifterMotor.getCurrentPosition());
            datalog.servoPosition.set("%.2f", grabberServo.getPosition());
            datalog.touchPress.set(myTouchSensor.isPressed());
            datalog.potValue.set("%.1f", myPotSensor.getVoltage());
            datalog.totalLight.set(myColorSensor.alpha());

            // The logged timestamp is taken when writeLine() is called.
            datalog.writeLine();

            // All log values are text; numeric formatting does not apply at the
            // telemetry stage.  Formatting can be done at the logging stage.
            telemetry.addData("Lifter Motor Encoder", datalog.motorEncoder);
            telemetry.addData("Grabber Position (commanded)", datalog.servoPosition);
            telemetry.addData("Touched", datalog.touchPress);
            telemetry.addData("Pot. Voltage", datalog.potValue);
            telemetry.addData("Color Sensor Alpha (total light)", datalog.totalLight);
            telemetry.update();

            sleep(20);
        }

        /*
         * The datalog is automatically closed and flushed to disk after 
         * the OpMode ends - no need to do that manually :')
         */
    }

    /*
     * This class encapsulates all the fields that will go into the datalog.
     */
    public static class Datalog
    {
        // The underlying datalogger object - it cares only about an array of loggable fields
        private final Datalogger datalogger;

        // These are all of the fields that we want in the datalog.
        // Note that order here is NOT important. The order is important in the setFields() call below
        public Datalogger.GenericField opModeStatus = new Datalogger.GenericField("OpModeStatus");
        public Datalogger.GenericField loopCounter = new Datalogger.GenericField("Loop Counter");
        
        public Datalogger.GenericField motorEncoder = new Datalogger.GenericField("Lifter Enc.");
        public Datalogger.GenericField servoPosition = new Datalogger.GenericField("Grabber Pos.");
        public Datalogger.GenericField touchPress = new Datalogger.GenericField("Touched");
        public Datalogger.GenericField potValue = new Datalogger.GenericField("Pot. Value");
        public Datalogger.GenericField totalLight = new Datalogger.GenericField("Total Light");

        public Datalog(String name)
        {
            // Build the underlying datalog object
            datalogger = new Datalogger.Builder()

                    // Pass through the filename
                    .setFilename(name)

                    // Request an automatic timestamp field
                    .setAutoTimestamp(Datalogger.AutoTimestamp.DECIMAL_SECONDS)

                    // Tell it about the fields we care to log.
                    // Note that order *IS* important here! The order in which we list
                    // the fields is the order in which they will appear in the log.
                    .setFields(
                            opModeStatus,
                            loopCounter,
                            motorEncoder,
                            servoPosition,
                            touchPress,
                            potValue,
                            totalLight
                    )
                    .build();
        }

        // Tell the datalogger to gather the values of the fields
        // and write a new line in the log.
        public void writeLine()
        {
            datalogger.writeLine();
        }
    }
}
