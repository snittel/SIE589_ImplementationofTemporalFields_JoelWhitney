import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

/**
 * Created by Joel on 1/15/2016.
 * One method of the Data class. The user should never have to change anything here.
 * NOTE: It is assumed the CSV used has column names in the first row.
 */

public class CSVData extends Data {
    // CONSTRUCTOR
    public CSVData(String connectionString) {
        super(connectionString);
    }

    // METHODS
    // This will be specific for each Data child
    @Override
    public ResultSet performQuery(Map mappedSettings) {
        String csvdir = (String) mappedSettings.get("csvdir");
        String query = (String) mappedSettings.get("query");
        try {
            // Load the driver.
            Class.forName("org.relique.jdbc.csv.CsvDriver");
            // Create a connection. The first command line parameter is the directory containing the .csv files.
            Connection conn = DriverManager.getConnection("jdbc:relique:csv:" + csvdir);
            // Create a Statement object to execute the query with.
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            data = stmt.executeQuery(query);
        } catch(Exception e) {
            e.printStackTrace();
        } return data;
    }
}
