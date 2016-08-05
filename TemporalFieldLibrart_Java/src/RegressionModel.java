import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joel on 1/17/2016.
 * One estimation model of the Interpolation class.
 * Represents a regression model with one defined independent variable. Provides operations to compute the regression coefficients
 * and evaluate the resulting function at a certain point.
 */

public abstract class RegressionModel extends Interpolation {
    // DECLARE VARIABLES
    public boolean computed;     // Have the unknown parameters been calculated yet?
    public double a, b;     // The y intercept of the straight line, The gradient of the line
    public String rows = "2";     // number of tuples for queryWindow ( split between over queryTime )
    public Double xValues[], yValues[];      // The X values of the data set points 'datetime',  // The Y values of the data set points 'value';
    public Integer preTup = 0;      // Needs to be 1 preTup
    public Integer postTup = 0;      // Needs to be 1 postTup
    public Integer sameTup = 0;      // Needs to be 1 sameTup

    // CONSTRUCTOR
    public RegressionModel(Data data, String start, String end, String sID, String time) {
        super(data, start, end, sID, time);
    }

    // METHODS
    // This will be specific for each Interpolation child
    public abstract Double returnValue() throws SQLException;

    // Iterate through data and if tuple datetime is within window pass to the x/y arrays
    // Returns nothing, but gets x/y arrays for the Regression calculations
    public void getArrays() throws SQLException {
        String dateObjectString;
        DateTime dateObject;
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime queryTimeObject = format.parseDateTime(queryTime);
        Double tupDateMS = null;
        Double tupValue = null;

        // Initialize arrays
        List<Double> xValuesAL = new ArrayList<Double>();     // The X values of the data set points 'datetime
        List<Double> yValuesAL = new ArrayList<Double>();     // The Y values of the data set points 'value';

        // GET DATA
        // prepare cnx vars in data object
        dataObject.prepareCNXVariables(startDate, endDate, sensorID);
        // build query based on interpolation
        dataObject.updateQuery(rows, startDate, endDate, sensorID, queryTime);
        // get data from map
        ResultSet data = dataObject.getData();
        // check tuples to make sure there is tuple before and after querytime
        checkTuples(data);

        //  ITERATE OVER RESULT SET AND ADD TO ARRAY LIST dates and VALUES
        // Loop through RS and fetch the tuple values before and after querytime
        Integer rowCount = getRowCount(data);
        if (data.first()) {
            // make sure there is 1 tuple for either: before and on/after OR after and on/before
            if ((preTup == 1 && (postTup == 1 || sameTup == 1)) || (postTup == 1 && (preTup == 1 || sameTup == 1))) {
                for (int i = 0; i < rowCount; i++) {
                    dateObjectString = data.getString("datetime");
                    String valueString = data.getString("value");
                    Double valueDouble = Double.parseDouble(valueString);
                    if (dateObjectString.endsWith(".0")) {
                        dateObjectString = dateObjectString.substring(0, dateObjectString.length() - 2);
                    }
                    dateObject = format.parseDateTime(dateObjectString);
                    double dateMS = dateObject.getMillis();
                    tupDateMS = dateMS;
                    tupValue = valueDouble;
                    // Add before and after values to ArrayList
                    xValuesAL.add(tupDateMS);
                    yValuesAL.add(tupValue);
                    data.next();
                }
            } else {
                System.out.println("\nAdjust queryTime to be within temporal field window");
            }
        }
        // Converts ArrayList into actual Array to be used by calculation.
        // This will be useful if I restructure so window can be number of rows.
        xValues = new Double[xValuesAL.size()];
        for (int i = 0; i < xValuesAL.size(); i++) xValues[i] = xValuesAL.get(i);
        yValues = new Double[yValuesAL.size()];
        for(int i = 0; i < yValuesAL.size(); i++) yValues[i] = yValuesAL.get(i);
    }

    // check to make sure there is tuples before and after query time
    public void checkTuples(ResultSet data) throws SQLException {
        // ITERATE THROUGH RS AND IF ALL NODES ARE POSITIVE/NEGATIVE RETURN ERROR.
        // THERE SHOULD BE AT LEAST ONE NEGATIVE AND ONE POSITIVE TIMEDIFF OTHERWISE THE QUERYTIME IS AFTER THE CLOSEST NODES. THIS WILL ATTEMPT TO PREDICT A FUTURE VALUE
        Integer rowCount = getRowCount(data);
        if (data.first()) {
            for (int i = 0; i < rowCount; i++) {
                // get timeDiff of tuple
                String timeDiff = data.getString("timediff");
                Double timeDiffDouble = Double.parseDouble(timeDiff);
                // if negative (< 0) turn post to 1, else turn pre to 1
                if (timeDiffDouble < 0) {
                    preTup = 1;
                } else if (timeDiffDouble > 0) {
                    postTup = 1;
                } else if (timeDiffDouble == 0){
                    sameTup = 1;
                }
                data.next();
            }
        }
    }

    // Compute the coefficients of a straight line the best fits the data set
    public abstract void compute();

    // Get the coefficients of the fitted straight line
    // Return An array of coefficients {intercept, gradient}
    public abstract double[] getCoefficients();

    // Evaluate the computed model at a certain point
    // Return The value of the fitted straight line at the point x
    public abstract double evaluateAt(double queryTimeMS);
}
