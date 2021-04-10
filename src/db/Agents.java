package db;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Agents implements ITableEntity {

    // DB Fields
    private int AgentId;
    private String AgtFirstName;
    private String AgtMiddleInitial;
    private String AgtLastName;
    private String BusPhone;
    private String AgtEmail;
    private String AgtPosition;
    private int AgencyId;

    // Constructor
    public Agents(int agentId, String agtFirstName, String agtMiddleInitial, String agtLastName, String busPhone, String agtEmail, String agtPosition, int agencyId) {
        AgentId = agentId;
        AgtFirstName = agtFirstName;
        AgtMiddleInitial = agtMiddleInitial;
        AgtLastName = agtLastName;
        BusPhone = busPhone;
        AgtEmail = agtEmail;
        AgtPosition = agtPosition;
        AgencyId = agencyId;

    }

    /**
     * Provides a map of preferred labels for column names in this table.
     * Not all column names need to be mapped; for those cases the SQL column name is used
     * @return Map of keyed column names to their preferred labels
     */
    public static HashMap<String, String> GetColumnLabels(){

        HashMap<String, String> columnLabels = new HashMap<>();

        columnLabels.put("AgentId", "ID Number:");
        columnLabels.put("AgtFirstName", "First Name:");
        columnLabels.put("AgtMiddleInitial", "Middle Initial:");
        columnLabels.put("AgtLastName", "Last Name:");
        columnLabels.put("AgtBusPhone", "Business Phone #:");
        columnLabels.put("AgtEmail", "Email:");
        columnLabels.put("AgtPosition", "Position:");
        columnLabels.put("AgencyId", "Agency Id:");

        return columnLabels;
    }

    /**
     * Provides a map of column keys to additional validation (not standard SQL requirements) needed for those columns.
     * This allows the program to run those validators or add them to listeners when working with data for those particular columns.
     * @return Map with an array of validation methods for keyed column.
     */
    public static HashMap<String, ICustomValidator> GetValidators(){

        HashMap<String, ICustomValidator> columnValidators = new HashMap<>();

        // Validator for AgencyID: checks to ensure foreign key constraints met (AgencyID exists in other column)
        columnValidators.put("AgencyId", (tableName, columnName, value) -> {

            DbManager db = new DbManager();
            boolean isValid = db.columnIntValueExists("Agencies", "AgencyId", Integer.parseInt(value));
            if(isValid == false) {
                throw new SQLException("The provided agency ID does not exist in the database");
            }
            return true;

        });

        return columnValidators;

    }






}
