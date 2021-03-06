package db;

import javafx.scene.control.Alert;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static java.lang.Integer.parseInt;

public class DbManager {

    final String DB_NAME = "travelexperts"; // TODO FOR HARV: rename me!

    // Members
    Connection connection;

    // Constructor establishes a connection
    public DbManager(){

        String url = "jdbc:mysql://localhost:3306/" + DB_NAME; // TODO FOR HARV: rename me!

        try {
        // Connect using local db url and credentials
        connection = DriverManager.getConnection(url, "root", ""); // TODO FOR HARV: rename me!
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    // Methods

    /**
     * Gets a list of all tables from the db.
     * @return ArrayList<String>of table names as written in the db
     */
    public ArrayList<String> getAllTableNames(){
        ArrayList<String> tableNames = new ArrayList<>();
        String query = "SHOW tables";
        try {
            Statement statement = connection.createStatement();
            ResultSet res = statement.executeQuery(query);

            while(res.next()){
                String nextname = (res.getString(1)); // get next table name
                tableNames.add(nextname.substring(0, 1).toUpperCase() + nextname.substring(1)); // capitalize first letter
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();

        }
            return tableNames;
    }

    /**
     * Grabs all record data from a given table, using the DbManager's connection.
     *
     * @param tableName Chosen table to grab from
     * @return Dataset containing all Agent data
     */
    public ResultSet getRecords(String tableName){

        // Declare variables
        ResultSet res = null;

        try {

            // Create sql query
            // have to cat on table name, since sql paramateriziation doesn't work for database objects.
            // The values come from a controlled finite dropdown, so no injection risk
            String query = "SELECT * FROM " + tableName;
            Statement statement = connection.createStatement();
            res = statement.executeQuery(query);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        // Return result of query
        return res;
    }

    /**
     * Grabs one record from the connected DB's Agent table using its AgentId.
     *
     * @param tableName Name of table to be searched
     * @param id AgentId from the given record.
     * @return Result set with corresponding record.
     */
    public ResultSet getRecord(String tableName, int id){

        ResultSet res = null;
        String pkColumn = null;

        try {

            pkColumn = getPKColumnForTable(tableName);

            // Create sql query with parameter for id, catting in injection-safe parameters
            String query = "SELECT * FROM " + tableName + " WHERE " + pkColumn + " = ?";
            PreparedStatement statement = connection.prepareStatement(query);

            // Attach method arguments to fill query params
            statement.setInt(1, id);

            // Run statement
            res = statement.executeQuery();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return res;
    }

    /**
     * getRecord overload for String primary keys - this method is ONLY to be used when the PK's type in the DATABASE is String/varchar/text
     * @param tableName Chosen table
     * @param pk Chosen primary key String value.
     * @return
     */
    public ResultSet getRecord(String tableName, String pk){

        ResultSet res = null;
        String pkColumn = null;

        try {

            pkColumn = getPKColumnForTable(tableName);

            // Create sql query with parameter for id, catting in injection-safe parameters
            String query = "SELECT * FROM " + tableName + " WHERE " + pkColumn + " = ?";
            PreparedStatement statement = connection.prepareStatement(query);

            // Attach method arguments to fill query params
            statement.setString(1, pk);

            // Run statement
            res = statement.executeQuery();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return res;
    }

    /**
     * Gets the primary key column name for the chosen table
     *
     * @param tableName The table to find the PK for
     * @return String containing the primary key column name
     * @throws SQLException
     */
    public String getPKColumnForTable(String tableName) throws SQLException {

        String pkColumn;

        //First, we need to determine what the primary key is for the chosen table
        // We query database metadata to find this, concatting in the table name
        String pkQuery =
                "SHOW KEYS FROM " + tableName + " WHERE Key_name = 'PRIMARY'";
        Statement pkStatement = connection.createStatement();
        ResultSet pkRes = pkStatement.executeQuery(pkQuery);
        pkRes.next(); //TODO: might need to loop for multi-col

        // The fifth column of this query contains the Column name for the primary key
        pkColumn = pkRes.getString(5);
        return pkColumn;
    }

    /**
     * DTO class designed to hold the table and column of a PK that a FK references.
     * Returned by the getForeignKeyReferences method below.
     */
    public class ForeignKeyReference {
        private final String foreignKeyTable;
        private final String foreignKeyColumn;

        public ForeignKeyReference(String foreignKeyTable, String foreignKeyColumn) {
            this.foreignKeyTable = foreignKeyTable;
            this.foreignKeyColumn = foreignKeyColumn;
        }

        public String getForeignKeyRefTable() {
            return foreignKeyTable;
        }

        public String getForeignKeyRefColumn() {
            return foreignKeyColumn;
        }
    }

    /**
     * Queries the DB to see if the passed in column is a foreign key that references
     * any other primary keys in the database. If so, the referenced primary key's
     * table and column name are returned.
     * @param tableName
     * @param colName
     * @return
     */
    public ForeignKeyReference getForeignKeyReferences(String tableName, String colName){

        ForeignKeyReference fkRef = null;

        try {
            // Create sql query to check information schema to see if the given column is a foreign key,
            // and if so, to what PK
            String query =
                    "SELECT REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME " +
                        "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                        "WHERE REFERENCED_TABLE_SCHEMA = '" + DB_NAME +  "' " +
                        "AND TABLE_NAME = ? " +
                        "AND COLUMN_NAME = ?;";
            PreparedStatement statement = connection.prepareStatement(query);

            // Attach method arguments to fill query params
            statement.setString(1, tableName);
            statement.setString(2, colName);

            // Run statement
            ResultSet res = statement.executeQuery();

            // Check to see if res found anything
            // todo: probably need to think about compound key references at some point
            if (res.next()){
                // If so, put the referenced table and column in an object to return
                String tableRef = res.getString(1);
                String colNameRef = res.getString(2);
                fkRef = new ForeignKeyReference(tableRef, colNameRef);
                return fkRef;
            }

            // Otherwise, the given column is not a FK, so return null
            return null;

        }
        catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Pulls out column name data from a given table
     *
     * @param tableName Name of the table to get columns from.
     * @return List of column names as Strings.
     */
    public ArrayList<String> getColumnNames(String tableName){

        ArrayList<String> colNames = new ArrayList<>();

        try {
            // Call GetRecords to pull in all data
            ResultSet res = getRecords(tableName);
            ResultSetMetaData resMD = res.getMetaData();

            for(int i = 1; i <= resMD.getColumnCount(); i++){
                colNames.add(resMD.getColumnName(i));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return colNames;
    }

    /**
     * Create an array of display values for each record in a chosen table
     * @param tableName The table to get records from
     * @return Array of strings, each providing information for one record in that table
     */
    public ArrayList<String> getRecordNames(String tableName){

        ArrayList<String> names = new ArrayList<>();

        try {
            // Grab all records for the chosen table
            ResultSet res = getRecords(tableName);

            // Create a descriptive string for each record
            while(res.next()){
                // String together the first few columns. This is definitely imperfect as is!
                String currName = res.getString(1);

                // Now we add to the name with the other column values
                String nextBit = res.getString(2);
                currName += (nextBit == null || nextBit.equals("null") ? // ternary to skip null values
                        "" : ": " + nextBit);
                // Subsequent columns will be in try blocks just in case the columnIndex doesn't exist (table with only 2 columns)
                try{
                    nextBit = (res.getString(3));
                    currName += (nextBit == null || nextBit.equals("null") ?
                            "" : " " + nextBit);
                } catch (Exception e){
                    // just keep going!
                }
                try{
                    nextBit = (res.getString(4));
                    currName += (nextBit == null || nextBit.equals("null") ?
                            "" : " " + nextBit);
                } catch (Exception e){
                    // just keep going!
                }

                // Add this big string as a name
                names.add(currName);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return names;
    }


    /**
     * Takes the inputs provided by the user, parses them into the correct type, and updates the corresponding record.
     * @param tableName The table the record to update exists in
     * @param recordData An array which holds record values for each field in the table, in
     */
    public void updateRecord(String tableName, String pkColDataType, HashMap<String, String> recordData) throws SQLException {

        // Create a set of keys to iterate through
        Set<String> colNames = recordData.keySet();

        // Initialize a variable for holding current column name (needed in case of exception)
        String currentColumn = null;

        try {

            // We need to know the primary key column for the table in order to update
            String pkCol = getPKColumnForTable(tableName);

            // Begin creating an update statement
            String query = "UPDATE " + tableName + " SET "; // create first bit with table name
            // For each column of data being updated, we add the column name and a spot for its param
            for (String colName : colNames) {
                // Ignore adding pkCol to query (since it should not be updated)
                if(! colName.equals(pkCol))
                    query += colName + " = ?, "; // adds another column update to the query
            }
            // Finish the update statement
            query = query.substring(0, query.length() - 2); // remove the trailing ","
            query += " WHERE " + pkCol + " = ";

            // Add the WHERE clause. It will need quotes around it or not, depending on data type
            if(pkColDataType.equals("int")) {
                query += recordData.get(pkCol);
            }
            else if(pkColDataType.equals("varchar")){
                query += "'" + recordData.get(pkCol) + "'";
            }

            // Add query as a prepared statement
            PreparedStatement statement = connection.prepareStatement(query);

            int i = 1; // Need a counter for the following loop, but since it's a dictionary for-each is cleaner, we can add a separate counter here

            // Now we set parameters, which will require parsing each record value according to its database type
            for (String colName : colNames) {
                // Ignore adding pkCol to query (since it should not be updated)
                if(! colName.equals(pkCol)) {
                    currentColumn = colName;

                    // First, we need to determine what type the current value should be parsed to
                    String dbColumnDataType = getColumnDataType(tableName, colName);

                    // Put the current string value in a variable for shorthand
                    String inputValue = recordData.get(colName);
                    if (inputValue == null || inputValue.isBlank()) {
                        statement.setString(i, null);
                    } else {

                        // For now we just need the data type name, not the length.
                        String[] dataTypebits = dbColumnDataType.split("\\("); // this breaks up eg "decimal(19,4)" after the datatype name
                        String datatype = dataTypebits[0]; // the name of the datatype
                        String lengthData; //used below to validate length

                        // With the data type, we can determine what parsing action needs to be done to set the param for this column
                        // This is not exhaustive but should work for our purposes

                        switch (datatype) {
                            case "int":

                                int value = parseInt(inputValue);
                                statement.setInt(i, value);
                                break;

                            case "decimal":
                                statement.setBigDecimal(i, new BigDecimal(inputValue));
                                break;

                            case "datetime":
                                statement.setDate(i, Date.valueOf(LocalDate.parse(inputValue)));
                                break;

                            case "varchar":

                                // Validate length
                                lengthData = dataTypebits[1].split("\\)")[0];
                                int maxLength = parseInt(lengthData);
                                if (inputValue != null && inputValue.length() > maxLength) {
                                    throw new SQLException(colName + " exceeds the max number of characters");
                                }

                                statement.setString(i, inputValue);

                            default: // notably for "varchar"
                                statement.setString(i, inputValue);
                        }
                    }

                    i++; // add to the counter before we go to the next column of data
                }
            }

            // Execute the statement
            statement.executeUpdate();

        }
        // In the event of an error, we throw a new error of a different type with some extra info (the presentation layer is looking for this)
        catch (SQLException e){
            throw e;
            //throw new SQLDataException("There was an error updating the " + currentColumn + " column. Please check the value.");
        }
    }

    public void addRecord(String tableName, HashMap<String, String> recordData) throws SQLException {
        // Create a set of keys to iterate through
        Set<String> colNames = recordData.keySet();
        //Get primary key column name for current table
        String pkColName = getPKColumnForTable(tableName);
        // Initialize a variable for holding current column name (needed in case of exception)
        String currentColumn = null;

        try {

            // We need to know the primary key column for the table in order to not try to update it
            //String pkCol = getPKColumnForTable(tableName);

            // Begin creating an insert statement
            String query = "INSERT INTO " + tableName + " ( "; // create first bit with table name
            // For each column of data being updated except the primary key, we add the column name and a spot for its param
            for (String colName : colNames) {
                    query += colName + ", "; // adds another column update to the query
            }

            // Finish the update statement
            query = query.substring(0, query.length() - 2); // remove the trailing ","
            query += ") VALUES ( "; //+ pkCol + " = " + recordData.get(pkCol); // add a Where clause using the PK column
            //add a missing update value for each column
            for(String colName : colNames) {
                query += "?, ";
            }
            query = query.substring(0, query.length() - 2); // remove the trailing ","
            query += ")";

            // Add query as a prepared statement
            PreparedStatement statement = connection.prepareStatement(query);

            int i = 1; // Need a counter for the following loop, but since it's a dictionary for-each is cleaner, we can add a separate counter here

            // Now we set parameters, which will require parsing each record value according to its database type
            for (String colName : colNames) {
                currentColumn = colName;

                // First, we need to determine what type the current value should be parsed to
                String dbColumnDataType = getColumnDataType(tableName, colName);

                // Put the current string value in a variable for shorthand
                String inputValue = recordData.get(colName);
                // For now we just need the data type name, not the length.
                String[] dataTypebits =  dbColumnDataType.split("\\("); // this breaks up eg "decimal(19,4)" after the datatype name
                String datatype = dataTypebits[0]; // the name of the datatype
                String lengthData; //used below to validate length

                // With the data type, we can determine what parsing action needs to be done to set the param for this column
                // This is not exhaustive but should work for our purposes
                // If null, we can just set to null
                if(inputValue == null || inputValue.isBlank()){
                    statement.setString(i, null);
                }
                else {
                    // Otherwise we set value based on data type
                    switch (datatype) {
                        case "int":
                            statement.setInt(i, parseInt(inputValue));
                            break;

                        case "decimal":
                            statement.setBigDecimal(i, new BigDecimal(inputValue));
                            break;

                        case "datetime":
                            statement.setDate(i, Date.valueOf(LocalDate.parse(inputValue)));
                            break;

                        case "varchar":

                            // Validate length
                            lengthData = dataTypebits[1].split("\\)")[0];
                            int maxLength = parseInt(lengthData);
                            if (inputValue != null && inputValue.length() > maxLength) {
                                throw new SQLException(colName + " exceeds the max number of characters");
                            }

                            statement.setString(i, inputValue);
                            break;

                        default: // notably for "varchar"
                            statement.setString(i, inputValue);
                    }
                }

                i++; // add to the counter before we go to the next column of data
            }

            // Execute the statement
            statement.executeUpdate();

        }
        // In the event of an error, we throw a new error of a different type with some extra info (the presentation layer is looking for this)
        catch (SQLException e){
            throw e;
            //throw new SQLDataException("There was an error updating the " + currentColumn + " column. Please check the value.");
        }
    }

    /**
     * Finds the data type (and max length) of a database column in a table.
     *
     * @param tableName The chosen table.
     * @param columnName The name of the column in that table.
     * @return String in the form of datatype(maxlength)
     * @throws SQLException
     */
    public String getColumnDataType(String tableName, String columnName) throws SQLException {
        // Create query to grab column metadate for the chosen column
        String query = "SHOW COLUMNS FROM " +  tableName + " WHERE Field = '" + columnName + "';";

        // Run the query
        Statement statement = connection.createStatement();
        ResultSet res=  statement.executeQuery(query);

        // Type date is in the second column of this query
        res.next(); // get first record (should just be one)
        String dataType = res.getString(2);

        return dataType;

    }


    /**
     * Checks the db to see if a table column contains a particular value.
     * Used to check foreign key constraints.
     * A near-overload of this exists to check for string values (columnStringValueExists)
     * @param tableName the table to search
     * @param columnName the column within that table to search
     * @param value the value to be searched for
     * @return true if the value exists
     * @throws SQLException
     */
    public boolean columnIntValueExists(String tableName, String columnName, int value) throws SQLException {

        // Create sql query with parameter for value, catting in injection-safe parameters
        String query = "SELECT * FROM " + tableName + " WHERE " + columnName + " = ?";
        PreparedStatement statement = connection.prepareStatement(query);

        // Attach method arguments to fill query params
        statement.setInt(1, value);

        // Run statement
        ResultSet res = statement.executeQuery();

        // If any values returned (ie if there is a .next() to go to), return true
        if (res.next())
            return true;

        // If no records, return false
        else
           return false;

    }

    /**
     ** Checks the db to see if a table column contains a particular value.
     * Used to check foreign key constraints.
     * A near-overload of this exists to check for string values (columnStringValueExists)
     * @param tableName the table to search
     * @param columnName the column within that table to search
     * @param value the value to be searched for
     * @return true if the value exists
     * @throws SQLException
     */
    public boolean columnStringValueExists(String tableName, String columnName, String value) throws SQLException {

        // Create sql query with parameter for value, catting in injection-safe parameters
        String query = "SELECT * FROM " + tableName + " WHERE " + columnName + " = ?";
        PreparedStatement statement = connection.prepareStatement(query);

        // Attach method arguments to fill query params
        statement.setString(1, value);

        // Run statement
        ResultSet res = statement.executeQuery();

        // If any values returned (ie if there is a .next() to go to), return true
        if (res.next()) {
            return true;
        }

            // If no records, return false
        else
            return false;

    }

    public ArrayList<String> getColumnValues(String tableName, String columnName) throws SQLException {

        ArrayList<String> possibleVals = new ArrayList<>();

        // Create sql query with parameter for value, catting in injection-safe parameters
        String query = "SELECT " + columnName + " FROM " + tableName;
        PreparedStatement statement = connection.prepareStatement(query);
        // Run statement
        ResultSet res = statement.executeQuery();
        while (res.next()){
            possibleVals.add(res.getString(1));
        }
        return possibleVals;
    }

    public boolean columnPrimaryKeyAutoIncrements(String tableName, String columnName) throws SQLException {
        String query = "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA ='" + DB_NAME + "' AND TABLE_NAME='" + tableName + "' AND COLUMN_NAME='"
                + columnName + "' AND EXTRA LIKE '%auto_increment%'";
        PreparedStatement statement = connection.prepareStatement(query);
        // Run statement
        ResultSet res = statement.executeQuery();

        // If any values returned (ie if there is a .next() to go to), return true
        if (res.next())
            return true;

        // If no records, return false
        else
            return false;

    }

    public int highestPKValueForTable(String tableName, String pkColName) throws SQLException {
        try {
            String query = "SELECT MAX(" + pkColName + ") FROM " + tableName;
            PreparedStatement statement = connection.prepareStatement(query);
            // Run statement
            ResultSet res = statement.executeQuery();
            res.next();
            return Integer.parseInt(res.getString(1));
        }
        catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public String findDataType(String table, String col) {
        String datatype = null;
        try {
            datatype = (getColumnDataType(table, col)) // gets the full datatype (ie "varchar(10)"
                    .split("\\(")[0]; // gets just the data type name ("varchar")
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return datatype;
    }

    /**
     * Gets the max length of a column's datatype (character length for varchar, or number scale for double etc)
     * Note that right now, it's only being used for varchar validation
     * @param tableName
     * @param columnName
     * @return
     * @throws SQLException
     */
    public Integer findDataTypeMaxLength(String tableName, String columnName) {

        Integer lengthData = null;

        // Create query to grab column metadate for the chosen column
        String query = "SELECT CHARACTER_MAXIMUM_LENGTH, NUMERIC_SCALE " +
                "FROM INFORMATION_SCHEMA.Columns " +
                "WHERE TABLE_SCHEMA = '" + DB_NAME + "' AND TABLE_NAME = '" + tableName + "' " +
                "AND COLUMN_NAME = '" + columnName + " ';";

        // Run the query
        try {
            Statement statement = connection.createStatement();

            ResultSet res=  statement.executeQuery(query);

            // Type date is in the second column of this query
            if(res.next()) {
                lengthData = res.getInt(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return lengthData;

    }


    public ArrayList<String> getNullableColumnsNames(String tableName){

        ArrayList<String> nullableColumns = new ArrayList<>();

        try{


        // Create a query getting back all nullable columns in this table
        String query =
            "SELECT COLUMN_NAME " +
            "FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE table_name = '" + tableName + "' " + // not worried about injection here since user has no control over customizing this value
            "AND IS_NULLABLE = 'NO'";
        Statement statement = connection.createStatement();
        ResultSet res = statement.executeQuery(query);

        // Add each record as a key-value pair in the hashmap
        while(res.next()){
            nullableColumns.add(res.getString(1));
        }

        }
        catch (SQLException e) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Connection Error");
            a.setHeaderText("Could not connect to the database. Please reload the application or contact IT.");
            a.setContentText(e.getMessage());
            a.showAndWait();
        }
        return nullableColumns;
    }


}
