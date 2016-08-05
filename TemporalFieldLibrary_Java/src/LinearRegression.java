import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.SQLException;

/**
 * Created by Joel on 1/15/2016.
 * One method of the RegressionModel.
 * Represents a LinearRegression using least squares method. Provides operations to compute the regression coefficients
 * and evaluate the resulting function at a certain point. Time is converted from DateTime object to Double milliseconds from Epoch.
 */

public class LinearRegression extends RegressionModel {
    // CONSTRUCTOR
    public LinearRegression(Data data, String start, String end, String sID, String time) {
        super(data, start, end, sID, time);
    }

    // METHODS
    // This will be specific for each RegressionModel child
    @Override
    public Double returnValue() throws SQLException {
        getArrays();
        compute();
        double[] coefficients = getCoefficients();
        double rSquared = calcError();
        //System.out.printf("The regression line calculated is: 'y = %.8fx + %.4f' with R^2 = %.6f on %s data points", coefficients[1], coefficients[0], rSquared, yValues.length);
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime queryTimeObject = format.parseDateTime(queryTime);
        double queryTimeMS = queryTimeObject.getMillis();
        //System.out.printf("\nThe estimated value for Sensor %s at '%s' is: %.4f\n", sensorID, queryTime, evaluateAt(queryTimeMS));
        return evaluateAt(queryTimeMS);
    }
    // Compute the coefficients of a straight line the best fits the data set
    @Override
    public void compute() {
        // throws exception if regression can not be performed
        if (xValues.length < 2 | yValues.length < 2) {
            throw new IllegalArgumentException("Must have more than two values");
        }
        // get the value of the gradient using the formula b = cov[x,y] / var[x]
        b = covariance(xValues, yValues) / variance(xValues);
        // get the value of the y-intercept using the formula a = ybar + b * xbar
        a = mean(yValues) - b * mean(xValues);
        // set the computed flag to true after we have calculated the coefficients
        computed = true;
    }

     // Get the coefficients of the fitted straight line
     // Return An array of coefficients {intercept, gradient}
    @Override
    public double[] getCoefficients() {
        if (!computed)
            throw new IllegalStateException("Model has not yet computed");
        return new double[] { a, b };
    }

    // Evaluate the computed model at a certain point
    // Return The value of the fitted straight line at the point x
    @Override
    public double evaluateAt(double queryTimeMS) {
        if (!computed)
            throw new IllegalStateException("Model has not yet computed");
        return a + b * queryTimeMS;
    }

    // Needed to perform part of the least squares formula
    // Uses the x/y arrays to calculate the covariance
    public static double covariance(Double[] x, Double[] y) {
        double xmean = mean(x);
        double ymean = mean(y);
        double result = 0;
        for (int i = 0; i < x.length; i++) {
            result += (x[i] - xmean) * (y[i] - ymean);
        }
        result /= x.length - 1;
        return result;
    }

    // Needed to perform part of the least squares formula
    // Uses the x/y arrays to calculate the variance
    public static double variance(Double[] data) {
        // Get the mean of the data set
        double mean = mean(data);
        double sumOfSquaredDeviations = 0;
        // Loop through the data set
        for (int i = 0; i < data.length; i++) {
            // sum the difference between the data element and the mean squared
            sumOfSquaredDeviations += Math.pow(data[i] - mean, 2);
        }
        // Divide the sum by the length of the data set - 1 to get our result
        return sumOfSquaredDeviations / (data.length - 1);
    }

    // Needed to perform part of the least squares formula
    public static double mean(Double[] data) {
        double sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum += data[i];
        }
        return sum / data.length;
    }

    // Calculate R^2
    public double calcError() {
        double errWithLineSum = 0.0;
        double errMeanYSum = 0.0;
        double yFitLine[] = new double[yValues.length];
        for (int i = 0; i < yValues.length; i++ ) {
            yFitLine[i] = evaluateAt(xValues[i]);
            double errWithLine = Math.pow((yValues[i] - yFitLine[i]), 2);
            errWithLineSum += errWithLine;
            double errMeanY = Math.pow((yValues[i] - mean(yValues)), 2);
            errMeanYSum += errMeanY;
        }
        double rSquared = 1.0 - (errWithLineSum/errMeanYSum);
        return rSquared;
    }
}
