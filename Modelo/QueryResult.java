package modelo;

import java.util.List;

/**
 * Resultado de ejecutar SQL libre (equivalente a lo que mostraba el Treeview
 * y la barra de estado en la versión de Python).
 *  - Si es un SELECT/PRAGMA: tiene columnas y filas.
 *  - Si es INSERT/UPDATE/DELETE/CREATE: solo tiene filas afectadas.
 */
public class QueryResult {

    private final List<String> columns;
    private final List<List<Object>> rows;
    private final int affectedRows;
    private final boolean resultSet;

    private QueryResult(List<String> columns, List<List<Object>> rows, int affectedRows, boolean resultSet) {
        this.columns = columns;
        this.rows = rows;
        this.affectedRows = affectedRows;
        this.resultSet = resultSet;
    }

    public static QueryResult ofRows(List<String> columns, List<List<Object>> rows) {
        return new QueryResult(columns, rows, rows.size(), true);
    }

    public static QueryResult ofUpdate(int affectedRows) {
        return new QueryResult(List.of(), List.of(), affectedRows, false);
    }

    /** true si la sentencia devolvió filas (SELECT, PRAGMA...). */
    public boolean hasResultSet() { return resultSet; }
    public List<String> getColumns() { return columns; }
    public List<List<Object>> getRows() { return rows; }
    public int getAffectedRows() { return affectedRows; }
}
