module com.loteriascorp {
    requires javafx.controls;
    requires javafx.fxml;
    requires poi.ooxml;

    opens com.loteriascorp to javafx.fxml;
    exports com.loteriascorp;
}