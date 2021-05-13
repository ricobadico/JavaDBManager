package sample;

import db.DbManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

import java.sql.SQLException;

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

            // Highlight the field
            colInput.requestFocus();
            colInput.selectAll();

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
        } catch (SQLException e) {
            // Throws an error if false, so we showAndWait a message instead
            makeWarningAlert("Database Error","Something went wrong connecting to the database.", e.getMessage());

            // Highlight the field
            colInput.requestFocus();
            colInput.selectAll();
        }
        if(constraintMet == false){
            makeWarningAlert("Validation Error","Please provide a valid value for " + colName + ".",
                    "Value must be found in the " + foreignKeyColumn + " column in " + foreignKeyTable + ".");

            // Highlight the field
            colInput.requestFocus();
            colInput.selectAll();
        }
        return constraintMet;
    }

    public static boolean isNotNull(Control colInput, String colName) {

        // Attempt to parse as an int
        try {

        // Get value
        String value = ((IValidates)colInput).getInputAsString();

            if(value == null) {
                throw new Exception(colName + " cannot be null.");
            }
            else
                return true;
            // If the parse failed, bring up message to admonish the user's foolery
        } catch (Exception e){
            makeWarningAlert("Validation Error","Please provide a number for " + colName + ".", e.getMessage());

            // Highlight the field
            colInput.requestFocus();
            if (colInput instanceof ValidatingTextField)
                ((ValidatingTextField)colInput).selectAll();

            // Return false
            return false;
        }
    }

    public static boolean isDecimal(String colName, ValidatingTextField colInput) {
        // Get value
        String value = colInput.getText();

        // If null, we don't want to run test, technically pass (let a nullable test handle that)
        if (value == null || value.isEmpty()) return true;

        // Attempt to parse as an int
        try {
            double val = Double.parseDouble(value);

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

            // Highlight the field
            colInput.requestFocus();
            colInput.selectAll();

            // Return false
            return false;

        }
    }
}
