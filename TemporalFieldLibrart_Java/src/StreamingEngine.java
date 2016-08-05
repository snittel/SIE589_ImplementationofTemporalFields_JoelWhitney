import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.SQLException;

/**
 * Created by Joel on 1/25/2016.
 */
public class StreamingEngine {
    // DECLARE VARIABLES
    public TemporalField[] temporalFieldArray;
    public Integer windowSizeMin;
    public Integer windowOverlapMIN;
    public DateTime start;
    public DateTime end;
    public Integer pass = 0;

    // CONSTRUCTOR
    public StreamingEngine(TemporalField[] tempFieldArray, Integer windowSize, Integer windowOverlap) {
        temporalFieldArray = tempFieldArray;
        windowSizeMin = windowSize;
        windowOverlapMIN = windowOverlap;
    }

    // METHODS
    /**  startStream() is the stream engine and returns a double depending on
     *      what the user selects as their method for the value variable
     */
    public void updateStream() throws SQLException {
        updateTempField();
        DateTime now = new DateTime();
        while (end.isAfter(now)){
            now = new DateTime();
        }
        pass += 1;
        // Return value
    }

    public void updateTempField() {
        DateTime now = new DateTime();
        if (pass == 0) {
            start = now;
        } else {
            start = now.minusMinutes(windowOverlapMIN);
        }
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        String startString = format.print(start);
        end = start.plusMinutes(windowSizeMin);
        String endString = format.print(end);
        for (int i = 0; i < temporalFieldArray.length; i++) {
            TemporalField tempField = temporalFieldArray[i];
            tempField.updateStartEnd(startString, endString);
        }
        System.out.printf("\n\nFilling a %s minute window with a %s minute overlap ('%s' - '%s')...", windowSizeMin, windowOverlapMIN, startString, endString);
    }

    public int getPassCount() {
        return pass;
    }
}
