import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joel on 1/13/2016.
 * The superclass for the different value data retrieval methods.
 * Only thing user may need to change is column names for the SQL query
 */

public abstract class Data {
    // DECLARE VARIABLES
    public String cnxString;
    public ResultSet data;
    public Map storedMap;

    // CONSTRUCTOR
    public Data(String connectionString) {
        cnxString = connectionString;
    }

    // METHODS
    // Get data method
    public ResultSet getData() {
        ResultSet data = performQuery(storedMap);
        return data;
    }

    // Generic method that is specific to the subclass
    public abstract ResultSet performQuery(Map mappedSettings);

    // Break out cnxString and prepares the query to a HashMap of mapped variables
    public void prepareCNXVariables(String start, String end, String sensorID) {
        // split cnxString to usable hashmap
        Map<String, String> mappedSettings = new HashMap<String, String>(); // create hashmap for key value combos
        String[] splitSettings = cnxString.split(","); // split string into pairs
        for (int i = 0; i < splitSettings.length; i++) { // for each pair
            String splitSetting = splitSettings[i];
            String[] keyValue = splitSetting.split(";"); // split each pair into key/value
            mappedSettings.put(keyValue[0], String.valueOf(keyValue[1])); // assign to hashmap based on above split
        }
        int lastkey = splitSettings.length;
        String table = (String) mappedSettings.get("table");
        String query = "SELECT * FROM " + table + " WHERE node = '" + sensorID + "' AND datetime >= '" + start + "' AND datetime <= '" + end + "';";
        mappedSettings.put("query", query);
        storedMap = mappedSettings;
    }

    // Add query to mappedSettings
    public void updateQuery(String rows, String start, String end, String sensorID, String queryTime) {
        String table = (String) storedMap.get("table");
        String dbtype = (String) storedMap.get("dbtype");
        if (dbtype.equals("PostgreSQL")) {
            String updateQuery = "SELECT * FROM (SELECT node, value, datetime, lat, lon, EXTRACT(EPOCH FROM datetime - '" + queryTime + "') / 60::Integer AS timediff FROM " + table + " WHERE node = '" + sensorID + "' AND datetime >= '" + start + "' AND datetime <= '" + end + "') AS t ORDER BY ABS(timediff) ASC LIMIT " + rows + ";";
            storedMap.put("query", updateQuery);
        } else {
            String updateQuery = "SELECT * FROM (SELECT node, value, datetime, lat, lon, TIMESTAMPDIFF(SECOND, datetime,'" + queryTime + "') AS timediff FROM " + table + " WHERE node = '" + sensorID + "' AND datetime >= '" + start + "' AND datetime <= '" + end + "' ORDER BY ABS(timediff) ASC LIMIT " + rows + ") AS t ORDER BY datetime ASC;";
            storedMap.put("query", updateQuery);
        }
    }

}
