# Ruta de la Base de Datos SQLite

## 📍 Ruta Exacta

La base de datos SQLite de Room se almacena en el directorio de datos privados de la aplicación:

### Ruta Principal

```
/data/data/com.example.points/databases/points_database
```

### Archivos Relacionados

Room crea automáticamente archivos adicionales para optimización:

1. **Base de datos principal:**
   ```
   /data/data/com.example.points/databases/points_database
   ```

2. **Shared Memory (archivo temporal):**
   ```
   /data/data/com.example.points/databases/points_database-shm
   ```

3. **Write-Ahead Log (WAL):**
   ```
   /data/data/com.example.points/databases/points_database-wal
   ```

---

## 📋 Información de la Base de Datos

### Configuración

- **Package Name:** `com.example.points`
- **Nombre de BD:** `points_database`
- **Versión:** `1`
- **Tablas:**
  - `favorite_pois` (POIs favoritos)
  - `search_history` (Historial de búsquedas)
  - `cached_pois` (Caché de POIs)

### Código de Configuración

```kotlin
// En PointsDatabase.kt (línea 40)
Room.databaseBuilder(
    context.applicationContext,
    PointsDatabase::class.java,
    "points_database"  // ← Nombre de la base de datos
)
```

---

## 🔍 Cómo Acceder a la Base de Datos

### Opción 1: Android Studio - Device File Explorer

1. **Abrir Android Studio**
2. **Conectar dispositivo/emulador**
3. **Ir a:** `View` → `Tool Windows` → `Device File Explorer`
4. **Navegar a:** `data` → `data` → `com.example.points` → `databases`
5. **Ver archivos:**
   - `points_database`
   - `points_database-shm`
   - `points_database-wal`

### Opción 2: ADB (Android Debug Bridge)

#### Ver archivos de la base de datos:

```bash
# Listar archivos en el directorio de databases
adb shell "run-as com.example.points ls -la /data/data/com.example.points/databases/"
```

#### Exportar la base de datos al PC:

```bash
# Exportar la base de datos principal
adb shell "run-as com.example.points cat /data/data/com.example.points/databases/points_database" > points_database.db

# Exportar el archivo WAL (si existe)
adb shell "run-as com.example.points cat /data/data/com.example.points/databases/points_database-wal" > points_database-wal

# Exportar el archivo SHM (si existe)
adb shell "run-as com.example.points cat /data/data/com.example.points/databases/points_database-shm" > points_database-shm
```

#### Exportar usando pull (requiere permisos root o aplicación debuggable):

```bash
# Con permisos root
adb root
adb pull /data/data/com.example.points/databases/points_database ./points_database.db
```

### Opción 3: Desde la Aplicación (Código)

Agregar un método para obtener la ruta de la base de datos:

```kotlin
// En PointsDatabase.kt o en una clase de utilidad
fun getDatabasePath(context: Context): String {
    return context.applicationContext.getDatabasePath("points_database").absolutePath
}

// Obtener el directorio de databases
fun getDatabasesDirectory(context: Context): String {
    return context.applicationContext.getDatabasePath("points_database").parent
}
```

**Log en la aplicación:**

```kotlin
// Agregar en PointsApplication.onCreate() o DefaultAppContainer
val dbPath = context.getDatabasePath("points_database").absolutePath
Log.d("Database", "Ruta de la base de datos: $dbPath")
```

---

## 🔐 Permisos Necesarios

### Para Acceder desde ADB:

1. **Aplicación en modo debug:** La aplicación debe estar en modo debug para usar `run-as`
2. **Permisos root:** Para acceder directamente sin `run-as` (requiere dispositivo root)

### Verificar si la aplicación es debuggable:

```bash
# Verificar si la aplicación es debuggable
adb shell "run-as com.example.points ls"
```

Si funciona, la aplicación es debuggable y puedes acceder a los archivos.

---

## 📊 Verificar que la Base de Datos Existe

### Desde ADB:

```bash
# Verificar si el directorio existe
adb shell "run-as com.example.points ls -la /data/data/com.example.points/databases/"

# Ver el tamaño de los archivos
adb shell "run-as com.example.points du -h /data/data/com.example.points/databases/"
```

### Desde Código:

```kotlin
// Verificar si la base de datos existe
val dbFile = context.getDatabasePath("points_database")
if (dbFile.exists()) {
    Log.d("Database", "Base de datos existe: ${dbFile.absolutePath}")
    Log.d("Database", "Tamaño: ${dbFile.length()} bytes")
} else {
    Log.d("Database", "Base de datos no existe aún")
}
```

---

## 🗄️ Inspeccionar la Base de Datos

### Opción 1: SQLite Browser

1. **Exportar la base de datos** (ver Opción 2 de ADB)
2. **Abrir con DB Browser for SQLite:**
   - Descargar: https://sqlitebrowser.org/
   - Abrir: `points_database.db`
   - Ver tablas y datos

### Opción 2: Android Studio - Database Inspector

1. **Abrir Android Studio**
2. **Ejecutar la aplicación en modo debug**
3. **Ir a:** `View` → `Tool Windows` → `App Inspection` → `Database Inspector`
4. **Seleccionar:** `points_database`
5. **Ver tablas y datos en tiempo real**

### Opción 3: Stetho (Facebook)

Agregar Stetho para inspección en Chrome:

```kotlin
// En build.gradle.kts
implementation("com.facebook.stetho:stetho:1.6.0")

// En PointsApplication.onCreate()
if (BuildConfig.DEBUG) {
    Stetho.initializeWithDefaults(this)
}
```

Luego abrir Chrome y navegar a: `chrome://inspect`

---

## 🧪 Verificar que la Base de Datos Funciona

### Agregar Logs en la Aplicación:

```kotlin
// En PointsDatabase.kt
fun getDatabase(context: Context): PointsDatabase {
    return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
            context.applicationContext,
            PointsDatabase::class.java,
            "points_database"
        )
            .fallbackToDestructiveMigration()
            .build()
        
        // Log de la ruta de la base de datos
        val dbPath = context.applicationContext.getDatabasePath("points_database")
        Log.d("PointsDatabase", "Base de datos creada en: ${dbPath.absolutePath}")
        Log.d("PointsDatabase", "Base de datos existe: ${dbPath.exists()}")
        
        INSTANCE = instance
        instance
    }
}
```

### Verificar en Logcat:

```
D/PointsDatabase: Base de datos creada en: /data/data/com.example.points/databases/points_database
D/PointsDatabase: Base de datos existe: true
```

---

## 📝 Comandos ADB Útiles

### Ver todas las bases de datos de la aplicación:

```bash
adb shell "run-as com.example.points ls -la /data/data/com.example.points/databases/"
```

### Ver el tamaño de la base de datos:

```bash
adb shell "run-as com.example.points du -h /data/data/com.example.points/databases/points_database"
```

### Eliminar la base de datos (para testing):

```bash
adb shell "run-as com.example.points rm /data/data/com.example.points/databases/points_database"
adb shell "run-as com.example.points rm /data/data/com.example.points/databases/points_database-shm"
adb shell "run-as com.example.points rm /data/data/com.example.points/databases/points_database-wal"
```

### Ver las tablas en la base de datos:

```bash
# Exportar la base de datos primero
adb shell "run-as com.example.points cat /data/data/com.example.points/databases/points_database" > points_database.db

# Usar sqlite3 para inspeccionar
sqlite3 points_database.db ".tables"
sqlite3 points_database.db "SELECT * FROM favorite_pois;"
sqlite3 points_database.db "SELECT * FROM search_history;"
sqlite3 points_database.db "SELECT * FROM cached_pois;"
```

---

## 🎯 Resumen

### Ruta Completa:

```
/data/data/com.example.points/databases/points_database
```

### Archivos:

- `points_database` - Base de datos principal
- `points_database-shm` - Shared Memory (temporal)
- `points_database-wal` - Write-Ahead Log (temporal)

### Acceso:

1. **Android Studio Device File Explorer** (más fácil)
2. **ADB con `run-as`** (requiere aplicación debuggable)
3. **ADB con root** (requiere dispositivo root)
4. **Database Inspector** (Android Studio)
5. **Stetho** (Chrome DevTools)

### Verificación:

- Verificar que los archivos existen
- Verificar el tamaño de los archivos
- Inspeccionar las tablas y datos
- Ver logs en Logcat

---

## ⚠️ Notas Importantes

1. **Permisos:** Solo la propia aplicación puede acceder a su directorio de datos privados
2. **Debug:** La aplicación debe estar en modo debug para usar `run-as` desde ADB
3. **Root:** Se requiere root para acceder directamente sin `run-as`
4. **WAL y SHM:** Estos archivos son temporales y se crean automáticamente por SQLite
5. **Backup:** Los archivos se incluyen en el backup automático de Android (si está configurado)

---

## 🔧 Solución de Problemas

### La base de datos no existe:

- Verificar que la aplicación se haya ejecutado al menos una vez
- Verificar que `PointsDatabase.getDatabase()` se haya llamado
- Verificar logs en Logcat para errores

### No se puede acceder desde ADB:

- Verificar que la aplicación esté en modo debug
- Verificar que el dispositivo esté conectado
- Verificar que `run-as` funcione: `adb shell "run-as com.example.points ls"`

### La base de datos está vacía:

- Verificar que se hayan insertado datos
- Verificar que no se haya eliminado la base de datos
- Verificar que no se esté usando `fallbackToDestructiveMigration()` que recrea la BD

---

## 📚 Referencias

- [Room Database - Android Developers](https://developer.android.com/training/data-storage/room)
- [ADB Command Reference](https://developer.android.com/studio/command-line/adb)
- [Database Inspector - Android Studio](https://developer.android.com/studio/inspector/database)
- [SQLite Documentation](https://www.sqlite.org/docs.html)

