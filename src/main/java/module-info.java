module com.examplegg {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.examplegg to javafx.fxml;
    exports com.examplegg;
}
