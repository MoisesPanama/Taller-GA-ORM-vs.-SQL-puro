package ec.edu.uteq.taller;

import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        ProductoRepositorioJdbc seguro = new ProductoRepositorioJdbc();
        ProductoRepositorioInseguro inseguro = new ProductoRepositorioInseguro();

        // ------------------------------------------------------------
        // 1) Demo: inyeccion SQL sobre el metodo INSEGURO
        // ------------------------------------------------------------
        String ataque = "' OR '1'='1";
        System.out.println("=== VERSION INSEGURA (concatenacion) ===");
        List<Producto> filasInsegura =
                inseguro.buscarPorNombreInseguro(ataque);
        System.out.println("Filas devueltas al atacante: "
                + filasInsegura.size()
                + " (deberia ser 0, pero devuelve TODAS)");

        // ------------------------------------------------------------
        // 2) Demo: el mismo ataque sobre el metodo SEGURO
        // ------------------------------------------------------------
        System.out.println("=== VERSION SEGURA (PreparedStatement) ===");
        List<Producto> filasSegura = seguro.buscarPorNombreSeguro(ataque);
        System.out.println("Filas devueltas al atacante: "
                + filasSegura.size()
                + " (correcto: 0)");

        // ------------------------------------------------------------
        // 3) Medicion del listar() con System.nanoTime()
        // ------------------------------------------------------------
        long inicio = System.nanoTime();
        List<Producto> lista1 = seguro.listar();
        long fin = System.nanoTime();
        double ms1 = (fin - inicio) / 1_000_000.0;
        System.out.printf("nanoTime : %d filas en %.3f ms %n",
                lista1.size(), ms1);

        // ------------------------------------------------------------
        // 4) Medicion del listar() con Spring StopWatch
        // ------------------------------------------------------------
        StopWatch sw = new StopWatch("listar-jdbc");
        sw.start("SELECT * FROM productos");
        List<Producto> lista2 = seguro.listar();
        sw.stop();
        System.out.printf("StopWatch: %d filas en %.3f ms %n",
                lista2.size(),
                sw.getTotalTimeNanos() / 1_000_000.0);
        System.out.println(sw.prettyPrint());

        // ------------------------------------------------------------
        // 5) Crear un producto nuevo
        // ------------------------------------------------------------
        Long nuevoId = seguro.crear(
                "Producto de prueba", new BigDecimal("99.99"), 5);
        System.out.println("Creado con id = " + nuevoId);

        // ------------------------------------------------------------
        // 6) Eliminarlo para dejar la base como estaba
        // ------------------------------------------------------------
        boolean borrado = seguro.eliminar(nuevoId);
        System.out.println("Eliminado: " + borrado);
    }
}