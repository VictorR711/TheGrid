package modelo;

/**
 * Excepción única del modelo. Envuelve JSchException y SQLException para que
 * el controlador no dependa de las librerías internas (SSH / JDBC).
 */
public class ModelException extends Exception {

    public ModelException(String mensaje) {
        super(mensaje);
    }

    public ModelException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
