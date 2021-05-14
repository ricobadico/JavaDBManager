package sample;

import db.DbManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.ArrayList;

import static sample.ControllerHelper.makeWarningAlert;

public class ValidationManager {

    public static boolean isInt(String colName, TextField colInput) {
        // Get value
        String value = colInput.getText();

        // If null, we don't want to run test, technically pass (let a nullable test handle that)
        if (value == null || value.isEmpty()) return true;

        // Attempt to parse as an int

        try {
            int val = Integer.parseInt(value);

            if (val < 0) {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                a.setTitle("Negative Value");
                a.setHeaderText("You have provided a negative number for " + colName + ". Please confirm this is correct");
                a.showAndWait();
            }

            return true;

            // If the parse failed, bring up message to admonish the user's foolery
        } catch (NumberFormatException e){
            makeWarningAlert("Validation Error","Please provide a number for " + colName + ".", e.getMessage());

            // Return false
            return false;

        }
    }

    public static boolean foreignKeyConstraintMet(String colName, String foreignKeyTable, String foreignKeyColumn, TextField colInput) {

        String value = colInput.getText();

        // If null, we don't want to run test, technically pass (let a nullable test handle that)
        if (value == null || value.isEmpty()) return true;

        // Set up a db connection
        DbManager connection = new DbManager();

        // Check if this value exists among the values that the given column (a foreign key) references
        boolean constraintMet = false;
        try {
            constraintMet = connection.columnStringValueExists(foreignKeyTable, foreignKeyColumn, value);


            if (constraintMet == false) {
                // Get possible values
                ArrayList<String> possibleVals = connection.getColumnValues(foreignKeyTable, foreignKeyColumn);
                String listofPVals = "";
                for (String val : possibleVals) {
                    listofPVals += val + ", ";
                }
                listofPVals = listofPVals.substring(0, listofPVals.length()-2);

                makeWarningAlert("Validation Error", "Please provide a valid value for " + colName + ".",
                        "Value must be found in the " + foreignKeyColumn + " column in " + foreignKeyTable + ". Possible values include:\n" + listofPVals);

            }
            return constraintMet;
        } catch (SQLException e) {
            // Throws an error if false, so we showAndWait a message instead
            makeWarningAlert("Database Error", "Something went wrong connecting to the database.", e.getMessage());
            return false;
        }
    }

    public static boolean isNotNull(Control colInput, String colName) {

            // Get value
            String value = ((IValidates) colInput).getInputAsString();

            if (value.isBlank()) {
                makeWarningAlert("Validation Error", "Please provide a value for " + colName + ".", colName + " cannot be null.");
                return false;
            } else
                return true;
    }

    public static boolean isDecimal(String colName, ValidatingTextField colInput) {
        // Get value
        String value = colInput.getText();
        String cleanVal = value.replaceAll(",", "").replaceAll("\\$", "");

        // If null, we don't want to run test, technically pass (let a nullable test handle that)
        if (cleanVal == null || value.isEmpty()) return true;

        // Attempt to parse as an int
        try {
            double val = Double.parseDouble(cleanVal);

            // By default, negatives are allowed, but we can prompt the user to be safe
            if (val < 0) {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                a.setTitle("Negative Value");
                a.setHeaderText("You have provided a negative number for " + colName + ". Please confirm this is correct");
                a.showAndWait();
            }

            return true;

            // If the parse failed, bring up message to admonish the user's foolery
        } catch (NumberFormatException e){
            makeWarningAlert("Validation Error","Please provide a number for " + colName + ".", e.getMessage());


            // Return false
            return false;

        }
    }
}
