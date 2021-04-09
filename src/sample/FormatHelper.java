package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.text.DecimalFormat;
import java.text.ParsePosition;

public class FormatHelper {

    public static void  formatCurrency (TextField colInput, DecimalFormat myFormat) {

        // Remove extra characters from formatted input string to get at the actual value (if any exist)
        String cleanInput = colInput.getText().replaceAll(",", "").replaceAll("$", "");
        // Re-format the value and set the input text to it
        colInput.setText(myFormat.format(Double.valueOf(cleanInput)));

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
}
