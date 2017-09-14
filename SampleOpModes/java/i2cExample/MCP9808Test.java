package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/*
 * Created by Dryw Wade
 *
 * OpMode for testing Adafruit's MCP9808 temperature sensor driver
 */
@TeleOp(name = "MCP9808", group = "Tests")
public class MCP9808Test extends LinearOpMode
{
    private MCP9808 tempSensor;

    public void runOpMode() throws InterruptedException
    {
        tempSensor = hardwareMap.get(MCP9808.class, "tempSensor");

        // Uncomment to use parameter version of driver class. This will require you to respecify
        // the sensor type from MCP9808 to MCP9808Params
//        MCP9808Params.Parameters parameters = new MCP9808Params.Parameters();
//        parameters.hysteresis = MCP9808Params.Hysteresis.HYST_1_5;
//        parameters.alertControl = MCP9808Params.AlertControl.ALERT_ENABLE;
//        tempSensor.initialize(parameters);

        tempSensor.setTemperatureLimit(24, MCP9808.Register.T_LIMIT_LOWER);
        tempSensor.setTemperatureLimit(26, MCP9808.Register.T_LIMIT_UPPER);
        tempSensor.setTemperatureLimit(25, MCP9808.Register.T_LIMIT_CRITICAL);

        waitForStart();

        while(opModeIsActive())
        {
            telemetry.addData("Temperature", tempSensor.getTemperature());
            telemetry.addData("", "");

            telemetry.addData("Lower Limit", tempSensor.getTemperatureLimit(MCP9808.Register.T_LIMIT_LOWER));
            telemetry.addData("Lower Limit Triggered", tempSensor.lowerLimitTriggered());
            telemetry.addData("Upper Limit", tempSensor.getTemperatureLimit(MCP9808.Register.T_LIMIT_UPPER));
            telemetry.addData("Upper Limit Triggered", tempSensor.upperLimitTriggered());
            telemetry.addData("Critical Limit", tempSensor.getTemperatureLimit(MCP9808.Register.T_LIMIT_CRITICAL));
            telemetry.addData("Critical Limit Triggered", tempSensor.criticalLimitTriggered());
            telemetry.addData("", "");

            telemetry.addData("Config", Integer.toHexString(tempSensor.readShort(MCP9808.Register.CONFIGURATION)));
            telemetry.addData("Manufacturer ID", tempSensor.getManufacturerIDRaw());

            telemetry.update();
            idle();
        }
    }
}
