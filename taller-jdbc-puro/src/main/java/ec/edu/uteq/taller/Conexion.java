package ec.edu.uteq.taller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Conexion {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/taller_db";
    private static final String USUARIO = "taller";
    private static final String CONTRASENA = "taller";

    private Conexion() { /* utilidad, no instanciable */ }

    public static Connection abrir() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}