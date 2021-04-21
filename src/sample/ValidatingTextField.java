package sample;
import javafx.scene.control.*;

/**
 * Wrapper for javaFX TextFieldl exactly the same but implements
 * IValidates interface, which allows the object to store
 * and test multiple custom validation methods (wrapped as CustomValidator objects)
 */
public class ValidatingTextField extends TextField implements IValidates{

}
