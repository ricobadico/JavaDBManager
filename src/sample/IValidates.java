package sample;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Interface that allows an object to hold validators,
 * as well as call a method to test them.
 */
public interface IValidates {

    ArrayList<CustomValidator> _validators = new ArrayList<CustomValidator>();

    default ArrayList<CustomValidator> getValidators() {
        return _validators;
    }

    default void addValidator(CustomValidator validator){
        _validators.add(validator);
    }

    default boolean validate(){

        // Initialize variable to keep track of any invalid validation calls
        boolean allPassed = true;

        // Iterate through validators
        for (CustomValidator vldtr: _validators) {
            // If any validation method fails, allPassed fails.
            if(vldtr.checkValidity() == false) {
                allPassed = false;
                // we could break out at this point, but keeping it running will trigger any additional failure messages
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
