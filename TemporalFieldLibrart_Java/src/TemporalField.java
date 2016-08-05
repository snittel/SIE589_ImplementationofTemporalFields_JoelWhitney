import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

/**
 * Created by Joel on 1/5/2016.
 * The TemporalField model. Allows the user to create a TemporalField object based on the TemporalFieldEngine settings.
 * The TemporalField has one method (getValue()) prints out the value depending on the Interpolation object instantiated.
 * The user needs to:
 * 1. Choose (comment/uncomment) which Interpolation object to use (based on what method they prefer).
 */

public class TemporalField {
    // DECLARE VARIABLES
    public String startDate, endDate, queryTime, sensorID;
    public Data dataObject;

    // CONSTRUCTOR
    public TemporalField(String start, String end, String sID, Data datum) throws ParseException {
        startDate = start;
        endDate = end;
        sensorID = sID;
        dataObject = datum;
    }

    // Get value method
    public Double getValue(String time) throws SQLException {
        // declare variable
        //ResultSet data = getData();
        queryTime = time;

        /** 1. Choose Interpolation object to instantiate
        */
        //NearestNeighbor getTimeValue = new NearestNeighbor(dataObject, startDate, endDate, sensorID, queryTime);
        LinearRegression getTimeValue = new LinearRegression(dataObject, startDate, endDate, sensorID, queryTime);

        // Call returnValue() on the getValue object
        return getTimeValue.returnValue();
    }


    public Double getAvg() throws SQLException {
        // declare variables
        ResultSet data = getData();
        Double sum = 0.0;
        Double average;
        // get row count of ResultSet
        int rowCount = getResultSetSize();
        // for each tuple(i) in ResultSet rowCount
        if (data.first()) {
            for (int iIndex = 0; iIndex < rowCount; iIndex++) {
                String valueString = data.getString("value");
                Double valueDouble = Double.parseDouble(valueString);
                sum += valueDouble;
                data.next();
            }
        }
        average = sum / (rowCount);
        return average;
    }

    public Double getMin() throws SQLException {
        // declare variables
        ResultSet data = getData();
        Double min = 0.0;
        // get row count of ResultSet
        int rowCount = getResultSetSize();
        // for each tuple(i) in ResultSet rowCount
        if (data.first()) {
            for (int iIndex = 0; iIndex < rowCount; iIndex++) {
                String valueString = data.getString("value");
                Double valueDouble = Double.parseDouble(valueString);
                if (iIndex == 0) {
                    min = valueDouble;
                } else if (valueDouble < min) {
                    min = valueDouble;
                }
                data.next();
            }
        }
        return min;
    }

    public Double getMax() throws SQLException {
        // declare variables
        ResultSet data = getData();
        Double max = 0.0;
        // get row count of ResultSet
        int rowCount = getResultSetSize();
        // for each tuple(i) in ResultSet rowCount
        if (data.first()) {
            for (int iIndex = 0; iIndex < rowCount; iIndex++) {
                String valueString = data.getString("value");
                Double valueDouble = Double.parseDouble(valueString);
                if (iIndex == 0) {
                    max = valueDouble;
                } else if (valueDouble > max) {
                    max = valueDouble;
                }
                data.next();
            }
        }
        return max;
    }

    // The getData method
    public ResultSet getData() {
        // prepare cnx variables
        dataObject.prepareCNXVariables(startDate, endDate, sensorID);
        // get data
        return dataObject.getData();
    }

    // Allows TempField to update constructor parameters
    public void updateStartEnd(String start, String end) {
        startDate = start;
        endDate = end;
    }

    // get RS size
    public int getResultSetSize() throws SQLException {
        ResultSet data = getData();
        data.last();
        int resultSetSize = data.getRow();
        data.beforeFirst();
        return resultSetSize;
    }

}