package db;

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

    // Map of preferred labels to replace each SQL column name with
    public static HashMap<String, String> GetColumnLabels(){

        HashMap<String, String> ColumnLabels = new HashMap<>();

        ColumnLabels.put("AgentId", "ID Number:");
        ColumnLabels.put("AgtFirstName", "First Name:");
        ColumnLabels.put("AgtMiddleInitial", "Middle Initial:");
        ColumnLabels.put("AgtLastName", "Last Name:");
        ColumnLabels.put("AgtBusPhone", "Business Phone #:");
        ColumnLabels.put("AgtEmail", "Email:");
        ColumnLabels.put("AgtPosition", "Position:");
        ColumnLabels.put("AgencyId", "Agency Id:");

        return ColumnLabels;
    }



}
