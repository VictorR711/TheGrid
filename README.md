# TheGrid

Aplicación de escritorio en Java (JavaFX) que se conecta por túnel SSH a la base de datos
`its5a`, descarga sus tablas a una base SQLite local y permite hacer CRUD sobre ellas.

## Integrantes

1. Leidy Miriam Morayta Perez
2. Angela Alicia Pimienta Diaz
3. Victor Adrian Romero Minaya
4. Diego Valadez Almeyda

## Funcionamiento

1. **Login**: host, usuario y contraseña del servidor SSH. Se abre el túnel.
2. **Base de datos**: usuario y contraseña de MariaDB. Se conecta a `its5a` por el túnel.
3. **Tablas**: lista de tablas de `its5a`. Al seleccionar una se guarda en SQLite (`its5a_local.db`).
4. **CRUD**: crear, consultar, actualizar y eliminar registros de la tabla descargada.

## Estructura (MVC)

| Parte | Ubicación |
|---|---|
| Modelo | `src/main/java/modelo` (túnel SSH, MariaDB y SQLite) |
| Vista | `src/main/resources/vistas` (archivos `.fxml`) |
| Controlador | `src/main/java/com/thegrid/controller` |

## Cómo ejecutarlo

Abrir el proyecto en IntelliJ y ejecutar el Main.

Las capturas están en la carpeta `Screenshots`.
