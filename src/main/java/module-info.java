module com.theknife {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;


    opens com.theknife to javafx.fxml;
    exports com.theknife;
}
