package db.PredefinedTableClasses;

import db.CustomValidator;
import db.DbManager;
import db.ITableEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Bookings implements ITableEntity {

    public static HashMap<String, String> GetColumnLabels(){

        HashMap<String, String> columnLabels = new HashMap<>();

        columnLabels.put("BookingId", "ID Number:");
        columnLabels.put("BookingDate", "Booking Date:");
        columnLabels.put("BookingNo", "Booking Number:");
        columnLabels.put("TravelerCount", "Traveler Count:");
        columnLabels.put("CustomerId", "Customer Id:");
        columnLabels.put("TripTypeId", "Trip Type:");
        columnLabels.put("Package Id", "Package Id:");

        return columnLabels;
    }

    public static HashMap<String, CustomValidator> GetValidators(){

        HashMap<String, CustomValidator> columnValidators = new HashMap<>();

        return columnValidators;
    }


    public static ArrayList<String> GetRecordNames() throws SQLException {

        ArrayList<String> recordNames = new ArrayList<>();

        // Get all records from a table
        DbManager db = new DbManager();
        ResultSet res = db.getRecords("bookings");

        // For each record..
        while(res.next()){

            // Create a blank String we'll build up into a record name
            String recordname = "";

            /**
             *  Other than the table name above, this block is the only thing in this method that can't be copied and pasted from class to class
             */
            recordname  += res.getString(1) + ": "; //
            recordname  += "Booking No " + res.getString(3); // AgentFirstName

            // Once the string is built, add it to the array
            recordNames.add(recordname);
        }

        // return the filled array
        return recordNames;

    }
}
