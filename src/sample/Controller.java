/**
 * Sample Skeleton for 'sample.fxml' Controller Class
 */

package sample;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import db.ITableEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import db.DbManager;

public class Controller {

    // Member variables
    DbManager connection = null; // current connection
    String currentTable = null; // selected table
    Class<ITableEntity> predefinedClassFile = null; // predefined table class (if one exists)

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="vboxLabels"
    private VBox vboxLabels; // Value injected by FXMLLoader

    @FXML // fx:id="vboxInputs"
    private VBox vboxInputs; // Value injected by FXMLLoader

    @FXML // fx:id="btnEdit"
    private Button btnEdit; // Value injected by FXMLLoader

    @FXML // fx:id="btnSave"
    private Button btnSave; // Value injected by FXMLLoader

    @FXML // fx:id="cbxTableList"
    private ComboBox<String> cbxTableList; // Value injected by FXMLLoader

    @FXML // fx:id="cbxRecordList1"
    private ComboBox<String> cbxRecordList; // Value injected by FXMLLoader

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert vboxLabels != null : "fx:id=\"vboxLabels\" was not injected: check your FXML file 'sample.fxml'.";
        assert vboxInputs != null : "fx:id=\"vboxInputs\" was not injected: check your FXML file 'sample.fxml'.";
        assert btnEdit != null : "fx:id=\"btnEdit\" was not injected: check your FXML file 'sample.fxml'.";
        assert btnSave != null : "fx:id=\"btnSave\" was not injected: check your FXML file 'sample.fxml'.";
        assert cbxTableList != null : "fx:id=\"cbxTableList\" was not injected: check your FXML file 'sample.fxml'.";
        assert cbxRecordList != null : "fx:id=\"cbxRecordList1\" was not injected: check your FXML file 'sample.fxml'.";

        // Get table names for table combo box
        // These are currently going to be hard-coded to a few options, but all the subsequent methods are set to to take anything
        ArrayList<String> tableNames = new ArrayList<String>(Arrays.asList("Agents", "Customers", "Packages", "Suppliers", "Bookings", "Agencies"));
        ObservableList tableNamesContents = FXCollections.observableList(tableNames); // add list to combo box
        cbxTableList.setItems(tableNamesContents);

        // Set listener for Table combo box
        cbxTableList.getSelectionModel().selectedItemProperty().addListener(change ->
            populateTableSelection()
        );

        // Set listener for record combo box change
        cbxRecordList.getSelectionModel().selectedItemProperty().addListener(change ->
            populateRecordSelection()
        );

        // Set listener for Edit button
        btnEdit.setOnMouseClicked(mouseEvent -> enterEditMode());

        // Set listener for Save button
        btnSave.setOnMouseClicked(mouseEvent -> saveInputs());
    }

    /**
     * Uses a new selection on the Table combo box to update the application:
     * - Labels for records of the chosen table are added to the Records combo box.
     * - Labels and fields are provided for each column in the chosen table.
     * - The Record combo box is enabled for selection.
     */
    private void populateTableSelection() {

        // Get currently selected table name
        String chosenTable = cbxTableList.getSelectionModel().getSelectedItem();
        currentTable = chosenTable; // set member-level variable to track state of this

        // Check to see if a class exists with the selected item.
        //  If so, we want to use it instead of the default-choosing code
        try {
            predefinedClassFile = (Class<ITableEntity>) Class.forName("db." + currentTable,    // Checks for the classfile in the database package
                    false, this.getClass().getClassLoader()); // Extra params to make it work
        // Class.forName calls an exception if the class doesn't exist.
        } catch (ClassNotFoundException e) {
            System.out.println("Using programmatic defaults");
        }

        // Get record names for combo box
        connection = new db.DbManager(); // create manager class (establishes connection)
        ArrayList<String> recordNames = connection.getRecordNames(chosenTable); // call method to get descriptive names for each record
        ObservableList cbxContents = FXCollections.observableList(recordNames); // convert to observable list
        cbxRecordList.setItems(cbxContents); // add list to combo box

        // Remove existing text fields and labels to make room for the ones this table calls for
        vboxLabels.getChildren().clear();
        vboxInputs.getChildren().clear();

        // Create labels and textboxes for each column
        ArrayList<String> columnNames = connection.getColumnNames(chosenTable); // get col names

        // If we have a class with column labels, we can grab them now
        HashMap<String, String> formattedColumnLabels = null;
        if(predefinedClassFile != null) {
            try {
                // These lines just run the GetColumnLabels method from the current classfile.
                // Since that method is required by the interface, we know it should work
                Method getColumnLabels = predefinedClassFile.getMethod("GetColumnLabels");
                formattedColumnLabels = (HashMap<String, String>) getColumnLabels.invoke(predefinedClassFile, null);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        // Create the columns needed for this table's data
        for (String colName: columnNames) { // for each column:

            Label columnLabel = new Label(colName); // create a new label with that name

            // Update to use the column's display label (if defined in the class)
            if(formattedColumnLabels != null) {
                // Find the formatted label associated with the actual db column name (in colName variable)
                columnLabel.setText(formattedColumnLabels.get(colName));
            }

            // Set up other label formatting
            columnLabel.setStyle("-fx-font: 16 System"); // update size
            columnLabel.setPadding(new Insets(0,0,5,0)); // add a little padding
            vboxLabels.getChildren().add(columnLabel); // attach it to the vbox

            // Add an input and listeners based on the column's datatype
            Control colInput;

            // Datetime data: create datepicker
            if(findDataType(colName).equals("datetime")) {
                colInput = new DatePicker();

            }

            // Decimal data: create textbox with decimal formatting and validation
            else if (findDataType(colName).equals("decimal")) {
                colInput = new TextField(); // add a text field

                // Add an on-blur listener that will format the input to a nice currency display
                DecimalFormat myFormat = new DecimalFormat("$###,##0.00");
                colInput.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
                    if (observableValue.getValue() == false)
                        FormatHelper.formatCurrency((TextField) colInput, myFormat);
                });

            // Int data: create textbox and add int validation
            } else if (findDataType(colName).equals("int")) {
                colInput = new TextField(); // add a text field

                // Add validation on leaving textfield
                colInput.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
                    if (observableValue.getValue() == false) {

                        // Run int validation
                        Validator.isPositiveInt(colName, (TextField) colInput);

                    }
                });

            // Varchar/ misc data
            // TODO: add varchar length validation here instead of elsewhere
            // TODO: Figure out if there are other data types this should be checking for
            } else {
                colInput = new TextField(); // add a text field
            }
            colInput.setId("input" + colName); // give it an id
                vboxInputs.getChildren().add(colInput); // add it to other vbox

            // Set input to read-only (default until edit mode is entered)
            colInput.setDisable(true);

            // TODO: add custom validation, if any, in a Table class

            // Check for table class
            if(predefinedClassFile != null){

                // TODO - search class for fields with custom validation.
                //  Possibly this requires the class having a map of column names as keys as validation calls as values.
                //  We can check the current column name with the colName variable
                //  This is needed for, for instance, ensuring start date is before end date in packages


            }

        }

        // Enable second combobox
        cbxRecordList.setDisable(false);

        // Disable edit button (until new record is selected)
        btnEdit.setDisable(true);
    }




    /**
     * Uses a new selection from the Records combo box to update the application:
     * - Current data for each field in the record are displayed in corresponding text boxes.
     * - The Edit button is enabled for editing.
     */
    private void populateRecordSelection() {

        // Get currently selected record in combo box
        String chosenItem = cbxRecordList.getSelectionModel().getSelectedItem();

        // When switching from one table to another, this event fires with the selection being null.
        // So, we only populate data when a non-null selection is chosen.
        if (chosenItem != null) {

            // Split item on delimiter between ID and rest of name, grab the ID (in first index), parse to int
            int chosenID = Integer.parseInt(
                    chosenItem.split(":")[0]);

            // Use that ID to grab record data
            connection = new db.DbManager(); // create manager class (establishes connection)
            ResultSet record = connection.getRecord(currentTable, chosenID);

            // Iterate through the inputs generated for the page, adding the next bit of data from the result set
            try {
                record.next(); // have to call this once to get at the data!
                for (int i = 0; i < vboxInputs.getChildren().size(); i++) {
                    // Grab the input at that index
                    Control input = (Control) vboxInputs.getChildren().get(i);
                    // Grab the data for that field from the results (adding 1 to match index)
                    String data = record.getObject(i + 1) + "";
                    // If data is null, replace with empty string
                    if (data.equals("null")) {
                        data = "";
                    }

                    // Set input based on column data type. We can find this out by looking at the control's ID, which was generated with it
                    String colName = input.getId().replace("input", "");
                    // Else if decimal
                    if(findDataType(colName).equals("decimal")) {
                        DecimalFormat myFormat = new DecimalFormat("$###,##0.00");
                        //Format as currency
                        double dataAsDecimal = Double.valueOf((data.replaceAll(",","").replaceAll("\\$","")));
                        System.out.println("Decimal: " + dataAsDecimal);
                        ((TextField) input).setText(myFormat.format(dataAsDecimal));
                    }
                    // Else if date
                    else if(findDataType(colName).equals("datetime")){
                        LocalDate timeData = LocalDateTime.parse(data).toLocalDate(); // convert string to date
                        ((DatePicker)input).setValue(timeData); // set it as default value for datepicker
                    }
                    // If anything not needing special formatting (varchar/string, int)
                    else {
                        ((TextField) input).setText(data);
                    }
                }

                btnEdit.setDisable(false);

            } catch (SQLException e) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setTitle("Connection Issue");
                a.setHeaderText("We can't seem to connect to the database!");
                a.setContentText(e.getMessage());
                a.show();
            }
        }
    }

    /**
     * Puts the app in edit mode:
     * - Enables all text fields to allow for user editing.
     * - Enables save button.
     */
    private void enterEditMode() {

        // We want to figure out which column is the PK so we can keep it disabled
        try {
            // Figure out which column is PK
            String pkCol = connection.getPKColumnForTable(currentTable);
            String IDForPKTextBox = "input" + pkCol; // the id of the pk textbox takes this form
            System.out.println(pkCol);

            // Enable all text fields (except PK)
            for (Node textfield : vboxInputs.getChildren()) {
                System.out.println(textfield.getId());
                if( ! textfield.getId().equals(IDForPKTextBox)) { // check if ID corresponds to pk column
                    textfield.setDisable(false);
                }
            }

            // Disable edit button
            btnEdit.setDisable(true);

            // Enable save button
            btnSave.setDisable(false);
        }catch (SQLException e){
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Connection Issue");
            a.setHeaderText("We can't seem to connect to the database!");
            a.setContentText(e.getMessage());
            a.show();
        }

    }

    /**
     * Updates the current record using the input fields
     * TODO: consider concurrency
     */
    private void saveInputs() {

        // Create a new connection
        DbManager connection = new DbManager();

        // Initialize a string array to hold all textfield inputs
        HashMap<String, String> textInputs = new HashMap<>();

        // Gather the text boxes data
        for(int i = 0; i < vboxInputs.getChildren().size(); i++){
            // Get column name from labels
            String columnName = ((Label)vboxLabels.getChildren().get(i)).getText();

            String input;

            // We need to process the data a bit based on the data type in the db
            // Datetime: pulled from DatePicker
            if(findDataType(columnName).equals("datetime")){
                LocalDate dateInput = ((DatePicker)vboxInputs.getChildren().get(i)).getValue();
                input = dateInput.toString();

            // Decimal: Pulled from textfield, needs to be stripped of currency characters if not null
            } else if(findDataType(columnName).equals("decimal")) {
                // Get input from text box
                input = ((TextField)vboxInputs.getChildren().get(i)).getText();

                // If empty space, set value to null
                if(input.isBlank())
                    input = null;
                else {
                    // Otherwise, remove extra characters
                    input = ((input.replaceAll(",", "")).replaceAll("\\$", ""));
                    System.out.println("DECIMAL: " + input);
                }
            // Varchar/Int/ anything else  TODO: is there anything else?
            } else {
                // Get input from text box
                 input = ((TextField)vboxInputs.getChildren().get(i)).getText();

                // If empty space, set value to null
                if(input.isBlank())
                    input = null;
            }

            // Add pair to arraylist
            textInputs.put(columnName, input);
        }

        // Attempt to update the database with the given values
        try {
            connection.updateRecord(currentTable, textInputs);

            // Disable checkboxes
            for (Node textfield : vboxInputs.getChildren()) {
                    textfield.setDisable(true);
            }
            // Toggle button enables
            btnEdit.setDisable(false);
            btnSave.setDisable(true);

            // Let user know all worked!
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Update Successful");
            a.setContentText("The record has been saved in the database.");
            a.show();

        }
        // In the event the SQL fails, pop up an alert
        catch (SQLException e) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Bad Update");
            a.setHeaderText("Something went wrong! (Better validation to come)");
            a.setContentText(e.getMessage());
            a.show();
        }
    }

    public String findDataType(String col) {
        String datatype = null;
        try {
            datatype = (connection.getColumnDataType(currentTable, col)) // gets the full datatype (ie "varchar(10)"
                    .split("\\(")[0]; // gets just the data type name ("varchar")
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return datatype;
    }

}
