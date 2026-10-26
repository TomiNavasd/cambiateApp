# CambiateApp

**Integrantes:** Tomás Navas y Lourdes Barrientos
**Materia:** Aplicaciones Móviles, UNAJ. Trabajo Integrador Módulo 2.

CambiateApp es un armario digital para Android. El usuario carga sus prendas con una foto, las organiza por categoría y ocasión, y la app le sugiere outfits armados con su propia ropa. Un agente de IA (Claude) analiza la foto de una prenda nueva para precargar sus datos y sugiere combinaciones de outfits. La app también avisa cada día qué ponerse según el clima de mañana.

## Cómo probar la app (APK)

**Descarga del APK:** `<link-al-apk>`

### Instalación

**En un celular Android:**
1. Descargá el archivo `CambiateApp.apk` desde el link.
2. Abrilo. Si Android lo pide, permití "Instalar apps desconocidas" para la app desde la que lo abriste (Chrome, Archivos, etc.).
3. Instalá y abrí **CambiateApp**.

**En un emulador de Android Studio:**
1. Usá un emulador con **Google Play** (el ícono de Play Store aparece en la lista de dispositivos).
2. Arrastrá el archivo `CambiateApp.apk` sobre la ventana del emulador.
3. Para el login con Google, agregá antes una cuenta de Google al emulador (Ajustes → Contraseñas y cuentas).
4. La ubicación del emulador por defecto no es real: cambiala en los tres puntos (`…`) → **Location**.

**Requisitos:** Android `<minSdk>` o superior, y conexión a internet (ver "Modo sin conexión" para lo que funciona sin ella). El selector de idioma requiere Android 13 o superior.

### Cuenta de prueba

Para no tener que cargar datos, ya hay una cuenta con prendas y outfits:

- **Usuario:** `luliiibar@gmail.com`
- **Contraseña:** `112233`

También se puede crear una cuenta propia con **Registrate**, o entrar con **Continuar con Google**. Si el login con Google no funcionara en el dispositivo de prueba, el de email y contraseña funciona siempre.

### Qué conviene probar

| Qué | Dónde |
|---|---|
| Registro, login, recuperar contraseña y login con Google | Pantalla inicial |
| Armario con búsqueda y filtro por categoría | Pestaña **Armario** |
| Detalle, edición y eliminación de una prenda | Tocar una prenda |
| **Escaneo con IA:** al agregar una prenda con foto, la IA completa nombre, descripción, categoría, colores y ocasiones | Botón **+** en el armario |
| Outfits sugeridos por la IA (de 6 a 10), guardar y marcar favorito | Pestaña **Outfits** |
| Mapa con la ubicación y el clima de mañana | Pestaña **Mapa** |
| Aviso diario del clima y botón para probarlo ahora | Armario → menú ⋮ → Ajustes → Recordatorios del clima |
| Cambio de idioma (español, inglés, portugués) y tema claro/oscuro | Armario → menú ⋮ → Ajustes |
| Funcionamiento sin internet | Activar el modo avión y navegar |

Al usarla pide permisos de **ubicación** (mapa y clima), de **notificaciones** (aviso diario) y de **cámara o galería** (fotos). Hay que aceptarlos para ver esas funciones.

## Funcionalidades

| Requisito del enunciado | Cómo está resuelto |
|---|---|
| Autenticación con Firebase Auth | Email y contraseña, login con Google y recuperación de contraseña |
| Listado de datos del usuario | Armario con búsqueda y filtro por categoría |
| Detalle de un elemento | Pantalla de detalle de cada prenda |
| Crear y editar en Firestore, asociado al usuario | Alta y edición de prendas y outfits; cada documento guarda el `userId` |
| Ubicación y mapa | Mapa (OpenStreetMap / OpenTopoMap) con la ubicación actual, usada también para el clima |
| Hardware extra | Cámara y galería para la foto de la prenda |
| Notificaciones | Aviso local diario con el clima de mañana (WorkManager) |
| Opcional: API externa con Retrofit | Open-Meteo (clima) |
| Opcional: IA | Claude analiza la foto de la prenda y sugiere outfits |

Además: tema claro y oscuro, tres idiomas (español, inglés y portugués) y funcionamiento sin internet.

## Modo sin conexión

- Las listas de prendas y outfits se leen de la copia local de Firestore, y los cambios hechos sin internet quedan en cola y se sincronizan solos.
- Un banner avisa cuando no hay conexión.
- El último pronóstico del clima se guarda y se muestra con la fecha de su última actualización (vence a las 12 horas).
- Los outfits caen a reglas simples si la IA no responde.
- Subir una foto nueva y el escaneo con IA necesitan internet: una prenda nueva requiere conexión por la foto, pero se pueden editar las existentes.

## Tecnologías

Kotlin, Jetpack Compose, Material 3, Hilt, Navigation Compose, Firebase Authentication y Firestore, Credential Manager (login con Google), Cloudinary (fotos), Retrofit con Gson (clima), OkHttp (API de Claude), DataStore, WorkManager, FusedLocationProvider, osmdroid y Coil. Tests con JUnit 4 y `kotlinx-coroutines-test`.

## Arquitectura

Clean Architecture en un solo módulo (`app`), separada por capas en paquetes de `com.example.cambiateapp`:

```
domain/         Kotlin puro: modelos, errores, contratos de repositorio y casos de uso
data/           Firestore, Retrofit, DataStore, WorkManager, notificaciones y cliente de IA
presentation/   Pantallas por carpeta (Route + Screen), ViewModels, estados y eventos
di/             Módulos de Hilt
```

Las dependencias apuntan hacia el dominio: el dominio no conoce a Android, a los datos ni a las pantallas, y las pantallas no acceden directamente a los datos. Se puede comprobar con estos comandos, que no deben devolver nada:

```
git grep -nE "import (android|androidx|com\.google)" -- app/src/main/java/com/example/cambiateapp/domain
git grep -nE "import com\.example\.cambiateapp\.(data|presentation|di|ui)" -- app/src/main/java/com/example/cambiateapp/domain
git grep -nE "import com\.example\.cambiateapp\.data" -- app/src/main/java/com/example/cambiateapp/presentation
```

Cada pantalla se divide en `XRoute` (con estado, conoce al ViewModel) y `XScreen` (sin estado, con `@Preview`). Los errores del dominio viajan como `DomainException` y la capa de presentación los traduce a textos.

Se eligió un único módulo porque el proyecto lo desarrollan dos personas y es de tamaño acotado; la separación en capas y la regla de dependencias se mantienen y se verifican con los comandos de arriba.

## Seguridad

- Las reglas de Firestore están en [`firestore.rules`](firestore.rules): cada usuario solo puede leer, crear, modificar y borrar sus propias prendas y outfits, se valida el formato de los datos y todo lo demás está denegado.
- `google-services.json` y la clave de la IA (`local.properties`) no están en el repositorio.

## APIs y servicios externos

| Servicio | Para qué | Clave |
|---|---|---|
| Firebase Authentication y Firestore | Usuarios y datos | `google-services.json` |
| Google Credential Manager | Login con Google | SHA-1 en Firebase |
| Open-Meteo | Pronóstico del clima de mañana | No necesita |
| OpenTopoMap (datos de OpenStreetMap) | Imágenes del mapa | No necesita |
| Cloudinary | Alojar las fotos de las prendas | Upload preset |
| API de Anthropic (Claude Haiku 4.5) | Escaneo de prendas y sugerencia de outfits | `ANTHROPIC_API_KEY` |

## Límites conocidos

- La clave de Anthropic viaja dentro del APK (en `BuildConfig`) y se podría extraer. En un proyecto real iría detrás de un backend propio.
- Una escritura en cola sin internet que las reglas rechacen después no le avisa al usuario.
- Las prendas cargadas antes de las etiquetas con IA se etiquetan recién cuando se editan.
- El nombre y la descripción de una prenda quedan en el idioma en que se escanearon.
- El login con Google solo funciona en equipos cuyo SHA-1 esté registrado en Firebase.
- OpenTopoMap y OpenStreetMap tienen políticas de uso: para producción habría que contratar un proveedor de mapas.

## Tests

Hay más de 100 tests unitarios de casos de uso, mappers, repositorios y ViewModels, con fakes escritos a mano:

```
.\gradlew.bat :app:testDebugUnitTest
```


