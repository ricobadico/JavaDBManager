package sample;

import db.CustomValidator;
import db.DbManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Interface that allows a Control object to hold validators,
 * as well as call a method to test them.
 */
public interface IValidates {

    ArrayList<CustomValidator> _validators = new ArrayList<CustomValidator>();

    // Properties this interface cares about - since interfaces can't have constructors, we need to call the setters
    String currentTable = null;
    String columnName = null;

    default ArrayList<CustomValidator> getValidators() {
        return _validators;
    }

    default void addValidator(CustomValidator validator){
        _validators.add(validator);
    }

    default void addOnBlurValidation(){

        DbManager connection = new DbManager();

        // Grab the validation corresponding to the current column (if one exists)
        if(!_validators.isEmpty()){

            // Add validation check as an on-blur listener for the input
            ((Control)this).focusedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (observableValue.getValue() == false) {

                    // A little messy here... we always want to pass the input's value as a string when validating,
                    // but depending on the input given need to grab that differently
                    String valueToValidate;
                    if (connection.findDataType(currentTable, columnName).equals("datetime")) // getValue() grabs from date picker
                        valueToValidate = ((DatePicker) this).getValue().toString();
                    else // we can use getText() to grab from textfields
                        valueToValidate = ((ValidatingTextField) this).getText();

                    // Now, we use the custom validation

                    // Gather up args needed for validation
                    HashMap<String, String> args = new HashMap<String, String>() {{
                        put("tableName", currentTable);
                        put("columnName", columnName);
                        put("value", valueToValidate);
                    }};

                    // Run all validation
                    for (CustomValidator vldtr : _validators) {
                        try {
                            boolean isValid = vldtr.checkValidity(args);
                            // if validation fails, it throws an exception with a useful message we can capture in an alert
                        } catch(SQLException e){
                            Alert a = new Alert(Alert.AlertType.WARNING);
                            a.setTitle("Validation Error");
                            a.setHeaderText("Special validation error for " + columnName + ".");
                            a.setContentText(e.getMessage());
                            a.show();

                            // Highlight the field
                            ((Control) this).requestFocus();
                            System.out.println("Control class name: " + this.getClass().getName());
                            if (this.getClass().getName().equals("ValidatingTextField"))
                                ((ValidatingTextField) this).selectAll();
                        }
                    }
                }
            });
        }
    }


    default boolean validate(String valueToValidate) {

        // Initialize variable to keep track of any invalid validation calls
        boolean allPassed = true;

        // Gather up args needed for validation
        HashMap<String, String> args = new HashMap<String, String>() {{
            put("tableName", currentTable);
            put("columnName", columnName);
            put("value", (valueToValidate));
        }};

        // Iterate through validators
        for (CustomValidator vldtr: _validators) {
            // If any validation method fails, allPassed fails.
            try {

                if(vldtr.checkValidity(args) == false) {
                    allPassed = false;
                    // we could break out at this point, but keeping it running will trigger any additional failure messages
                }
            } catch (SQLException e) {
                allPassed = false;
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setTitle("Validation Error");
                a.setHeaderText("Special validation error for " + args.get("colName") + ".");
                a.setContentText(e.getMessage());
                a.show();

                // Highlight the field
                ((Control)this).requestFocus();
                System.out.println("Control class name: " + this.getClass().getName());
                if (this.getClass().getName().equals("ValidatingTextField"))
                    ((ValidatingTextField) this).selectAll();
            }
        }

        // Check to see if everything passed
        if (allPassed == true){
            return true;
        }

        // Otherwise return false
        else {
            return false;
        }

    }



}
