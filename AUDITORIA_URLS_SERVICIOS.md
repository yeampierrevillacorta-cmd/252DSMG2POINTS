# 🔍 Auditoría de URLs de Servicios Externos y APIs

## Documento de Auditoría de Infraestructura

Este documento contiene una auditoría completa de todas las URLs de servicios externos, APIs del backend y endpoints de nube configurados en el proyecto **Points App**.

**Versión:** 1.0  
**Fecha:** 2025  
**Auditor:** Arquitecto de Software / DevOps

---

## 📊 Tabla Resumen de Servicios

| Categoría | Servicio / Proveedor | URL Base / Endpoint Detectado | Archivo/Clase donde se encuentra | Notas |
| :--- | :--- | :--- | :--- | :--- |
| **Backend API** | Google Cloud Run (Spring Boot) | `https://mysyncapp-backend-860998153214.us-central1.run.app/` | `EnvironmentConfig.kt:195`, `DefaultAppContainer.kt:42-45` | URL por defecto si no está en `.env`. Endpoints: `/api/v1/sync/push`, `/api/v1/sync/pull` |
| **Backend API** | Google Cloud Run (Spring Boot) | `https://mysyncapp-backend-xxxxx-uc.a.run.app/` | `DefaultAppContainer.kt:45` | URL placeholder en comentario (fallback) |
| **Almacenamiento** | Firebase Storage | `gs://points-eb13e.firebasestorage.app` | `google-services.json:5` | Bucket configurado en Firebase. Acceso mediante `FirebaseStorage.getInstance()` |
| **Almacenamiento** | Firebase Storage | `points-eb13e.firebasestorage.app` | `google-services.json:5`, `EnvironmentConfig.kt:168` | Variable `FIREBASE_STORAGE_BUCKET` (desde `.env`) |
| **Base de Datos** | Firebase Firestore | `https://points-eb13e.firebaseio.com` (implícito) | `FirebaseFirestore.getInstance()` | URL implícita basada en `project_id: points-eb13e` |
| **Autenticación** | Firebase Authentication | `https://identitytoolkit.googleapis.com` (implícito) | `FirebaseAuth.getInstance()` | URL implícita del SDK de Firebase |
| **Inteligencia Artificial** | Google Gemini API | `https://generativelanguage.googleapis.com/` | `DefaultAppContainer.kt:40`, `GeminiApiService.kt` | Endpoint: `v1beta/models/gemini-1.5-flash:generateContent` |
| **Inteligencia Artificial** | Google Gemini API Key | `AIzaSyAA...` (fallback hardcodeado) | `EnvironmentConfig.kt:187` | ⚠️ **SEGURIDAD**: API Key hardcodeada como fallback. Debe removerse en producción |
| **Mapas / Geo** | Google Maps API | `maps.googleapis.com` (implícito) | `AndroidManifest.xml:32`, `build.gradle.kts:59-62` | API Key desde `@string/google_maps_key` o `EnvironmentConfig.GOOGLE_MAPS_API_KEY` |
| **Mapas / Geo** | Google Maps API Key (Debug) | `AIzaSyA7...` | `app/src/debug/res/values/google_maps_api.xml:24` | ⚠️ **SEGURIDAD**: API Key hardcodeada en recursos. Solo para debug |
| **Mapas / Geo** | Google Maps Navigation | `https://maps.google.com/?q=lat,lon` | `ShareUtils.kt:36,139,206,309` | URLs para abrir Google Maps con coordenadas |
| **Clima** | OpenWeatherMap API | `https://api.openweathermap.org/` | `DefaultAppContainer.kt:39`, `WeatherApiService.kt:8` | Endpoint: `data/2.5/onecall` |
| **Clima** | OpenWeatherMap Icons | `https://openweathermap.org/img/wn/{icon}@2x.png` | `POIDetailScreen.kt:1015` | URLs de iconos de clima |
| **Firebase Config** | Firebase Project ID | `points-eb13e` | `google-services.json:4`, `EnvironmentConfig.kt:162` | Variable `FIREBASE_PROJECT_ID` |
| **Firebase Config** | Firebase API Key | `AIzaSyDD...` | `google-services.json:18` | ⚠️ **SEGURIDAD**: API Key visible en `google-services.json` (normal para Firebase) |
| **Firebase Config** | Firebase App ID | `1:1044569579247:android:32575788c9aebb6152c51a` | `google-services.json:10` | Identificador único de la app Android |
| **Firebase Config** | Firebase Project Number | `1044569579247` | `google-services.json:3`, `EnvironmentConfig.kt:165` | Variable `FIREBASE_PROJECT_NUMBER` |
| **Repositorios** | JitPack (Maven) | `https://jitpack.io` | `settings.gradle.kts:19` | Repositorio Maven para dependencias (Dashboard Theras) |

---

## 🔍 Detalles por Categoría

### Backend API (Spring Boot)

#### URL Principal
- **URL Base:** `https://mysyncapp-backend-860998153214.us-central1.run.app/`
- **Proveedor:** Google Cloud Run
- **Región:** `us-central1`
- **Configuración:**
  - Variable de entorno: `BACKEND_BASE_URL` (en `.env`)
  - Fallback hardcodeado: `EnvironmentConfig.kt:195`
  - Placeholder comentado: `DefaultAppContainer.kt:45`

#### Endpoints Detectados
1. **POST** `/api/v1/sync/push`
   - **Archivo:** `SyncApiService.kt:20`, `sync/network/SyncApiService.kt:19`
   - **Propósito:** Enviar cambios locales al servidor (favoritos, caché, historial)
   - **Autenticación:** JWT Bearer Token (Firebase Auth)

2. **GET** `/api/v1/sync/pull`
   - **Archivo:** `SyncApiService.kt:29`, `sync/network/SyncApiService.kt:25`
   - **Propósito:** Obtener cambios del servidor desde última sincronización
   - **Query Params:** `userId`, `lastSyncAt` (ISO 8601)
   - **Autenticación:** JWT Bearer Token (Firebase Auth)

#### Configuración de Retrofit
- **Cliente:** `backendRetrofit` en `DefaultAppContainer.kt:180-189`
- **Interceptores:**
  - `backendHeadersInterceptor`: Agrega headers comunes y JWT token
  - `loggingInterceptor`: Logging HTTP (solo en DEBUG)
- **Timeout:** 30 segundos (conexión, lectura, escritura)

---

### Firebase Services

#### Firebase Firestore
- **URL Implícita:** `https://points-eb13e.firebaseio.com`
- **Configuración:** `FirebaseFirestore.getInstance()`
- **Colecciones Detectadas:**
  - `puntos_interes` (POIs)
  - `incidentes` (Incidentes urbanos)
  - `eventos` (Eventos)
  - `users` (Usuarios)
- **Archivo:** Uso directo del SDK, sin URL explícita

#### Firebase Storage
- **Bucket:** `points-eb13e.firebasestorage.app`
- **URL Completa:** `gs://points-eb13e.firebasestorage.app`
- **Configuración:** `FirebaseStorage.getInstance()`
- **Rutas Detectadas:**
  - `brand_logos/` (logos de marcas)
  - `media/` (imágenes y videos)
- **Archivo:** `google-services.json:5`, `StorageRepository.kt:8`

#### Firebase Authentication
- **URL Implícita:** `https://identitytoolkit.googleapis.com`
- **Configuración:** `FirebaseAuth.getInstance()`
- **Métodos Usados:**
  - `createUserWithEmailAndPassword()`
  - `signInWithEmailAndPassword()`
  - `sendPasswordResetEmail()`
  - `getIdToken()` (para JWT en backend)

#### Firebase Configuración
- **Project ID:** `points-eb13e`
- **Project Number:** `1044569579247`
- **App ID:** `1:1044569579247:android:32575788c9aebb6152c51a`
- **API Key:** `AIzaSyDDzYoZoyr-1ee7RUZbYuWgI7aSpLG7ITo`
- **Archivo:** `google-services.json`

---

### Google Gemini API

#### URL Base
- **URL:** `https://generativelanguage.googleapis.com/`
- **Endpoint:** `v1beta/models/gemini-1.5-flash:generateContent`
- **Método:** POST
- **Query Param:** `key` (API Key)

#### Configuración
- **Archivo:** `DefaultAppContainer.kt:40,171-177`, `GeminiApiService.kt:20`
- **API Key:**
  - Variable: `GEMINI_API_KEY` (desde `.env`)
  - **⚠️ FALLO DE SEGURIDAD:** Fallback hardcodeado en `EnvironmentConfig.kt:187`
    ```kotlin
    "AIzaSyAA0Bd-Ppbk6GvTwbOPen4q_VuQs35AC5c"
    ```
  - **Recomendación:** Remover el fallback hardcodeado en producción

#### Uso
- Generación automática de descripciones para POIs
- Fallback a descripción predeterminada si la API falla

---

### Google Maps API

#### Configuración
- **URL Base:** `maps.googleapis.com` (implícito, manejado por SDK)
- **API Key Sources:**
  1. `@string/google_maps_key` (desde `res/values/google_maps_api.xml`)
  2. `EnvironmentConfig.GOOGLE_MAPS_API_KEY` (desde `.env`)

#### API Keys Detectadas
1. **Debug Key:** `AIzaSyA7evjmRwUEECBNBFaOiWgfxV8GEEzfBZQ`
   - **Archivo:** `app/src/debug/res/values/google_maps_api.xml:24`
   - **⚠️ SEGURIDAD:** Hardcodeada en recursos (solo para debug)

2. **Production Key:** Desde variable de entorno `GOOGLE_MAPS_API_KEY`

#### Funcionalidades
- Visualización de mapas (Google Maps Android SDK)
- Geocodificación y geocodificación inversa
- Navegación (abre Google Maps app)

#### URLs de Navegación
- **Formato:** `https://maps.google.com/?q={lat},{lon}`
- **Archivo:** `ShareUtils.kt:36,139,206,309`
- **Uso:** Abrir Google Maps con coordenadas para navegación

---

### OpenWeatherMap API

#### URL Base
- **URL:** `https://api.openweathermap.org/`
- **Endpoint:** `data/2.5/onecall`
- **Método:** GET

#### Query Parameters
- `lat`: Latitud
- `lon`: Longitud
- `appid`: API Key (desde `EnvironmentConfig.OPENWEATHER_API_KEY`)
- `units`: `metric` (por defecto)
- `exclude`: `minutely,hourly,daily,alerts` (por defecto)

#### Configuración
- **Archivo:** `DefaultAppContainer.kt:39,162-168`, `WeatherApiService.kt:8`
- **API Key:** Variable `OPENWEATHER_API_KEY` (desde `.env`)
- **Validación:** El sistema valida que la API key esté configurada antes de hacer peticiones

#### Iconos de Clima
- **URL Base:** `https://openweathermap.org/img/wn/`
- **Formato:** `{icon}@2x.png`
- **Archivo:** `POIDetailScreen.kt:1015`
- **Ejemplo:** `https://openweathermap.org/img/wn/01d@2x.png`

---

## ⚠️ Problemas de Seguridad Detectados

### 1. API Key Hardcodeada de Gemini (CRÍTICO)
- **Ubicación:** `EnvironmentConfig.kt:187`
- **Código:**
  ```kotlin
  return if (envValue.isEmpty()) {
      "AIzaSyAA0Bd-Ppbk6GvTwbOPen4q_VuQs35AC5c"  // ⚠️ HARDCODEADA
  } else {
      envValue
  }
  ```
- **Riesgo:** API Key expuesta en el código fuente
- **Recomendación:** 
  - Remover el fallback hardcodeado
  - Lanzar excepción si la API key no está configurada
  - Usar solo variables de entorno

### 2. API Key de Google Maps en Recursos Debug
- **Ubicación:** `app/src/debug/res/values/google_maps_api.xml:24`
- **Key:** `AIzaSyA7evjmRwUEECBNBFaOiWgfxV8GEEzfBZQ`
- **Riesgo:** API Key visible en recursos (aunque solo para debug)
- **Recomendación:**
  - Usar BuildConfig para diferentes keys según buildType
  - O usar solo variables de entorno

### 3. Firebase API Key en google-services.json
- **Ubicación:** `google-services.json:18`
- **Key:** `AIzaSyDDzYoZoyr-1ee7RUZbYuWgI7aSpLG7ITo`
- **Nota:** Esto es **normal** para Firebase, pero debe estar en `.gitignore`
- **Recomendación:** Verificar que `google-services.json` esté en `.gitignore`

---

## 📋 Variables de Entorno Requeridas

Las siguientes variables deben estar configuradas en el archivo `.env` (ubicado en `app/src/main/assets/.env`):

| Variable | Propósito | Ejemplo | Requerida |
| :--- | :--- | :--- | :--- |
| `GOOGLE_MAPS_API_KEY` | API Key de Google Maps | `AIza...` | ✅ Sí |
| `FIREBASE_PROJECT_ID` | ID del proyecto Firebase | `points-eb13e` | ✅ Sí |
| `FIREBASE_PROJECT_NUMBER` | Número del proyecto Firebase | `1044569579247` | ⚠️ Opcional |
| `FIREBASE_STORAGE_BUCKET` | Bucket de Firebase Storage | `points-eb13e.firebasestorage.app` | ⚠️ Opcional |
| `FIREBASE_API_KEY` | API Key de Firebase | `AIza...` | ✅ Sí |
| `FIREBASE_APP_ID` | ID de la aplicación Firebase | `1:1044569579247:android:...` | ✅ Sí |
| `OPENWEATHER_API_KEY` | API Key de OpenWeatherMap | `abc123...` | ✅ Sí |
| `GEMINI_API_KEY` | API Key de Google Gemini | `AIza...` | ⚠️ Opcional (con fallback) |
| `BACKEND_BASE_URL` | URL del backend Spring Boot | `https://...run.app/` | ⚠️ Opcional (con fallback) |
| `ENVIRONMENT` | Entorno de ejecución | `development` / `production` | ⚠️ Opcional |
| `DEBUG_MODE` | Modo debug | `true` / `false` | ⚠️ Opcional |

---

## 🔧 Configuración del Backend (Spring Boot)

### URLs de Base de Datos

#### Desarrollo (application-dev.yml)
- **URL:** `jdbc:postgresql://localhost:5432/mysyncapp_db`
- **Usuario:** `mysyncapp_user`
- **Archivo:** `Backend-Postgres/src/main/resources/application-dev.yml:3`

#### Producción (application-prod.yml)
- **URL:** `jdbc:postgresql:///${DB_NAME}?socketFactory=com.google.cloud.sql.postgres.SocketFactory&cloudSqlInstance=${CLOUD_SQL_CONNECTION_NAME}`
- **Tipo:** Google Cloud SQL (PostgreSQL)
- **Configuración:** Variables de entorno `DB_NAME`, `CLOUD_SQL_CONNECTION_NAME`
- **Archivo:** `Backend-Postgres/src/main/resources/application-prod.yml:5`

### Endpoints del Backend

#### Sincronización
- **Base URL:** Configurada en variable de entorno o fallback
- **Endpoints:**
  - `POST /api/v1/sync/push` - Enviar cambios al servidor
  - `GET /api/v1/sync/pull` - Obtener cambios del servidor

#### Seguridad
- **JWT Secret:** Variable de entorno `JWT_SECRET` (producción)
- **JWT Expiration:** 86400000 ms (24 horas)
- **Archivo:** `application-prod.yml:18-19`

---

## 📊 Resumen de Proveedores

| Proveedor | Servicios Utilizados | Cantidad |
| :--- | :--- | :--- |
| **Google Cloud Platform** | Cloud Run, Cloud SQL, Maps API, Gemini API | 4 |
| **Firebase (Google)** | Firestore, Storage, Authentication, Analytics | 4 |
| **OpenWeatherMap** | Weather API, Iconos | 2 |
| **JitPack** | Repositorio Maven | 1 |

---

## ✅ Recomendaciones

### Seguridad
1. **Remover API Keys Hardcodeadas:**
   - Eliminar fallback de Gemini API Key en `EnvironmentConfig.kt:187`
   - Usar BuildConfig para Google Maps API Key según buildType

2. **Verificar .gitignore:**
   - Asegurar que `.env` esté en `.gitignore`
   - Asegurar que `google-services.json` esté en `.gitignore` (o usar template)

3. **Variables de Entorno:**
   - Todas las API keys deben estar en `.env`
   - No usar valores hardcodeados como fallback

### Configuración
1. **Backend URL:**
   - Configurar `BACKEND_BASE_URL` en `.env` para producción
   - Remover URL hardcodeada de fallback

2. **Firebase:**
   - Verificar que todas las variables de Firebase estén en `.env`
   - El `google-services.json` debe estar actualizado

3. **Documentación:**
   - Documentar todas las variables de entorno requeridas
   - Crear `.env.example` con placeholders

---

## 📝 Notas Adicionales

### Diferencias Debug vs Release
- **Google Maps API Key:** Diferente key en `debug/res/values/google_maps_api.xml`
- **Logging:** HTTP logging solo en modo DEBUG (`BuildConfig.DEBUG`)
- **Backend URL:** Misma URL para ambos (debe configurarse según entorno)

### URLs Implícitas
Algunos servicios no tienen URLs explícitas porque se manejan mediante SDKs:
- **Firebase Firestore:** URL implícita basada en `project_id`
- **Firebase Authentication:** URL implícita del SDK
- **Google Maps:** URLs implícitas del SDK de Android

### Endpoints No Documentados
- Los endpoints del backend Spring Boot están documentados en:
  - `SyncApiService.kt`
  - `sync/network/SyncApiService.kt`
  - Backend: `SyncController.java`

---

**Documento generado mediante auditoría exhaustiva del código fuente y archivos de configuración del proyecto Points App.**

