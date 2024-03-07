module com.chaldea.visualparsing {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.slf4j;

    opens com.chaldea.visualparsing to javafx.fxml;
    exports com.chaldea.visualparsing;
    exports com.chaldea.visualparsing.grammar;
    exports com.chaldea.visualparsing.gui;
    exports com.chaldea.visualparsing.exception;
    exports com.chaldea.visualparsing.exception.grammar;
    exports com.chaldea.visualparsing.controller;
    exports com.chaldea.visualparsing.parsing;
    opens com.chaldea.visualparsing.controller to javafx.fxml;
    exports com.chaldea.visualparsing.debug;
}