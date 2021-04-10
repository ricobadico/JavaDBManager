package db;

import java.util.HashMap;

// A little interface that Table classes should inherit.
public interface ITableEntity {

    /**
     * Provides a list of labels to display corresponding to SQL column names
     * @return Map of column name keys to column label values
     */
    public static HashMap<String, String> GetColumnLabels() {
        return null;
    }

    /**
     * Provides custom validation to use for the given column input
     * @return Map of column name keys to ICustomValidator objects. These contain one method which takes in tablename, columnname, and value, and return true if valid.
     */
    public static HashMap<String, ICustomValidator> GetValidators() { return null; }

}
