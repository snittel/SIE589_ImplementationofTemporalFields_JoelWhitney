import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Joel on 1/13/2016.
 * The superclass for the different value estimation models/methods. This class doesn't do a whole lot besides
 * provides a means to initialize variables to be used by all children. It also includes a getRow() method that is used
 * by both the NearestNeighbor and the LinearRegression in the calculation process.
 */

public abstract class Interpolation {
    // DECLARE VARIABLES
    public Data dataObject;
    public String startDate, endDate, sensorID, queryTime;

    // CONSTRUCTOR
    public Interpolation(Data data, String start, String end, String sID, String time){
        dataObject = data;
        startDate = start;
        endDate = end;
        sensorID = sID;
        queryTime = time;
    }

    // METHODS
    public abstract Double returnValue() throws SQLException;

    // get row count method
    public int getRowCount(ResultSet data) {
        if (data == null) {
            return 0;
        }
        try {
            data.last();
            return data.getRow();
        } catch (SQLException exp) {
            exp.printStackTrace();
        } finally {
            try {
                data.beforeFirst();
            } catch (SQLException exp) {
                exp.printStackTrace();
            }
        }
        return 0;
    }
}
