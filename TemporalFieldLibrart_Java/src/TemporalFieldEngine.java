import org.jfree.chart.plot.Plot;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.ParseException;

/**
 * Created by Joel on 1/5/2016.
 * The engine that runs the TemporalField estimation. User needs to:
 * 1. Choose (comment/uncomment) which cnxString to use (based on data location).
 * 2. Choose (comment/uncomment) which Data object to use (based on data location).
 * 3. Choose whether to use the static (1) or streaming (2) functionality
 * 4. Create tempField object(s) and adjust the settings.
 * (STATIC)
 * 5a. Create PlotTimeSeries object and adjust the settings.
 * 5b. User can plot raw data and/or interpolated values at various intervals
 * (STREAMING)
 * 5. Instantiate StreamingEngine object
 * 6a. Create PlotTimeSeries object and adjust the settings.
 * 6b. User can plot raw data and/or interpolated values at various intervals
 */

public class TemporalFieldEngine {
    // DECLARE VARIABLES
    /**
     * 1. Choose connection string
     * (Using three in this example)
     */
    public static String cnxString = "dbtype;MySQL,host;localhost,port;8889,database;BlueberrySensors,table;MoistureDataFAKE,username;root,password;root"; // MySQL cnxString (for use with streaming and python script)
    //public static String cnxString = "dbtype;MySQL,host;localhost,port;8889,database;BlueberrySensors,table;moisturedata,username;root,password;root"; // MySQL cnxString (for use with the real static data)
    //public static String cnxString = "dbtype;PostgreSQL,host;localhost,port;5432,database;blueberrysensors,table;moisturedata,username;joelw,password;999jbw"; // PostgreSQL cnxString (for use with the real static data)
    //public static String cnxString = "dbtype;MySQL,host;108.167.160.69,port;3306,database;abconet1_BlueberrySensors,table;MoistureData,username;abconet1_read,password;readnotwrite"; // eHost MySQL cnxString
    //public static String cnxString = "dbtype;CSV,csvdir;C:\\Users\\Joel\\Dropbox\\School Material\\Graduate Project\\,table;MoistureData"; // CSV cnxString (for real static data)
    //public static String cnxString = "asd"; // Spark cnxString

    // Main program
    public static void main(String[] args) throws ParseException, SQLException, FileNotFoundException {
        /** 2. Choose which Data object to instantiate
         * (Using three in this example)
         */
        MySQLData data = new MySQLData(cnxString);
        //PostgreSQLData data = new PostgreSQLData(cnxString);
        //CSVData data = new CSVData(cnxString);

        /** 3. Choose whether to use the static (false) or streaming (true) functionality
         *
         */
        Boolean streamingData = true;

        // STATIC FUNCTIONALITY SECTION
        if (!streamingData) {
            /** 4. Create tempField object(s) and adjust the settings.
             * (Using three in this example)
             */
            // tempField1
            String start1, end1, sensorID1, queryTime1;
            start1 = "2015-09-03 09:19:00";
            end1 = "2015-09-13 01:43:00";
            sensorID1 = "1";
            queryTime1 = "2015-09-04 01:22:00";
            // Instantiate tempField1 and getTimeValue
            TemporalField tempField1 = new TemporalField(start1, end1, sensorID1, data);
            System.out.printf("\nThe tempField1 object returned: %s at '%s'", tempField1.getValue(queryTime1), queryTime1);

//            // tempField2
//            String start2, end2, sensorID2, queryTime2;
//            start2 = "2015-09-03 09:19:00";
//            end2 = "2015-09-13 01:43:00";
//            sensorID2 = "2";
//            queryTime2 = "2015-09-04 01:22:00";
//            // Instantiate tempField1 and getTimeValue
//            TemporalField tempField2 = new TemporalField(start2, end2, sensorID2, data);
//            System.out.printf("\nThe tempField2 object returned: %s at '%s'", tempField2.getValue(queryTime2), queryTime2);
//
//            // tempField3
//            String start3, end3, sensorID3, queryTime3;
//            start3 = "2015-09-03 09:19:00";
//            end3 = "2015-09-13 01:43:00";
//            sensorID3 = "3";
//            queryTime3 = "2015-09-04 01:22:00";
//            // Instantiate tempField1 and getTimeValue
//            TemporalField tempField3 = new TemporalField(start3, end3, sensorID3, data);
//            System.out.printf("\nThe tempField3 object returned: %s at '%s'", tempField3.getValue(queryTime3), queryTime3);

            /** 5a. Create PlotTimeSeries object and adjust the settings.
             * User can plot raw data and/or interpolated values at various intervals
             */
            // Create array of tempField objects and plot data
            TemporalField[] temporalFieldsArray = {tempField1};
            // Initiate PlotTimeSeries object
            PlotTimeSeries plotData = new PlotTimeSeries("Time Series of Temporal Fields");
            // Calculate the raw datapoints
            plotData.prepareRawData(temporalFieldsArray);
            /** 5b. User can plot raw data and/or interpolated values at various intervals
             *
             */
            // Calculate the interpolated datapoints at each interval
            //plotData.prepareIntervals(temporalFieldsArray, 5);
            plotData.prepareIntervals(temporalFieldsArray, 999);

            plotData.convertToCSV("C:/Users/Joel/Desktop");
            // Plot the datapoints
            plotData.plot();
        }
        // STREAMING FUNCTIONALITY SECTION
        else {

            /** 4. Create window for tempField object(s) over streaming data.
             * (Using three in this example)
             */
            // tempField1
            String start1, end1, sensorID1, queryTime1;
            start1 = "now";
            end1 = "future";
            sensorID1 = "1";
            queryTime1 = ""; // <- not used for streaming field. User will specify minutes to get value from start of window
            // Instiate tempField1 and getTimeValue
            TemporalField tempField1 = new TemporalField(start1, end1, sensorID1, data);

            // tempField2
            String start2, end2, sensorID2, queryTime2;
            start2 = "now";
            end2 = "future";
            sensorID2 = "2";
            queryTime2 = ""; // <- not used for streaming field. User will specify minutes to get value from start of window
            // Instiate tempField1 and getTimeValue
            TemporalField tempField2 = new TemporalField(start2, end2, sensorID2, data);

            // tempField3
            String start3, end3, sensorID3, queryTime3;
            start3 = "now";
            end3 = "future";
            sensorID3 = "3";
            queryTime3 = ""; // <- not used for streaming field. User will specify minutes to get value from start of window
            // Instiate tempField1 and getTimeValue
            TemporalField tempField3 = new TemporalField(start3, end3, sensorID3, data);

            /** 5. Instantiate StreamingEngine object
             *  User passes StreamingEngine TempFieldObject/windowSize and can do: getAvg, getMax, getMin over window (from StreamWindow Class)
             */
            // Stream window settings
            Integer windowSizeMIN = 10;
            Integer windowOverlapMIN = 8;
            // Create array of tempField objects and plot data
            TemporalField[] temporalFieldsArray = {tempField1, tempField2, tempField3};
            // Instantiate StreamEngine objects for each tempField
            StreamingEngine stream = new StreamingEngine(temporalFieldsArray, windowSizeMIN, windowOverlapMIN);

            /** 6a. Create PlotTimeSeries object and adjust the settings.
             *
             */
            // Initiate PlotTimeSeries object
            PlotTimeSeries plotData = new PlotTimeSeries("Time Series of Temporal Fields");

            while (true) {
                // Return values for each stream
                // stream1
                stream.updateStream();
                System.out.printf("\nThe tempField1 object returned a min/max/avg value of '%.4f'/'%.4f'/'%.4f' on %s tuples", tempField1.getMin(), tempField1.getMax(), tempField1.getAvg(), tempField1.getResultSetSize());
                System.out.printf("\nThe tempField2 object returned a min/max/avg value of '%.4f'/'%.4f'/'%.4f' on %s tuples", tempField2.getMin(), tempField2.getMax(), tempField2.getAvg(), tempField2.getResultSetSize());
                System.out.printf("\nThe tempField3 object returned a min/max/avg value of '%.4f'/'%.4f'/'%.4f' on %s tuples", tempField3.getMin(), tempField3.getMax(), tempField3.getAvg(), tempField3.getResultSetSize());

                /** 6b. User can plot raw data and/or interpolated values at various intervals
                 *
                 */
                // Calculate the raw datapoints
                plotData.prepareRawData(temporalFieldsArray);
                // Calculate the interpolated datapoints at each interval
                //plotData.prepareIntervals(temporalFieldsArray, 1);
                //plotData.prepareIntervals(temporalFieldsArray, 2);
                plotData.prepareIntervals(temporalFieldsArray, 3);

                // If first pass open plot, otherwise refresh
                if (stream.getPassCount() == 1) {
                    plotData.plot();
                } else {
                    plotData.refreshChart();
                }
            }
        }
    }
}