package ec.edu.uteq.taller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoRepositorioInseguro {

    // --- NO USAR EN PRODUCCION! Solo demuestra la vulnerabilidad ---
    public List<Producto> buscarPorNombreInseguro(String nombreBuscado)
            throws SQLException {
        String sql = "SELECT id, nombre, precio, stock FROM productos "
                + "WHERE nombre = '" + nombreBuscado + "'";
        List<Producto> resultado = new ArrayList<>();
        try (Connection con = Conexion.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                resultado.add(new Producto(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getBigDecimal("precio"),
                        rs.getInt("stock")
                ));
            }
        }
        return resultado;
    }
}