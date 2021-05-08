package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Customers implements ITableEntity {

    public static HashMap<String, String> GetColumnLabels() {

        HashMap<String, String> columnLabels = new HashMap<>();

        columnLabels.put("CustomerId", "ID Number:");
        columnLabels.put("CustFirstName", "First Name:");
        columnLabels.put("CustLastName", "Last Name:");
        columnLabels.put("CustAddress", "Address:");
        columnLabels.put("CustCity", "City:");
        columnLabels.put("CustProv", "Province:");
        columnLabels.put("CustPostal", "Postal:");
        columnLabels.put("CustCountry", "Country:");
        columnLabels.put("CustHomePhone", "Home Phone #:");
        columnLabels.put("CustBusPhone", "Business Phone:");
        columnLabels.put("CustEmail", "Email:");
        columnLabels.put("AgentId", "Agent ID:");

        return columnLabels;
    }

    public static HashMap<String, CustomValidator> GetValidators(){

        HashMap<String, CustomValidator> columnValidators = new HashMap<>();


        /**
         * New validation methods go here. The structure is as follows:
         * Put a new entry into the hashmap. The key is the sql column nam (eg. "AgencyID"),
         * the value is a new Custom Validator object that overwrites the checkValidity method
         * to be whatever validation you want to add. The validation should return true or throw an exception to be caught.
         */
        // TODO: we need to make some custom validation for certain classes. This has to wait however for Eric's IValidates fix, broken right now...

        // Validator for AgencyID: checks to ensure foreign key constraints met (AgencyID exists in other column)
//        columnValidators.put("AgencyId", new CustomValidator() {
//            @Override
//            public boolean checkValidity(HashMap<String, String> args) throws SQLException {
//                DbManager db = new DbManager();
//                boolean isValid = db.columnIntValueExists("Agencies", "AgencyId", Integer.parseInt(args.get("value")));
//                if(isValid == false) {
//                    throw new SQLException("The provided agency ID does not exist in the database");
//                }
//                return true;
//            }
//        });


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
        ResultSet res = db.getRecords("customers"); /** Be sure to change the table name here if copying this to another class*/

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
                    + " " + res.getString(3); // AgentLastName

            // Once the string is built, add it to the array
            recordNames.add(recordname);
        }

        // return the filled array
        return recordNames;
    }
}
