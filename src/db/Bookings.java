package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Bookings implements ITableEntity{
    /**
     * Provides a map of preferred labels for column names in this table.
     * Not all column names need to be mapped; for those cases the SQL column name is used
     * @return Map of keyed column names to their preferred labels
     */
    public static HashMap<String, String> GetColumnLabels(){

        HashMap<String, String> columnLabels = new HashMap<>();

        columnLabels.put("BookingId", "ID Number:");
        columnLabels.put("BookingDate", "Booking Date:");
        columnLabels.put("BookingNo", "Booking Number:");
        columnLabels.put("TravelerCount", "Traveler Count:");
        columnLabels.put("CustomerId", "Customer Id:");
        columnLabels.put("TripTypeId", "Trip Type:");
        columnLabels.put("Package Id", "Package Id:");

        return columnLabels;
    }

    /**
     * Provides a map of column keys to additional validation (not standard SQL requirements) needed for those columns.
     * This allows the program to run those validators or add them to listeners when working with data for those particular columns.
     * @return Map with an array of validation methods for keyed column.
     */
    public static HashMap<String, CustomValidator> GetValidators(){

        HashMap<String, CustomValidator> columnValidators = new HashMap<>();


        /**
         * New validation methods go here. The structure is as follows:
         * Put a new entry into the hashmap. The key is the sql column nam (eg. "AgencyID"),
         * the value is a new Custom Validator object that overwrites the checkValidity method
         * to be whatever validation you want to add. The validation should return true or throw an exception to be caught.
         */

        /**
         * More validators for columns in this table can go here
         */

        // Return back the map of validators
        return columnValidators;
    }


    public static ArrayList<String> GetRecordNames() throws SQLException {

        ArrayList<String> recordNames = new ArrayList<>();

        // Get all records from a table
        DbManager db = new DbManager();
        ResultSet res = db.getRecords("bookings"); /** Be sure to change the table name here if copying this to another class*/

        // For each record..
        while(res.next()){

            // Create a blank String we'll build up into a record name
            String recordname = "";

            // Build up the string using values from the record
            // To do this, refer to the column index numbers in the database
            /**
             *  Other than the table name above, this block is the only thing in this method that can't be copied and pasted from class to class
             */
            recordname  += res.getString(1) + ": "; //
            recordname  += "Booking No " + res.getString(3); // AgentFirstName

            // Once the string is built, add it to the array
            recordNames.add(recordname);
        }

        // return the filled array
        return recordNames;

    }
}
