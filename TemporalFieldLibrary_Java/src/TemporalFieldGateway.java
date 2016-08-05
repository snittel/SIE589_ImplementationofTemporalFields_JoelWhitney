/**
 * Created by Joel on 4/19/2016.
 * User just has to run this Class and a Gateway will be opened to the JVM allowing the Python plugin in QGIS to access this Class
 * User needs to:
 * 1. Declare DB connection details
 * 2. Run TemporalFieldGateway.main()
 * 3. Use plugin in QGIS
 */

import py4j.GatewayServer;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;


public class TemporalFieldGateway {
    /**
     * Declare DB connection details
     */
    public String cnxString;

    public String getdbtype() {
        // Break out cnxString and prepares the query to a HashMap of mapped variables
        Map<String, String> mappedSettings = new HashMap<String, String>(); // create hashmap for key value combos
        String[] splitSettings = cnxString.split(","); // split string into pairs
        for (int i = 0; i < splitSettings.length; i++) { // for each pair
            String splitSetting = splitSettings[i];
            String[] keyValue = splitSetting.split(";"); // split each pair into key/value
            mappedSettings.put(keyValue[0], String.valueOf(keyValue[1])); // assign to hashmap based on above split
        }
        String dbtype = (String) mappedSettings.get("dbtype");
        return dbtype;
    }

    public Data getData(){
        String dbtype = getdbtype();
        if (dbtype.equals("MySQL")) {
            MySQLData data = new MySQLData(cnxString);
            return data;
        } else if (dbtype.equals("PostgreSQL")){
            PostgreSQLData data = new PostgreSQLData(cnxString);
            return data;
        } else {
            CSVData data = new CSVData(cnxString);
            return data;
        }
    }

    public double getValue(String start, String end, String sensorID, String queryTime) throws ParseException, SQLException {
        // tempField1
        // Instiate tempField1 and getTimeValue
        Data data = getData();
        TemporalField tempField1 = new TemporalField(start, end, sensorID, data);
        Double value = tempField1.getValue(queryTime);
        System.out.printf("\nThe tempField1 object returned: %s at '%s'", value, queryTime);
        return value;
    }

    public double getAvg(String start, String end, String sensorID, String queryTime) throws ParseException, SQLException {
        // tempField1
        // Instiate tempField1 and getTimeValue
        Data data = getData();
        TemporalField tempField1 = new TemporalField(start, end, sensorID, data);
        Double value = tempField1.getAvg();
        System.out.printf("\nThe tempField1 object returned: %s at '%s'", value, queryTime);
        return value;
    }

    public double getMin(String start, String end, String sensorID, String queryTime) throws ParseException, SQLException {
        // tempField1
        // Instiate tempField1 and getTimeValue
        Data data = getData();
        TemporalField tempField1 = new TemporalField(start, end, sensorID, data);
        Double value = tempField1.getMin();
        System.out.printf("\nThe tempField1 object returned: %s at '%s'", value, queryTime);
        return value;
    }

    public double getMax(String start, String end, String sensorID, String queryTime) throws ParseException, SQLException {
        // tempField1
        // Instiate tempField1 and getTimeValue
        Data data = getData();
        TemporalField tempField1 = new TemporalField(start, end, sensorID, data);
        Double value = tempField1.getMax();
        System.out.printf("\nThe tempField1 object returned: %s at '%s'", value, queryTime);
        return value;
    }

    public void interpolateIntervals(String start, String end, String sensorID, String plotInterval, Integer interval, String csv, String outFolder, String plot) throws SQLException, ParseException, FileNotFoundException {
        Data data = getData();
        TemporalField tempField1 = new TemporalField(start, end, sensorID, data);
        // Create array of tempField objects and plot data
        TemporalField[] temporalFieldsArray = {tempField1};
        // Initiate PlotTimeSeries object
        PlotTimeSeries plotData = new PlotTimeSeries("Time Series of Temporal Fields");
        // Calculate the raw datapoints
        plotData.prepareRawData(temporalFieldsArray);
        if (plotInterval.equals("True")){
            // Calculate the interpolated datapoints at each interval
            plotData.prepareIntervals(temporalFieldsArray, interval);
        } if (csv.equals("True")) {
            // convert the XYDataset to CSV file
            plotData.convertToCSV(outFolder);
        } if (plot.equals("True")) {
            // Plot the datapoints
            plotData.plot();
        }
    }

    public static void main(String[] args) {
        TemporalFieldGateway app = new TemporalFieldGateway();
        if (args.length > 0) {
            app.cnxString = args[0];
        }
        // app is now the gateway.entry_point
        GatewayServer server = new GatewayServer(app, 25335);
        server.start();
    }
}