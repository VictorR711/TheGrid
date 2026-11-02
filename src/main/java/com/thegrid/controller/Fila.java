package com.thegrid.controller;

import java.util.List;

/**
 * Un renglón de la tabla tal como se muestra en un TableView.
 * Guarda junto a los valores el rowid de SQLite, que es lo que pide el
 * modelo para actualizar o eliminar ese registro.
 */
public class Fila {

    private final long rowId;
    private final List<Object> valores;

    public Fila(long rowId, List<Object> valores) {
        this.rowId = rowId;
        this.valores = valores;
    }

    public long getRowId() { return rowId; }
    public List<Object> getValores() { return valores; }
}
