# 🔄 Implementación de Sincronización Automática

## 📋 Resumen

Se ha implementado un sistema completo de sincronización automática que cumple con las **Unidades 6 y 7** del curso de Android Basics con Compose.

### ✅ Unidades Implementadas

- **Unidad 6: Data Persistence** - DataStore para preferencias
- **Unidad 7: WorkManager** - Tareas en segundo plano

---

## 🎯 Funcionalidades Implementadas

### 1. Sincronización Automática (WorkManager - Unidad 7)
- ✅ Sincronización periódica en segundo plano
- ✅ Configuración de frecuencia (15 min, 30 min, 1 hora, 2 horas, 4 horas)
- ✅ Restricciones de red (WiFi solo o cualquier conexión)
- ✅ Reintentos automáticos en caso de error
- ✅ Sincronización manual bajo demanda

### 2. DataStore (Unidad 6)
- ✅ Migración de SharedPreferences a DataStore
- ✅ Almacenamiento reactivo de preferencias
- ✅ Configuración de sincronización persistente

### 3. Integración con Backend
- ✅ Sincronización bidireccional (push/pull)
- ✅ Sincronización de favoritos
- ✅ Sincronización de caché de POIs
- ✅ Sincronización de historial de búsqueda

### 4. Interfaz de Usuario
- ✅ Pantalla de configuración de sincronización
- ✅ Indicadores de estado
- ✅ Botón de sincronización manual
- ✅ Historial de última sincronización

---

## 📁 Archivos Creados

### DataStore (Unidad 6)
```
sync/data/
  └── SyncPreferences.kt          # Gestor de preferencias con DataStore
```

### WorkManager (Unidad 7)
```
sync/worker/
  ├── SyncWorker.kt               # Worker para sincronización en segundo plano
  └── SyncWorkManager.kt          # Gestor de WorkManager
```

### Repositorio y Modelos
```
sync/repository/
  └── RemoteSyncRepository.kt     # Repositorio para API de sincronización

sync/model/
  └── SyncModels.kt               # DTOs para sincronización

sync/network/
  └── SyncApiService.kt           # Interfaz Retrofit para API
```

### ViewModel y UI
```
sync/viewmodel/
  └── SyncViewModel.kt            # ViewModel para gestión de estado

sync/screens/
  └── SyncSettingsScreen.kt       # Pantalla de configuración
```

---

## 🔧 Configuración Necesaria

### 1. Variables de Entorno

Agregar al archivo `.env` en `app/src/main/assets/.env`:

```bash
BACKEND_BASE_URL=https://mysyncapp-backend-xxxxx-uc.a.run.app/
```

**⚠️ IMPORTANTE**: Reemplaza `xxxxx` con el hash real de tu servicio Cloud Run.

#### Cómo Obtener la URL del Backend

El backend está desplegado en **Google Cloud Run** con estos datos:
- **Proyecto**: `conexionpostgres`
- **Servicio**: `mysyncapp-backend`
- **Región**: `us-central1`

**Opción 1: Usando gcloud CLI**
```bash
cd Backend-Postgres
gcloud run services describe mysyncapp-backend --region us-central1 --format 'value(status.url)'
```

**Opción 2: Desde la Consola de Google Cloud**
1. Ir a: https://console.cloud.google.com/run
2. Seleccionar proyecto: `conexionpostgres`
3. Buscar servicio: `mysyncapp-backend`
4. Copiar la URL que aparece (formato: `https://mysyncapp-backend-xxxxx-uc.a.run.app`)

**Opción 3: Verificar si ya está desplegado**
Si ya desplegaste el backend, la URL debería estar en los logs del despliegue o en la consola de Cloud Run.

**Nota**: La URL debe terminar con `/` (barra diagonal) para que Retrofit funcione correctamente.

### 2. Dependencias Agregadas

```kotlin
// WorkManager (Unidad 7)
implementation("androidx.work:work-runtime-ktx:2.9.0")

// DataStore (Unidad 6) - Ya estaba en dependencias
implementation("androidx.datastore:datastore-preferences:1.1.1")
```

---

## 🚀 Uso

### Acceder a Configuración de Sincronización

1. Ir a **Perfil** → Botón **"Sincronización"**
2. Configurar preferencias:
   - Activar/desactivar sincronización
   - Configurar frecuencia
   - Elegir WiFi solo o cualquier conexión
   - Sincronizar manualmente

### Sincronización Automática

- Se programa automáticamente al iniciar la app si está habilitada
- Se ejecuta en segundo plano según la frecuencia configurada
- Requiere conexión a internet
- Respeta restricciones de WiFi si está configurado

---

## 📊 Flujo de Sincronización

```
1. WorkManager programa trabajo periódico
   ↓
2. SyncWorker se ejecuta en segundo plano
   ↓
3. PULL: Obtiene cambios del servidor
   ↓
4. Actualiza base de datos local (Room)
   ↓
5. PUSH: Envía cambios locales al servidor
   ↓
6. Actualiza timestamp de última sincronización
   ↓
7. Notifica éxito/error
```

---

## 🎓 Conceptos del Curso Implementados

### Unidad 6: Data Persistence
- ✅ **DataStore**: Almacenamiento de preferencias reactivo
- ✅ **Room**: Base de datos local (ya existente)
- ✅ **Flows**: Datos reactivos desde DataStore

### Unidad 7: WorkManager
- ✅ **Worker**: Tarea en segundo plano
- ✅ **PeriodicWorkRequest**: Trabajo periódico
- ✅ **Constraints**: Restricciones de red y batería
- ✅ **WorkManager**: Gestión de trabajos en segundo plano

---

## 🔍 Archivos Modificados

1. `app/build.gradle.kts` - Agregada dependencia de WorkManager
2. `data/AppContainer.kt` - Agregados syncPreferences, remoteSyncRepository, syncWorkManager
3. `data/DefaultAppContainer.kt` - Configuración de Retrofit para backend
4. `PointsApplication.kt` - Inicialización de sincronización automática
5. `AppNavigation.kt` - Ruta para pantalla de sincronización
6. `constants/AppRoutes.kt` - Ruta SYNC_SETTINGS
7. `profile/ProfileScreen.kt` - Botón para acceder a sincronización
8. `utils/EnvironmentConfig.kt` - Variable BACKEND_BASE_URL

---

## ⚠️ Notas Importantes

1. **URL del Backend**: Debe configurarse en `.env` como `BACKEND_BASE_URL`
2. **Frecuencia Mínima**: WorkManager requiere mínimo 15 minutos para trabajos periódicos
3. **Autenticación**: La sincronización requiere usuario autenticado
4. **Permisos**: Se requieren permisos de internet (ya configurados)

---

## 🧪 Pruebas

Para probar la sincronización:

1. **Sincronización Manual**:
   - Ir a Perfil → Sincronización
   - Presionar "Sincronizar Ahora"

2. **Sincronización Automática**:
   - Activar sincronización automática
   - Configurar frecuencia
   - Esperar el tiempo configurado
   - Verificar logs en Logcat con tag "SyncWorker"

3. **Verificar Datos**:
   - Agregar favoritos localmente
   - Esperar sincronización
   - Verificar en backend que se hayan guardado

---

## 📚 Referencias del Curso

- [Unidad 6: Data Persistence](https://developer.android.com/courses/android-basics-compose/unit-6)
- [Unidad 7: WorkManager](https://developer.android.com/courses/android-basics-compose/unit-7)

---

**Implementado**: Diciembre 2024  
**Cumple con**: Unidades 6 y 7 de Android Basics con Compose

