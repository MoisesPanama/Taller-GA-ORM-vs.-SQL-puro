# Taller GA — ORM vs. SQL puro (JDBC vs. Spring Data JPA)

Implementación comparativa de un CRUD de productos usando **JDBC puro con `PreparedStatement`** y **Spring Data JPA + Hibernate**, ambos contra la misma base de datos PostgreSQL.

Aplicaciones Web — Ingeniería de Software — UTEQ 2026-2027
GA — Taller en clase | Formativa | Individual

## Qué se construyó

El mismo CRUD mínimo implementado dos veces, una por enfoque de acceso a datos:

- **Listar** todos los productos, midiendo el tiempo con `System.nanoTime()` y con `StopWatch` de Spring.
- **Crear** un producto nuevo y recuperar su id autogenerado.
- **Eliminar** un producto por id.
- **Demostración de inyección SQL**: un método vulnerable por concatenación de cadenas (`ProductoRepositorioInseguro`) contrastado con la versión parametrizada y segura (`ProductoRepositorioJdbc.buscarPorNombreSeguro`).

## Estructura del repositorio

```
Taller-GA-ORM-vs.-SQL-puro/
├── taller-jdbc-puro/                  # JDBC puro con PreparedStatement
│   └── src/main/java/ec/edu/uteq/taller/
│       ├── Producto.java              # record del modelo
│       ├── Conexion.java              # utilidad DriverManager
│       ├── ProductoRepositorioJdbc.java     # CRUD + busqueda segura
│       ├── ProductoRepositorioInseguro.java # demo vulnerable (solo pedagogico)
│       └── Main.java                  # benchmark + demo SQL injection
├── taller-jpa/                        # Spring Data JPA + Hibernate
│   └── src/main/java/ec/edu/uteq/taller/
│       ├── Producto.java              # entidad @Entity
│       ├── ProductoRepository.java    # interfaz extends JpaRepository
│       └── TallerJpaApplication.java  # CommandLineRunner con las mismas mediciones
├── .gitignore
└── README.md
```

## Requisitos previos

- Java 21 LTS (probado también con Java 25, sin problemas de compatibilidad)
- Maven 3.9+
- PostgreSQL 16+ (probado con PostgreSQL 18) corriendo en `localhost:5432`
- Base `taller_db` con usuario `taller` / contraseña `taller`, y tabla `productos` sembrada con 100 registros (ver script SQL en la guía del taller)

## Instrucciones de ejecución

### Módulo JDBC puro

1. Entrar a la carpeta del proyecto:
```
   cd taller-jdbc-puro
```
2. Compilar:
```
   mvn compile
```
3. Ejecutar la clase `Main` (el `pom.xml` no declara el plugin `exec-maven-plugin` por defecto, así que se invoca con las coordenadas completas):
```
   mvn org.codehaus.mojo:exec-maven-plugin:3.1.0:java "-Dexec.mainClass=ec.edu.uteq.taller.Main"
```

La consola debe mostrar la demo de inyección SQL (100 filas en la versión insegura, 0 en la segura), las dos mediciones de tiempo del listado, y la creación/eliminación de un producto de prueba.

### Módulo Spring Data JPA

1. Entrar a la carpeta del proyecto:
```
   cd taller-jpa
```
2. Ejecutar con Spring Boot:
```
   mvn spring-boot:run
```
3. La misma secuencia de mediciones (`nanoTime` y `StopWatch`) se ejecuta automáticamente al arrancar, dentro del `CommandLineRunner` de `TallerJpaApplication`.

> **Nota sobre el primer tiempo medido en JPA:** el `nanoTime` de la primera consulta suele salir más alto que el de JDBC puro por el arranque en frío de Hibernate (inicialización del `EntityManager`, compilación de la query JPQL a SQL, y calentamiento del pool de conexiones HikariCP). El `StopWatch`, medido en la segunda llamada a `findAll()`, refleja mejor el rendimiento en caliente.

## Tabla comparativa final

| Criterio | JDBC puro con PreparedStatement | Spring Data JPA + Hibernate |
|---|---|---|
| **Líneas de código** (repositorio + conexión / entidad) | 87 líneas (`Conexion.java` 14 + `ProductoRepositorioJdbc.java` 73) | 50 líneas (`Producto.java` 45 + `ProductoRepository.java` 5) |
| **Tiempo del listado** (100 filas) | ~23 ms (nanoTime) / ~23 ms (StopWatch) | ~80 ms (nanoTime, arranque en frío) / ~5 ms (StopWatch, en caliente) |
| **Facilidad de mantenimiento** | SQL en cadenas Java; el mapeo `ResultSet` → objeto es código repetitivo (boilerplate) que hay que ajustar a mano ante cualquier cambio de esquema. | Hibernate mapea vía anotaciones; una interfaz de 2-3 líneas (`extends JpaRepository`) resuelve el CRUD completo. |
| **Prevención de SQL Injection** | Se previene siempre que se use `PreparedStatement` con marcadores `?` y `setXxx()`; comprobado con el ataque `' OR '1'='1'` (100 filas en la versión insegura → 0 en la segura). | Se previene por defecto: Hibernate genera siempre `PreparedStatement` parametrizado, incluso en consultas derivadas como `findByNombre`. |

## Flujo de Git/GitHub utilizado

Se trabajó sobre la rama `feat/jdbc-puro`, con un commit por unidad lógica de trabajo y Pull Request hacia `main` al finalizar cada bloque:

1. `chore`: `.gitignore` para Maven e IDEs.
2. `feat`: proyecto Maven `taller-jdbc-puro` con `pom.xml` configurado.
3. `feat`: modelo `Producto` y utilidad `Conexion`; eliminación de clases de ejemplo del archetype.
4. `feat`: repositorio JDBC (CRUD + búsqueda segura) y repositorio inseguro para la demo de inyección SQL.
5. `feat`: `Main` con demo de SQL injection y mediciones de rendimiento.
6. `feat`: módulo Spring Data JPA equivalente para la comparativa (entidad, repositorio, mediciones).
7. `fix`: corrección de contenido vacío en `ProductoRepositorioJdbc.java` (no se había guardado en un commit anterior).

## Autor

Panamá Murillo Moisés Antonio — Aplicaciones Web, Ingeniería de Software, UTEQ 2026-2027