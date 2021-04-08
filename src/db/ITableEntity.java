package db;

import java.util.HashMap;

// A little interface that Table classes should inherit. Provides a list of labels to display corresponding to SQL column names
public interface ITableEntity {

    public static HashMap<String, String> GetColumnLabels() {
        return null;
    }

}
