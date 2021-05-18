package app;

import app.Validation.*;
import db.CustomValidator;
import db.DbManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Class which assists in the creation and setup of controls for the javafx scene
 */

public class ControlMaker {

    String colName;
    String currentTable;



    public ControlMaker(String colName, String currentTable) {
        this.colName = colName;
        this.currentTable = currentTable;
    }

    public void addSoftPhoneValidation(IValidates colInput) {
        colInput.addValidator(new CustomValidator() {
            @Override
            public boolean checkValidity(HashMap<String, String> args, Control colInput) throws SQLException {
                return ValidationManager.phoneNumSoftValidate(colName, (IValidates) colInput);
            }
        });
    }

    public void addSoftEmailValidation(IValidates colInput) {
        colInput.addValidator(new CustomValidator() {
            @Override
            public boolean checkValidity(HashMap<String, String> args, Control colInput) throws SQLException {
                return ValidationManager.emailSoftValidate(colName, (IValidates) colInput);
            }
        });
    }

    public void addFKeyValidation(IValidates colInput, DbManager.ForeignKeyReference fkRef) {
        // Add that validator to the internal list of validators for this control
        colInput.addValidator(new CustomValidator() {
            @Override
            public boolean checkValidity(HashMap<String, String> args, Control colInput) throws SQLException {
                return ValidationManager.foreignKeyConstraintMet(colName, fkRef.getForeignKeyRefTable(),
                        fkRef.getForeignKeyRefColumn(), (ValidatingTextField) colInput);
            }
        });
    }

    public void addDoubleDecimalValidation(IValidates colInput) {
        colInput.addValidator(
                new CustomValidator() {
                    @Override
                    public boolean checkValidity(HashMap<String, String> args, Control colInput) throws SQLException {
                        return ValidationManager.isDecimal(colName, (ValidatingTextField) colInput);
                    }
                });
    }

    public void addIntValidation(IValidates colInput) {
        colInput.addValidator(
                new CustomValidator() {
                    @Override
                    public boolean checkValidity(HashMap<String, String> args, Control colInput) throws SQLException {
                        return ValidationManager.isInt(colName, (ValidatingTextField) colInput);
                    }
                });
    }

    public void addNonNullValidation(IValidates colInput) {
        colInput.addValidator(new CustomValidator() {
            @Override
            public boolean checkValidity(HashMap<String, String> args, Control colInput) throws SQLException {
                return ValidationManager.isNotNull(colInput, colName);
            }
        });
    }

    public ValidatingTextField makeTextField() {
        ValidatingTextField field = new ValidatingTextField(currentTable, colName);
        field.setMinWidth(350);
        field.setMaxWidth(350);
        return field;
    }

    public Label makeColumnLabel(HashMap<String, String> formattedColumnLabels) {
        // Format the detail label a bit to add space
        String tidierDefaultLabel = FormatHelper.getTidierDefaultLabel(colName);

        Label columnLabel = new Label(tidierDefaultLabel); // create a new label with that name

        // Update to use the column's display label (if defined in the class)
        if(formattedColumnLabels != null && formattedColumnLabels.get(colName) != null) {
            // Find the formatted label associated with the actual db column name (in colName variable)
            columnLabel.setText(formattedColumnLabels.get(colName));
        }

        // Set up other label formatting
        columnLabel.setStyle("-fx-font: 16 System"); // update size
        columnLabel.setPadding(new Insets(0,0,5,0)); // add a little padding
        columnLabel.setId("lbl" + colName);
        return columnLabel;
    }

    public Control makeTextArea(Label columnLabel) {
        Control colInput;
        colInput = new ValidatingTextArea(currentTable, colName); // add a text area
        colInput.setMaxHeight(30); // Set it's height
        columnLabel.setMaxHeight(42); // Adjust the label to keep things aligned
        columnLabel.setMinHeight(42);
        columnLabel.setAlignment(Pos.TOP_LEFT);
        return colInput;
    }

    public void setDecimalFormatListener(Control colInput) {
        DecimalFormat myFormat = new DecimalFormat("$###,##0.00");
        colInput.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (observableValue.getValue() == false)
                FormatHelper.formatCurrency((ValidatingTextField) colInput, myFormat);
        });
    }

    public Control makeDatePicker() {
        Control colInput;
        colInput = new ValidatingDatePicker(currentTable, colName);
        colInput.setMinWidth(350); // sets width of datepicker to match textinput length
        colInput.setMaxWidth(350); // sets width of datepicker to match textinput length
        return colInput;
    }


}
