package org.firstinspires.ftc.teamcode;

import android.support.annotation.NonNull;

import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDeviceWithParameters;
import com.qualcomm.robotcore.hardware.configuration.I2cSensor;
import com.qualcomm.robotcore.util.TypeConversion;

/*
 * Created by Dryw Wade
 *
 * Driver for Adafruit's MCP9808 temperature sensor
 *
 * This version of the driver makes use of the I2C device with parameters, which allows the user to
 * choose certain settings for the configuration register. This requires a slightly different setup
 * of the sensor in the OpMode, where a parameters object must be created, then the options inside
 * it can be modified, then the sensor is initialized with the parameters.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // Ignore access and unused warnings
// Both driver classes cannot register the sensor at the same time. One driver should have the
// sensor registered, and the other should be commented out
//@I2cSensor(name = "MCP9808 Temperature Sensor", description = "Temperature Sensor from Adafruit", xmlTag = "MCP9808")
public class MCP9808Params extends I2cDeviceSynchDeviceWithParameters<I2cDeviceSynch, MCP9808Params.Parameters>
{
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // User Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public double getTemperature()
    {
        short dataRaw = getTemperatureRaw();

        // The first 3 bits are alert bits that we don't care about here. We need to force them to
        // be 0s or 1s if the number is positive or negative depending on the sign
        if((dataRaw & 0x1000) == 0x1000) // Negative
            dataRaw |= 0xE000;
        else // Positive
            dataRaw &= 0x1FFF;

        // Multiply by least significant bit (2^-4 = 1/16) to scale
        return dataRaw / 16.0;
    }

    public double getTemperatureLimit(Register register)
    {
        // Register is shifted by 2
        short dataRaw = (short) (getTemperatureLimitRaw(register) >> 2);

        // The first 5 bits need to be forced to be 0s or 1s depending on the sign of the number
        if((dataRaw & 0x0400) == 0x0400) // Negative
            dataRaw |= 0xF800;
        else // Positive
            dataRaw &= 0x07FF;

        // Multiply by least significant bit (2^-2 = 1/4) to scale
        return dataRaw / 4.0;
    }

    public void setTemperatureLimit(double limit, Register register)
    {
        // Make sure we're given valid register
        if(!(register == Register.T_LIMIT_LOWER || register == Register.T_LIMIT_UPPER || register == Register.T_LIMIT_CRITICAL))
            throw new IllegalArgumentException("Invalid temperature limit register!");

        // Register only accepts values ranging from -256 to 255.75
        if(limit > 255.75 || limit < -256.0)
            throw new IllegalArgumentException("Temperature limit out of bounds!");

        // Divide by least significant bit (2^-2 = 1/4)
        short temp = (short) (limit * 4.0);

        // Register shifted by 2
        writeShort(register, (short) (temp << 2));
    }

    public boolean criticalLimitTriggered()
    {
        return (getTemperatureRaw() & 0x8000) == 0x8000;
    }

    public boolean upperLimitTriggered()
    {
        return (getTemperatureRaw() & 0x4000) == 0x4000;
    }

    public boolean lowerLimitTriggered()
    {
        return (getTemperatureRaw() & 0x2000) == 0x2000;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Raw Register Reads
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public short getTemperatureRaw()
    {
        return readShort(Register.TEMPERATURE);
    }

    public short getTemperatureLimitRaw(Register register)
    {
        // Make sure we're given valid register
        if(!(register == Register.T_LIMIT_LOWER || register == Register.T_LIMIT_UPPER || register == Register.T_LIMIT_CRITICAL))
            throw new IllegalArgumentException("Invalid temperature limit register!");

        return readShort(register);
    }

    public short getManufacturerIDRaw()
    {
        return readShort(Register.MANUFACTURER_ID);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Read and Write Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected void writeShort(final Register reg, short value)
    {
        deviceClient.write(reg.bVal, TypeConversion.shortToByteArray(value));
    }

    protected short readShort(Register reg)
    {
        return TypeConversion.byteArrayToShort(deviceClient.read(reg.bVal, 2));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Registers and Config Settings
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public enum Register
    {
        FIRST(0),
        CONFIGURATION(0x01),
        T_LIMIT_UPPER(0x02),
        T_LIMIT_LOWER(0x03),
        T_LIMIT_CRITICAL(0x04),
        TEMPERATURE(0x05),
        MANUFACTURER_ID(0x06),
        DEVICE_ID_REVISION(0x07),
        RESOLUTION(0x08),
        LAST(RESOLUTION.bVal);

        public int bVal;

        Register(int bVal)
        {
            this.bVal = bVal;
        }
    }

    public enum Hysteresis
    {
        HYST_0(0x0000),
        HYST_1_5(0x0200),
        HYST_3(0x0400),
        HYST_6(0x0600);

        public int bVal;

        Hysteresis(int bVal)
        {
            this.bVal = bVal;
        }
    }

    public enum AlertControl
    {
        ALERT_DISABLE(0x0000),
        ALERT_ENABLE(0x0008);

        public int bVal;

        AlertControl(int bVal)
        {
            this.bVal = bVal;
        }
    }

    // More settings are available on the sensor, but not included here. Could be added later

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Construction and Initialization
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public final static I2cAddr ADDRESS_I2C_DEFAULT = I2cAddr.create7bit(0x18);

    public MCP9808Params(I2cDeviceSynch deviceClient)
    {
        super(deviceClient, true, new Parameters());

        this.setOptimalReadWindow();
        this.deviceClient.setI2cAddress(ADDRESS_I2C_DEFAULT);

        super.registerArmingStateCallback(false); // Deals with USB cables getting unplugged
        // Sensor starts off disengaged so we can change things like I2C address. Need to engage
        this.deviceClient.engage();
    }

    protected void setOptimalReadWindow()
    {
        // Sensor registers are read repeatedly and stored in a register. This method specifies the
        // registers and repeat read mode
        I2cDeviceSynch.ReadWindow readWindow = new I2cDeviceSynch.ReadWindow(
                Register.FIRST.bVal,
                Register.LAST.bVal - Register.FIRST.bVal + 1,
                I2cDeviceSynch.ReadMode.REPEAT);
        this.deviceClient.setReadWindow(readWindow);
    }

    @Override
    protected synchronized boolean internalInitialize(@NonNull Parameters params)
    {
        this.parameters = params.clone();

        deviceClient.setI2cAddress(params.i2cAddr);

        int configSettings = params.hysteresis.bVal | params.alertControl.bVal;

        writeShort(Register.CONFIGURATION, (short) configSettings);

        // Mask out alert signal bit, which we can't control
        return (readShort(Register.CONFIGURATION) & 0xFFEF) == configSettings;
    }

    public static class Parameters implements Cloneable
    {
        I2cAddr i2cAddr = ADDRESS_I2C_DEFAULT;

        // All settings available
        Hysteresis hysteresis = Hysteresis.HYST_0;
        AlertControl alertControl = AlertControl.ALERT_DISABLE;

        public Parameters clone()
        {
            try
            {
                return (Parameters) super.clone();
            }
            catch(CloneNotSupportedException e)
            {
                throw new RuntimeException("Internal Error: Parameters not cloneable");
            }
        }
    }

    @Override
    public Manufacturer getManufacturer()
    {
        return Manufacturer.Adafruit;
    }

    @Override
    public String getDeviceName()
    {
        return "Adafruit MCP9808 Temperature Sensor";
    }
}
