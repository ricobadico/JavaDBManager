package db;

import java.sql.SQLException;

public interface ICustomValidator {

    /** Little interface for checking the validity of one value based on its intended column.
     * Designed to be extra validation checks above standard sql data requirements.
     * All validity checks should take these params, but for comparison-based validity,
     * it is possible to query other database data, or use "inputCOLUMNNAME" (eg "inputAgentId")
     * to get at the Control for another column's current input in-app.
     */
    boolean checkValidity(String tableName, String columnName, String value) throws SQLException;

}
