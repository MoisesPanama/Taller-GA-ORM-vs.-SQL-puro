package ec.edu.uteq.taller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class TallerJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TallerJpaApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(ProductoRepository repo) {
        return args -> {

            // ------------------------------------------------------------
            // 1) Medicion del findAll() con System.nanoTime()
            // ------------------------------------------------------------
            long inicio = System.nanoTime();
            List<Producto> lista1 = repo.findAll();
            long fin = System.nanoTime();
            double ms1 = (fin - inicio) / 1_000_000.0;
            System.out.printf("nanoTime : %d filas en %.3f ms %n",
                    lista1.size(), ms1);

            // ------------------------------------------------------------
            // 2) Medicion del findAll() con Spring StopWatch
            // ------------------------------------------------------------
            StopWatch sw = new StopWatch("findAll-jpa");
            sw.start("SELECT * FROM productos (JPA)");
            List<Producto> lista2 = repo.findAll();
            sw.stop();
            System.out.printf("StopWatch: %d filas en %.3f ms %n",
                    lista2.size(),
                    sw.getTotalTimeNanos() / 1_000_000.0);
            System.out.println(sw.prettyPrint());

            // ------------------------------------------------------------
            // 3) Crear un producto nuevo
            // ------------------------------------------------------------
            Producto nuevo = new Producto("Producto de prueba JPA",
                    new BigDecimal("99.99"), 5);
            Producto guardado = repo.save(nuevo);
            System.out.println("Creado con id = " + guardado.getId());

            // ------------------------------------------------------------
            // 4) Eliminarlo para dejar la base como estaba
            // ------------------------------------------------------------
            repo.deleteById(guardado.getId());
            System.out.println("Eliminado: true");
        };
    }
}