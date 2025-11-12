# 📱 Contexto Completo de la Aplicación Points

## 📋 Tabla de Contenidos

1. [Descripción General](#descripción-general)
2. [Arquitectura del Proyecto](#arquitectura-del-proyecto)
3. [Modelos de Datos](#modelos-de-datos)
4. [Sistema de Autenticación y Roles](#sistema-de-autenticación-y-roles)
5. [Pantallas y Navegación](#pantallas-y-navegación)
6. [ViewModels y Lógica de Negocio](#viewmodels-y-lógica-de-negocio)
7. [Repositorios y Servicios](#repositorios-y-servicios)
8. [Componentes UI](#componentes-ui)
9. [Integraciones Externas](#integraciones-externas)
10. [Configuración y Dependencias](#configuración-y-dependencias)
11. [Flujos de Usuario](#flujos-de-usuario)
12. [Utilidades y Helpers](#utilidades-y-helpers)
13. [Estructura de Archivos](#estructura-de-archivos)

---

## 🎯 Descripción General

**Points** es una aplicación Android moderna desarrollada con **Jetpack Compose** que permite a los usuarios gestionar y descubrir **Puntos de Interés (POIs)**, reportar **incidentes** y gestionar **eventos** en su ciudad.

### Características Principales

- 🗺️ **Mapas Interactivos**: Visualización de POIs e incidentes en Google Maps
- 🔍 **Búsqueda Avanzada**: Filtros por categoría, distancia y características
- 📍 **Gestión de POIs**: Crear, editar y administrar puntos de interés
- ⚠️ **Sistema de Incidentes**: Reportar y gestionar incidentes urbanos
- 📅 **Gestión de Eventos**: Crear y visualizar eventos con soporte para eventos recurrentes
- ⭐ **Sistema de Calificaciones**: Calificar y revisar lugares
- 🧭 **Navegación**: Integración con Google Maps para direcciones
- 📸 **Imágenes**: Subir y visualizar fotos de POIs, eventos e incidentes
- 📍 **Ubicación en Tiempo Real**: Detección automática de ubicación
- 👥 **Sistema de Roles**: Administradores, Moderadores y Ciudadanos

---

## 🏗️ Arquitectura del Proyecto

### Patrón Arquitectónico

La aplicación sigue el patrón **MVVM (Model-View-ViewModel)** con las siguientes capas:

```
┌─────────────────────────────────────┐
│         UI Layer (Compose)          │
│  - Screens                          │
│  - Components                       │
│  - Navigation                       │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      ViewModel Layer                │
│  - ViewModels                       │
│  - UIState                          │
│  - Business Logic                   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Repository Layer               │
│  - Repositories                     │
│  - Data Sources                     │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Data Layer                     │
│  - Firebase (Firestore, Storage)    │
│  - Google Maps API                 │
│  - Local Services                  │
└─────────────────────────────────────┘
```

### Tecnologías Principales

- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose
- **Arquitectura**: MVVM
- **Navegación**: Jetpack Navigation Compose
- **Backend**: Firebase (Firestore, Storage, Auth)
- **Mapas**: Google Maps Android API
- **Carga de Imágenes**: Coil
- **Animaciones**: Lottie
- **Variables de Entorno**: dotenv-kotlin

---

## 📊 Modelos de Datos

### 1. PointOfInterest (POI)

**Ubicación**: `app/src/main/java/com/example/points/models/PointOfInterest.kt`

Representa un punto de interés en el mapa.

```kotlin
data class PointOfInterest(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: CategoriaPOI = CategoriaPOI.COMIDA,
    val ubicacion: Ubicacion = Ubicacion(),
    val direccion: String = "",
    val telefono: String? = null,
    val email: String? = null,
    val sitioWeb: String? = null,
    val horarios: List<Horario> = emptyList(),
    val imagenes: List<String> = emptyList(),
    val calificacion: Double = 0.0,
    val totalCalificaciones: Int = 0,
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaActualizacion: Timestamp = Timestamp.now(),
    val estado: EstadoPOI = EstadoPOI.PENDIENTE,
    val usuarioId: String = "",
    val moderadorId: String? = null,
    val fechaModeracion: Timestamp? = null,
    val comentariosModeracion: String? = null,
    val caracteristicas: List<CaracteristicaPOI> = emptyList(),
    val precio: RangoPrecio? = null,
    val accesibilidad: Boolean = false,
    val estacionamiento: Boolean = false,
    val wifi: Boolean = false
)
```

**Categorías de POI**:
- COMIDA, ENTRETENIMIENTO, CULTURA, DEPORTE, SALUD, EDUCACION
- TRANSPORTE, SERVICIOS, TURISMO, RECARGA_ELECTRICA
- PARQUES, SHOPPING, OTRO

**Estados de POI**:
- PENDIENTE → EN_REVISION → APROBADO / RECHAZADO
- SUSPENDIDO (para casos especiales)

**Características**:
- Accesibilidad, estacionamiento, WiFi, mascotas, terraza, etc.

### 2. Incident

**Ubicación**: `app/src/main/java/com/example/points/models/Incident.kt`

Representa un incidente reportado por un usuario.

```kotlin
data class Incident(
    val id: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    val fotoUrl: String? = null,
    val videoUrl: String? = null,
    val ubicacion: Ubicacion = Ubicacion(),
    val fechaHora: Timestamp = Timestamp.now(),
    val estado: EstadoIncidente = EstadoIncidente.PENDIENTE,
    val usuarioId: String = ""
)
```

**Tipos de Incidente**:
- INSEGURIDAD, ACCIDENTE_TRANSITO, INCENDIO, INUNDACION
- VANDALISMO, SERVICIO_PUBLICO, OTRO

**Estados de Incidente**:
- PENDIENTE → EN_REVISION → CONFIRMADO / RECHAZADO → RESUELTO

### 3. Event

**Ubicación**: `app/src/main/java/com/example/points/models/Event.kt`

Representa un evento en la ciudad.

```kotlin
data class Event(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: CategoriaEvento = CategoriaEvento.CULTURAL,
    val ubicacion: Ubicacion = Ubicacion(),
    val direccion: String = "",
    val fechaInicio: Timestamp = Timestamp.now(),
    val fechaFin: Timestamp = Timestamp.now(),
    val horaInicio: String = "",
    val horaFin: String = "",
    val esRecurrente: Boolean = false,
    val frecuenciaRecurrencia: FrecuenciaRecurrencia? = null,
    val fechaFinRecurrencia: Timestamp? = null,
    val imagenes: List<String> = emptyList(),
    val organizador: String = "",
    val contacto: ContactoEvento = ContactoEvento(),
    val precio: PrecioEvento = PrecioEvento(),
    val capacidad: Int? = null,
    val inscripciones: Int = 0,
    val estado: EstadoEvento = EstadoEvento.PENDIENTE,
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaActualizacion: Timestamp = Timestamp.now(),
    val usuarioId: String = "",
    val moderadorId: String? = null,
    val fechaModeracion: Timestamp? = null,
    val comentariosModeracion: String? = null,
    val caracteristicas: List<CaracteristicaEvento> = emptyList(),
    val etiquetas: List<String> = emptyList(),
    val sitioWeb: String? = null,
    val redesSociales: RedesSociales = RedesSociales(),
    val esGratuito: Boolean = true,
    val requiereInscripcion: Boolean = false,
    val edadMinima: Int? = null,
    val edadMaxima: Int? = null,
    val accesibilidad: Boolean = false,
    val estacionamiento: Boolean = false,
    val transportePublico: Boolean = false,
    val cancelado: Boolean = false,
    val motivoCancelacion: String? = null
)
```

**Categorías de Evento**:
- CULTURAL, DEPORTIVO, MUSICAL, EDUCATIVO, GASTRONOMICO
- TECNOLOGICO, ARTISTICO, COMERCIAL, RELIGIOSO, COMUNITARIO
- FESTIVAL, CONFERENCIA, TALLER, EXPOSICION, FERIA, OTRO

**Estados de Evento**:
- PENDIENTE → EN_REVISION → APROBADO / RECHAZADO
- CANCELADO, FINALIZADO

**Recurrencia**:
- Soporta eventos recurrentes (DIARIO, SEMANAL, MENSUAL, ANUAL)

### 4. User

**Ubicación**: `app/src/main/java/com/example/points/models/User.kt`

```kotlin
data class User(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val tipo: TipoUsuario = TipoUsuario.CIUDADANO,
    val notificaciones: Boolean = true,
    val photoUrl: String? = null,
    val telefono: String = ""
)
```

**Tipos de Usuario**:
- CIUDADANO: Usuario regular que puede crear POIs, reportar incidentes y ver eventos
- MODERADOR: Puede moderar POIs y eventos, gestionar incidentes
- ADMINISTRADOR: Acceso completo al sistema, gestión de usuarios

### 5. Ubicacion

Modelo compartido para ubicaciones geográficas:

```kotlin
data class Ubicacion(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val direccion: String = ""
)
```

---

## 🔐 Sistema de Autenticación y Roles

### Autenticación

**Implementación**: Firebase Authentication

**Pantallas de Autenticación**:
- `LoginScreen`: Inicio de sesión con email/password
- `RegisterScreen`: Registro de nuevos usuarios
- `ForgotPasswordScreen`: Recuperación de contraseña

**Ubicación**: `app/src/main/java/com/example/points/auth/`

### Roles y Permisos

#### CIUDADANO
- ✅ Ver POIs aprobados
- ✅ Crear nuevos POIs (pendientes de moderación)
- ✅ Reportar incidentes
- ✅ Ver eventos aprobados
- ✅ Ver perfil propio
- ❌ Moderar contenido
- ❌ Gestionar usuarios

#### MODERADOR
- ✅ Todas las funciones de CIUDADANO
- ✅ Moderar POIs (aprobar/rechazar)
- ✅ Moderar eventos (aprobar/rechazar)
- ✅ Gestionar incidentes
- ✅ Ver dashboard de administración
- ❌ Gestionar usuarios
- ❌ Eliminar contenido permanentemente

#### ADMINISTRADOR
- ✅ Todas las funciones de MODERADOR
- ✅ Gestionar usuarios (crear, editar, eliminar, cambiar roles)
- ✅ Eliminar contenido permanentemente
- ✅ Acceso a analíticas del sistema
- ✅ Configuración del sistema

### Flujo de Autenticación

```
Login → Verificar tipo de usuario → Redirigir:
  - ADMINISTRADOR → AdminHomeScreen
  - MODERADOR → AdminHomeScreen
  - CIUDADANO → ClientHomeScreen
```

---

## 🗺️ Pantallas y Navegación

### Sistema de Navegación

**Archivo Principal**: `app/src/main/java/com/example/points/AppNavigation.kt`

**Rutas Definidas**: `app/src/main/java/com/example/points/constants/AppRoutes.kt`

### Pantallas de Autenticación

| Ruta | Pantalla | Descripción |
|------|----------|-------------|
| `login` | LoginScreen | Inicio de sesión |
| `register` | RegisterScreen | Registro de usuario |
| `forgot_password` | ForgotPasswordScreen | Recuperación de contraseña |

### Pantallas de Cliente (CIUDADANO)

| Ruta | Pantalla | Descripción |
|------|----------|-------------|
| `client_home` | ClientHomeScreen | Pantalla principal del cliente |
| `poi_map` | POIScreen | Mapa de POIs |
| `poi_search` | POISearchScreen | Búsqueda de POIs |
| `poi_detail/{poiId}` | POIDetailScreen | Detalles de un POI |
| `poi_submission` | POISubmissionScreen | Crear nuevo POI |
| `incidents` | IncidentsScreen | Lista de incidentes |
| `incidents_map` | IncidentsMapScreen | Mapa de incidentes |
| `incident_detail/{incidentId}` | IncidentDetailScreen | Detalles de incidente |
| `create_incident` | CreateIncidentScreen | Reportar incidente |
| `events` | EventsScreen | Lista de eventos |
| `event_schedule` | EventScheduleScreen | Calendario de eventos |
| `profile` | ProfileScreen | Perfil del usuario |
| `edit_profile` | EditProfileScreen | Editar perfil |

### Pantallas de Administración

| Ruta | Pantalla | Descripción |
|------|----------|-------------|
| `admin_home` | AdminHomeScreen | Dashboard de administración |
| `admin_poi_management` | AdminPOIManagementScreen | Gestión de POIs |
| `admin_incidents` | AdminIncidentsScreen | Gestión de incidentes |
| `admin_events` | AdminEventsScreen | Gestión de eventos |
| `admin_user_management` | AdminUserManagementScreen | Gestión de usuarios (solo ADMIN) |
| `admin_profile` | ProfileScreen | Perfil de administrador |

### Layouts

- **MainLayout**: Layout principal para clientes (con bottom navigation)
- **AdminMainLayout**: Layout principal para administradores (con sidebar)

### Navegación por Roles

El sistema redirige automáticamente según el tipo de usuario después del login:

```kotlin
when (userType) {
    TipoUsuario.ADMINISTRADOR -> navController.navigate(AppRoutes.ADMIN_HOME)
    TipoUsuario.MODERADOR -> navController.navigate(AppRoutes.ADMIN_HOME)
    else -> navController.navigate(AppRoutes.CLIENT_HOME)
}
```

---

## 🧠 ViewModels y Lógica de Negocio

### PointOfInterestViewModel

**Ubicación**: `app/src/main/java/com/example/points/viewmodel/PointOfInterestViewModel.kt`

**Estado UI**:
```kotlin
data class POIUIState(
    val isLoading: Boolean = false,
    val pois: List<PointOfInterest> = emptyList(),
    val filteredPOIs: List<PointOfInterest> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: CategoriaPOI? = null,
    val showOnlyNearby: Boolean = false,
    val userLocation: Pair<Double, Double>? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false
)
```

**Funciones Principales**:
- `loadAllPOIs()`: Cargar todos los POIs aprobados
- `loadPOIsByCategory(categoria)`: Filtrar por categoría
- `searchPOIs(query)`: Buscar POIs
- `loadNearbyPOIs(lat, lon, radiusKm)`: POIs cercanos
- `submitPOI(poi)`: Crear nuevo POI
- `setCategoryFilter(categoria)`: Aplicar filtro de categoría
- `toggleNearbyFilter()`: Alternar filtro de cercanía
- `clearFilters()`: Limpiar todos los filtros

### IncidentViewModel

**Ubicación**: `app/src/main/java/com/example/points/viewmodel/IncidentViewModel.kt`

Gestiona el estado y operaciones de incidentes.

### EventViewModel

**Ubicación**: `app/src/main/java/com/example/points/viewmodel/EventViewModel.kt`

Gestiona el estado y operaciones de eventos.

### UserManagementViewModel

**Ubicación**: `app/src/main/java/com/example/points/viewmodel/UserManagementViewModel.kt`

Gestiona usuarios (solo para administradores):
- Listar usuarios
- Cambiar roles
- Activar/desactivar usuarios
- Editar información de usuarios
- Eliminar usuarios

### LoginViewModel / RegisterViewModel

**Ubicación**: `app/src/main/java/com/example/points/auth/`

Gestionan la autenticación de usuarios.

---

## 💾 Repositorios y Servicios

### PointOfInterestRepository

**Ubicación**: `app/src/main/java/com/example/points/repository/PointOfInterestRepository.kt`

**Operaciones**:
- `getAllApprovedPOIs()`: Flow de todos los POIs aprobados
- `getPOIsByCategory(categoria)`: Flow de POIs por categoría
- `searchPOIs(query)`: Flow de búsqueda de POIs
- `getNearbyPOIs(lat, lon, radiusKm)`: Flow de POIs cercanos
- `getPendingPOIs()`: Flow de POIs pendientes (moderación)
- `getPOIsInReview()`: Flow de POIs en revisión
- `createPOI(poi)`: Crear nuevo POI
- `updatePOI(poi)`: Actualizar POI existente
- `approvePOI(poiId, comentarios)`: Aprobar POI
- `rejectPOI(poiId, comentarios)`: Rechazar POI
- `deletePOI(poiId)`: Eliminar POI
- `uploadPOIImage(poiId, imageUri)`: Subir imagen
- `getPOIById(poiId)`: Obtener POI por ID

**Características**:
- Usa `callbackFlow` para streams en tiempo real
- Calcula distancias con fórmula de Haversine
- Maneja errores con `Result<T>`

### IncidentRepository

**Ubicación**: `app/src/main/java/com/example/points/repository/IncidentRepository.kt`

Gestiona operaciones CRUD de incidentes.

### EventRepository

**Ubicación**: `app/src/main/java/com/example/points/repository/EventRepository.kt`

Gestiona operaciones CRUD de eventos, incluyendo eventos recurrentes.

### UserRepository

**Ubicación**: `app/src/main/java/com/example/points/repository/UserRepository.kt`

Gestiona operaciones de usuarios.

### StorageRepository

**Ubicación**: `app/src/main/java/com/example/points/storage/StorageRepository.kt`

Gestiona la subida de archivos a Firebase Storage:
- Imágenes de POIs
- Imágenes de eventos
- Fotos de incidentes

**Rutas de Storage**:
- `poi_images/{poiId}_{uuid}.jpg`
- `event_images/{eventId}_{uuid}.jpg`
- `incident_images/{incidentId}_{uuid}.jpg`

### LocationService

**Ubicación**: `app/src/main/java/com/example/points/services/LocationService.kt`

Gestiona la obtención de ubicación del usuario.

---

## 🎨 Componentes UI

### Componentes Reutilizables

**Ubicación**: `app/src/main/java/com/example/points/components/`

#### Componentes Principales

1. **MainLayout.kt**
   - Layout principal con bottom navigation para clientes
   - Navegación entre: Home, POIs, Incidentes, Eventos, Perfil

2. **AdminMainLayout.kt**
   - Layout principal para administradores
   - Sidebar con opciones de administración

3. **AppHeader.kt**
   - Header reutilizable con título y acciones

4. **PointsCards.kt** (AdvancedCards.kt)
   - Cards personalizados para POIs, eventos, incidentes

5. **PointsButtons.kt** (AdvancedButtons.kt)
   - Botones personalizados con estilos consistentes

6. **PointsInputs.kt**
   - Campos de entrada personalizados

7. **PointsImages.kt**
   - Componentes para mostrar imágenes con Coil

8. **PointsLoading.kt**
   - Indicadores de carga (shimmer, progress)

9. **PointsFeedback.kt**
   - Snackbars, diálogos de confirmación, mensajes de error/éxito

10. **PointsBadges.kt**
    - Badges para estados, categorías, etc.

11. **PointsChips.kt**
    - Chips para filtros y etiquetas

12. **OptimizedImageLoader.kt**
    - Carga optimizada de imágenes con Coil

13. **ShareOptionsDialog.kt / POIShareOptionsDialog.kt**
    - Diálogos para compartir contenido

### Sistema de Diseño

**Tema**: `app/src/main/java/com/example/points/ui/theme/`

- **Theme.kt**: Tema principal con soporte para modo claro/oscuro y Dynamic Color
- **Color.kt**: Paleta de colores de la marca
- **Typography.kt**: Tipografía del sistema
- **Shapes.kt**: Formas y esquinas redondeadas
- **DesignTokens.kt**: Tokens de diseño

**Colores Principales**:
- Primary: Teal/Verde (identidad de marca)
- Secondary: Verde complementario
- Tertiary: Azul
- Error: Rojo para errores

**Soporte**:
- ✅ Modo claro/oscuro
- ✅ Dynamic Color (Android 12+)
- ✅ Material Design 3

---

## 🔌 Integraciones Externas

### Firebase

#### Firestore
- **Colecciones**:
  - `puntos_interes`: POIs
  - `incidentes`: Incidentes
  - `eventos`: Eventos
  - `users`: Usuarios

#### Firebase Storage
- Almacenamiento de imágenes
- Estructura: `{tipo}_images/{id}_{uuid}.jpg`

#### Firebase Authentication
- Autenticación con email/password
- Recuperación de contraseña

### Google Maps

**API Key**: Configurada en variables de entorno

**Funcionalidades**:
- Visualización de mapas
- Marcadores personalizados
- Clustering de marcadores
- Navegación a ubicaciones
- Búsqueda de direcciones

**Componentes**:
- `POIMapScreen`: Mapa de POIs
- `IncidentsMapScreen`: Mapa de incidentes
- `MarkerUtils.kt`: Utilidades para marcadores
- `MapStyleUtils.kt`: Estilos de mapa

### Coil

**Uso**: Carga asíncrona de imágenes
- Imágenes de POIs
- Fotos de perfil
- Imágenes de eventos

---

## ⚙️ Configuración y Dependencias

### Variables de Entorno

**Archivo**: `.env` (no committeado)

**Variables Requeridas**:
```bash
GOOGLE_MAPS_API_KEY=tu_clave_aqui
FIREBASE_PROJECT_ID=tu_proyecto
FIREBASE_PROJECT_NUMBER=tu_numero
FIREBASE_STORAGE_BUCKET=tu_bucket
FIREBASE_API_KEY=tu_clave_firebase
FIREBASE_APP_ID=tu_app_id
ENVIRONMENT=development
DEBUG_MODE=true
```

**Carga**: `EnvironmentConfig.kt` inicializa las variables al iniciar la app.

### Dependencias Principales

**Gradle**: `app/build.gradle.kts`

```kotlin
// Firebase BOM
implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")
implementation("com.google.firebase:firebase-storage")

// Google Maps
implementation("com.google.maps.android:maps-compose:4.4.1")
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.android.gms:play-services-location:21.3.0")

// Coil
implementation("io.coil-kt:coil-compose:2.7.0")

// Lottie
implementation("com.airbnb.android:lottie-compose:6.1.0")

// Shimmer
implementation("com.valentinilk.shimmer:compose-shimmer:1.2.0")

// Environment variables
implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2024.09.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.navigation:navigation-compose:2.8.0")
```

### Configuración de Build

- **Min SDK**: 24
- **Target SDK**: 36
- **Compile SDK**: 36
- **Java Version**: 11
- **Kotlin**: 2.0.21

### Permisos

**AndroidManifest.xml**:
- `ACCESS_FINE_LOCATION`: Ubicación precisa
- `ACCESS_COARSE_LOCATION`: Ubicación aproximada
- `INTERNET`: Conexión a internet
- `ACCESS_NETWORK_STATE`: Estado de red
- `CAMERA`: Tomar fotos
- `READ_EXTERNAL_STORAGE`: Leer imágenes

---

## 🔄 Flujos de Usuario

### Flujo: Crear POI

```
1. Usuario navega a POISubmissionScreen
2. Completa formulario (nombre, descripción, categoría, ubicación, etc.)
3. Sube imágenes (opcional)
4. Envía formulario
5. POI se crea con estado PENDIENTE
6. Moderador/Admin revisa y aprueba/rechaza
7. Si aprobado, aparece en el mapa
```

### Flujo: Reportar Incidente

```
1. Usuario navega a CreateIncidentScreen
2. Selecciona tipo de incidente
3. Describe el incidente
4. Toma/selecciona foto (opcional)
5. Selecciona ubicación (automática o manual)
6. Envía reporte
7. Incidente aparece con estado PENDIENTE
8. Administrador revisa y cambia estado
```

### Flujo: Crear Evento

```
1. Usuario navega a CreateEventDialog
2. Completa información del evento
3. Define fechas y horarios
4. Configura recurrencia (opcional)
5. Sube imágenes
6. Envía evento
7. Evento se crea con estado PENDIENTE
8. Moderador/Admin aprueba
9. Evento aparece en calendario y lista
```

### Flujo: Moderación (Admin/Moderador)

```
1. Admin/Moderador accede a AdminPOIManagementScreen
2. Ve lista de POIs pendientes
3. Revisa detalles del POI
4. Acepta o rechaza con comentarios
5. POI cambia de estado
6. Usuario creador recibe notificación (si implementado)
```

### Flujo: Gestión de Usuarios (Solo Admin)

```
1. Admin accede a AdminUserManagementScreen
2. Ve lista de usuarios
3. Puede:
   - Cambiar rol de usuario
   - Activar/desactivar usuario
   - Editar información
   - Eliminar usuario
```

---

## 🛠️ Utilidades y Helpers

### ConfigHelper.kt

Utilidades para configuración.

### EnvironmentConfig.kt

Gestión de variables de entorno.

### IconToBitmapUtils.kt

Conversión de iconos a bitmaps para marcadores.

### ImageLoaderConfig.kt

Configuración de Coil para carga de imágenes.

### MapStyleUtils.kt

Estilos personalizados para Google Maps.

### MarkerUtils.kt

Utilidades para crear y gestionar marcadores en mapas.

### PasswordUtils.kt

Utilidades para validación de contraseñas.

### POIIconUtils.kt

Mapeo de categorías de POI a iconos.

### POIUtils.kt

Utilidades generales para POIs.

### ShareUtils.kt

Utilidades para compartir contenido.

### Constants

**Ubicación**: `app/src/main/java/com/example/points/constants/`

- `AppRoutes.kt`: Rutas de navegación
- `AppSpacing.kt`: Espaciado del sistema
- `ButtonText.kt`: Textos de botones
- `ContentDescription.kt`: Descripciones de accesibilidad
- `ErrorMessage.kt`: Mensajes de error
- `IconSize.kt`: Tamaños de iconos
- `LoadingMessage.kt`: Mensajes de carga
- `ReviewStatus.kt`: Estados de revisión
- `SectionTitle.kt`: Títulos de secciones
- `SuccessMessage.kt`: Mensajes de éxito

---

## 📁 Estructura de Archivos

```
app/src/main/java/com/example/points/
├── MainActivity.kt                    # Actividad principal
├── PointsApplication.kt               # Clase Application
├── AppNavigation.kt                  # Navegación principal
│
├── auth/                            # Autenticación
│   ├── LoginScreen.kt
│   ├── LoginViewModel.kt
│   ├── LoginUiState.kt
│   ├── RegisterScreen.kt
│   ├── RegisterViewModel.kt
│   ├── RegisterUiState.kt
│   └── ForgotPasswordScreen.kt
│
├── models/                          # Modelos de datos
│   ├── PointOfInterest.kt
│   ├── Incident.kt
│   ├── Event.kt
│   ├── User.kt
│   └── Notification.kt
│
├── screens/                         # Pantallas
│   ├── HomeScreen.kt
│   ├── ClientHomeScreen.kt
│   ├── AdminHomeScreen.kt
│   ├── POIScreen.kt
│   ├── POIMapScreen.kt
│   ├── POISearchScreen.kt
│   ├── POIDetailScreen.kt
│   ├── POISubmissionScreen.kt
│   ├── IncidentsScreen.kt
│   ├── IncidentsMapScreen.kt
│   ├── IncidentDetailScreen.kt
│   ├── CreateIncidentScreen.kt
│   ├── EventsScreen.kt
│   ├── EventScheduleScreen.kt
│   ├── AdminPOIManagementScreen.kt
│   ├── AdminIncidentsScreen.kt
│   ├── AdminEventsScreen.kt
│   ├── AdminUserManagementScreen.kt
│   └── [otros diálogos y pantallas]
│
├── components/                      # Componentes UI
│   ├── MainLayout.kt
│   ├── AdminMainLayout.kt
│   ├── AppHeader.kt
│   ├── AdvancedCards.kt
│   ├── AdvancedButtons.kt
│   ├── PointsInputs.kt
│   ├── PointsImages.kt
│   ├── PointsLoading.kt
│   ├── PointsFeedback.kt
│   ├── PointsBadges.kt
│   ├── PointsChips.kt
│   └── OptimizedImageLoader.kt
│
├── viewmodel/                       # ViewModels
│   ├── PointOfInterestViewModel.kt
│   ├── IncidentViewModel.kt
│   ├── EventViewModel.kt
│   └── UserManagementViewModel.kt
│
├── repository/                      # Repositorios
│   ├── PointOfInterestRepository.kt
│   ├── IncidentRepository.kt
│   ├── EventRepository.kt
│   └── UserRepository.kt
│
├── services/                        # Servicios
│   └── LocationService.kt
│
├── storage/                         # Almacenamiento
│   ├── StorageRepository.kt
│   └── StoragePaths.kt
│
├── utils/                           # Utilidades
│   ├── ConfigHelper.kt
│   ├── EnvironmentConfig.kt
│   ├── IconToBitmapUtils.kt
│   ├── ImageLoaderConfig.kt
│   ├── MapStyleUtils.kt
│   ├── MarkerUtils.kt
│   ├── PasswordUtils.kt
│   ├── POIIconUtils.kt
│   ├── POIUtils.kt
│   └── ShareUtils.kt
│
├── constants/                       # Constantes
│   ├── AppRoutes.kt
│   ├── AppSpacing.kt
│   ├── ButtonText.kt
│   ├── ContentDescription.kt
│   ├── ErrorMessage.kt
│   ├── IconSize.kt
│   ├── LoadingMessage.kt
│   ├── ReviewStatus.kt
│   ├── SectionTitle.kt
│   └── SuccessMessage.kt
│
├── profile/                         # Perfil
│   ├── ProfileScreen.kt
│   ├── ProfileViewModel.kt
│   └── UserProfile.kt
│
└── ui/theme/                        # Sistema de diseño
    ├── Theme.kt
    ├── Color.kt
    ├── Typography.kt
    ├── Shapes.kt
    ├── DesignTokens.kt
    └── Type.kt
```

---

## 🔍 Detalles Técnicos Importantes

### Gestión de Estado

- **StateFlow**: Para estado reactivo en ViewModels
- **UIState**: Data classes para encapsular estado de UI
- **Flow**: Para streams de datos desde repositorios

### Manejo de Errores

- Uso de `Result<T>` para operaciones que pueden fallar
- Mensajes de error en UIState
- Logging con Android Log

### Optimizaciones

- **Lazy Loading**: Carga diferida de imágenes
- **Pagination**: (Pendiente de implementar para listas grandes)
- **Caching**: Firebase maneja caché automáticamente
- **Shimmer**: Efectos de carga para mejor UX

### Seguridad

- Variables de entorno para claves API
- Validación de permisos por rol
- Autenticación requerida para operaciones sensibles
- Validación de datos en formularios

### Testing

- Estructura preparada para tests unitarios
- Tests de instrumentación para UI
- Configuración en `build.gradle.kts`

---

## 🚀 Funcionalidades Pendientes / Mejoras Futuras

### Identificadas en el Código

1. **Notificaciones en Tiempo Real**
   - Pantalla `NotificationsScreen` existe pero es placeholder
   - Falta implementar Firebase Cloud Messaging

2. **Analíticas**
   - Pantalla `AdminAnalyticsScreen` es placeholder
   - Falta implementar dashboard con estadísticas

3. **Configuración del Sistema**
   - Pantalla `AdminSettingsScreen` es placeholder
   - Falta implementar configuración avanzada

4. **Mis Reportes**
   - Pantalla `MyReportsScreen` es placeholder
   - Falta implementar historial de reportes del usuario

5. **Alertas**
   - Pantalla `AlertsScreen` es placeholder
   - Falta implementar sistema de alertas

6. **Edición de Incidentes**
   - TODO en código: "Implementar edición de incidente"

7. **Paginación**
   - Listas grandes podrían beneficiarse de paginación

8. **Búsqueda Avanzada**
   - Filtros más complejos para POIs
   - Búsqueda por múltiples criterios

9. **Sistema de Calificaciones**
   - Modelo tiene campos pero falta UI completa

10. **Comentarios/Reviews**
    - Falta sistema de comentarios para POIs

---

## 📝 Notas para Desarrollo

### Agregar Nueva Funcionalidad

1. **Crear Modelo** (si es necesario)
   - Agregar en `models/`
   - Definir estados y enums relacionados

2. **Crear Repositorio**
   - Agregar en `repository/`
   - Implementar operaciones CRUD con Firebase

3. **Crear ViewModel**
   - Agregar en `viewmodel/`
   - Definir UIState
   - Implementar lógica de negocio

4. **Crear Pantalla**
   - Agregar en `screens/`
   - Usar componentes reutilizables
   - Conectar con ViewModel

5. **Agregar Ruta**
   - Actualizar `AppRoutes.kt`
   - Agregar en `AppNavigation.kt`

6. **Agregar Navegación**
   - Botones/links en pantallas relevantes
   - Actualizar layouts de navegación si es necesario

### Convenciones de Código

- **Nombres**: camelCase para variables/funciones, PascalCase para clases
- **Paquetes**: Organizados por funcionalidad
- **Comentarios**: Documentación en KotlinDoc para funciones públicas
- **Logging**: Usar `Log.d()` para debug, `Log.e()` para errores

### Debugging

- Logs disponibles en Logcat con tags:
  - `PointsApp`: Configuración general
  - `POIViewModel`: POIs
  - `POIRepository`: Operaciones de POIs
  - Similar para otros componentes

---

## 📞 Recursos Adicionales

- **README.md**: Documentación general del proyecto
- **ENVIRONMENT_SETUP.md**: Guía de configuración de variables de entorno
- **Firebase Console**: https://console.firebase.google.com/
- **Google Cloud Console**: https://console.cloud.google.com/

---

**Última actualización**: Generado automáticamente desde el código fuente
**Versión de la App**: 1.0
**Versión de Android**: Min SDK 24, Target SDK 36

