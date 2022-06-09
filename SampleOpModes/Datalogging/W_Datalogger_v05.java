/*
 
 This 'utility' class allows FTC OpModes to log data to a CSV file,
 for direct import to a spreadsheet like Excel or Google Sheets.
 
 The spreadsheet's default first column ("Time") shows # of seconds since
 the datalogging class was instantiated by the user's OpMode, which may
 or may not begin immediate data collection/logging.
 
 The second default column is the time (in milliseconds) *between*
 data readings. This avoids the need to later calculate the "delta"
 between chronological time readings.
 
 The remaining columns are determined by the user's OpMode.
 
 
 This class processes all data as text (type char or String) to create the
 comma-separated-values (CSV) file.  The spreadsheet still interprets any
 numeric data as numbers.
 
 
 
 
 1/6/2022   This v05 supports downloading the datalog file from OnBot Java,
 rather than file transfer via USB cable or wireless Android Debug Bridge (adb).
 The latter methods can still be used if needed.
 
 For reasons unique to OBJ, the datalog is now initially saved as a .txt file,
 then downloaded as a .csv file.
 
 1/3/2022     v04 inserted the timestamp at the beginning of a completed
 line, just before writing the line to the datalog file.  Previously
 the timestamp was inserted at the beginning of a new line, after writing
 the last completed line. Neither method records the exact moment of data 
 capture, but this method has less time lag and relates to the current
 line rather than the previous line.

 v03 added another default column: the time (in milliseconds) *between*
 data readings. This avoids the need to later calculate the "delta"
 between chronological time readings.
 
 v02 moved code from the *public* method newLine() to a *private* method
 flushLineBuffer(), to meet Java encapsulation guidelines.
 
 v02 also added a finalize() method to provide garbage collection and 
 improve exception handling, 

Future features?

Ensure data is logged to the intended field, possibly using
hashmaps.  Don't rely on the order of .addField() commands to correspond to 
the order of column headings.
DS: "You can introduce the concept of data schemas by using maps to store the data 
till it's flushed to the disk where the tags in the data are written in a stable order.""

Also: support asynchronous data sources?

DS: A bonus section of the concept of "event overlays" embedded in the value where 
you log an "event" to a row, tagged with the timestamp it happened with all other 
fields being nil. These markers help when parsing the wall of data to know when 
some values (esp sensor values) should see discontinuities.

 Side note: As of release JDK 5, the StringBuffer class has been supplemented 
 with an equivalent class designed for use by a single thread, StringBuilder.
 The StringBuilder class should generally be used in preference to this one,
 as it supports all of the same operations but it is faster, as it performs
 no synchronization.


 Credit to Olavi Kamppari, who shared a more advanced version dated 9/9/2015.

*/

package org.firstinspires.ftc.teamcode;

import java.io.File;                    // already used in FTC SDK
import java.io.Writer;
import java.io.IOException;
import java.io.FileWriter;              // subclass of java.io.Writer

public class W_Datalogger_v05 {
    
    // Declare members.
    private Writer writer;              // contains write() method to store file
    private StringBuffer lineBuffer;    // its methods build each line (row) of data
    private long timeBase;              // time of instantiation (milliseconds)
    private long nsBase;                // time of reset (nanoseconds)    

    // This constructor runs once, to initialize an instantiation of the class.
    public W_Datalogger_v05 (String fileName) {
        
        // Build the path with the filename provided by the calling OpMode.
        String directoryPath    = "/sdcard/FIRST/java/src/Datalogs";
        String filePath         = directoryPath + "/" + fileName + ".txt";
        
        // src and any subfolder contents appear in OnBot Java (left side).
        // .txt files allow data display in OBJ.  Download as .csv files.
        
        new File(directoryPath).mkdir();  // create Datalogs folder if needed
        
        // Set up the file writer and line buffer.
        try {
            writer = new FileWriter(filePath);
            lineBuffer = new StringBuffer(128);     // initial length 128
        }
        catch (IOException e) {
        }
        
        timeBase = System.currentTimeMillis();
        nsBase = System.nanoTime();
        addField("Time");               // first/default column label
        addField("d ms");               // second/default column label
        
    }   // end constructor


    // This *private* method is called by the *public* methods firstLine()
    // and newLine().
    private void flushLineBuffer(){

        try {
            lineBuffer.append('\n');                // end-of-line character
            writer.write(lineBuffer.toString());    // add line (row) to file
            lineBuffer.setLength(0);                // clear the line (row)
        }
        catch (IOException e) {
        }
        
    }   // end flushLineBuffer() method
    

    // This *private* method is called by the *public* method newLine().
    private void insertTimestamps(){
        
        long milliTime,nanoTime;

        // Update time for first two columns (cumulative and incremental time).
        milliTime   = System.currentTimeMillis();
        nanoTime    = System.nanoTime();

        // Insert timestamps at position 0, *before* the OpMode data fields.
        lineBuffer.insert
            
            (0, String.format("%.3f",(milliTime - timeBase) / 1000.0) + ','
              + String.format("%.3f",(nanoTime - nsBase) / 1.0E6) + ',');

        // Divide milliseconds by 1,000 to log seconds, in field named "Time".
        // Divide nanoseconds by 1,000,000 to log milliseconds, in "d ms".

        // The 1000.0 decimal and 1.0E6 scientific notation avoid a type error;
        // the expressions' variables are 'long'.

        nsBase      = nanoTime;         // reset for incremental time delta

    }   // end insertTimestamps() method
    

    // The OpMode calls this *public* method to complete the first row (labels).
    public void firstLine() {
        flushLineBuffer();
    }

    // The OpMode calls this *public* method to add timestamps and complete the
    // current line (row) of data.
    public void newLine() {
        insertTimestamps();
        flushLineBuffer();
    }
    
    
    // These two (overloaded) methods add a text field to the line (row),
    // preceded by a comma.  This creates the comma-separated values (CSV).
    
    public void addField(String s) {
        if (lineBuffer.length()>0) {
            lineBuffer.append(',');
        }
        lineBuffer.append(s);
    }

    public void addField(char c) {
        if (lineBuffer.length()>0) {
            lineBuffer.append(',');
        }
        lineBuffer.append(c);
    }
    // Checking the line length (before inserting a comma) is not needed when a 
    // default timestamp (and its comma) will be inserted before all data, as in
    // the current example. The check is here in case the default timestamp is removed.
    
    
    // The following (overloaded) method converts Boolean to text 
    // (Java type char) and adds it to the current line (row).
    
    public void addField(boolean b) {
        addField(b ? '1' : '0');
    }

    // These (overloaded) methods accept various numeric types,
    // all converted to type String for the method listed above.
    // Spreadsheet programs typically interpret these correctly as numbers.

    public void addField(byte b) {
        addField(Byte.toString(b));
    }

    public void addField(short s) {
        addField(Short.toString(s));
    }

    public void addField(long l) {
        addField(Long.toString(l));
    }

    public void addField(float f) {
        addField(Float.toString(f));
    }

    public void addField(double d) {
        addField(Double.toString(d));
    }

    // Any 'int' values are processed as 'long', through Java's implicit
    // type casting or type promotion.


    // The OpMode calls this method to allow optional reset of timers.
    public void resetTime() {
        timeBase = System.currentTimeMillis();
        nsBase = System.nanoTime();
    }


    // The OpMode must call this method when finished logging data.
    public void closeDataLogger() {
        try {
            writer.close();             // close the file
        }
        catch (IOException e) {
        }
    }


    // This method provides garbage collection and improves exception handling.
    @Override
    protected void finalize() throws Throwable {
        closeDataLogger();
        super.finalize();
    }
    
}   // end class
