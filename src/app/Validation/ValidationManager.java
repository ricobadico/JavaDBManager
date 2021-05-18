package app.Validation;

import db.DbManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static app.ControllerHelper.makeWarningAlert;
import static app.FormatHelper.deformatCurrency;

public class ValidationManager {

    public static boolean isInt(String colName, IValidates colInput) {
        // Get value
        String value = colInput.getInputAsString();

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

                // Based on confirmation result, change return
                Optional<ButtonType> result = a.showAndWait();
                if (result.get() == ButtonType.OK){
                    return true;
                } else {
                    return false;
                }
            }

            return true;

            // If the parse failed, bring up message to admonish the user's foolery
        } catch (NumberFormatException e){
            makeWarningAlert("Validation Error","Please provide a number for " + colName + ".", e.getMessage());

            // Return false
            return false;

        }
    }

    public static boolean foreignKeyConstraintMet(String colName, String foreignKeyTable, String foreignKeyColumn, IValidates colInput) {

        String value = colInput.getInputAsString();

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

    /**
     * Confirms value is a decimal.
     * @param colName
     * @param colInput
     * @return
     */
    public static boolean isDecimal(String colName, IValidates colInput) {
        // Get value
        String value = colInput.getInputAsString();
        String cleanVal = deformatCurrency(value);

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


    /**
     * Checks if a given value is a phone number or not.
     * Regex thanks to https://www.baeldung.com/java-regex-validate-phone-numbers
     * @param colName
     * @param colInput
     * @return
     */
    public static boolean phoneNumSoftValidate(String colName, IValidates colInput) {
        // Get value
        String value = colInput.getInputAsString();

        // If null, we don't want to run test, technically pass (let a nullable test handle that)
        if (value == null || value.isEmpty()) return true;

        // Check phone number regex  match
        Pattern pattern = Pattern.compile("^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$");
        Matcher matcher = pattern.matcher(value);

        if (! matcher.matches()) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Phone Validation");
            a.setHeaderText("Possibly invalid data for " + colName);
            a.setContentText("It looks like " + colName + " may require a phone number. Please confirm your value is correct.");
            a.showAndWait();

            // Based on confirmation result, change return
            Optional<ButtonType> result = a.showAndWait();
            if (result.get() == ButtonType.OK){
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a given value is an email or not.
     * Regex thanks to https://mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/
     * @param colName
     * @param colInput
     * @return
     */
    public static boolean emailSoftValidate(String colName, IValidates colInput) {
        // Get value
        String value = colInput.getInputAsString();

        // If null, we don't want to run test, technically pass (let a nullable test handle that)
        if (value == null || value.isEmpty()) return true;

        // Check phone number regex  match
        Pattern pattern = Pattern.compile(
                "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
        Matcher matcher = pattern.matcher(value);

        if (! matcher.matches()) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Email Validation");
            a.setHeaderText("Possibly invalid data for " + colName);
            a.setContentText("It looks like " + colName + " may require an email. Please confirm your value is correct.");
            a.showAndWait();

            // Based on confirmation result, change return
            Optional<ButtonType> result = a.showAndWait();
            if (result.get() == ButtonType.OK){
                return true;
            } else {
                return false;
            }
        }
            return true;
    }
}
