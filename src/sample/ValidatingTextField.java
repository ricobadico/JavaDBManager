package sample;
import db.CustomValidator;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Wrapper for javaFX TextFieldl exactly the same but implements
 * IValidates interface, which allows the object to store
 * and test multiple custom validation methods (wrapped as CustomValidator objects)
 */
public class ValidatingTextField extends TextField implements IValidates{

    // Extra fields this keeps track of
    String tableName;
    String columnName;

    ArrayList<CustomValidator> _validators;

    public ValidatingTextField(String tableName, String columnName) {
        super();
        this.tableName = tableName;
        this.columnName = columnName;
        this._validators = new ArrayList<CustomValidator>();
    }

    // Gets list of validators (used by IValidates interface)
    @Override
    public ArrayList<CustomValidator> getValidators() {
        return _validators;
    }

    // Gets input value (IValidates forces this to be implemented; since ValidatingDatePicker also has this,
    // allows for easy extraction of value without needing to care how it's done)
    @Override
    public String getInputAsString() {
        return this.getText();
    }


}
