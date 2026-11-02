package modelo;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Túnel SSH con port forwarding local (equivalente a la pestaña "SSH" de
 * DBeaver). Todo lo que se envíe a 127.0.0.1:puertoLocal viaja cifrado hasta
 * el host y sale hacia remoteHost:remotePort (el servidor de base de datos).
 */
public class SshTunnel {

    private Session session;

    /**
     * Abre el túnel y devuelve el puerto local asignado automáticamente.
     */
    public int open(String host, int sshPort, String user, String password,
                    String remoteHost, int remotePort) throws ModelException {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, sshPort);
            session.setPassword(password);
            // Para práctica de clase se omite la verificación de la llave del host.
            // En producción debería validarse con known_hosts.
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(10_000); // timeout de 10 s

            // lport = 0 -> JSch elige un puerto local libre
            return session.setPortForwardingL(0, remoteHost, remotePort);
        } catch (JSchException e) {
            close();
            throw new ModelException("No se pudo abrir el túnel SSH: " + e.getMessage(), e);
        }
    }

    public boolean isOpen() {
        return session != null && session.isConnected();
    }

    public void close() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        session = null;
    }
}
