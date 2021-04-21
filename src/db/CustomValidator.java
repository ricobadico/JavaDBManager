package db;

import java.sql.SQLException;
import java.util.HashMap;

public abstract class CustomValidator {

    /** Little interface for checking the validity of one value based on its intended column.
     * Designed to be extra validation checks above standard sql data requirements.
     * All validity checks should take these params, but for comparison-based validity,
     * it is possible to query other database data, or use "inputCOLUMNNAME" (eg "inputAgentId")
     * to get at the Control for another column's current input in-app.
     */

    public abstract boolean checkValidity(HashMap <String, String> args) throws SQLException;

}
