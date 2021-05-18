package app;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.text.DecimalFormat;
import java.text.ParsePosition;

/**
 * Class that provides various text formatting methods for the app
 */

public class FormatHelper {

    public static void  formatCurrency (TextField colInput, DecimalFormat myFormat) {

        // Remove extra characters from formatted input string to get at the actual value (if any exist)
        String cleanInput = deformatCurrency(colInput.getText());
        // If not empty, re-format the value and set the input text to it
        try {
            if (!cleanInput.isBlank()) {
                colInput.setText(myFormat.format(Double.parseDouble(cleanInput)));
            }
        } catch (NumberFormatException | NullPointerException e){
            System.out.println("No currency to format! Validator will alert user");
        }
    }

    public static String deformatCurrency(String formattedString){
        return formattedString.replaceAll(",", "").replaceAll("\\$", "");
    }


    // Thanks to https://www.programcreek.com/java-api-examples/?api=javafx.scene.control.TextFormatter
    public static void configureTextFieldToAcceptOnlyDecimalValues(TextField textField) {

        DecimalFormat format = new DecimalFormat("#");

        final TextFormatter<Object> decimalTextFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().isEmpty()) {
                return change;
            }
            ParsePosition parsePosition = new ParsePosition(0);
            Object object = format.parse(change.getControlNewText(), parsePosition);

            if (object == null || parsePosition.getIndex() < change.getControlNewText().length()) {
                return null;
            } else {
                return change;
            }
        });
        textField.setTextFormatter(decimalTextFormatter);
    }

    public static String getTidierDefaultLabel(String colName) {
        String tidierDefaultLabel = "";
        for (int i = 0; i < colName.length(); i++){
            char c = colName.charAt(i);
            if(i != 0 && Character.isUpperCase(c)){
                tidierDefaultLabel += " ";
            }
            tidierDefaultLabel += c;
        }
        tidierDefaultLabel += ":";
        return tidierDefaultLabel;
    }


}
