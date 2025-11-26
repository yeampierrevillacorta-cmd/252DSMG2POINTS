# Demostración de Room Database con SQLite

## 📱 Implementación Completa

La aplicación Points utiliza **Room Database** (abstracción sobre SQLite) para almacenamiento local persistente con la siguiente arquitectura:

---

## 🏗️ Arquitectura Implementada

### Componentes Room Database

```
database/
├── PointsDatabase.kt       ✅ Base de datos principal (Singleton)
├── Converters.kt           ✅ Convertidores de tipos (Timestamp ↔ Long)
├── entity/                 ✅ Entidades (Tablas)
│   ├── FavoritePOI.kt     ✅ Tabla: favorite_pois
│   ├── CachedPOI.kt       ✅ Tabla: cached_pois
│   └── SearchHistory.kt   ✅ Tabla: search_history
└── dao/                    ✅ Data Access Objects (Consultas)
    ├── FavoritePOIDao.kt  ✅ CRUD de favoritos
    ├── CachedPOIDao.kt    ✅ CRUD de caché
    └── SearchHistoryDao.kt ✅ CRUD de historial
```

### Repositorios Implementados

```
repository/
├── LocalPOIRepository.kt       ✅ Gestión de favoritos y caché
└── LocalSearchRepository.kt    ✅ Gestión de historial de búsquedas
```

---

## 📊 Base de Datos SQLite

### Ubicación en el Dispositivo

```
/data/data/com.example.points/databases/points_database
```

### Tablas Creadas

#### 1. **favorite_pois** - POIs Favoritos

```sql
CREATE TABLE favorite_pois (
    poiId TEXT PRIMARY KEY NOT NULL,
    nombre TEXT NOT NULL,
    descripcion TEXT NOT NULL,
    categoria TEXT NOT NULL,
    direccion TEXT NOT NULL,
    lat REAL NOT NULL,
    lon REAL NOT NULL,
    calificacion REAL NOT NULL,
    imagenUrl TEXT,
    fechaAgregado INTEGER NOT NULL,
    fechaActualizacion INTEGER NOT NULL
);
```

**Propósito:** Almacenar POIs marcados como favoritos por el usuario.

#### 2. **cached_pois** - Caché de POIs

```sql
CREATE TABLE cached_pois (
    poiId TEXT PRIMARY KEY NOT NULL,
    nombre TEXT NOT NULL,
    descripcion TEXT NOT NULL,
    categoria TEXT NOT NULL,
    direccion TEXT NOT NULL,
    lat REAL NOT NULL,
    lon REAL NOT NULL,
    calificacion REAL NOT NULL,
    totalCalificaciones INTEGER NOT NULL,
    imagenUrl TEXT,
    estado TEXT NOT NULL,
    fechaCreacionMillis INTEGER NOT NULL,
    fechaActualizacionMillis INTEGER NOT NULL,
    fechaCache INTEGER NOT NULL,
    jsonData TEXT NOT NULL
);
```

**Propósito:** Cachear POIs vistos recientemente para acceso offline.

**Característica Especial:** `jsonData` almacena el objeto completo serializado en JSON para reconstruir el POI completo.

#### 3. **search_history** - Historial de Búsquedas

```sql
CREATE TABLE search_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    query TEXT NOT NULL,
    category TEXT,
    fechaBusqueda INTEGER NOT NULL,
    resultados INTEGER NOT NULL
);
```

**Propósito:** Guardar historial de búsquedas del usuario.

---

## 🎯 Funcionalidades Implementadas

### 1. **Favoritos de POIs**

#### Agregar a Favoritos

```kotlin
// En el ViewModel o Repositorio
viewModelScope.launch {
    val result = localPOIRepository.addToFavorites(poi)
    if (result.isSuccess) {
        Log.d("Demo", "✅ POI agregado a favoritos en SQLite")
    }
}
```

**Flujo:**
1. Usuario marca POI como favorito
2. `LocalPOIRepository.addToFavorites(poi)` se ejecuta
3. POI se convierte a `FavoritePOI` (entity)
4. `FavoritePOIDao.insertFavorite()` ejecuta INSERT en SQLite
5. POI se guarda en la tabla `favorite_pois`

#### Leer Favoritos

```kotlin
// En el ViewModel
localPOIRepository.getAllFavorites()
    .collect { favoritos ->
        Log.d("Demo", "📚 Favoritos desde SQLite: ${favoritos.size} POIs")
        favoritos.forEach { poi ->
            Log.d("Demo", "  - ${poi.nombre}")
        }
    }
```

**Flujo:**
1. `FavoritePOIDao.getAllFavorites()` ejecuta SELECT en SQLite
2. Room retorna `Flow<List<FavoritePOI>>`
3. Se convierte a `List<PointOfInterest>`
4. UI se actualiza automáticamente (Flow)

#### Eliminar de Favoritos

```kotlin
viewModelScope.launch {
    val result = localPOIRepository.removeFromFavorites(poiId)
    if (result.isSuccess) {
        Log.d("Demo", "❌ POI eliminado de favoritos")
    }
}
```

#### Verificar si es Favorito

```kotlin
val esFavorito = localPOIRepository.isFavorite(poiId)
Log.d("Demo", "¿Es favorito? $esFavorito")
```

---

### 2. **Caché de POIs**

#### Guardar en Caché

```kotlin
viewModelScope.launch {
    val result = localPOIRepository.cachePOI(poi)
    if (result.isSuccess) {
        Log.d("Demo", "💾 POI guardado en caché SQLite")
    }
}
```

**Características Especiales:**
- **Serialización JSON:** El POI completo se serializa a JSON y se guarda en `jsonData`
- **Límite de Caché:** Solo se mantienen los 50 POIs más recientes
- **Uso Offline:** Los POIs en caché están disponibles sin conexión

#### Leer desde Caché

```kotlin
localPOIRepository.getCachedPOIs(limit = 50)
    .collect { pois ->
        Log.d("Demo", "📦 POIs desde caché: ${pois.size}")
    }
```

#### Limpiar Caché Antiguo

```kotlin
viewModelScope.launch {
    // Elimina POIs en caché con más de 7 días
    val result = localPOIRepository.cleanOldCache(maxAgeDays = 7)
    if (result.isSuccess) {
        Log.d("Demo", "🧹 Caché antiguo limpiado")
    }
}
```

---

### 3. **Historial de Búsquedas**

#### Guardar Búsqueda

```kotlin
viewModelScope.launch {
    val result = localSearchRepository.saveSearch(
        query = "restaurantes",
        category = "COMIDA",
        resultados = 15
    )
    if (result.isSuccess) {
        Log.d("Demo", "🔍 Búsqueda guardada en historial")
    }
}
```

#### Leer Historial Reciente

```kotlin
localSearchRepository.getRecentSearches(limit = 10)
    .collect { searches ->
        Log.d("Demo", "📜 Historial de búsquedas: ${searches.size}")
        searches.forEach { search ->
            Log.d("Demo", "  - ${search.query} (${search.resultados} resultados)")
        }
    }
```

#### Obtener Solo Queries

```kotlin
localSearchRepository.getRecentSearchQueries(limit = 10)
    .collect { queries ->
        Log.d("Demo", "Búsquedas recientes: ${queries.joinToString(", ")}")
    }
```

#### Limpiar Historial

```kotlin
viewModelScope.launch {
    val result = localSearchRepository.clearHistory()
    if (result.isSuccess) {
        Log.d("Demo", "🗑️ Historial limpiado")
    }
}
```

---

## 🔬 Cómo Demostrar en la Aplicación

### Opción 1: Logs en Logcat

Todos los métodos de los repositorios ya tienen logs implementados:

```kotlin
// LocalPOIRepository.kt
Log.d("LocalPOIRepository", "POI agregado a favoritos: ${poi.id}")
Log.d("LocalPOIRepository", "POI guardado en caché: ${poi.id}")

// LocalSearchRepository.kt
Log.d("LocalSearchRepository", "Búsqueda guardada en historial: $query")
```

**Para ver los logs:**
1. Ejecutar la aplicación
2. Abrir Logcat en Android Studio
3. Filtrar por "LocalPOIRepository" o "LocalSearchRepository"
4. Interactuar con la aplicación (agregar favoritos, buscar, etc.)

### Opción 2: Database Inspector (Android Studio)

1. **Ejecutar la aplicación** en modo debug
2. **Ir a:** `View` → `Tool Windows` → `App Inspection`
3. **Seleccionar:** `Database Inspector`
4. **Explorar tablas:**
   - `favorite_pois`
   - `cached_pois`
   - `search_history`
5. **Ver datos en tiempo real** mientras usas la aplicación

### Opción 3: Device File Explorer

1. **Ir a:** `View` → `Tool Windows` → `Device File Explorer`
2. **Navegar a:** `data` → `data` → `com.example.points` → `databases`
3. **Descargar archivo:** `points_database`
4. **Abrir con DB Browser for SQLite** (https://sqlitebrowser.org/)

---

## 📝 Ejemplo Completo de Uso

### Escenario: Usuario Marca un POI como Favorito

```kotlin
// 1. Usuario toca botón de favorito en la UI
Button(onClick = {
    viewModel.toggleFavorite(poi)
}) {
    Icon(Icons.Default.Favorite, "Favorito")
}

// 2. ViewModel procesa la acción
fun toggleFavorite(poi: PointOfInterest) {
    viewModelScope.launch {
        val isFavorite = localPOIRepository.isFavorite(poi.id)
        
        if (isFavorite) {
            // Eliminar de favoritos
            localPOIRepository.removeFromFavorites(poi.id)
            Log.d("POIViewModel", "❌ POI eliminado de favoritos: ${poi.nombre}")
        } else {
            // Agregar a favoritos
            localPOIRepository.addToFavorites(poi)
            Log.d("POIViewModel", "✅ POI agregado a favoritos: ${poi.nombre}")
        }
        
        // Actualizar UI
        _isFavorite.value = !isFavorite
    }
}

// 3. LocalPOIRepository ejecuta la operación
suspend fun addToFavorites(poi: PointOfInterest): Result<Unit> {
    return withContext(Dispatchers.IO) {
        try {
            // Convertir PointOfInterest → FavoritePOI
            val favorite = poi.toFavoritePOI()
            
            // Insertar en SQLite usando Room
            favoriteDao.insertFavorite(favorite)
            
            Log.d("LocalPOIRepository", "POI agregado a favoritos: ${poi.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("LocalPOIRepository", "Error al agregar POI a favoritos", e)
            Result.failure(e)
        }
    }
}

// 4. Room ejecuta el INSERT en SQLite
@Dao
interface FavoritePOIDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoritePOI)
}

// 5. Datos guardados en SQLite
// Tabla: favorite_pois
// poiId | nombre | descripcion | categoria | direccion | lat | lon | ...
// ----------------------------------------------------------------
// poi123 | Restaurante | Comida rica | COMIDA | Calle 123 | 40.4 | -3.7 | ...
```

---

## 🔄 Flujo de Datos Completo

### Online (con Firebase):

```
Usuario interactúa
    ↓
UI (Compose)
    ↓
ViewModel
    ↓
PointOfInterestRepository (Firebase)
    ↓
LocalPOIRepository (Room/SQLite) ← Caché local
    ↓
SQLite Database
```

### Offline (solo SQLite):

```
Usuario interactúa
    ↓
UI (Compose)
    ↓
ViewModel
    ↓
LocalPOIRepository (Room/SQLite)
    ↓
SQLite Database
```

---

## ✅ Verificación de la Implementación

### Checklist de Funcionalidades

- [x] **Room Database configurado** (`PointsDatabase.kt`)
- [x] **3 Entidades creadas** (FavoritePOI, CachedPOI, SearchHistory)
- [x] **3 DAOs implementados** con operaciones CRUD
- [x] **2 Repositorios locales** (LocalPOIRepository, LocalSearchRepository)
- [x] **Convertidores de tipos** (Timestamp ↔ Long)
- [x] **Operaciones asíncronas** con coroutines
- [x] **Flow para actualizaciones en tiempo real**
- [x] **Logs para debugging**
- [x] **Manejo de errores** con Result<T>

### Dependencias Gradle

```kotlin
// Room para almacenamiento local
val roomVersion = "2.6.1"
implementation("androidx.room:room-runtime:$roomVersion")
implementation("androidx.room:room-ktx:$roomVersion")
ksp("androidx.room:room-compiler:$roomVersion")

// Gson para serialización JSON
implementation("com.google.code.gson:gson:2.10.1")
```

---

## 🎮 Comandos ADB para Inspección

### Ver archivos de la base de datos:

```bash
adb shell "run-as com.example.points ls -la /data/data/com.example.points/databases/"
```

### Exportar la base de datos:

```bash
adb shell "run-as com.example.points cat /data/data/com.example.points/databases/points_database" > points_database.db
```

### Ver tablas (usando sqlite3):

```bash
sqlite3 points_database.db ".tables"
```

### Ver datos de favoritos:

```bash
sqlite3 points_database.db "SELECT * FROM favorite_pois;"
```

### Ver datos de caché:

```bash
sqlite3 points_database.db "SELECT poiId, nombre, fechaCache FROM cached_pois ORDER BY fechaCache DESC LIMIT 10;"
```

### Ver historial de búsquedas:

```bash
sqlite3 points_database.db "SELECT * FROM search_history ORDER BY fechaBusqueda DESC LIMIT 10;"
```

---

## 📊 Estadísticas de la Base de Datos

### Contar registros:

```sql
-- Favoritos
SELECT COUNT(*) FROM favorite_pois;

-- Caché
SELECT COUNT(*) FROM cached_pois;

-- Historial
SELECT COUNT(*) FROM search_history;
```

### Ver tamaño de la base de datos:

```bash
adb shell "run-as com.example.points du -h /data/data/com.example.points/databases/points_database"
```

---

## 🎯 Casos de Uso Demostrados

### 1. **Funcionalidad Offline**
✅ Los favoritos y el caché funcionan sin conexión
✅ El historial de búsquedas persiste entre sesiones

### 2. **Persistencia de Datos**
✅ Los datos se mantienen después de cerrar la aplicación
✅ Los datos sobreviven a reinicios del dispositivo

### 3. **Sincronización Automática**
✅ Flow actualiza la UI automáticamente cuando cambian los datos
✅ No se requiere polling manual

### 4. **Rendimiento**
✅ Acceso rápido a datos locales (sin latencia de red)
✅ Consultas optimizadas con índices SQLite

### 5. **Seguridad de Tipos**
✅ Room genera código en tiempo de compilación
✅ Errores de consulta detectados en compilación

---

## 📚 Resumen de Archivos Clave

### Base de Datos:
- `app/src/main/java/com/example/points/database/PointsDatabase.kt`
- `app/src/main/java/com/example/points/database/Converters.kt`

### Entidades:
- `app/src/main/java/com/example/points/database/entity/FavoritePOI.kt`
- `app/src/main/java/com/example/points/database/entity/CachedPOI.kt`
- `app/src/main/java/com/example/points/database/entity/SearchHistory.kt`

### DAOs:
- `app/src/main/java/com/example/points/database/dao/FavoritePOIDao.kt`
- `app/src/main/java/com/example/points/database/dao/CachedPOIDao.kt`
- `app/src/main/java/com/example/points/database/dao/SearchHistoryDao.kt`

### Repositorios:
- `app/src/main/java/com/example/points/repository/LocalPOIRepository.kt`
- `app/src/main/java/com/example/points/repository/LocalSearchRepository.kt`

---

## 🎉 Conclusión

La implementación de **Room Database con SQLite** está **completamente funcional** y lista para demostración. La arquitectura sigue las mejores prácticas de Android con:

- ✅ Separación de responsabilidades (Entities, DAOs, Repositorios)
- ✅ Operaciones asíncronas con Coroutines
- ✅ Flujos reactivos con Flow
- ✅ Manejo robusto de errores
- ✅ Logs detallados para debugging
- ✅ Type-safe con Room

La base de datos SQLite proporciona almacenamiento local persistente, permitiendo que la aplicación funcione offline y mejorando significativamente la experiencia del usuario.

