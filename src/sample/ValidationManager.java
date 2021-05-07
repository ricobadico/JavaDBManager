package sample;

import db.DbManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class ValidationManager {

    public static boolean isPositiveInt(String colName, TextField colInput) {
        // Get value
        String value = colInput.getText();

        // Attempt to parse as an int
        try {
            Integer.parseInt(value);

            //todo validate positive, throw a different message and return false if fails

            return true;

            // If the parse failed, bring up message to admonish the user's foolery
        } catch (NumberFormatException e){
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Validation Error");
            a.setHeaderText("Please provide a number for " + colName + ".");
            a.setContentText(e.getMessage());
            a.show();

            // Highlight the field
            colInput.requestFocus();
            colInput.selectAll();

            // Return false
            return false;

        }
    }

    public static boolean foreignKeyConstraintMet(String colName, String foreignKeyTable, String foreignKeyColumn, TextField colInput) {

        String value = colInput.getText();

        // Set up a db connection
        DbManager connection = new DbManager();

        // Check if this value exists among the values that the given column (a foreign key) references
        boolean constraintMet = false;
        try {
            constraintMet = connection.columnStringValueExists(foreignKeyTable, foreignKeyColumn, value);
        } catch (SQLException e) {
            // Throws an error if false, so we show a message instead
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Database Error");
            a.setHeaderText("Something went wrong connecting to the database.");
            a.setContentText(e.getMessage());
            a.show();

            // Highlight the field
            colInput.requestFocus();
            colInput.selectAll();
        }
        if(constraintMet == false){
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Validation Error");
            a.setHeaderText("Please provide a valid value for " + colName + ".");
            a.setContentText("Value must be found in the " + foreignKeyColumn + " column in " + foreignKeyTable + ".");
            a.show();

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
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Validation Error");
            a.setHeaderText("Please provide a number for " + colName + ".");
            a.setContentText(e.getMessage());
            a.show();

            // Highlight the field
            colInput.requestFocus();
            if (colInput instanceof ValidatingTextField)
                ((ValidatingTextField)colInput).selectAll();

            // Return false
            return false;
        }
    }
}
