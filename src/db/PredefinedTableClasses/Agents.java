package db.PredefinedTableClasses;

import db.CustomValidator;
import db.DbManager;
import db.ITableEntity;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Agents implements ITableEntity {

//    // DB Fields
//    private int AgentId;
//    private String AgtFirstName;
//    private String AgtMiddleInitial;
//    private String AgtLastName;
//    private String BusPhone;
//    private String AgtEmail;
//    private String AgtPosition;
//    private int AgencyId;
//
//    // Constructor
//    public Agents(int agentId, String agtFirstName, String agtMiddleInitial, String agtLastName, String busPhone, String agtEmail, String agtPosition, int agencyId) {
//        AgentId = agentId;
//        AgtFirstName = agtFirstName;
//        AgtMiddleInitial = agtMiddleInitial;
//        AgtLastName = agtLastName;
//        BusPhone = busPhone;
//        AgtEmail = agtEmail;
//        AgtPosition = agtPosition;
//        AgencyId = agencyId;
//
//    }

    /**
     * Provides a map of preferred labels for column names in this table.
     * Not all column names need to be mapped; for those cases the SQL column name is used
     * @return Map of keyed column names to their preferred labels
     */
    public static HashMap<String, String> GetColumnLabels(){

        HashMap<String, String> columnLabels = new HashMap<>();

        columnLabels.put("AgentId", "ID Number:");
        columnLabels.put("AgtFirstName", "First Name:");
        columnLabels.put("AgtMiddleInitial", "Middle Initial:");
        columnLabels.put("AgtLastName", "Last Name:");
        columnLabels.put("AgtBusPhone", "Business Phone #:");
        columnLabels.put("AgtEmail", "Email:");
        columnLabels.put("AgtPosition", "Position:");
        columnLabels.put("AgencyId", "Agency Id:");

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

        // Sample validation to show how it can be easily added. Note that error is thrown rather than returning false.
        // This is done for easy bubbling up of error messages, and so the same pattern can be used to catch any errors the program doesn't account for.
        // Todo: probably should remove this but it's a pretty harmless demonstration
        columnValidators.put("AgtLastName", new CustomValidator() {
            @Override
            public boolean checkValidity(HashMap<String, String> args, Control colInput) throws SQLException {

                    // Test the validity condition
                    if(args.get("value") != null
                       && args.get("value").equals("TEST_VALIDATION")) {
                            throw new SQLException("This is a simple test validator to demonstrate functionality!");
                    }
                    // If above check passed, we're good!
                    return true;
            }
        });


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
        ResultSet res = db.getRecords("agents"); /** Be sure to change the table name here if copying this to another class*/

        // For each record..
        while(res.next()){

            // Create a blank String we'll build up into a record name
            String recordname = "";

            // Build up the string using values from the record
            // To do this, refer to the column index numbers in the database
            // TODO: Right now, the first part of the label MUST be the Id# followed by a ":". We may change this later, but it isn't an awful convention.
            /**
             *  Other than the table name above, this block is the only thing in this method that can't be copied and pasted from class to class
             */
            recordname  += res.getString(1) + ": "; // AgentId
            recordname  += res.getString(2) // AgentFirstName
                        + " " + res.getString(4); // AgentLastName

            // Once the string is built, add it to the array
            recordNames.add(recordname);
        }

        // return the filled array
        return recordNames;

    }



}
