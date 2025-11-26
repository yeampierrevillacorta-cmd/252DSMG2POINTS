# Funcionamiento de la Creación de Dashboards y Gráficos

## 📋 Índice
1. [Arquitectura General](#arquitectura-general)
2. [Flujo de Datos](#flujo-de-datos)
3. [Componentes Principales](#componentes-principales)
4. [Proceso Detallado](#proceso-detallado)
5. [Dependencias y Tecnologías](#dependencias-y-tecnologías)

---

## 🏗️ Arquitectura General

La aplicación sigue una arquitectura **MVVM (Model-View-ViewModel)** con separación de responsabilidades:

```
MainActivity (Entry Point)
    ↓
DashboardScreen (UI - Jetpack Compose)
    ↓
DashboardViewModel (Lógica de Negocio)
    ↓
AccountRepository (Acceso a Datos)
    ↓
Firebase Firestore (Base de Datos)
```

---

## 🔄 Flujo de Datos

### Flujo Completo desde el Inicio hasta la Visualización

```
1. MainActivity.onCreate()
   └─> Carga DashboardScreen()

2. DashboardScreen()
   ├─> Crea instancia de DashboardViewModel
   ├─> Observa el estado UI (uiState)
   └─> LaunchedEffect: Llama a viewModel.cargarDashboard()

3. DashboardViewModel.cargarDashboard()
   ├─> Llama a dashboardRepository.getAllUsers()
   └─> Procesa los datos recibidos

4. AccountRepository.getAllUsers()
   ├─> Consulta Firebase Firestore (colección "usuarios")
   ├─> Mapea documentos a objetos UserProfileData
   └─> Retorna Result<List<UserProfileData>>

5. DashboardViewModel (procesamiento)
   ├─> Filtra usuarios con tipoDocumento válido
   ├─> Agrupa por tipoDocumento
   ├─> Calcula cantidad por tipo
   ├─> Crea List<NpersonasXTipoDocumento>
   └─> Actualiza _uiState con los datos procesados

6. DashboardScreen (reactividad)
   ├─> Detecta cambio en uiState
   ├─> Extrae datosDashboard
   └─> Pasa datos a BarrasScreen()

7. BarrasScreen()
   ├─> Recibe List<NpersonasXTipoDocumento>
   └─> Llama a Barras()

8. Barras()
   ├─> Transforma datos a formato BarChartData.Bar
   ├─> Asigna colores aleatorios (Utils.colorAleatorio())
   └─> Renderiza BarChart con la librería tehras-charts
```

---

## 🧩 Componentes Principales

### 1. **MainActivity.kt**
**Ubicación:** `app/src/main/java/com/dsm/a252dsmdashboards/MainActivity.kt`

**Responsabilidad:** Punto de entrada de la aplicación Android.

**Funcionalidad:**
- Configura el tema de Material Design 3
- Inicializa Jetpack Compose
- Renderiza el `DashboardScreen` dentro de un `Scaffold`

```kotlin
// Punto de entrada
setContent {
    _252dsmdashboardsTheme {
        Scaffold {
            DashboardScreen()
        }
    }
}
```

---

### 2. **DashboardScreen.kt**
**Ubicación:** `app/src/main/java/com/dsm/a252dsmdashboards/ui/screens/DashboardScreen.kt`

**Responsabilidad:** Pantalla principal que coordina la UI y el ViewModel.

**Funcionalidad:**
- Crea y gestiona el `DashboardViewModel`
- Observa cambios en el estado UI mediante `collectAsState()`
- Usa `LaunchedEffect` para cargar datos al iniciar
- Pasa los datos procesados a `BarrasScreen` para visualización

**Características clave:**
- **Reactividad:** Se actualiza automáticamente cuando cambia `uiState`
- **Ciclo de vida:** Carga datos una vez al montarse el componente

---

### 3. **DashboardViewModel.kt**
**Ubicación:** `app/src/main/java/com/dsm/a252dsmdashboards/ui/screens/DashboardViewModel.kt`

**Responsabilidad:** Lógica de negocio y gestión del estado de la UI.

**Componentes:**

#### **DashboardUiState**
```kotlin
data class DashboardUiState(
    val datosDashboard: List<NpersonasXTipoDocumento> = listOf(),
    val flag_error_dashboard: Boolean = false
)
```
- Estado inmutable que contiene los datos del dashboard y flag de error

#### **Funciones principales:**

**`cargarDashboard()`**
- Obtiene todos los usuarios desde el repositorio
- **Procesamiento de datos:**
  1. Filtra usuarios con `tipoDocumento` no nulo/vacío
  2. Agrupa usuarios por `tipoDocumento` usando `groupBy()`
  3. Calcula la cantidad por tipo: `lista.size`
  4. Crea objetos `NpersonasXTipoDocumento(tipo, cantidad)`
- Actualiza el `_uiState` con los datos procesados o maneja errores

**`resetFlags()`**
- Restablece las banderas de error

**Factory Pattern**
- Utiliza `ViewModelProvider.Factory` para inyección de dependencias
- Obtiene el `AccountRepository` desde `DashboardApplication.container`

---

### 4. **AccountRepository.kt**
**Ubicación:** `app/src/main/java/com/dsm/a252dsmdashboards/data/repository/AccountRepository.kt`

**Responsabilidad:** Acceso a datos desde Firebase Firestore.

**Funcionalidad:**

#### **`getAllUsers(): Result<List<UserProfileData>>`**
- Consulta la colección `"usuarios"` en Firestore
- Mapea documentos a objetos `UserProfileData`
- Maneja errores con `Result.success()` o `Result.failure()`

**Mapeo de campos:**
- `uid`: ID del documento o campo "uid"
- `correo` → `email`
- `nombre`, `apellidopaterno`, `apellidomaterno`
- `tipo_documento` → `tipoDocumento` (campo clave para el dashboard)
- `genero`, `telefono`, `estado`, etc.

**Manejo de errores:**
- Captura excepciones y las registra en Log
- Retorna `Result.failure()` para manejo en la capa superior

---

### 5. **DashboardBarrasScreen.kt**
**Ubicación:** `app/src/main/java/com/dsm/a252dsmdashboards/ui/screens/DashboardBarrasScreen.kt`

**Responsabilidad:** Renderización visual del gráfico de barras.

**Componentes:**

#### **`BarrasScreen(data: List<NpersonasXTipoDocumento>)`**
- Función Composable que recibe los datos procesados
- Muestra un título "Grafico de Barras"
- Llama a `Barras()` para renderizar el gráfico

#### **`Barras(data: List<NpersonasXTipoDocumento>)`**
- **Transformación de datos:**
  - Convierte `NpersonasXTipoDocumento` a `BarChartData.Bar`
  - Cada barra tiene:
    - `label`: Tipo de documento (descripcion)
    - `value`: Cantidad de personas (cantidad)
    - `color`: Color aleatorio generado por `Utils.colorAleatorio()`

- **Configuración del gráfico:**
  - Usa la librería **tehras-charts** (`BarChart`)
  - `SimpleValueDrawer`: Muestra etiquetas en el eje X
  - Dimensiones: 300dp de altura, padding de 30dp horizontal y 80dp vertical

---

### 6. **Utils.kt**
**Ubicación:** `app/src/main/java/com/dsm/a252dsmdashboards/utils/Utils.kt`

**Responsabilidad:** Utilidades para la generación de colores.

**Funcionalidad:**

#### **`colorAleatorio(): Color`**
- Genera un color aleatorio de una paleta predefinida
- **Paleta de colores:** 11 colores Material Design
  - Rojo, Verde, Amarillo, Púrpura, Rosa, Azul, etc.
- Selecciona un color aleatorio y lo remueve de la lista (para evitar repeticiones)

**Nota:** Hay un pequeño bug: la lista se crea nueva en cada llamada, por lo que los colores pueden repetirse entre diferentes gráficos.

---

### 7. **Modelos de Datos**

#### **NpersonasXTipoDocumento.kt**
```kotlin
data class NpersonasXTipoDocumento(
    val descripcion: String,  // Tipo de documento
    val cantidad: Int          // Cantidad de personas
)
```
- Modelo para datos agregados del dashboard
- Representa el resumen por tipo de documento

#### **UserProfileData.kt**
- Modelo completo de usuario desde Firestore
- Incluye todos los campos del perfil de usuario
- Usa anotaciones `@PropertyName` para mapeo con Firestore

---

### 8. **AppContainer.kt**
**Ubicación:** `app/src/main/java/com/dsm/a252dsmdashboards/data/AppContainer.kt`

**Responsabilidad:** Contenedor de dependencias (Dependency Injection simple).

**Funcionalidad:**
- **`DefaultAppContainer`:** Implementación del contenedor
- Crea instancia única de `FirebaseFirestore` (lazy initialization)
- Crea instancia de `AccountRepository` con Firestore inyectado
- Proporciona el repositorio a través de `dashboardRepository`

---

### 9. **DashboardApplication.kt**
**Ubicación:** `app/src/main/java/com/dsm/a252dsmdashboards/DashboardApplication.kt`

**Responsabilidad:** Clase Application personalizada.

**Funcionalidad:**
- Inicializa el `AppContainer` en `onCreate()`
- Hace disponible el contenedor globalmente para inyección de dependencias
- Utilizado por el `ViewModelFactory` para obtener el repositorio

---

## 📊 Proceso Detallado

### Paso a Paso: Creación del Dashboard

#### **Paso 1: Inicialización de la Aplicación**
```
DashboardApplication.onCreate()
    └─> Crea DefaultAppContainer()
        └─> Inicializa FirebaseFirestore (lazy)
        └─> Crea AccountRepository (lazy)
```

#### **Paso 2: Carga de la Pantalla**
```
MainActivity.onCreate()
    └─> setContent { DashboardScreen() }
        └─> DashboardScreen se monta
            └─> Crea ViewModel con Factory
            └─> LaunchedEffect ejecuta cargarDashboard()
```

#### **Paso 3: Obtención de Datos**
```
cargarDashboard()
    └─> dashboardRepository.getAllUsers()
        └─> Firebase Firestore: collection("usuarios").get()
            └─> Mapea documentos a UserProfileData
            └─> Retorna Result<List<UserProfileData>>
```

#### **Paso 4: Procesamiento de Datos**
```
ViewModel procesa los datos:
    1. Filtra: usuarios.filter { !it.tipoDocumento.isNullOrBlank() }
    2. Agrupa: groupBy { it.tipoDocumento!! }
    3. Transforma: map { (tipo, lista) -> NpersonasXTipoDocumento(tipo, lista.size) }
    4. Actualiza estado: _uiState.value = copy(datosDashboard = resumen)
```

#### **Paso 5: Renderización**
```
UI reacciona al cambio de estado:
    └─> DashboardScreen.collectAsState() detecta cambio
        └─> Extrae: val datos = uiState.datosDashboard
        └─> Pasa a: BarrasScreen(datos)
            └─> Barras() transforma datos
                └─> Crea ArrayList<BarChartData.Bar>
                └─> Renderiza BarChart
```

---

## 🛠️ Dependencias y Tecnologías

### Librerías Principales

1. **Jetpack Compose**
   - UI declarativa y reactiva
   - Material Design 3

2. **Firebase Firestore**
   - Base de datos NoSQL en la nube
   - Colección: `"usuarios"`

3. **tehras-charts**
   - Librería para gráficos en Compose
   - Utilizada para renderizar `BarChart`

4. **ViewModel & StateFlow**
   - Gestión de estado y ciclo de vida
   - Reactividad con `StateFlow` y `collectAsState()`

5. **Coroutines**
   - Operaciones asíncronas
   - `suspend` functions para Firestore

### Estructura de Dependencias

```
build.gradle.kts
├─ androidx.compose (UI)
├─ firebase-firestore-ktx (Base de datos)
├─ androidx.lifecycle.viewmodel.compose (ViewModel)
└─ com.github.tehras:charts (Gráficos)
```

---

## 🔍 Consideraciones y Mejoras Potenciales

### Puntos de Atención

1. **Utils.colorAleatorio()**
   - La lista de colores se recrea en cada llamada
   - Puede generar colores repetidos entre diferentes gráficos
   - **Sugerencia:** Usar una instancia compartida o pasar colores como parámetro

2. **Manejo de Errores**
   - El flag `flag_error_dashboard` se establece pero no se muestra en la UI
   - **Sugerencia:** Mostrar mensaje de error al usuario

3. **Carga de Datos**
   - No hay indicador de carga
   - **Sugerencia:** Agregar estado `isLoading` y mostrar progreso

4. **Filtrado de Datos**
   - Solo se muestran usuarios con `tipoDocumento` válido
   - **Sugerencia:** Agregar categoría "Sin tipo" para usuarios sin documento

---

## 📝 Resumen Ejecutivo

El sistema de dashboards funciona mediante un flujo reactivo que:

1. **Obtiene datos** de Firebase Firestore (colección de usuarios)
2. **Procesa y agrega** los datos agrupando por tipo de documento
3. **Transforma** los datos al formato necesario para el gráfico
4. **Renderiza** un gráfico de barras interactivo usando Jetpack Compose

La arquitectura MVVM garantiza separación de responsabilidades, facilitando el mantenimiento y la escalabilidad del código.

---

## 📚 Referencias de Archivos

- `MainActivity.kt` - Punto de entrada
- `DashboardScreen.kt` - UI principal
- `DashboardViewModel.kt` - Lógica de negocio
- `DashboardBarrasScreen.kt` - Componente de gráfico
- `AccountRepository.kt` - Acceso a datos
- `AppContainer.kt` - Inyección de dependencias
- `DashboardApplication.kt` - Aplicación base
- `NpersonasXTipoDocumento.kt` - Modelo de datos agregado
- `UserProfileData.kt` - Modelo de usuario
- `Utils.kt` - Utilidades

---

*Documento generado para explicar el funcionamiento del sistema de dashboards*


