package app;

import javafx.scene.control.Alert;

/**
 * Helper class for methods that provide general, controller-wide functionality
 */

public class ControllerHelper {

    /**
     * Creates an Alert that pops up on screen and pauses running
     * @param title Title text in top bar
     * @param header Upper header section text
     * @param message Bottom message text
     */
    public static void makeWarningAlert(String title, String header, String message){
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(message);
        a.showAndWait();
    }
}
