# 🔗 Configuración de URL del Backend

## 📍 Información del Backend

Tu backend está desplegado en **Google Cloud Run** con la siguiente configuración:

- **Proyecto GCP**: `conexionpostgres`
- **Nombre del Servicio**: `mysyncapp-backend`
- **Región**: `us-central1`
- **Endpoints**:
  - `POST /api/v1/sync/push` - Enviar cambios al servidor
  - `GET /api/v1/sync/pull` - Obtener cambios del servidor

---

## 🔍 Cómo Obtener la URL del Backend

### Método 1: Usando gcloud CLI (Recomendado)

```bash
# Desde la carpeta Backend-Postgres
cd Backend-Postgres

# Obtener la URL del servicio
gcloud run services describe mysyncapp-backend --region us-central1 --format 'value(status.url)'
```

**Salida esperada**:
```
https://mysyncapp-backend-xxxxx-uc.a.run.app
```

### Método 2: Desde la Consola de Google Cloud

1. Ir a: https://console.cloud.google.com/run
2. Seleccionar proyecto: `conexionpostgres`
3. Buscar el servicio: `mysyncapp-backend`
4. Hacer clic en el servicio
5. Copiar la **URL** que aparece en la parte superior

### Método 3: Verificar Logs del Despliegue

Si ya desplegaste el backend, la URL debería aparecer al final del comando de despliegue:

```bash
Service [mysyncapp-backend] revision [mysyncapp-backend-00001-xxx] has been deployed and is serving 100 percent of traffic.
Service URL: https://mysyncapp-backend-xxxxx-uc.a.run.app
```

---

## ⚙️ Configurar en la App Android

### Paso 1: Obtener la URL

Usa uno de los métodos anteriores para obtener la URL. Debe tener este formato:
```
https://mysyncapp-backend-[HASH]-uc.a.run.app
```

### Paso 2: Agregar al archivo .env

Edita el archivo `.env` en `app/src/main/assets/.env` y agrega:

```bash
BACKEND_BASE_URL=https://mysyncapp-backend-xxxxx-uc.a.run.app/
```

**⚠️ IMPORTANTE**:
- Reemplaza `xxxxx` con el hash real de tu servicio
- La URL **DEBE terminar con `/`** (barra diagonal)
- Si no tienes el archivo `.env`, créalo en `app/src/main/assets/.env`

### Paso 3: Verificar Configuración

Después de agregar la URL, **reinstala la app** para que cargue el nuevo `.env`:

```bash
# Limpiar y reinstalar
./gradlew clean
./gradlew installDebug
```

---

## 🧪 Probar la Conexión

### Desde la Terminal

```bash
# Probar endpoint pull
curl "https://TU_URL_BACKEND/api/v1/sync/pull?userId=test"

# Probar endpoint push (requiere body JSON)
curl -X POST "https://TU_URL_BACKEND/api/v1/sync/push" \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"test","userId":"test","favorites":[],"cached":[],"searchHistory":[]}'
```

### Desde la App

1. Ir a **Perfil** → **Sincronización**
2. Presionar **"Sincronizar Ahora"**
3. Verificar en Logcat (tag: `SyncWorker` o `RemoteSyncRepository`)

---

## 📝 Ejemplo de .env Completo

```bash
# Google Maps
GOOGLE_MAPS_API_KEY=tu_clave_google_maps

# Firebase
FIREBASE_PROJECT_ID=tu_proyecto_firebase
FIREBASE_API_KEY=tu_clave_firebase
FIREBASE_APP_ID=tu_app_id

# OpenWeatherMap
OPENWEATHER_API_KEY=tu_clave_openweather

# Google Gemini (opcional)
GEMINI_API_KEY=tu_clave_gemini

# Backend de Sincronización
BACKEND_BASE_URL=https://mysyncapp-backend-xxxxx-uc.a.run.app/

# Environment
ENVIRONMENT=development
DEBUG_MODE=true
```

---

## 🐛 Solución de Problemas

### Error: "Unable to resolve host"
- Verifica que la URL esté correcta
- Asegúrate de que termine con `/`
- Verifica que el servicio esté desplegado y activo

### Error: "Connection refused" o "Timeout"
- Verifica que el servicio Cloud Run esté activo
- Revisa los logs del backend: `gcloud run services logs read mysyncapp-backend --region us-central1`

### Error: "404 Not Found"
- Verifica que la URL sea correcta
- Asegúrate de que el endpoint sea `/api/v1/sync/pull` o `/api/v1/sync/push`

### La URL no se carga
- Verifica que el archivo `.env` esté en `app/src/main/assets/.env`
- Reinstala la app después de modificar `.env`
- Verifica en Logcat que `EnvironmentConfig` cargue la variable

---

## 📚 Referencias

- [Documentación de Cloud Run](https://cloud.google.com/run/docs)
- [Guía de Despliegue del Backend](../Backend-Postgres/DEPLOY.md)

---

**Última actualización**: Diciembre 2024

