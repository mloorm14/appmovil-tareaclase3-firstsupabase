# Lista de alumnos con Supabase

Aplicación Android realizada como práctica de clase. Consulta la tabla `alumnos` de Supabase y muestra los estudiantes ordenados por nombre, con su paralelo, correo y teléfono. Desde esa pantalla se puede consultar la tabla `materias` por semestre.

**Estudiante:** Loor Medranda Marlon

## Tecnologías

- Kotlin y Android Views
- Supabase PostgREST
- Corrutinas de Kotlin

## Configuración y ejecución

1. Abre el proyecto en Android Studio y sincroniza Gradle.
2. Agrega a `local.properties` la URL del proyecto y su clave pública de Supabase, conservando la configuración del SDK que Android Studio haya generado:

   ```properties
   SUPABASE_URL=https://tu-proyecto.supabase.co
   SUPABASE_KEY=tu-clave-publica
   ```

3. Comprueba que existan las tablas `alumnos` y `materias`, y que sus permisos y políticas permitan la consulta con esa clave. La consulta de materias espera las columnas `id`, `nombre` y `nivel` (número de semestre del 1 al 10).
4. Ejecuta la aplicación en un dispositivo Android con conexión a internet. Pulsa «Ver materias por semestre», selecciona un semestre y luego una materia para mostrar la lista de alumnos. Como en la presentación de clase, la consulta muestra todos los alumnos: no hay un filtro por materia.

No coloques claves secretas o de servicio en la aplicación ni publiques `local.properties`.

## Captura de pantalla

La aplicación muestra los alumnos obtenidos desde Supabase:

![Lista de alumnos](img/captura.png)
