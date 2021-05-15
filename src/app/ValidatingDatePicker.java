package app;

import db.CustomValidator;
import javafx.scene.control.*;

import java.util.ArrayList;

/**
 * Wrapper for javaFX TextField exactly the same but implements
 * IValidates interface, which allows the object to store
 * and test multiple custom validation methods (wrapped as CustomValidator objects)
 */
public class ValidatingDatePicker extends DatePicker implements IValidates{

    // Extra fields this keeps track of
    String tableName;
    String columnName;
    ArrayList<CustomValidator> _validators = new ArrayList<CustomValidator>();
    boolean blurOnceCheck = true; // variable that tracks if onBlur validation currently firing (to prevent recursion)

    public ValidatingDatePicker(String tableName, String columnName) {
        super();
        this.tableName = tableName;
        this.columnName = columnName;
    }



    @Override
    public ArrayList<CustomValidator> getValidators() {
      return _validators;
    }

    // Gets input value (IValidates forces this to be implemented; since ValidatingDatePicker also has this,
    // allows for easy extraction of value without needing to care how it's done)
    @Override
    public String getInputAsString() {
        return this.getValue().toString();
    }

    @Override
    public boolean checkIfFirstBlur() {
        return blurOnceCheck;
    }

    @Override
    public void setFirstBlur(boolean val) {
        this.blurOnceCheck = val;
    }
}
