import org.jfree.chart.*;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Joel on 1/15/2016.
 * The plot class allows the user to plot the temporalFields to a TimeSeries chart.
 */

public class PlotTimeSeries extends ApplicationFrame {
    // DECLARE VARIABLES
    public String chartTitle;
    public TimeSeriesCollection storedDataset;
    public ChartPanel panel;
    public JFreeChart chart;

    // CONSTRUCTOR
    public PlotTimeSeries(String title) {
        super(title);
        chartTitle = title;
    }

    // METHODS
    // Sets theme for chart
    private static final long serialVersionUID = 1L;
    static {
        // set a theme using the new shadow generator feature available in
        // 1.0.14 - for backwards compatibility it is not enabled by default
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow",
                true));
    }

    /**  The prepareRawData method just updates the XYDataset to include the raw data
     */
    public void prepareRawData(TemporalField[] tempFieldsArray) throws SQLException, ParseException {
        addRawData(tempFieldsArray);
    }

    /**  The prepareIntervals method just updates the XYDataset to include the interpolated data at each interval
     */
    public void prepareIntervals(TemporalField[] tempFieldsArray, Integer minutes) throws SQLException, ParseException {
        interpolateIntervals(tempFieldsArray, minutes);
    }

    /**  The plot method creates the graph and uses the dataset
     */
    public void plot() throws SQLException, ParseException {
        ChartPanel chartPanel = (ChartPanel) createPanel();
        chartPanel.setPreferredSize(new Dimension(800, 500));
        setContentPane(chartPanel);
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
        System.out.println("Plotted data points..");
    }

    // Create panel method
    public JPanel createPanel() throws SQLException, ParseException {
        chart = createChart(storedDataset);
        panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    // Create chart method
    private JFreeChart createChart(XYDataset dataset) {
        // layout of chart
        chart = ChartFactory.createTimeSeriesChart(
                chartTitle,  // title
                "DateTime",             // x-axis label
                "Sensor Value",   // y-axis label
                dataset,            // data
                true,               // create legend
                true,               // generate tooltips
                false               // generate URLs
        );
        // more layout stuff
        chart.setBackgroundPaint(Color.white);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        // more layout stuff
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
        }
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // return chart
        return chart;
    }

    // Create dataset method from the temporalFields
    public void addRawData(TemporalField[] tempFieldsArray) throws SQLException, ParseException {
        // for each tempField object create a new TimeSeries and add it to the array of timeSeries
        // at the end you can add each TimeSeries to the Dataset by index of the array
        System.out.printf("\nPlotting the raw datapoints...");
        storedDataset = null;
        // for each tempField
        for (int i = 0; i < tempFieldsArray.length; i++) {
            String seriesTitle = "tempField" + (i + 1);
            TimeSeries series = new TimeSeries(seriesTitle);
            TemporalField tempField = tempFieldsArray[i];
            ResultSet data = tempField.getData();
            int rowCount = getResultSetSize(data);
            // for each tuple(i) in ResultSet
            if (data.first()) {
                for (int iIndex = 0; iIndex < rowCount; iIndex++) {
                    String dateObjectString = data.getString("datetime");
                    if (dateObjectString.endsWith(".0")) {
                        dateObjectString = dateObjectString.substring(0, dateObjectString.length() - 2);
                    }
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dateObject = format.parse(dateObjectString);
                    String nearestValueString = data.getString("value");
                    Double nearestValue = Double.parseDouble(nearestValueString);
                    // add the tuples data to the series
                    series.add(new Second(dateObject), nearestValue);
                    // move to next tuple
                    data.next();
                }
            }
            if (storedDataset == null) {
                storedDataset = new TimeSeriesCollection();
            }
            storedDataset.addSeries(series);
        }
    }

    // Update raw dataset with estimated value series
    public void interpolateIntervals(TemporalField[] tempFieldsArray, Integer Minutes) throws SQLException, ParseException {
        // for each tempField object create a new TimeSeries and add it to the array of timeSeries
        // at the end you can add each TimeSeries to the Dataset by index of the array
        System.out.printf("\nCalculating the interpolated datapoints at %s Minute intervals...", Minutes);
        for (int i = 0; i < tempFieldsArray.length; i++) {
            String seriesTitle = "tempField" + (i + 1) + " (" + Minutes + " Minute Interval)";
            TimeSeries series = new TimeSeries(seriesTitle);
            TemporalField tempField = tempFieldsArray[i];
            ResultSet data = tempField.getData();
            // for each tuple(i) in ResultSet
            if (data.first()) {
                // grab first date time and add to series
                String dateObjectString = data.getString("datetime");
                if (dateObjectString.endsWith(".0")) {
                    dateObjectString = dateObjectString.substring(0, dateObjectString.length() - 2);
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date dateObject = format.parse(dateObjectString);
                String nearestValueString = data.getString("value");
                Double nearestValue = Double.parseDouble(nearestValueString);
                // add the first tuple data to the series
                series.add(new Second(dateObject), nearestValue);

                // get variabls for for loop
                long stepSizeMS = Minutes * 60 * 1000;
                long startDateMS = dateObject.getTime() + stepSizeMS;

                // move to last tuple and get endDate
                data.last();
                // grab last date time and add to series
                dateObjectString = data.getString("datetime");
                if (dateObjectString.endsWith(".0")) {
                    dateObjectString = dateObjectString.substring(0, dateObjectString.length() - 2);
                }
                dateObject = format.parse(dateObjectString);
                nearestValueString = data.getString("value");
                nearestValue = Double.parseDouble(nearestValueString);
                // add the last tuple data to the series
                series.add(new Second(dateObject), nearestValue);

                // get endDate variable for loop
                long endDateMS = dateObject.getTime();

                // for long startDateMS -> endDateMS (Adding interval in MS for each step
                for (long j = startDateMS; j < endDateMS; j += stepSizeMS) {
                    Date evalDate = new Date(j);
                    String evalDateString = format.format(evalDate);
                    // call get value
                    Double estimatedValue = tempField.getValue(evalDateString);
                    // add the datetime and estimated value to the series
                    series.add(new Second(evalDate), estimatedValue);
                }
            }
            if (storedDataset == null) {
                storedDataset = new TimeSeriesCollection();
            }
            //add series to dataset
            storedDataset.addSeries(series);
        }
    }

    // Reresh method
    public void refreshChart() throws SQLException, ParseException {
        panel.removeAll();
        panel.revalidate(); // This removes the old chart
        chart = createChart(storedDataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        panel.setLayout(new BorderLayout());
        panel.add(chartPanel);
        panel.repaint(); // This method makes the new chart appear
        setContentPane(chartPanel);
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    // get RS size
    public int getResultSetSize(ResultSet rs) throws SQLException {
        rs.last();
        int resultSetSize = rs.getRow();
        rs.beforeFirst();
        return resultSetSize;
    }

    // convert public TimeSeriesCollection to CSV
    public void convertToCSV(String outFolder) throws SQLException, FileNotFoundException {
        String outputName = "TempField";
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        int seriesCount = storedDataset.getSeriesCount();
        for (int i = 0; i < seriesCount; i++) {
            String seriesIndex = Integer.toString(i);
            PrintWriter csvWriter = new PrintWriter(new File(outFolder + "/" + outputName + "_" + strDate + "_" + seriesIndex + ".csv"));
            String dataHeaders = "tuple" + "," + "xValue" + "," + "yValue";
            csvWriter.println(dataHeaders);

            Integer seriesSize = storedDataset.getItemCount(i);
            for (int j = 0; j < seriesSize; j++) {
                Number xValue = storedDataset.getX(i, j);
                Date date = new Date(xValue.longValue());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String dateString = formatter.format(date);
                Number yValue = storedDataset.getY(i, j);
                String row = Integer.toString(j) + "," + dateString + "," + String.valueOf(yValue);
                csvWriter.println(row);
            }
            csvWriter.close();
        }
    }
}
