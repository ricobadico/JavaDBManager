package app;

import db.CustomValidator;
import db.DbManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Interface that allows a Control object to hold validators,
 * as well as call a method to test them.
 */
public interface IValidates {


    ArrayList<CustomValidator> getValidators(); // gets list of validators from implementing class

    String getInputAsString(); // gets input string from validating class

    boolean checkIfFirstBlur(); // boolean that tracks whether onBlur has fired (to prevent recursion)

    void setFirstBlur(boolean val);

    default void addValidator(CustomValidator validator){
        getValidators().add(validator);
    }

    // TODO: this is only triggering once! Why?
    default void addOnBlurValidation(String tableName, String columnName){

        DbManager connection = new DbManager();

        // Grab the validation corresponding to the current column (if one exists)
        if(!getValidators().isEmpty()){

            // Quick lil hack! We only actually want to validate fields that are editable.
            // Notably, an Identity PK column will have these validators attached, but in
            // add mode will be blank and likely fail validation.
            // Since those columns are the only disabled ones at the time of this method being called,
            // we can bypass it here
            if(((Control)this).isDisabled()){
                return;
            }

            // Add validation check as an on-blur listener for the input
            ((Control)this).focusedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (!t1 && this.checkIfFirstBlur() == true) {


                    this.setFirstBlur(false);

                    // A little messy here... we always want to pass the input's value as a string when validating,
                    // but depending on the input given need to grab that differently
                    String valueToValidate = this.getInputAsString();

                    // Now, we use the custom validation

                    // Gather up args needed for validation
                    HashMap<String, String> args = new HashMap<String, String>() {{
                        put("tableName", tableName);
                        put("columnName", columnName);
                        put("value", valueToValidate);
                    }};

                    // Run all validation
                    for (CustomValidator vldtr : getValidators()) {
                        try {
                            boolean isValid = vldtr.checkValidity(args, (Control) this);
                            // if validation fails, it throws an exception with a useful message we can capture in an alert
                        } catch (SQLException e) {
                            Alert a = new Alert(Alert.AlertType.WARNING);
                            a.setTitle("Validation Error");
                            a.setHeaderText("Special validation error for " + columnName + ".");
                            a.setContentText(e.getMessage());
                            a.showAndWait();

                        }
                    }
                            setFirstBlur(true);
            }
            });
        }
    }


    default boolean validate(String tableName, String columnName, String valueToValidate) {

        // Quick lil hack! We only actually want to validate fields that are editable.
        // Notably, an Identity PK column will have these validators attached, but in
        // add mode will be blank and likely fail validation.
        // Since those columns are the only disabled ones at the time of this method being called,
        // we can bypass it here
        if(((Control)this).isDisabled()){
            return true;
        }

        // Initialize variable to keep track of any invalid validation calls
        boolean allPassed = true;

        // Gather up args needed for validation
        HashMap<String, String> args = new HashMap<String, String>() {{
            put("tableName", tableName);
            put("columnName", columnName);
            put("value", (valueToValidate));
        }};

        // Iterate through validators
        ArrayList<CustomValidator> validators = getValidators();
        for (CustomValidator vldtr: getValidators()) {
            // If any validation method fails, allPassed fails.
            try {

                if(vldtr.checkValidity(args, (Control) this) == false) {
                    allPassed = false;
                    // we could break out at this point, but keeping it running will trigger any additional failure messages
                }
            } catch (SQLException e) {
                allPassed = false;
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
