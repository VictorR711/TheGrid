package modelo;

import java.util.List;
import java.util.Map;

/**
 * Fachada del MODELO. Es la ÚNICA clase que debe conocer el controlador.
 * Coordina el túnel SSH, la base remota (its5a) y la base SQLite local.
 *
 * Flujo de uso desde el controlador:
 *   1. login(host, usuario, contraseña)
 *   2. getRemoteTables()
 *   3. downloadTable(nombre)          -> remota -> SQLite local
 *   4. insertRecord / updateRecord / deleteRecord / getLocalTable
 *   5. logout()
 *
 * El modelo no sabe nada de JavaFX ni de pantallas: solo recibe y devuelve datos.
 */
public class DatabaseModel {

    private static final int SSH_PORT = 22;
    private static final String REMOTE_DB_HOST = "127.0.0.1"; // visto desde el servidor SSH
    private static final int REMOTE_DB_PORT = 3306;           // MariaDB (puerto por defecto)
    private static final String LOCAL_DB_FILE = "its5a_local.db";

    private final SshTunnel tunnel = new SshTunnel();
    private final RemoteDatabase remote = new RemoteDatabase();
    private final LocalDatabase local = new LocalDatabase(LOCAL_DB_FILE);

    private boolean loggedIn = false;

    // ------------------------- Sesión -------------------------

    /**
     * Abre el túnel SSH y conecta a its5a. Se usan las mismas credenciales
     * para SSH y para la base de datos; si en tu servidor son distintas,
     * agrega parámetros dbUser/dbPassword.
     */
    public void login(String host, String user, String password) throws ModelException {
        if (host == null || host.isBlank() || user == null || user.isBlank()) {
            throw new ModelException("Host y usuario son obligatorios.");
        }
        try {
            int localPort = tunnel.open(host.trim(), SSH_PORT, user, password,
                    REMOTE_DB_HOST, REMOTE_DB_PORT);
            remote.connect(localPort, user, password);
            local.open();
            loggedIn = true;
        } catch (ModelException e) {
            logout(); // no dejar recursos a medias
            throw e;
        }
    }

    public void logout() {
        remote.close();
        tunnel.close();
        local.close();
        loggedIn = false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    // ------------------- Base remota (its5a) -------------------

    public List<String> getRemoteTables() throws ModelException {
        requireLogin();
        return remote.listTables();
    }

    /**
     * "Descarga" una tabla: la lee del host y la guarda en SQLite local.
     * Devuelve lo que quedó guardado localmente (ya con rowid para el CRUD).
     */
    public TableData downloadTable(String table) throws ModelException {
        requireLogin();
        TableData remota = remote.fetchTable(table);
        local.saveTable(remota);
        return local.selectAll(table);
    }

    // ------------------- Base local (CRUD) -------------------

    public List<String> getLocalTables() throws ModelException {
        requireLogin();
        return local.listTables();
    }

    public TableData getLocalTable(String table) throws ModelException {
        requireLogin();
        return local.selectAll(table);
    }

    public void insertRecord(String table, Map<String, Object> values) throws ModelException {
        requireLogin();
        local.insert(table, values);
    }

    public int updateRecord(String table, long rowId, Map<String, Object> values) throws ModelException {
        requireLogin();
        return local.update(table, rowId, values);
    }

    public int deleteRecord(String table, long rowId) throws ModelException {
        requireLogin();
        return local.delete(table, rowId);
    }

    /** Equivalente al editor SQL del Mini DBeaver (opcional para la tarea). */
    public QueryResult executeSql(String sql) throws ModelException {
        requireLogin();
        return local.executeSql(sql);
    }

    private void requireLogin() throws ModelException {
        if (!loggedIn) {
            throw new ModelException("Primero debes iniciar sesión.");
        }
    }
}
