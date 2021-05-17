package db.PredefinedTableClasses;

import db.CustomValidator;
import db.DbManager;
import db.ITableEntity;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;

import static app.FormatHelper.deformatCurrency;

public class Packages implements ITableEntity {

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


        // Validator for PkgEndDate - must be after PkgStartDate
        columnValidators.put("PkgEndDate", new CustomValidator() {
            @Override
            public boolean checkValidity(HashMap<String, String> args, Control colInput) throws SQLException {

                try {
                // Get endDate input value
                LocalDate endDate = LocalDate.parse(args.get("value"));

                //get StartDate input value - you can do this by getting a ref to the scene from the input, then searching for the ID of another input
                DatePicker startDateInput = (DatePicker) colInput.getScene().lookup("#inputPkgStartDate");
                LocalDate startDate = startDateInput.getValue();

                // Ensure that end date is after start date, otherwise, throw exception to bubble up
                if( ! endDate.isAfter(startDate) ) {
                    throw new SQLException("Package End date must be after Package Start Date");
                }

                // If above check passed, we're good!
                return true;

            // This catch exists to manage when start date is blank. In that case, we have nothing to compare,
            // so this check can actually return true (we can leave not-null validation to a separate validator, if needed)
             } catch (NullPointerException | DateTimeParseException e){
                    System.out.println("Skipping StartDate < EndDate validation with null value");
                    return true;
                }
            }
        });

        // Validator for Agency Commission < PackageBasePrice
        columnValidators.put("PkgAgencyCommission", new CustomValidator() {
            @Override
            public boolean checkValidity(HashMap<String, String> args, Control colInput) throws SQLException {

                try {
                    // Get basePrice input value
                    String cleanAgcyComString = deformatCurrency(args.get("value")); // remove formatting
                    Double agcyCom = Double.parseDouble(cleanAgcyComString); // cast to double

                    //get PkgBasePrice input value - you can do this by getting a ref to the scene from the input, then searching for the ID of another input
                    TextField pkgBasePriceInput = (TextField) colInput.getScene().lookup("#inputPkgBasePrice");
                    String cleanBasePriceString = deformatCurrency(pkgBasePriceInput.getText());
                    Double pkBasePrice = Double.parseDouble(cleanBasePriceString);

                    // Throw error if agency commission exceed base price
                    // (We could easily set commission to not exceed a given percentage of base price, depending on client wishes)
                    if( agcyCom > pkBasePrice) {
                        throw new SQLException("Agency commission amount cannot be more than total Package Base Price.");
                    }

                    // If above check passed, we're good!
                    return true;

                    // This catch exists to manage when start date is blank. In that case, we have nothing to compare,
                    // so this check can actually return true (we can leave not-null validation to a separate validator, if needed)
                } catch (NullPointerException | NumberFormatException e){
                    System.out.println("Skipping AgencyCommision < PackageBasePrice validation with null value");
                    return true;
                }
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
