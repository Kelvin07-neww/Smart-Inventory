module com.cvserbaada.smartinventory {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;
    requires mysql.connector.j;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires jbcrypt;
    requires org.slf4j;
    requires org.apache.pdfbox;

    opens com.cvserbaada.smartinventory to javafx.fxml;
    opens com.cvserbaada.smartinventory.controller to javafx.fxml;

    exports com.cvserbaada.smartinventory;
    exports com.cvserbaada.smartinventory.config;
    exports com.cvserbaada.smartinventory.database;
}
