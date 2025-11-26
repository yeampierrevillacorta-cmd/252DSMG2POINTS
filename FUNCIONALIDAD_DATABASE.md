# Funcionalidad de la Carpeta `database`

## 📋 Índice
1. [Introducción](#introducción)
2. [Arquitectura General](#arquitectura-general)
3. [Componentes Principales](#componentes-principales)
4. [Proceso de Funcionamiento](#proceso-de-funcionamiento)
5. [Flujo de Datos](#flujo-de-datos)
6. [Casos de Uso](#casos-de-uso)
7. [Ventajas y Beneficios](#ventajas-y-beneficios)

---

## 🎯 Introducción

La carpeta `database` implementa un sistema de almacenamiento local utilizando **Room Database** (una abstracción sobre SQLite) para Android. Su propósito principal es:

- **Almacenar datos localmente** para acceso offline
- **Mantener favoritos** de POIs (Puntos de Interés)
- **Cachear POIs** vistos recientemente
- **Guardar historial de búsquedas** del usuario

Esto permite que la aplicación funcione incluso sin conexión a Internet y mejora la experiencia del usuario al proporcionar acceso rápido a datos frecuentemente utilizados.

---

## 🏗️ Arquitectura General

La carpeta `database` sigue el patrón de arquitectura **Room Database** de Android, que consta de tres componentes principales:

```
database/
├── PointsDatabase.kt       # Base de datos principal (Singleton)
├── Converters.kt           # Convertidores de tipos
├── entity/                 # Entidades (Tablas)
│   ├── FavoritePOI.kt
│   ├── CachedPOI.kt
│   └── SearchHistory.kt
└── dao/                    # Data Access Objects (Consultas)
    ├── FavoritePOIDao.kt
    ├── CachedPOIDao.kt
    └── SearchHistoryDao.kt
```

### Relación entre Componentes

```
PointsDatabase (Base de datos)
    ↓
    ├── FavoritePOIDao → FavoritePOI (Entidad)
    ├── CachedPOIDao → CachedPOI (Entidad)
    └── SearchHistoryDao → SearchHistory (Entidad)
```

---

## 🔧 Componentes Principales

### 1. **PointsDatabase.kt** - Base de Datos Principal

**Función:** Configuración y gestión de la base de datos Room.

**Características:**
- **Singleton Pattern:** Una sola instancia de la base de datos en toda la aplicación
- **Thread-Safe:** Uso de `@Volatile` y `synchronized` para evitar condiciones de carrera
- **Nombre de BD:** `points_database`
- **Versión:** 1 (se incrementa cuando hay cambios en el esquema)

**Código Clave:**
```kotlin
@Database(
    entities = [
        FavoritePOI::class,
        SearchHistory::class,
        CachedPOI::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PointsDatabase : RoomDatabase()
```

**Proceso de Inicialización:**
1. Se llama `PointsDatabase.getDatabase(context)` desde `DefaultAppContainer`
2. Si no existe instancia, se crea una nueva con `Room.databaseBuilder()`
3. Se configura con `fallbackToDestructiveMigration()` (en desarrollo)
4. Se almacena la instancia en `INSTANCE` para reutilización

---

### 2. **Converters.kt** - Convertidores de Tipos

**Función:** Convierte tipos complejos de Kotlin/Java a tipos primitivos compatibles con SQLite.

**Problema que Resuelve:**
- SQLite solo soporta tipos primitivos: `Int`, `String`, `Long`, `Double`, etc.
- Firebase `Timestamp` no es compatible directamente con SQLite
- Se necesita convertir `Timestamp` ↔ `Long` (milisegundos)

**Implementación:**
```kotlin
@TypeConverter
fun fromTimestamp(timestamp: Timestamp?): Long? {
    return timestamp?.seconds?.times(1000)?.plus(timestamp.nanoseconds / 1000000)
}

@TypeConverter
fun dateToTimestamp(value: Long?): Timestamp? {
    return value?.let {
        val seconds = it / 1000
        val nanoseconds = ((it % 1000) * 1000000).toInt()
        Timestamp(seconds, nanoseconds)
    }
}
```

**Proceso:**
1. **Al guardar:** `Timestamp` → `Long` (milisegundos desde epoch)
2. **Al leer:** `Long` → `Timestamp` (reconstruye segundos y nanosegundos)

---

### 3. **Entities (Entidades)** - Tablas de la Base de Datos

#### 3.1. **FavoritePOI.kt** - POIs Favoritos

**Función:** Almacena los POIs marcados como favoritos por el usuario.

**Campos:**
- `poiId` (String, Primary Key): ID único del POI
- `nombre`, `descripcion`, `categoria`: Información básica
- `direccion`, `lat`, `lon`: Ubicación
- `calificacion`: Calificación del POI
- `imagenUrl`: URL de la imagen
- `fechaAgregado`, `fechaActualizacion`: Timestamps

**Tabla SQLite:** `favorite_pois`

**Uso:**
- Usuario marca un POI como favorito → Se guarda en esta tabla
- Usuario desmarca un POI → Se elimina de esta tabla
- La app muestra lista de favoritos → Se lee desde esta tabla

---

#### 3.2. **CachedPOI.kt** - Caché de POIs

**Función:** Almacena POIs vistos recientemente para acceso offline.

**Campos:**
- `poiId` (String, Primary Key): ID único del POI
- Campos básicos: `nombre`, `descripcion`, `categoria`, etc.
- `jsonData` (String): **Datos completos en JSON** para deserialización
- `fechaCache`: Fecha en que se guardó en caché
- `fechaCreacionMillis`, `fechaActualizacionMillis`: Timestamps en milisegundos

**Tabla SQLite:** `cached_pois`

**Características Especiales:**
- **JSON Storage:** Almacena el objeto completo en `jsonData` para reconstruir el POI completo
- **Límite de Caché:** Por defecto, se mantienen los 50 POIs más recientes
- **Limpieza Automática:** Se pueden eliminar POIs antiguos (más de 7 días)

**Uso:**
- Usuario visualiza un POI → Se guarda en caché
- Usuario está offline → Se muestran POIs desde caché
- La app limpia caché antiguo → Se eliminan POIs viejos

---

#### 3.3. **SearchHistory.kt** - Historial de Búsquedas

**Función:** Almacena el historial de búsquedas realizadas por el usuario.

**Campos:**
- `id` (Long, Primary Key, AutoGenerate): ID único auto-generado
- `query` (String): Texto de búsqueda
- `category` (String?): Categoría filtrada (opcional)
- `fechaBusqueda` (Long): Timestamp de la búsqueda
- `resultados` (Int): Número de resultados encontrados

**Tabla SQLite:** `search_history`

**Uso:**
- Usuario realiza una búsqueda → Se guarda en historial
- Usuario quiere ver búsquedas recientes → Se lee desde esta tabla
- Usuario limpia historial → Se eliminan todas las búsquedas

---

### 4. **DAOs (Data Access Objects)** - Consultas a la Base de Datos

#### 4.1. **FavoritePOIDao.kt**

**Función:** Define las operaciones CRUD para POIs favoritos.

**Operaciones:**
- `getAllFavorites()`: Obtener todos los favoritos (Flow)
- `getFavoriteById(poiId)`: Obtener un favorito por ID
- `insertFavorite(favorite)`: Agregar a favoritos
- `deleteFavorite(favorite)`: Eliminar de favoritos
- `isFavorite(poiId)`: Verificar si es favorito
- `getFavoriteCount()`: Contar favoritos

**Ejemplo de Consulta:**
```kotlin
@Query("SELECT * FROM favorite_pois ORDER BY fechaAgregado DESC")
fun getAllFavorites(): Flow<List<FavoritePOI>>
```

---

#### 4.2. **CachedPOIDao.kt**

**Función:** Define las operaciones CRUD para caché de POIs.

**Operaciones:**
- `getCachedPOIs(limit)`: Obtener POIs en caché (limitados)
- `getCachedPOIById(poiId)`: Obtener un POI desde caché
- `insertCachedPOI(poi)`: Guardar POI en caché
- `deleteOldCachedPOIs(timestamp)`: Eliminar POIs antiguos
- `getCachedCount()`: Contar POIs en caché

**Ejemplo de Consulta:**
```kotlin
@Query("SELECT * FROM cached_pois ORDER BY fechaCache DESC LIMIT :limit")
fun getCachedPOIs(limit: Int = 50): Flow<List<CachedPOI>>
```

---

#### 4.3. **SearchHistoryDao.kt**

**Función:** Define las operaciones CRUD para historial de búsquedas.

**Operaciones:**
- `getRecentSearches(limit)`: Obtener búsquedas recientes
- `getRecentSearchQueries(limit)`: Obtener solo los textos de búsqueda
- `insertSearch(search)`: Guardar búsqueda
- `deleteOldSearches(timestamp)`: Eliminar búsquedas antiguas
- `getSearchCount()`: Contar búsquedas

**Ejemplo de Consulta:**
```kotlin
@Query("SELECT * FROM search_history ORDER BY fechaBusqueda DESC LIMIT :limit")
fun getRecentSearches(limit: Int = 10): Flow<List<SearchHistory>>
```

---

## 🔄 Proceso de Funcionamiento

### 1. **Inicialización de la Base de Datos**

```
PointsApplication.onCreate()
    ↓
DefaultAppContainer(context)
    ↓
PointsDatabase.getDatabase(context)
    ↓
Room.databaseBuilder()
    ↓
PointsDatabase (Instancia única creada)
```

**Pasos Detallados:**
1. La aplicación inicia → `PointsApplication.onCreate()`
2. Se crea `DefaultAppContainer` con el contexto
3. Se llama `PointsDatabase.getDatabase(context)`
4. Si no existe instancia, se crea una nueva con `Room.databaseBuilder()`
5. Se configura la base de datos con las entidades y convertidores
6. Se almacena la instancia en `INSTANCE` (Singleton)
7. Se retorna la instancia para uso en repositorios

---

### 2. **Guardar un POI en Favoritos**

```
Usuario marca POI como favorito
    ↓
ViewModel.addToFavorites(poi)
    ↓
LocalPOIRepository.addToFavorites(poi)
    ↓
PointOfInterest.toFavoritePOI() (Conversión)
    ↓
FavoritePOIDao.insertFavorite(favorite)
    ↓
Room ejecuta INSERT en SQLite
    ↓
POI guardado en tabla favorite_pois
```

**Código de Ejemplo:**
```kotlin
// 1. Usuario marca como favorito
viewModel.addToFavorites(poi)

// 2. Repository convierte y guarda
suspend fun addToFavorites(poi: PointOfInterest): Result<Unit> {
    val favorite = poi.toFavoritePOI()  // Conversión
    favoriteDao.insertFavorite(favorite)  // Guardado
    return Result.success(Unit)
}

// 3. DAO ejecuta la inserción
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertFavorite(favorite: FavoritePOI)
```

---

### 3. **Leer POIs Favoritos**

```
ViewModel solicita favoritos
    ↓
LocalPOIRepository.getAllFavorites()
    ↓
FavoritePOIDao.getAllFavorites() (Flow)
    ↓
Room ejecuta SELECT en SQLite
    ↓
List<FavoritePOI> obtenida
    ↓
FavoritePOI.toPointOfInterest() (Conversión)
    ↓
List<PointOfInterest> retornada al ViewModel
```

**Código de Ejemplo:**
```kotlin
// 1. Repository obtiene favoritos
fun getAllFavorites(): Flow<List<PointOfInterest>> {
    return favoriteDao.getAllFavorites()
        .map { favorites ->
            favorites.map { it.toPointOfInterest() }  // Conversión
        }
        .flowOn(Dispatchers.IO)
}

// 2. DAO consulta la base de datos
@Query("SELECT * FROM favorite_pois ORDER BY fechaAgregado DESC")
fun getAllFavorites(): Flow<List<FavoritePOI>>
```

---

### 4. **Guardar POI en Caché**

```
Usuario visualiza un POI
    ↓
ViewModel.cachePOI(poi)
    ↓
LocalPOIRepository.cachePOI(poi)
    ↓
PointOfInterest.toCachedPOI() (Conversión a JSON)
    ↓
CachedPOIDao.insertCachedPOI(cachedPOI)
    ↓
Room ejecuta INSERT en SQLite
    ↓
POI guardado en tabla cached_pois (con JSON)
```

**Características Especiales:**
- **Serialización JSON:** El POI completo se convierte a JSON y se guarda en `jsonData`
- **Deserialización:** Al leer, se reconstruye el POI desde JSON
- **Límite de Caché:** Se mantienen solo los 50 POIs más recientes

**Código de Ejemplo:**
```kotlin
// 1. Conversión a CachedPOI con JSON
private fun PointOfInterest.toCachedPOI(): CachedPOI {
    return CachedPOI(
        // ... campos básicos ...
        jsonData = gson.toJson(this)  // Serialización a JSON
    )
}

// 2. Guardado en caché
suspend fun cachePOI(poi: PointOfInterest): Result<Unit> {
    val cachedPOI = poi.toCachedPOI()
    cachedDao.insertCachedPOI(cachedPOI)
    return Result.success(Unit)
}
```

---

### 5. **Leer POIs desde Caché (Offline)**

```
Usuario está offline
    ↓
ViewModel solicita POIs en caché
    ↓
LocalPOIRepository.getCachedPOIs()
    ↓
CachedPOIDao.getCachedPOIs() (Flow)
    ↓
Room ejecuta SELECT en SQLite
    ↓
List<CachedPOI> obtenida
    ↓
CachedPOI.toPointOfInterest() (Deserialización desde JSON)
    ↓
List<PointOfInterest> retornada al ViewModel
```

**Código de Ejemplo:**
```kotlin
// 1. Deserialización desde JSON
private fun CachedPOI.toPointOfInterest(): PointOfInterest? {
    return try {
        gson.fromJson(jsonData, PointOfInterest::class.java)  // Deserialización
    } catch (e: Exception) {
        null
    }
}

// 2. Obtención desde caché
fun getCachedPOIs(limit: Int = 50): Flow<List<PointOfInterest>> {
    return cachedDao.getCachedPOIs(limit)
        .map { cached ->
            cached.mapNotNull { it.toPointOfInterest() }  // Deserialización
        }
        .flowOn(Dispatchers.IO)
}
```

---

### 6. **Guardar Búsqueda en Historial**

```
Usuario realiza una búsqueda
    ↓
ViewModel.saveSearch(query, category)
    ↓
LocalSearchRepository.saveSearch(query, category)
    ↓
SearchHistory creado
    ↓
SearchHistoryDao.insertSearch(search)
    ↓
Room ejecuta INSERT en SQLite
    ↓
Búsqueda guardada en tabla search_history
```

**Código de Ejemplo:**
```kotlin
// 1. Guardado de búsqueda
suspend fun saveSearch(query: String, category: String?, resultados: Int = 0): Result<Unit> {
    val search = SearchHistory(
        query = query,
        category = category,
        resultados = resultados
    )
    searchDao.insertSearch(search)
    return Result.success(Unit)
}
```

---

## 📊 Flujo de Datos

### Flujo General: Firebase → Room → UI

```
Firebase Firestore (Cloud)
    ↓
PointOfInterestRepository (Online)
    ↓
LocalPOIRepository (Cache)
    ↓
PointsDatabase (Room/SQLite)
    ↓
ViewModel (UI State)
    ↓
Compose UI (Pantalla)
```

### Flujo Offline: Room → UI

```
PointsDatabase (Room/SQLite)
    ↓
LocalPOIRepository (Cache)
    ↓
ViewModel (UI State)
    ↓
Compose UI (Pantalla)
```

---

## 💡 Casos de Uso

### 1. **POIs Favoritos**

**Escenario:** Usuario marca un POI como favorito para acceder rápidamente después.

**Proceso:**
1. Usuario toca el botón "Agregar a favoritos"
2. `ViewModel.addToFavorites(poi)` se ejecuta
3. `LocalPOIRepository.addToFavorites(poi)` guarda en Room
4. El POI se guarda en la tabla `favorite_pois`
5. La UI se actualiza automáticamente (Flow)

**Beneficios:**
- Acceso rápido a POIs favoritos
- Funciona offline
- Sincronización automática con UI (Flow)

---

### 2. **Caché de POIs (Offline)**

**Escenario:** Usuario está offline pero quiere ver POIs vistos recientemente.

**Proceso:**
1. Usuario está offline
2. `ViewModel.getCachedPOIs()` se ejecuta
3. `LocalPOIRepository.getCachedPOIs()` lee desde Room
4. Los POIs se deserializan desde JSON
5. La UI muestra los POIs en caché

**Beneficios:**
- Funcionalidad offline
- Acceso rápido a POIs recientes
- Experiencia de usuario mejorada

---

### 3. **Historial de Búsquedas**

**Escenario:** Usuario quiere ver búsquedas recientes para repetir una búsqueda.

**Proceso:**
1. Usuario realiza una búsqueda
2. `ViewModel.saveSearch(query, category)` se ejecuta
3. `LocalSearchRepository.saveSearch()` guarda en Room
4. La búsqueda se guarda en la tabla `search_history`
5. Usuario puede ver historial de búsquedas

**Beneficios:**
- Búsquedas rápidas repetidas
- Historial personalizado
- Mejora la experiencia de usuario

---

### 4. **Limpieza de Caché**

**Escenario:** La aplicación limpia automáticamente POIs antiguos para liberar espacio.

**Proceso:**
1. `ViewModel.cleanOldCache()` se ejecuta periódicamente
2. `LocalPOIRepository.cleanOldCache()` elimina POIs antiguos
3. Se eliminan POIs con `fechaCache` mayor a 7 días
4. La base de datos se mantiene optimizada

**Beneficios:**
- Libera espacio en la base de datos
- Mantiene solo POIs recientes
- Mejora el rendimiento

---

## ✅ Ventajas y Beneficios

### 1. **Funcionalidad Offline**
- Los usuarios pueden acceder a favoritos y caché sin conexión
- Mejora la experiencia de usuario en áreas con conexión limitada

### 2. **Rendimiento Mejorado**
- Acceso rápido a datos locales (sin latencia de red)
- Consultas optimizadas con índices de SQLite
- Caché inteligente reduce llamadas a Firebase

### 3. **Sincronización Automática**
- Uso de `Flow` para actualizaciones en tiempo real
- La UI se actualiza automáticamente cuando cambian los datos
- No se requiere polling manual

### 4. **Persistencia de Datos**
- Los datos se mantienen incluso si la app se cierra
- Los favoritos y el historial persisten entre sesiones
- El caché se mantiene hasta que se limpie manualmente

### 5. **Escalabilidad**
- Room maneja eficientemente grandes cantidades de datos
- Consultas optimizadas con límites y ordenamiento
- Limpieza automática de datos antiguos

### 6. **Type-Safe**
- Room genera código en tiempo de compilación
- Errores de consulta se detectan en tiempo de compilación
- No hay errores de SQL en tiempo de ejecución

---

## 🔍 Resumen Ejecutivo

La carpeta `database` implementa un sistema de almacenamiento local robusto utilizando **Room Database** que:

1. **Almacena datos localmente** para acceso offline (favoritos, caché, historial)
2. **Convierte tipos complejos** (Timestamp ↔ Long) para compatibilidad con SQLite
3. **Proporciona operaciones CRUD** a través de DAOs (Data Access Objects)
4. **Sincroniza automáticamente** con la UI usando Flow
5. **Optimiza el rendimiento** con caché inteligente y limpieza automática

**Flujo Principal:**
- **Inicialización:** `PointsDatabase.getDatabase(context)` → Instancia única (Singleton)
- **Guardado:** `ViewModel` → `Repository` → `DAO` → `Room` → `SQLite`
- **Lectura:** `SQLite` → `Room` → `DAO` → `Repository` → `ViewModel` → `UI`
- **Offline:** Los datos se leen desde Room en lugar de Firebase

**Archivos Clave:**
- `PointsDatabase.kt`: Configuración de la base de datos
- `Converters.kt`: Conversión de tipos (Timestamp ↔ Long)
- `entity/*.kt`: Entidades (tablas)
- `dao/*.kt`: Data Access Objects (consultas)
- `LocalPOIRepository.kt`: Repositorio que usa Room
- `LocalSearchRepository.kt`: Repositorio para historial de búsquedas

Este sistema permite que la aplicación funcione eficientemente tanto online como offline, mejorando significativamente la experiencia del usuario.

