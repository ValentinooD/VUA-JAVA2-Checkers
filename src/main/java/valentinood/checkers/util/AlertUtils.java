package valentinood.checkers.util;

import javafx.scene.control.Alert;

public final class AlertUtils {
    private AlertUtils() {
    }




    public static void error(String title, String header) {
        error(title, header, null);
    }

    public static void error(String title, String header, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);

        if (ex != null) alert.setContentText(ex.toString());

        alert.show();
    }

    public static void information(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
