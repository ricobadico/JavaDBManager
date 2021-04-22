package sample;
import javafx.scene.control.*;

/**
 * Wrapper for javaFX TextFieldl exactly the same but implements
 * IValidates interface, which allows the object to store
 * and test multiple custom validation methods (wrapped as CustomValidator objects)
 */
public class ValidatingTextField extends TextField implements IValidates{

    // Extra fields this keeps track of
    String tableName;
    String columnName;

    public ValidatingTextField(String tableName, String columnName) {
        super();
        this.tableName = tableName;
        this.columnName = columnName;
    }
}
