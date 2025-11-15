module com.theknife {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.theknife to javafx.fxml;
    exports com.theknife;
}
