package modelo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Conexión de SOLO LECTURA a la base remota "its5a", pasando por el túnel SSH.
 * Usa el driver oficial de MariaDB (org.mariadb.jdbc).
 */
public class RemoteDatabase {

    public static final String SCHEMA = "its5a";

    private Connection connection;

    /** Conecta usando el puerto local del túnel (127.0.0.1:localPort). */
    public void connect(int localPort, String user, String password) throws ModelException {
        // Nos conectamos a 127.0.0.1 porque el túnel SSH redirige ese puerto local
        // hacia MariaDB en el servidor.
        String url = "jdbc:mariadb://127.0.0.1:" + localPort + "/" + SCHEMA;
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new ModelException("No se pudo conectar a la base " + SCHEMA + ": " + e.getMessage(), e);
        }
    }

    /** Lista las tablas del esquema its5a (equivale a ver el árbol de DBeaver). */
    public List<String> listTables() throws ModelException {
        List<String> tablas = new ArrayList<>();
        try {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tablas.add(rs.getString("TABLE_NAME"));
                }
            }
        } catch (SQLException e) {
            throw new ModelException("Error al listar tablas: " + e.getMessage(), e);
        }
        return tablas;
    }

    /** Lee TODOS los registros de una tabla y los devuelve en un TableData. */
    public TableData fetchTable(String table) throws ModelException {
        // Seguridad: el nombre de tabla debe existir realmente en el esquema,
        // así evitamos inyección SQL (los nombres no se pueden parametrizar).
        if (!listTables().contains(table)) {
            throw new ModelException("La tabla '" + table + "' no existe en " + SCHEMA);
        }

        try {
            String q = connection.getMetaData().getIdentifierQuoteString().trim();
            String sql = "SELECT * FROM " + q + table + q;

            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();

                List<String> columnas = new ArrayList<>();
                List<String> tipos = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                    columnas.add(md.getColumnLabel(i));
                    tipos.add(toSqliteType(md.getColumnType(i)));
                }

                TableData data = new TableData(table, columnas, tipos);
                while (rs.next()) {
                    List<Object> fila = new ArrayList<>();
                    for (int i = 1; i <= n; i++) {
                        fila.add(normalize(rs.getObject(i)));
                    }
                    data.addRow(fila);
                }
                return data;
            }
        } catch (SQLException e) {
            throw new ModelException("Error al leer la tabla " + table + ": " + e.getMessage(), e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // al cerrar no hay nada útil que hacer con el error
        }
        connection = null;
    }

    /** Traduce el tipo JDBC del servidor a una afinidad de tipo de SQLite. */
    private String toSqliteType(int jdbcType) {
        switch (jdbcType) {
            case Types.TINYINT: case Types.SMALLINT: case Types.INTEGER:
            case Types.BIGINT: case Types.BIT: case Types.BOOLEAN:
                return "INTEGER";
            case Types.FLOAT: case Types.REAL: case Types.DOUBLE:
                return "REAL";
            case Types.DECIMAL: case Types.NUMERIC:
                return "NUMERIC";
            case Types.BINARY: case Types.VARBINARY:
            case Types.LONGVARBINARY: case Types.BLOB:
                return "BLOB";
            default:
                return "TEXT"; // VARCHAR, CHAR, DATE, TIMESTAMP, etc.
        }
    }

    /** SQLite no tiene tipos fecha/decimal: se guardan como texto. */
    private Object normalize(Object value) {
        if (value instanceof java.util.Date      // java.sql.Date/Time/Timestamp
                || value instanceof java.time.temporal.TemporalAccessor
                || value instanceof BigDecimal) {
            return value.toString();
        }
        return value;
    }
}
