package ec.edu.uteq.taller;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    java.util.List<Producto> findByNombre(String nombre);
}