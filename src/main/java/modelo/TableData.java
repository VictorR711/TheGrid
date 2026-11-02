package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el contenido de una tabla: nombre, columnas, tipos y filas.
 * Es un simple contenedor de datos (no conoce ni la vista ni la BD).
 *
 * rowIds guarda el rowid de SQLite de cada fila (solo se llena al leer de la
 * base local). Se usa para identificar el registro en UPDATE y DELETE.
 */
public class TableData {

    private final String name;
    private final List<String> columns;
    private final List<String> columnTypes;
    private final List<List<Object>> rows = new ArrayList<>();
    private final List<Long> rowIds = new ArrayList<>();

    public TableData(String name, List<String> columns, List<String> columnTypes) {
        this.name = name;
        this.columns = columns;
        this.columnTypes = columnTypes;
    }

    public void addRow(List<Object> row) {
        rows.add(row);
    }

    public void addRow(long rowId, List<Object> row) {
        rowIds.add(rowId);
        rows.add(row);
    }

    public String getName() { return name; }
    public List<String> getColumns() { return columns; }
    public List<String> getColumnTypes() { return columnTypes; }
    public List<List<Object>> getRows() { return rows; }
    public List<Long> getRowIds() { return rowIds; }
}
