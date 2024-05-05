open module com.example.shooterlab2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.google.gson;
    requires java.sql;
    requires java.persistence;
    requires org.hibernate.orm.core;
    requires java.naming;

    //opens com.example.shooterlab2 to javafx.fxml,com.google.gson;

    exports com.example.shooterlab2;
}