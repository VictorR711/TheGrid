module TheGrid {

    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;               // JDBC
    requires com.jcraft.jsch;        // túnel SSH
    requires org.mariadb.jdbc;       // driver MariaDB
    requires org.xerial.sqlitejdbc;  // driver SQLite

    opens com.thegrid.controller to javafx.fxml;

    exports com.thegrid;
}
