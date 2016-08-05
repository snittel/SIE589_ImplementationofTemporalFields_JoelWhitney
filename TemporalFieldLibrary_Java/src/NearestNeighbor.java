import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Joel on 1/15/2016.
 * One estimation model of the Interpolation class.
 * Represents a method for retrieving the closest value to the queried time for a particular sensorID.
 */

public class NearestNeighbor extends Interpolation {
    // DECLARE VARIABLES
    public String rows = "1";

    // CONSTRUCTOR
    public NearestNeighbor(Data data, String start, String end, String sID, String time) {
        super(data, start, end, sID, time);
    }

    // METHODS
    // This will be specific for each Interpolation child
    @Override
    public Double returnValue() throws SQLException {
        // declare variables
        DateTime timeObject;
        DateTime nearestDate = null;
        String dateObjectString;
        DateTime dateObject;
        Double nearestValue = null;
        // convert string time into date object timeObject
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        timeObject = format.parseDateTime(queryTime);

        // GET DATA
        // prepare cnx vars in data object
        dataObject.prepareCNXVariables(startDate, endDate, sensorID);
        // build query based on interpolation
        dataObject.updateQuery(rows, startDate, endDate, sensorID, queryTime);
        // get data from map
        ResultSet data = dataObject.getData();

        // get row count of ResultSet
        int rowCount = getRowCount(data);
        // for each tuple(i) in ResultSet rowCount
        if (data.first()) {
            String nearestValueString = data.getString("value");
            nearestValue = Double.parseDouble(nearestValueString);
        }
        //System.out.printf("The nearest value for Sensor %s at '%s' is %.4f\n", sensorID, queryTime, nearestValue);
        return nearestValue;
    }
}
