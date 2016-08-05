import java.sql.*;
import java.util.Map;

/**
 * Created by Joel on 1/15/2016.
 * One method of the Data class. The user should never have to change anything here.
 */

public class PostgreSQLData extends Data {
    // CONSTRUCTOR
    public PostgreSQLData(String connectionString) {
        super(connectionString);
    }

    // METHODS
    // This will be specific for each Data child
    @Override
    public ResultSet performQuery(Map mappedSettings) {
        // Retrieve settings from hashmap
        String host = (String) mappedSettings.get("host");
        String port = (String) mappedSettings.get("port");
        String database = (String) mappedSettings.get("database");
        String username = (String) mappedSettings.get("username");
        String password = (String) mappedSettings.get("password");
        String query = (String) mappedSettings.get("query");
        // Initialize connection, sqlStatement, and resultset
        Connection connection;
        Statement command;
        ResultSet data = null;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } try {
            connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + database, username, password);
            command = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            data = command.executeQuery(query);
        } catch (SQLException e) {
            //TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data;
    }
}

