# Sistema de Atencion de Turnos

Aplicacion de consola en Java para administrar la atencion de clientes en una oficina de servicios: registra clientes y servicios, genera turnos y los atiende en orden de llegada (FIFO). Usa una **cola con lista enlazada propia**, un **arbol binario de busqueda (BST) propio** y persistencia en **SQLite mediante JDBC**.

Curso: Programacion II - Trabajo individual

## Estudiante

| Nombre | Carne |
|---|---|
| Christopher Guillermo Alexander Arriola Villagran | 1990-25-21997 |

**Repositorio Git:** https://github.com/alexanderarriola4-svg/PROYECTO-DE-PROGRAMACION

## Requisitos para ejecutar

- **JDK 21** (por ejemplo Eclipse Temurin 21). Verificar con `java -version`.
- **Apache Maven 3.9+** (opcional si se usa VS Code, que trae Maven integrado en la extension de Java). Verificar con `mvn -version`.
- **Visual Studio Code** con la extension **Extension Pack for Java** (Microsoft).
- Conexion a internet **solo la primera vez**, para que Maven descargue el controlador `sqlite-jdbc` (y JUnit para las pruebas).
- Opcional: `sqlite3` o **DB Browser for SQLite** para crear o revisar la base desde el script.

No se necesita instalar SQLite: el controlador JDBC lo incluye.

## Abrir y ejecutar en VS Code

1. Descomprimir `GrupoXX_AtencionTurnos.zip`.
2. En VS Code: **File -> Open Folder...** y elegir la carpeta `AtencionTurnos` (la que contiene `pom.xml`).
3. Esperar a que la extension de Java importe el proyecto Maven (barra inferior: *Java: Ready*).
4. Abrir `src/main/java/com/oficina/turnos/Main.java` y presionar **Run** sobre el metodo `main`, o ir a **Run and Debug** y elegir **Atencion de Turnos** (configuracion incluida en `.vscode/launch.json`).
5. El menu aparece en la **terminal integrada**.

> Importante: el programa debe ejecutarse con la carpeta raiz del proyecto como directorio de trabajo, porque la base se ubica con la ruta relativa `data/turnos.db`. La configuracion de `.vscode/launch.json` ya lo garantiza (`"cwd": "${workspaceFolder}"`).

### Abrir y ejecutar en NetBeans (alternativa)

1. **File -> Open Project...** y elegir la carpeta `AtencionTurnos`; NetBeans la reconoce como proyecto Maven.
2. Esperar a que descargue las dependencias (solo la primera vez, con internet).
3. Presionar **Run Project (F6)**. El menu aparece en la ventana **Output**, donde tambien se escriben las opciones.
4. Requiere NetBeans 20 o superior con el JDK 21 configurado en **Tools -> Java Platforms**.

### Ejecutar desde la terminal (alternativa)

Desde la carpeta raiz del proyecto:

```bash
mvn compile exec:java          # compila y ejecuta
mvn test                       # ejecuta las pruebas unitarias (11 pruebas)
mvn package                    # genera target/atencion-turnos.jar con dependencias
java -jar target/atencion-turnos.jar
```

En Windows, si las tildes se ven mal en la consola, ejecutar antes `chcp 65001`.

## Base de datos

- Ubicacion: `data/turnos.db` (ruta relativa, sin carpetas particulares de ninguna computadora).
- Se entrega con los datos iniciales (5 clientes y 3 servicios) y **sin turnos**.
- Tablas: `clientes`, `servicios` y `turnos`, con claves primarias y claves foraneas (`turnos.codigo_cliente -> clientes.codigo`, `turnos.codigo_servicio -> servicios.codigo`). Las claves foraneas se habilitan en cada conexion con `PRAGMA foreign_keys = ON`.
- El numero de turno usa `AUTOINCREMENT`, por lo que nunca se reutiliza.
- Un indice unico parcial impide, tambien desde la base, que un cliente tenga dos turnos `PENDIENTE`.
- Si `data/turnos.db` no existe, la aplicacion crea las tablas y carga los datos iniciales al iniciar. Si ya existe, **nunca** borra ni restablece datos.

### Crear la base desde el script

El script `sql/crear_y_cargar.sql` elimina las tablas (si existen), las crea de nuevo y carga los datos iniciales. Desde la carpeta raiz del proyecto:

**Con sqlite3 (linea de comandos):**

```bash
sqlite3 data/turnos.db < sql/crear_y_cargar.sql
```

En Windows PowerShell:

```powershell
Get-Content sql\crear_y_cargar.sql -Encoding UTF8 | sqlite3 data\turnos.db
```

**Con DB Browser for SQLite:** *New Database* -> guardar como `data/turnos.db` -> pestana *Execute SQL* -> abrir `sql/crear_y_cargar.sql` -> ejecutar (>) -> *Write Changes*.

**Sin herramientas:** borrar `data/turnos.db` y ejecutar la aplicacion; se creara una base nueva con los datos iniciales.

## Estructura del proyecto

```
AtencionTurnos/
+-- pom.xml
+-- data/turnos.db                    Base SQLite con datos iniciales
+-- sql/crear_y_cargar.sql            Script de creacion y carga
+-- docs/
|   +-- diagramas/uml_clases.png      Diagrama UML de clases (+ fuente .dot)
|   +-- diagramas/entidad_relacion.png Diagrama entidad-relacion (+ fuente .dot)
|   +-- Informe_de_comprobacion.docx  Resultado de la secuencia de comprobacion
|   +-- Guia_de_defensa.docx          Explicacion del codigo para la defensa
+-- src/
    +-- main/java/com/oficina/turnos/
    |   +-- Main.java                 Arma las piezas y arranca el menu
    |   +-- modelo/                   Cliente, Servicio, Turno, EstadoTurno, records de reporte
    |   +-- estructuras/              NodoTurno, ColaTurnos, NodoCliente, ArbolClientes
    |   +-- datos/                    ConexionSQLite, InicializadorBD, interfaces DAO e implementaciones SQLite
    |   +-- logica/                   GestorClientes, GestorServicios, GestorTurnos, GestorReportes, Validador
    |   +-- vista/                    MenuPrincipal, VistaClientes, VistaServicios, VistaTurnos, entrada y pantalla
    +-- test/java/...                 Pruebas de la cola, el BST y la secuencia completa
```

| Componente | Paquete | Responsabilidad |
|---|---|---|
| Modelo | `modelo` | Clases `Cliente`, `Servicio` y `Turno` |
| Estructuras | `estructuras` | Nodo, cola enlazada y BST propios |
| Acceso a datos | `datos` | Conexion SQLite y DAO (interfaces `ClienteDAO`, `ServicioDAO`, `TurnoDAO` + implementaciones `*DAOSQLite`) |
| Logica de negocio | `logica` | Validaciones y coordinacion BD <-> estructuras |
| Vista | `vista` | Menus, lectura de datos y presentacion |

## Decisiones de diseno principales

- **Primero la base, luego la memoria.** Cada operacion valida, guarda en SQLite y solo si eso funciona modifica la cola o el arbol. Si la base falla, se lanza una excepcion antes de tocar las estructuras, asi nunca muestran cambios no guardados.
- **Reconstruccion al iniciar.** Se leen todos los clientes para construir el BST y los turnos `PENDIENTE` ordenados por numero para reconstruir la cola.
- **Interfaces DAO.** Los gestores dependen de `ClienteDAO`, `ServicioDAO` y `TurnoDAO`, no de SQLite; las pruebas y el programa usan las implementaciones `*DAOSQLite`.
- **Colecciones de Java** (`ArrayList`, `HashMap`) solo para funciones adicionales: devolver resultados de consultas y armar los reportes (el `HashMap` asocia codigo de servicio con su nombre). No reemplazan la cola ni el BST.

## Distribucion del trabajo

Trabajo individual: el estudiante desarrollo todos los componentes (clientes y BST, turnos y cola enlazada, servicios, persistencia y reportes). El historial de commits del repositorio muestra el avance por modulo.

## Documentacion de entrega

- Diagrama UML de clases: `docs/diagramas/uml_clases.png`
- Diagrama entidad-relacion: `docs/diagramas/entidad_relacion.png`
- Informe de la secuencia de comprobacion: `docs/Informe_de_comprobacion.docx`
- Guia para la demostracion y la defensa: `docs/Guia_de_defensa.docx`
