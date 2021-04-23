package sample;

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

    public ValidatingDatePicker(String tableName, String columnName) {
        super();
        this.tableName = tableName;
        this.columnName = columnName;
    }


}
