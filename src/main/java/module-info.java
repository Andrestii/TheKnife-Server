module com.theknife {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.theknife to javafx.fxml;
    exports com.theknife;
}
