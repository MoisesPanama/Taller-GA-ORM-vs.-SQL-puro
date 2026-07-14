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

## Prerrequisitos

Para ejecutar este proyecto en una máquina nueva necesitas instalar tres cosas. Ninguna viene incluida en el repositorio.

### 1. Java 21 LTS

Descarga Temurin 21 desde `https://adoptium.net/` e instálalo con el asistente gráfico (marca la opción de agregarlo al PATH durante la instalación si el instalador la ofrece). Verifica con:
```
java -version
```

### 2. Apache Maven 3.9+

Windows no trae Maven preinstalado y no siempre está disponible por gestores de paquetes como `winget`. El método que funciona de forma confiable es instalarlo manualmente:

1. Descarga el "Binary zip archive" desde `https://maven.apache.org/download.cgi`.
2. Descomprime el `.zip` en una ruta simple (evita rutas con espacios o dentro de carpetas sincronizadas por OneDrive si es posible; puede causar inconsistencias). Ejemplo: `C:\apache-maven`.
3. Agrega la subcarpeta `bin` al PATH del usuario (Panel de control → Variables de entorno → edita `Path` del usuario → **Nueva** → pega la ruta completa hasta `\bin`).
4. **Cierra y vuelve a abrir la terminal por completo** (no solo la pestaña; el cambio de PATH no se aplica a terminales ya abiertas).
5. Verifica con:
```
mvn -version
```
Debe reportar la versión de Maven y detectar automáticamente tu Java 21 instalado.

### 3. PostgreSQL 16+

1. Descarga el instalador desde `https://www.postgresql.org/download/windows/` (te redirige al portal de EDB). Elige la versión 16.x o superior para Windows x86-64.
2. Ejecútalo como administrador. En el asistente usa:
   - Componentes: deja los 4 marcados (incluye **Command Line Tools**, necesario para `psql`).
   - Password del superusuario `postgres`: la que prefieras, tenla a mano.
   - Puerto: `5432` (por defecto).
3. Abre **SQL Shell (psql)** desde el menú Inicio. Presiona Enter en las primeras 4 preguntas (para aceptar los valores por defecto) y escribe la contraseña del superusuario cuando la pida.
4. Una vez en el prompt `postgres=#`, crea el rol y la base:
```sql
CREATE ROLE taller LOGIN PASSWORD 'taller';
CREATE DATABASE taller_db OWNER taller;
\c taller_db taller
```
5. Cuando pida contraseña del usuario `taller`, escribe `taller`. El prompt cambiará a `taller_db=>`. Ahí crea la tabla y siembra los 100 registros:
```sql
DROP TABLE IF EXISTS productos;

CREATE TABLE productos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    precio NUMERIC(10,2) NOT NULL,
    stock INTEGER NOT NULL
);

INSERT INTO productos (nombre, precio, stock)
SELECT
    'Producto ' || i,
    ROUND((RANDOM() * 500 + 10)::NUMERIC, 2),
    (RANDOM() * 100)::INTEGER
FROM generate_series(1, 100) AS s(i);

SELECT COUNT(*) AS total FROM productos;
```
El `SELECT COUNT` final debe devolver **100**.

> Las credenciales (`taller` / `taller`, `localhost:5432/taller_db`) están fijas en `Conexion.java` y en la configuración del módulo JPA. Para que el programa se conecte, la base debe existir exactamente con ese nombre de usuario, contraseña y puerto.

## Instrucciones de ejecución

### Módulo JDBC puro

```
cd taller-jdbc-puro
mvn compile
mvn org.codehaus.mojo:exec-maven-plugin:3.1.0:java "-Dexec.mainClass=ec.edu.uteq.taller.Main"
```

El `pom.xml` de este módulo no declara el plugin `exec-maven-plugin` por defecto, así que se invoca con sus coordenadas completas la primera vez (Maven lo descarga automáticamente).

La consola debe mostrar la demo de inyección SQL (100 filas en la versión insegura, 0 en la segura), las dos mediciones de tiempo del listado, y la creación/eliminación de un producto de prueba.

### Módulo Spring Data JPA

```
cd taller-jpa
mvn spring-boot:run
```

La misma secuencia de mediciones (`nanoTime` y `StopWatch`) se ejecuta automáticamente al arrancar, dentro del `CommandLineRunner` de `TallerJpaApplication`.

> **Nota sobre el primer tiempo medido en JPA:** el `nanoTime` de la primera consulta suele salir más alto que el de JDBC puro por el arranque en frío de Hibernate (inicialización del `EntityManager`, compilación de la query JPQL a SQL, y calentamiento del pool de conexiones HikariCP). El `StopWatch`, medido en la segunda llamada a `findAll()`, refleja mejor el rendimiento en caliente.

## Tabla comparativa final

| Criterio | JDBC puro con PreparedStatement | Spring Data JPA + Hibernate |
|---|---|---|
| **Líneas de código** (repositorio + conexión / entidad) | 87 líneas (`Conexion.java` 14 + `ProductoRepositorioJdbc.java` 73) | 50 líneas (`Producto.java` 45 + `ProductoRepository.java` 5) |
| **Tiempo del listado** (100 filas) | 23–49 ms (nanoTime) / 8–45 ms (StopWatch), medido en corridas distintas sobre dos máquinas diferentes — ver nota abajo | 80–144 ms (nanoTime, arranque en frío) / 5–11 ms (StopWatch, en caliente), medido en dos máquinas distintas |
| **Facilidad de mantenimiento** | SQL en cadenas Java; el mapeo `ResultSet` → objeto es código repetitivo (boilerplate) que hay que ajustar a mano ante cualquier cambio de esquema. | Hibernate mapea vía anotaciones; una interfaz de 2-3 líneas (`extends JpaRepository`) resuelve el CRUD completo. |
| **Prevención de SQL Injection** | Se previene siempre que se use `PreparedStatement` con marcadores `?` y `setXxx()`; comprobado con el ataque `' OR '1'='1'` (100 filas en la versión insegura → 0 en la segura). | Se previene por defecto: Hibernate genera siempre `PreparedStatement` parametrizado, incluso en consultas derivadas como `findByNombre`. |

> **Nota sobre la variabilidad de los tiempos:** los rangos de ambos módulos corresponden a corridas independientes en dos equipos distintos, no a un único run. Los tiempos de listado dependen fuertemente del hardware, la carga del sistema en ese momento, y si es la primera consulta de la conexión (arranque en frío) o una posterior (en caliente tras el JIT warm-up de la JVM / calentamiento de Hibernate y HikariCP), tal como advierte la guía del taller. Para un reporte formal, corre la medición tres veces en tu propio equipo y reporta la mediana, indicando el hardware usado.

## Flujo de Git/GitHub utilizado

Se trabajó sobre la rama `feat/jdbc-puro`, con un commit por unidad lógica de trabajo y Pull Request hacia `main` al finalizar cada bloque:

1. `chore`: `.gitignore` para Maven e IDEs.
2. `feat`: proyecto Maven `taller-jdbc-puro` con `pom.xml` configurado.
3. `feat`: modelo `Producto` y utilidad `Conexion`; eliminación de clases de ejemplo del archetype.
4. `feat`: repositorio JDBC (CRUD + búsqueda segura) y repositorio inseguro para la demo de inyección SQL.
5. `feat`: `Main` con demo de SQL injection y mediciones de rendimiento.
6. `feat`: módulo Spring Data JPA equivalente para la comparativa (entidad, repositorio, mediciones).
7. `fix`: corrección de contenido vacío en `ProductoRepositorioJdbc.java` (no se había guardado en un commit anterior).
8. `docs`: README con estructura, prerrequisitos, instrucciones de ejecución y tabla comparativa.

## Autor

Panamá Murillo Moisés Antonio — Aplicaciones Web, Ingeniería de Software, UTEQ 2026-2027