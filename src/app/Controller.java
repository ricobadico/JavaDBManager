/**
 * Sample Skeleton for 'sample.fxml' Controller Class
 */

package app;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

import app.Validation.*;
import db.CustomValidator;
import db.ITableEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import db.DbManager;

import static app.ControllerHelper.*;
import static app.FormatHelper.deformatCurrency;

/**
 * Main controller for the program, in all its spaghetti glory.
 * Introduction of add mode, and initial code to do inserts (including validation methods for getting PK), by Dexter.
 * Colours and side logo, and Customer class file, by Jetlyn.
 * All other layout and coding throughout the application by Eric.
 */

public class Controller {

    // Member variables
    DbManager connection = null; // current connection
    String currentTable = null; // selected table
    private String currentPKType = null; // current table's pk's data type
    Class<ITableEntity> predefinedClassFile = null; // predefined table class (if one exists)
    String userMode= null; // tells save button whether to run update or insert methods

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

    @FXML
    private Button btnAdd; // Value injected by FXMLLoader

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
        assert btnAdd != null : "fx:id=\"btnAdd\" was not injected: check your FXML file 'sample.fxml'.";
        assert btnSave != null : "fx:id=\"btnSave\" was not injected: check your FXML file 'sample.fxml'.";
        assert cbxTableList != null : "fx:id=\"cbxTableList\" was not injected: check your FXML file 'sample.fxml'.";
        assert cbxRecordList != null : "fx:id=\"cbxRecordList1\" was not injected: check your FXML file 'sample.fxml'.";

        // Get table names for table combo box
        // These are currently going to be hard-coded to a few options, but all the subsequent methods are set to to take anything
        connection = new DbManager();
        ArrayList<String> tableNames = connection.getAllTableNames();
        // All tables that have predefined classes written (that define special formatting and validation) get pushed to the top and starred
        ObservableList tableNamesContents = FXCollections.observableList(highlightPredefinedClasses(tableNames)); // add list to combo box
        cbxTableList.setItems(tableNamesContents);

        // Set listener for Table combo box
        cbxTableList.getSelectionModel().selectedItemProperty().addListener(change -> {
                populateTableSelection();
        });

        // Set listener for record combo box change
        cbxRecordList.getSelectionModel().selectedItemProperty().addListener(change -> {
                    // Get currently selected record in combo box
                    String chosenItem = cbxRecordList.getSelectionModel().getSelectedItem();

                    // When switching from one table to another, this event fires with the selection being null.
                    // So, we only populate data when a non-null selection is chosen.
                    if (chosenItem != null) {

                        // Split item on delimiter between ID and rest of name, grab the ID (in first index), parse to int
                        String chosenPK = chosenItem.split(":")[0];
                        populateRecordSelection(chosenPK);
                    }
                }

        );

        // Set listener for Edit button
        btnEdit.setOnMouseClicked(mouseEvent -> enterEditMode());

        //Set listener for Add button
        btnAdd.setOnMouseClicked(mouseEvent -> enterAddMode());

        // Set listener for Save button
        btnSave.setOnMouseClicked(mouseEvent -> {
            try {
                saveButtonSwitch();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    private void populateRecordCBOnly() {
        // Get record names for combo box
        connection = new db.DbManager(); // create manager class (establishes connection)
        ArrayList<String> recordNames = null;
        ObservableList cbxContents;
        // If a class has been predefined, it will have its own custom record names
        if(predefinedClassFile != null) {
            try {
                // call method to get descriptive names for each record
                Method getRecordNames = predefinedClassFile.getMethod("GetRecordNames");
                recordNames = (ArrayList<String>) getRecordNames.invoke(predefinedClassFile, null);

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            // Otherwise, create defaults record names
        } else{
            recordNames = connection.getRecordNames(currentTable); // call method to get descriptive names for each record
        }
        cbxContents = FXCollections.observableList(recordNames); // convert to observable list
        cbxRecordList.setItems(cbxContents); // add list to combo box
    }

    /**
     * Uses a new selection on the Table combo box to update the application:
     * - Labels for records of the chosen table are added to the Records combo box.
     * - Labels and fields are provided for each column in the chosen table.
     * - The Record combo box is enabled for selection.
     */
    private void populateTableSelection() {

        // Get current table name (removing formatting asterisk if one exists),
        // setting member-level variable to track state of this
        currentTable = cbxTableList.getSelectionModel().getSelectedItem()
            .replace("*", "");

        // Get a list of columns in the current table that are nullable (used for validation below)
        ArrayList<String> nullableColumns = connection.getNullableColumnsNames(currentTable);

        // Check to see if a class exists with the selected item.
        //  If so, we want to use it instead of the default-choosing code
        try {
            predefinedClassFile = (Class<ITableEntity>) Class.forName("db.PredefinedTableClasses." + currentTable,    // Checks for the classfile in the database package
                    false, this.getClass().getClassLoader()); // Extra params to make it work
        // Class.forName calls an exception if the class doesn't exist.
        } catch (ClassNotFoundException e) {
            predefinedClassFile = null;
        }

        // Get record names for combo box
        connection = new db.DbManager();
        ArrayList<String> recordNames = null;
        ObservableList cbxContents;
        // If a class has been predefined, it will have its own custom record names
        if(predefinedClassFile != null) {
            try {
                // call method to get descriptive names for each record
                Method getRecordNames = predefinedClassFile.getMethod("GetRecordNames");
                recordNames = (ArrayList<String>) getRecordNames.invoke(predefinedClassFile, null);

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        // Otherwise, create defaults record names
        } else{
            recordNames = connection.getRecordNames(currentTable); // call method to get descriptive names for each record
        }
        // Now, attach those record names to the combo box
        cbxContents = FXCollections.observableList(recordNames); // convert to observable list
        cbxRecordList.setItems(cbxContents); // add list to combo box

        // Remove existing text fields and labels to make room for the ones this table calls for
        vboxLabels.getChildren().clear();
        vboxInputs.getChildren().clear();

        // Create labels and textboxes for each column
        ArrayList<String> columnNames = connection.getColumnNames(currentTable); // get col names

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

            ControlMaker cm = new ControlMaker(colName, currentTable); // Helper object for controller work
            Integer maxLength = connection.findDataTypeMaxLength(currentTable,colName);  // Get column max length, used later below

            Label columnLabel = cm.makeColumnLabel(formattedColumnLabels);
            vboxLabels.getChildren().add(columnLabel); // attach it to the vbox


            // Now, we add a control based on the column's datatype

            Control colInput;
            String datatype = connection.findDataType(currentTable, colName);
            // Datetime data: create datepicker
            if(datatype.equals("datetime")) {
                colInput = cm.makeDatePicker();
            }
            // Decimal data: create textbox with decimal formatting and validation
            else if (datatype.equals("decimal")) {
                colInput = cm.makeTextField(); // add a text field
            // Int data: create textbox
            } else if (datatype.equals("int")) {
                colInput = cm.makeTextField(); // add a text field
            // Text data that could be longer than one line
            } else if (maxLength != null && maxLength > 50) {
                colInput = cm.makeTextArea(columnLabel);

            // Varchar/ misc data
            // TODO: Figure out if there are other data types this should be checking for
            } else {
                colInput = cm.makeTextField(); // add a text field
            }
            colInput.setId("input" + colName); // give it an id
            vboxInputs.getChildren().add(colInput); // add it to other vbox

            // Time to add a slew of validators to the internal list of validators for this control.
            // These get called when the user leaves the input field, and are checked before inserting/updating

            // Add non-null validation if applicable to this column
            if (nullableColumns.contains(colName)){
                cm.addNonNullValidation((IValidates) colInput);
            }
            // Add validator to check for positive integers for int inputs
            if (datatype.equals("int")) {
                cm.addIntValidation((IValidates) colInput);
            }
            // Add decimal/double validation
            else if (datatype.equals("decimal") || datatype.equals("double")) {
                cm.addDoubleDecimalValidation((IValidates) colInput);
            }
            // Add foreign key reference validation if needed (regardless of data type)
            // Call DB information schema to see if this column is a foreign key, and if so, to what
            DbManager.ForeignKeyReference fkRef = connection.getForeignKeyReferences(currentTable, colName);
            if (fkRef != null){ // if a fk reference to a pk was found..
                cm.addFKeyValidation((IValidates) colInput, fkRef);
            }
            // Add soft confirmation validation for potential phone number fields
             if(colName.toLowerCase(Locale.ROOT).contains("phone")){
                 cm.addSoftPhoneValidation((IValidates) colInput);
             }
            // Add soft confirmation validation for potential email fields
            if(colName.toLowerCase(Locale.ROOT).contains("email")){
                cm.addSoftEmailValidation((IValidates) colInput);
            }

            // If a preexisting table class exists, add custom validation if any
            if(predefinedClassFile != null){  // Check for table class
                // Pull out class method that provides a map of additional validation functions (classes implementing ITableEntity are guaranteed to have this)
                Method getValidators = null;
                HashMap<String, CustomValidator> additionalValidators = null;
                try {
                    getValidators = predefinedClassFile.getMethod("GetValidators");
                    additionalValidators = (HashMap<String, CustomValidator>) getValidators.invoke(predefinedClassFile, null);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

                //In the event that any of those validators refer to the current column, we add it to the control
                if(additionalValidators.containsKey(colName)) {
                    // Grab that validator
                    CustomValidator validator = additionalValidators.get(colName);
                    // Add that validator to the internal list of validators (so we can run them later on clicking Save)
                    ((IValidates) colInput).addValidator(validator);
                }
            }

            //Set all accumulated validators to trigger on leaving the control
            ((IValidates) colInput).addOnBlurValidation(currentTable, colName, btnAdd.getScene());

            // Set input to read-only (default until edit mode is entered)
            colInput.setDisable(true);

        } // end loop over columns

        // Enable second combobox
        cbxRecordList.setDisable(false);
        btnAdd.setDisable(false);

        // Disable edit button (until new record is selected)
        btnEdit.setDisable(true);

        // Set a class variable for current table's pk's data type, to use the proper methods below
        try {
            String pkColumn = connection.getPKColumnForTable(currentTable);
            currentPKType = connection.findDataType(currentTable, pkColumn);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }




    /**
     * Uses a new selection from the Records combo box to update the application:
     * - Current data for each field in the record are displayed in corresponding text boxes.
     * - The Edit button is enabled for editing.
     */
    private void populateRecordSelection(String chosenPK) {

        // Enable the add button in case it's disabled
        btnAdd.setDisable(false);

        connection = new db.DbManager(); // create manager class (establishes connection)

        ResultSet record = null;
        // Use that ID to grab record data. Which method overload depends on pk data type
            if (currentPKType.equals("int")) {
                record = connection.getRecord(currentTable, Integer.parseInt(chosenPK));
            }
            else if (currentPKType.equals("varchar")) {
                record = connection.getRecord(currentTable, chosenPK);
            }
            else {
                makeWarningAlert("Feature not added!", "You've found the bounds of the demonstration.", "Currently, the app only works for tables with either int or String PK, and only for single-column PKs.");
            }

            // Iterate through the inputs generated for the page, adding the next bit of data from the result set
            try {
                record.next(); // have to call this once to get at the data!
                for (int i = 0; i < vboxInputs.getChildren().size(); i++) {
                    // Grab the input at that index
                    Control input = (Control) vboxInputs.getChildren().get(i);
                    // Grab the data for that field from the results (adding 1 to match index) - trim for cleanliness
                    String data = String.valueOf(record.getObject(i + 1)).trim();

                    // If data is null, replace with empty string
                    if (data.isBlank() || data.equals("null")) {
                        ((IValidates)input).setInputToBlank();
                    }
                    else {
                        // Set input based on column data type. We can find this out by looking at the control's ID, which was generated with it
                        String colName = input.getId().replace("input", "");
                        // if decimal
                        if (connection.findDataType(currentTable, colName).equals("decimal")) {
                            DecimalFormat myFormat = new DecimalFormat("$###,##0.00");
                            //Format as currency
                            double dataAsDecimal = Double.valueOf(deformatCurrency(data));
                            ((ValidatingTextField) input).setText(myFormat.format(dataAsDecimal));
                        }
                        else {
                            ((IValidates) input).setInputAsString(data); // add data to input
                        }
                    }
                    vboxInputs.getChildren().get(i).setDisable(true); // set default state of disabled until Edit Mode entered
                }

                btnEdit.setDisable(false);


            } catch (SQLException e) {
                makeWarningAlert("Connection Issue","We can't seem to connect to the database!", e.getMessage());
            }
        }


    /**
     * Puts the app in edit mode:
     * - Enables all text fields to allow for user editing.
     * - Enables save button.
     */
    private void enterEditMode() {
        userMode = "update";
        // We want to figure out which column is the PK so we can keep it disabled
        try {
            // Figure out which column is PK
            String pkCol = connection.getPKColumnForTable(currentTable);
            String IDForPKTextBox = "input" + pkCol; // the id of the pk textbox takes this form

            // Enable all text fields (except PK)
            for (Node textfield : vboxInputs.getChildren()) {
                if( ! textfield.getId().equals(IDForPKTextBox)) { // check if ID corresponds to pk column
                    textfield.setDisable(false);
                }
            }

            // Disable edit button
            btnEdit.setDisable(true);

            // Enable save button
            btnSave.setDisable(false);
        }catch (SQLException e){
            makeWarningAlert("Connection Issue","We can't seem to connect to the database!", e.getMessage());
        }

    }

    /**
     * Puts the app in Add mode:
     *  - Empties all text fields to allow for adding new entries
     *  - Enables save button
     */

    private void enterAddMode() {
        userMode = "insert";

        try {
            // Figure out which column is PK
            String pkCol = connection.getPKColumnForTable(currentTable);
            boolean doesAutoIncr = connection.columnPrimaryKeyAutoIncrements(currentTable, pkCol);

            String IDForPKTextBox = "input" + pkCol; // the id of the pk textbox takes this form


            // Enable all text fields (except PK)
            for (Node child : vboxInputs.getChildren()) {
                Control tf = (Control) child;
                if( ! (doesAutoIncr && tf.getId().equals(IDForPKTextBox))) { // check if ID corresponds to pk column in auto-increment table
                    tf.setDisable(false);
                }
                // Clear this field
                ((IValidates)tf).setInputToBlank();
            }
            // Disable buttons
            btnEdit.setDisable(true);
            btnAdd.setDisable(true);

            // Enable save button
            btnSave.setDisable(false);

        }catch (SQLException e){
            makeWarningAlert("Connection Issue","We can't seem to connect to the database!", e.getMessage());
        }

    }

    public void saveButtonSwitch() throws SQLException {
        if (userMode == "update") {
            saveUpdates();
        }
        else if (userMode == "insert") {
            saveInsert();
        }
    }

    private void saveInsert() throws SQLException {
        boolean validationGate = true;
        System.out.println("Start of add operation");
        DbManager connection = new DbManager();
        //Get primary key column to determine primary key later
        String pkColumnName = null;
        try {
            pkColumnName = connection.getPKColumnForTable(currentTable);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


        // Initialize a string array to hold all textfield inputs
        HashMap<String, String> textInputs = new HashMap<>();

        // Gather the text boxes data
        for(int i = 0; i < vboxInputs.getChildren().size(); i++) {

            //Control inputControl = (Control) vboxLabels.getChildren().get(i); -- I tried putting all subsequent references into a var and it stopped working, much to my dismay
            // Get column name from labels
            String columnName = vboxLabels.getChildren().get(i) // get the current label
                    .getId().substring(3); // and get the id (eg "lblAgentId") and remove the lbl to get the SQL column name

            // Get input value for the column
            String input = null;
            // Special case to handle if the current column is a primary key

            // Pk autoincrements
            input = getInput(connection, i, columnName);

            // Add pair to arraylist
            textInputs.put(columnName, input);

            // Now that we've gotten a value for this current input, we can check validation
            IValidates inputControl = (IValidates) vboxInputs.getChildren().get(i); // grab ref to input
            // In the event any of the validators attached to the input fail, we leave the Update method.
            // (the inner validate methods will take care of alerting the user)
            if(!inputControl.validate(currentTable, columnName, input)){
                validationGate = false;
                System.out.println("error test" + currentTable + " " + columnName + " " + input);
            }
        }

        // If any validation failed, we stop
        if(validationGate == false){
            return;
        }

        try {
            connection.addRecord(currentTable, textInputs);

            // Disable checkboxes
            for (Node textfield : vboxInputs.getChildren()) {
                textfield.setDisable(true);
            }
            // Toggle button enables
            btnAdd.setDisable(false);
            btnEdit.setDisable(false);
            btnSave.setDisable(true);

            // Get outta insert mode
            userMode = null;

            // Let user know all worked!
            makeInfoAlert("Add Successful", "The record has been saved in the database.");

            //Populate the record combo box
            populateRecordCBOnly();
            //Select the record that was added
            cbxRecordList.getSelectionModel().selectLast();
        }
        // In the event the SQL fails, pop up an alert
        catch (SQLException e) {
            makeWarningAlert("Bad Update","Something went wrong!", e.getMessage());
        }
    }

    /**
     * Updates the current record using the input fields
     * TODO: We could consider concurrency
     */
    private void saveUpdates() {
        boolean validationGate = true;
        // Create a new connection
        DbManager connection = new DbManager();
        //Get primary key column to determine primary key later
        String pkColumnName = null;
        try {
            pkColumnName = connection.getPKColumnForTable(currentTable);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        // Initialize a string array to hold all textfield inputs
        HashMap<String, String> textInputs = new HashMap<>();

        // Loop through inputs - here, we can pull data, and call validators
        for(int i = 0; i < vboxInputs.getChildren().size(); i++){
            // Get column name from labels
            String columnName = (vboxLabels.getChildren().get(i)) // get the current label
                    .getId().substring(3); // and get the id (eg "lblAgentId") and remove the lbl to get the SQL column name;

            // Get value from input
            String input = getInput(connection, i, columnName);

            // Add pair to arraylist
            textInputs.put(columnName, input);

            // Check validation
            IValidates inputControl = (IValidates) vboxInputs.getChildren().get(i); // grab ref to input
            // In the event any of the validators attached to the input fail, we leave the Update method.
            // (the inner validate methods will take care of alerting the user)
            if(!inputControl.validate(currentTable, columnName, input)){
                validationGate = false;
                System.out.println("error test" + currentTable + " " + columnName + " " + input);
            }
        }

        // If any validation failed, we stop
        if(validationGate == false){
            return;
        }


        // Attempt to update the database with the given values
        try {
            //Find and store the value of the primary key for the row being editted
            connection.updateRecord(currentTable, currentPKType, textInputs);

            // Disable checkboxes
            for (Node textfield : vboxInputs.getChildren()) {
                textfield.setDisable(true);
            }
            // Toggle button enables
            btnEdit.setDisable(false);
            btnSave.setDisable(true);

            // Get outta update mode
            userMode = null;

            // Let user know all worked!
            makeInfoAlert("Update Successful", "The record has been saved in the database.");

            //Get String arraylist of info Records combo box will be populated with
            ArrayList<String> updatedInfo = connection.getRecordNames(currentTable);
            //Variable for index position of the edited row
            int cbLabelIndexPos = 0;

            //Loop through updatedInfo until entry matching stored primary key value is found. Store index position.
            String pkValue = textInputs.get(pkColumnName);
            for (int i = 0; i < updatedInfo.size(); i++) {
                String currentID = updatedInfo.get(i).split(":")[0];
                if (currentID.equals(pkValue)) {
                    cbLabelIndexPos = i;
                }
            }
            //Populate the record combo box
            populateRecordCBOnly();
            //Select the record that was edited
            cbxRecordList.getSelectionModel().select(cbLabelIndexPos);
        }
        // In the event the SQL fails, pop up an alert
        catch (SQLException e) {
            makeWarningAlert("Bad Update","Something went wrong!", e.getMessage());
        }
    }




    /**
     * Grabs the input value of a textbox based on its datatype.
     * @param connection DB manager object
     * @param i current iteration
     * @param columnName DB name of current column
     * @return
     */
    private String getInput(DbManager connection, int i, String columnName) {
        String input;

        // We need to process the data a bit based on the data type in the db
        // Decimal: Pulled from textfield, needs to be stripped of currency characters if not null
        if(connection.findDataType(currentTable, columnName).equals("decimal")) {
            // Get input from text box
            input = ((IValidates)vboxInputs.getChildren().get(i)).getInputAsString();

            // If empty space, set value to null
            if(input.isBlank())
                input = null;
            else {
                // Otherwise, remove extra characters
                input = deformatCurrency(input);
            }
        // Varchar/Int/ anything else
        } else {
            // Get input from text box
             input = ((IValidates)vboxInputs.getChildren().get(i)).getInputAsString();

            // If empty space, set value to null
            if(input.isBlank())
                input = null;
        }
        return input;
    }

    /**
     * Takes an array of table names and formats it so that tables
     * with predefined class files are on top with an asterisk.
     * @param tableNames ArrayList of table name strings
     */
    private ArrayList<String> highlightPredefinedClasses(ArrayList<String> tableNames) {
        ArrayList<String> topTables = new ArrayList<>();
        for (int i = 0; i < tableNames.size(); i++){
            String tableName = tableNames.get(i);
            // Check to see if a class exists with the selected item.
            //  If so, we want to use it instead of the default-choosing code
            Class<ITableEntity> checkForTableClass;
            try {
                checkForTableClass = (Class<ITableEntity>) Class.forName("db.PredefinedTableClasses." + tableName,    // Checks for the classfile in the database package
                        false, this.getClass().getClassLoader()); // Extra params to make it work
                // Class.forName calls an exception if the class doesn't exist.
            } catch (ClassNotFoundException e) {
                checkForTableClass = null;
            }
            // If a predefined class did exist, we add an * and bring it to the top of the array
            if(checkForTableClass != null) {
                tableName = "*" + tableName;
                tableNames.remove(i);
                topTables.add(topTables.size(), tableName);
            }
        }
        // Once done, we add the top classes to the top of the original list
        topTables.addAll(tableNames);
        return topTables;
    }

}
