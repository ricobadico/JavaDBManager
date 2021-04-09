package sample;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class Validator {

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
}
