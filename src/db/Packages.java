package db;

import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Packages implements ITableEntity{

    public static HashMap<String, String> GetColumnLabels() {

        HashMap<String, String> columnLabels = new HashMap<>();

        columnLabels.put("PackageId", "ID Number:");
        columnLabels.put("PkgName", "Name:");
        columnLabels.put("PkgStartDate", "Start Date:");
        columnLabels.put("PkgEndDate", "End Date:");
        columnLabels.put("PkgDesc", "Description:");
        columnLabels.put("PkgBasePrice", "Base Price:");
        columnLabels.put("PkgAgencyCommission", "Agency Commission:");

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

        // Validator for PkgEndDate - must be after PkgStartDate
        columnValidators.put("PkgEndDate", new CustomValidator() {
            @Override
            public boolean checkValidity(HashMap<String, String> args, Control colInput) throws SQLException {

                // Get endDate input value
                LocalDate endDate = LocalDate.parse(args.get("value"));

                //get StartDate input value - you can do this by getting a ref to the scene from the input, then searching for the ID of another input
                DatePicker startDateInput = (DatePicker) colInput.getScene().lookup("#inputPkgStartDate");
                LocalDate startDate = startDateInput.getValue();

                // Ensure that end date isn't the same day or before Start date
                if( ! endDate.isAfter(startDate)) {
                    throw new SQLException("Package End date must be after Package Start Date");
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
        ResultSet res = db.getRecords("packages"); /** Be sure to change the table name here if copying this to another class*/

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
            recordname  += res.getString(1) + ": "; // PackageId
            recordname  += res.getString(2); // PackageName

            // Once the string is built, add it to the array
            recordNames.add(recordname);
        }

        // return the filled array
        return recordNames;
    }
}
