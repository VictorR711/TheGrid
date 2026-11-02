package modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Base SQLite local. Aquí se guardan las tablas "descargadas" y se hace el CRUD.
 *
 * Para identificar cada registro en UPDATE/DELETE se usa el rowid de SQLite,
 * que existe en toda tabla normal aunque la tabla remota no tuviera llave primaria.
 */
public class LocalDatabase {

    private final String path;
    private Connection connection;

    public LocalDatabase(String path) {
        this.path = path;
    }

    // ------------------------------------------------------------------
    // Conexión
    // ------------------------------------------------------------------

    /** Abre o crea el archivo .db. */
    public void open() throws ModelException {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch (SQLException e) {
            throw new ModelException("No se pudo abrir la base local: " + e.getMessage(), e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }
        connection = null;
    }

    // ------------------------------------------------------------------
    // "Descarga" de una tabla remota
    // ------------------------------------------------------------------

    /**
     * Crea (reemplazando si ya existía) la tabla local y copia todos los
     * registros en una sola transacción: o se guarda todo o no se guarda nada.
     */
    public void saveTable(TableData data) throws ModelException {
        String tabla = quote(data.getName());
        try {
            connection.setAutoCommit(false);

            try (Statement st = connection.createStatement()) {
                st.executeUpdate("DROP TABLE IF EXISTS " + tabla);

                StringJoiner defs = new StringJoiner(", ");
                for (int i = 0; i < data.getColumns().size(); i++) {
                    defs.add(quote(data.getColumns().get(i)) + " " + data.getColumnTypes().get(i));
                }
                st.executeUpdate("CREATE TABLE " + tabla + " (" + defs + ")");
            }

            StringJoiner cols = new StringJoiner(", ");
            StringJoiner marks = new StringJoiner(", ");
            for (String c : data.getColumns()) {
                cols.add(quote(c));
                marks.add("?");
            }
            String insert = "INSERT INTO " + tabla + " (" + cols + ") VALUES (" + marks + ")";

            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                for (List<Object> fila : data.getRows()) {
                    bind(ps, fila);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            connection.commit();
        } catch (SQLException e) {
            rollbackQuietly();
            throw new ModelException("Error al guardar la tabla local: " + e.getMessage(), e);
        } finally {
            restoreAutoCommit();
        }
    }

    // ------------------------------------------------------------------
    // CRUD
    // ------------------------------------------------------------------

    /** Lista las tablas que ya existen en la base local. */
    public List<String> listTables() throws ModelException {
        List<String> tablas = new ArrayList<>();
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                tablas.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new ModelException("Error al listar tablas locales: " + e.getMessage(), e);
        }
        return tablas;
    }

    /** READ: devuelve todos los registros de una tabla local (con su rowid). */
    public TableData selectAll(String table) throws ModelException {
        requireTable(table);
        String sql = "SELECT rowid AS _rid_, * FROM " + quote(table);

        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();

            List<String> columnas = new ArrayList<>();
            List<String> tipos = new ArrayList<>();
            for (int i = 2; i <= n; i++) {          // la columna 1 es el rowid
                columnas.add(md.getColumnLabel(i));
                tipos.add(md.getColumnTypeName(i));
            }

            TableData data = new TableData(table, columnas, tipos);
            while (rs.next()) {
                List<Object> fila = new ArrayList<>();
                for (int i = 2; i <= n; i++) {
                    fila.add(rs.getObject(i));
                }
                data.addRow(rs.getLong(1), fila);
            }
            return data;
        } catch (SQLException e) {
            throw new ModelException("Error al consultar " + table + ": " + e.getMessage(), e);
        }
    }

    /** CREATE: inserta un registro. values = columna -> valor. */
    public void insert(String table, Map<String, Object> values) throws ModelException {
        validateColumns(table, values);
        try {
            String sql;
            if (values.isEmpty()) {
                sql = "INSERT INTO " + quote(table) + " DEFAULT VALUES";
            } else {
                StringJoiner cols = new StringJoiner(", ");
                StringJoiner marks = new StringJoiner(", ");
                for (String c : values.keySet()) {
                    cols.add(quote(c));
                    marks.add("?");
                }
                sql = "INSERT INTO " + quote(table) + " (" + cols + ") VALUES (" + marks + ")";
            }
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                bind(ps, new ArrayList<>(values.values()));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ModelException("Error al insertar: " + e.getMessage(), e);
        }
    }

    /** UPDATE: modifica el registro identificado por su rowid. */
    public int update(String table, long rowId, Map<String, Object> values) throws ModelException {
        if (values.isEmpty()) {
            return 0;
        }
        validateColumns(table, values);

        StringJoiner sets = new StringJoiner(", ");
        for (String c : values.keySet()) {
            sets.add(quote(c) + " = ?");
        }
        String sql = "UPDATE " + quote(table) + " SET " + sets + " WHERE rowid = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            List<Object> params = new ArrayList<>(values.values());
            params.add(rowId);
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new ModelException("Error al actualizar: " + e.getMessage(), e);
        }
    }

    /** DELETE: elimina el registro identificado por su rowid. */
    public int delete(String table, long rowId) throws ModelException {
        requireTable(table);
        String sql = "DELETE FROM " + quote(table) + " WHERE rowid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, rowId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new ModelException("Error al eliminar: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // SQL libre
    // ------------------------------------------------------------------

    /**
     * Ejecuta una o varias sentencias separadas por ';'. Si alguna falla se
     * hace rollback de todo. Devuelve el resultado de la última sentencia.
     * Limitación: un ';' dentro de un texto también divide.
     */
    public QueryResult executeSql(String sqlText) throws ModelException {
        QueryResult ultimo = null;
        try {
            connection.setAutoCommit(false);

            for (String sentencia : sqlText.split(";")) {
                sentencia = sentencia.trim();
                if (sentencia.isEmpty()) {
                    continue;
                }
                try (Statement st = connection.createStatement()) {
                    // execute() devuelve true si hay ResultSet
                    if (st.execute(sentencia)) {
                        try (ResultSet rs = st.getResultSet()) {
                            ultimo = readResultSet(rs);
                        }
                    } else {
                        ultimo = QueryResult.ofUpdate(st.getUpdateCount());
                    }
                }
            }
            connection.commit();
            return ultimo;
        } catch (SQLException e) {
            rollbackQuietly();
            throw new ModelException("Error SQL: " + e.getMessage(), e);
        } finally {
            restoreAutoCommit();
        }
    }

    private QueryResult readResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int n = md.getColumnCount();

        List<String> columnas = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            columnas.add(md.getColumnLabel(i));
        }
        List<List<Object>> filas = new ArrayList<>();
        while (rs.next()) {
            List<Object> fila = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                fila.add(rs.getObject(i));
            }
            filas.add(fila);
        }
        return QueryResult.ofRows(columnas, filas);
    }

    // ------------------------------------------------------------------
    // Utilidades privadas
    // ------------------------------------------------------------------

    /** Obtiene los nombres de columna reales de la tabla (PRAGMA table_info). */
    private List<String> getColumnNames(String table) throws ModelException {
        List<String> cols = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + quote(table) + ")")) {
            while (rs.next()) {
                cols.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new ModelException("Error al leer la estructura de " + table + ": " + e.getMessage(), e);
        }
        return cols;
    }

    private void requireTable(String table) throws ModelException {
        if (getColumnNames(table).isEmpty()) {
            throw new ModelException("La tabla local '" + table + "' no existe. Descárgala primero.");
        }
    }

    /** Los nombres de columna no se pueden parametrizar: se validan contra la tabla real. */
    private void validateColumns(String table, Map<String, Object> values) throws ModelException {
        List<String> reales = getColumnNames(table);
        if (reales.isEmpty()) {
            throw new ModelException("La tabla local '" + table + "' no existe. Descárgala primero.");
        }
        for (String c : values.keySet()) {
            if (!reales.contains(c)) {
                throw new ModelException("La columna '" + c + "' no existe en " + table);
            }
        }
    }

    /** Asigna los parámetros de un PreparedStatement manejando null. */
    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object v = params.get(i);
            if (v == null) {
                ps.setNull(i + 1, Types.NULL);
            } else {
                ps.setObject(i + 1, v);
            }
        }
    }

    /** Escapa un identificador (tabla/columna) con comillas dobles. */
    private String quote(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void restoreAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}
