# 🌱 Mini-Invernadero IoT — App Android (Kotlin + XML + Firebase)

Aplicación móvil para **monitorear y manipular** un mini-invernadero IoT. Continúa el prototipo
de la Unidad 1 (ESP32 + sensor de temperatura + servomotor que abre una ventana de ventilación)
y lo conecta con una app Android a través de **Firebase Realtime Database**.

- **Lenguaje:** Kotlin · **Pantallas:** layouts XML (una Activity por pantalla, `findViewById`)
- **Base de datos:** Firebase Realtime Database (usuarios, estado del invernadero y acciones)
- **Comunicación:** Wi-Fi / Internet sobre HTTPS (TLS)

---

## 1. Problemática

Las plantas de un invernadero se dañan si la temperatura sube demasiado y nadie ventila a tiempo.
La app permite **ver la temperatura en tiempo real desde cualquier lugar**, **abrir o cerrar la
ventana de ventilación** (servomotor) y revisar el **historial y las estadísticas** de lo ocurrido.
Si la temperatura supera **28 °C**, la ventana se abre automáticamente.

**Dispositivos a conectar:** el nodo sensor (temperatura), el nodo actuador (ventana/servomotor)
y al menos dos teléfonos Android con la app. Mientras no se integre el ESP32 físico, el sensor se
**simula** desde la pantalla *Manipular Circuito* (cuentas con permiso de Operador).

## 2. Pantallas y funcionalidades

| Pantalla | Qué hace |
|---|---|
| **Login** | Inicia sesión con RUT y contraseña, validados contra los usuarios guardados en Firebase |
| **Crear Cuenta** | Registra RUT, nombre, apellido, correo, cargo y contraseña en la colección `usuarios` |
| **Menú** | Muestra el nombre del usuario logueado y redirige a las demás pantallas; cierre de sesión con confirmación |
| **Manipular Circuito** | Temperatura y estado de la ventana en tiempo real; botones **Abrir / Cerrar** y simulador del sensor |
| **Ver Acciones** | Listado en tiempo real: fecha, hora, usuario (RUT), acción y temperatura |
| **Estadísticas** | Aperturas, cierres, alertas de temperatura alta, lecturas, temperatura máxima y promedio |
| **Ver Perfil** | Todos los datos del usuario logueado (la contraseña se muestra protegida) |

**Permisos:** toda cuenta nueva es **Monitor** (solo observa). El permiso **Operador** (puede manipular
el invernadero) lo asigna el administrador desde la consola de Firebase.

**Interacción entre dos dispositivos Android:** el Operador (dispositivo A) abre la ventana o publica
una lectura, y el Monitor (dispositivo B) lo ve al instante en *Manipular Circuito*, *Ver Acciones*
y *Estadísticas*, gracias a la sincronización en tiempo real de Firebase.

## 3. Justificación de herramientas y decisiones

| Decisión | Elección | Justificación |
|---|---|---|
| IDE y lenguaje | Android Studio + Kotlin | Lenguaje oficial recomendado por Google para Android |
| Interfaz | Layouts XML + Material Components | Estándar de diseño de Android; separa la interfaz (XML) de la lógica (Kotlin) |
| Navegación | Una Activity por pantalla, datos del usuario enviados por `Intent` | Estructura simple y clara, igual a la trabajada en clases |
| Comunicación | Wi-Fi / Internet (HTTPS) vía Firebase | **Distancia:** sin límite (por Internet), a diferencia de Bluetooth (~10 m). **Consumo:** moderado; Firebase mantiene una sola conexión eficiente. **Estabilidad:** reconexión automática y caché local |
| Base de datos y login | Firebase Realtime Database | Usuarios y datos de sensores en la nube, sincronizados en tiempo real entre dispositivos; el ESP32 también puede escribir en ella (biblioteca *Firebase ESP Client*) |
| Sesión técnica | Firebase Authentication anónima | Invisible para el usuario; permite que las reglas exijan una sesión válida (`auth != null`) y la base no quede abierta a Internet |

## 4. Seguridad (ISO/IEC 27400 — seguridad en IoT)

| Medida | Dónde está implementada |
|---|---|
| **Cifrado de credenciales:** la contraseña nunca se guarda; se guarda un hash **PBKDF2** (10.000 iteraciones) con una **sal aleatoria** distinta por usuario | `Seguridad.kt`, `CrearCuenta.kt` |
| **Verificación segura:** comparación del hash en tiempo constante; mensaje genérico de error (no revela si el RUT existe) | `Seguridad.kt`, `Login.kt` |
| **Protección contra fuerza bruta:** bloqueo de 30 s tras 5 intentos fallidos | `Login.kt` |
| **Contraseñas robustas:** mínimo 8 caracteres con letras y números; doble ingreso | `CrearCuenta.kt` |
| **Validación de datos:** RUT con dígito verificador, correo válido y campos obligatorios en la app; tipos y rangos validados también en el servidor | `Seguridad.kt`, `database.rules.json` |
| **Control de acceso por permisos:** Monitor solo observa; Operador puede manipular | `ManipularCircuito.kt` |
| **Mínimo privilegio:** las cuentas nuevas solo pueden crearse como Monitor; el usuario no puede cambiar su permiso ni editar cuentas existentes | `database.rules.json` |
| **Base de datos cerrada:** solo se puede leer/escribir con sesión de Firebase; no se puede descargar la lista completa de usuarios | `BaseDatos.kt`, `database.rules.json` |
| **Integridad del historial:** las acciones solo se pueden agregar (nunca editar ni borrar) y deben pertenecer a un RUT registrado | `database.rules.json` |
| **Conexión segura:** todo el tráfico es HTTPS/TLS; tráfico sin cifrar prohibido | Firebase SDK + `usesCleartextTraffic="false"` |
| **Sin datos sensibles en respaldos ni en el repositorio:** `allowBackup="false"`; `google-services.json` excluido de Git; la contraseña no viaja entre pantallas | `AndroidManifest.xml`, `.gitignore`, `Usuario.kt` |

**Limitación conocida y mejora futura:** como el login se valida contra la propia base de datos
(requisito de la actividad), el permiso de Operador se controla en la app. En una versión
productiva se migraría a Firebase Authentication con correo y contraseña para que el servidor
también verifique el permiso de cada escritura.

## 5. Estructura del proyecto

```
app/src/main/
├── AndroidManifest.xml
├── java/cl/invernadero/app/          ← PROGRAMACIÓN (Kotlin)
│   ├── Login.kt                      # Pantalla Login (lanzadora)
│   ├── CrearCuenta.kt                # Pantalla Crear Cuenta
│   ├── MenuPrincipal.kt              # Pantalla Menú
│   ├── ManipularCircuito.kt          # Pantalla Manipular Circuito
│   ├── Acciones.kt + AccionesAdapter.kt  # Pantalla Ver Acciones (lista)
│   ├── Estadisticas.kt               # Pantalla Estadísticas
│   ├── Perfil.kt                     # Pantalla Ver Perfil
│   ├── Usuario.kt, Accion.kt         # Modelos de datos
│   ├── BaseDatos.kt                  # Acceso a Firebase
│   ├── Seguridad.kt                  # Hash de contraseñas y validaciones
│   └── Utilidades.kt                 # Funciones de apoyo
└── res/                              ← PANTALLAS (XML)
    ├── layout/activity_*.xml         # Una por pantalla + item_accion.xml
    ├── drawable/borde_campo.xml      # Borde de los campos
    └── values/colores_app.xml        # Colores de la app
```

**Estructura en Firebase Realtime Database:**
```
usuarios/{rut}            -> rut, nombre, apellido, correo, cargo, permiso, hash, sal, creado
invernadero/estado        -> temperatura, ventanaAbierta, actualizado
invernadero/acciones/{id} -> fecha, rut, tipo, accion, temperatura
```

---

## 6. Puesta en marcha (paso a paso)

### 6.1 Crear el proyecto en Android Studio
1. **New Project → Empty Views Activity**.
2. **Name:** `Invernadero` · **Package name:** `cl.invernadero.app` · **Language:** Kotlin · **Minimum SDK:** API 24.
3. En el panel *Project*, **borra** `MainActivity.kt` y `res/layout/activity_main.xml`.
4. Copia desde este proyecto, respetando las rutas:
   - `app/src/main/java/cl/invernadero/app/` → todos los archivos `.kt`.
   - `app/src/main/res/` → las carpetas `layout` y `drawable`, y el archivo `values/colores_app.xml`.
   - `app/src/main/AndroidManifest.xml` → reemplaza el de tu proyecto.
5. En `res/values/themes.xml` cambia `Theme.Material3.DayNight.NoActionBar` por
   `Theme.Material3.Light.NoActionBar`, y borra la carpeta `res/values-night` (la app usa fondo blanco).

### 6.2 Configurar Firebase
1. Entra a <https://console.firebase.google.com> → **Agregar proyecto**.
2. **Authentication → Comenzar → Anónimo → Habilitar** (sesión técnica invisible para el usuario).
3. **Realtime Database → Crear base de datos →** elige la ubicación → **Modo bloqueado**.
4. En **Realtime Database → Reglas**, pega el contenido de `database.rules.json` y **Publicar**.
5. **Configuración del proyecto → Agregar app → Android** con el paquete `cl.invernadero.app`.
6. Descarga `google-services.json` **(después de crear la base de datos)** y colócalo en la carpeta `app/`.

### 6.3 Dependencias (Gradle)
En el `build.gradle.kts` **de la raíz del proyecto**, dentro de `plugins { }`:
```kotlin
id("com.google.gms.google-services") version "4.5.0" apply false
```
En `app/build.gradle.kts`, dentro de `plugins { }`:
```kotlin
id("com.google.gms.google-services")
```
y dentro de `dependencies { }`:
```kotlin
implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
implementation("com.google.firebase:firebase-database")
implementation("com.google.firebase:firebase-auth")
implementation("androidx.recyclerview:recyclerview:1.4.0")
```
Luego **Sync Now**. Si Android Studio sugiere versiones más nuevas, puedes aceptarlas.

### 6.4 Crear los usuarios de prueba
1. Ejecuta la app y crea dos cuentas desde **Crear Cuenta** (con RUT válidos).
2. En la consola de Firebase → **Realtime Database → Datos → usuarios →** (RUT del operador) →
   cambia `permiso` de `monitor` a `operador`.
3. Inicia sesión con esa cuenta: en *Manipular Circuito* se habilitan los botones y el simulador.

### 6.5 Demostración con dos dispositivos
- **Dispositivo A (celular):** sesión de **Operador** → *Manipular Circuito*.
- **Dispositivo B (emulador de Android Studio):** sesión de **Monitor** → *Manipular Circuito*.
1. En A, mueve el simulador a 32 °C y presiona **Publicar Lectura del Sensor**.
2. En B, la temperatura cambia al instante y la ventana aparece **ABIERTA** (apertura automática).
3. En A, presiona **Cerrar**. B lo ve al momento.
4. En B, revisa **Ver Acciones** y **Estadísticas**: aparecen las acciones de A con su RUT.
5. En B, se comprueba que los botones están deshabilitados (control de acceso).

### 6.6 Mockups
Abre cualquier archivo de `res/layout/` y usa la pestaña **Design** (o **Split**): cada pantalla se
ve con datos de ejemplo (atributos `tools:`). Para exportarlas, usa el botón de cámara del editor
de diseño (*Save Screenshot*).

---

## 7. Pruebas de funcionamiento

| # | Prueba | Resultado esperado |
|---|---|---|
| 1 | Crear cuenta con datos válidos | "Cuenta creada" y aparece en `usuarios/{rut}` con `hash` y `sal` (sin contraseña visible) |
| 2 | Crear cuenta con RUT inválido (dígito verificador incorrecto) | "RUT inválido" |
| 3 | Crear cuenta con RUT ya registrado | "Ya existe una cuenta con ese RUT." |
| 4 | Contraseñas distintas o contraseña `abc` | Rechazo con el mensaje correspondiente |
| 5 | Login con RUT y contraseña correctos | Entra al Menú con el nombre del usuario |
| 6 | Login con contraseña incorrecta | "¡ Error de RUT y/o Contraseña !" |
| 7 | 5 intentos fallidos seguidos | Bloqueo de 30 segundos |
| 8 | Operador publica 32 °C con la ventana cerrada | Ventana ABIERTA automáticamente y acción "Temperatura alta" registrada |
| 9 | Operador presiona Abrir / Cerrar | Estado cambia en ambos dispositivos y se registra la acción |
| 10 | Monitor entra a Manipular Circuito | Ve los datos, pero los botones están deshabilitados |
| 11 | Ver Acciones / Estadísticas | Datos actualizados en tiempo real |
| 12 | Ver Perfil | Muestra todos los datos; contraseña "protegida" |
| 13 | Cerrar sesión | Pide confirmación y vuelve al Login; la flecha atrás no regresa al Menú |
| 14 | Intentar cambiar `permiso` a `operador` desde la app (Simulador de reglas de Firebase) | **Denegado** por las reglas |
| 15 | Modo avión | Mensaje de error, sin cierre inesperado de la app |
