# 📋 Datos Necesarios del Backend para Configurar Android

Este documento lista toda la información que necesitas obtener del backend Spring Boot y Cloud Run para configurar correctamente la aplicación Android.

## 🔗 Información de Cloud Run (Ya Disponible)

### ✅ Ya Tienes:
- **URL del Servicio**: `https://mysyncapp-backend-860998153214.us-central1.run.app`
- **Proyecto GCP**: `conexionpostgres`
- **Región**: `us-central1`
- **Instancia Cloud SQL**: `conexionpostgres:us-central1:mysyncapp-postgres`

## 🔍 Información que Necesitas Verificar/Obtener

### 1. Configuración de Seguridad del Backend

**Pregunta:** ¿El backend permite acceso sin autenticación o requiere JWT?

**Para verificar:**
- Revisa el archivo `SecurityConfig.java` en el backend
- Busca la línea: `.requestMatchers("/api/v1/sync/**")`
- Verifica si dice `.authenticated()` o `.permitAll()`

**Si dice `.authenticated()`:**
- Necesitas implementar autenticación JWT en Android
- O cambiar el backend a `.permitAll()` para desarrollo

**Si dice `.permitAll()`:**
- No necesitas hacer cambios adicionales en Android (ya está configurado)

---

### 2. Estructura de los Endpoints

**Necesitas verificar:**

#### Endpoint de PUSH:
- **URL completa**: `https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/push`
- **Método**: `POST`
- **Headers requeridos**: ¿Algún header especial además de Content-Type?
- **Body esperado**: ¿Coincide con `SyncRequest` que tenemos?

#### Endpoint de PULL:
- **URL completa**: `https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/pull`
- **Método**: `GET`
- **Query parameters**: 
  - `userId` (String) ✅ Ya configurado
  - `lastSyncAt` (String) ✅ Ya configurado
  - ¿Hay otros parámetros?

**Para verificar:**
```bash
# Probar endpoint PULL
curl -X GET "https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/pull?userId=test&lastSyncAt=" \
  -H "Content-Type: application/json"

# Probar endpoint PUSH
curl -X POST "https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/push" \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"test","userId":"test","favorites":[]}'
```

---

### 3. Estructura de los DTOs del Backend

**Necesitas verificar que los DTOs en el backend coincidan con los de Android:**

#### En el Backend (Java):
```java
// SyncRequest.java
public class SyncRequest {
    private String deviceId;
    private String userId;
    private String lastSyncAt;  // ¿Es String o LocalDateTime?
    private List<FavoritePOIDto> favorites;
    // ... otros campos
}

// FavoritePOIDto.java
public class FavoritePOIDto {
    private String poiId;
    private String userId;
    private String nombre;
    // ... otros campos
}
```

#### En Android (Kotlin) - Ya tenemos:
```kotlin
@Serializable
data class SyncRequest(
    val deviceId: String,
    val userId: String,
    val lastSyncAt: String? = null,
    val favorites: List<FavoritePOIDto> = emptyList(),
    // ...
)

@Serializable
data class FavoritePOIDto(
    val poiId: String,
    val userId: String,
    val nombre: String,
    // ...
)
```

**Verificar:**
- ✅ Nombres de campos coinciden (case-sensitive)
- ✅ Tipos de datos coinciden (String, Double, Boolean, etc.)
- ✅ Campos opcionales vs requeridos
- ✅ Formato de fechas (ISO 8601, timestamp, etc.)

---

### 4. Configuración de CORS (Si aplica)

**Pregunta:** ¿El backend tiene CORS configurado?

**Para verificar en el backend:**
- Busca configuración de CORS en `SecurityConfig.java` o `WebMvcConfig.java`
- Verifica si permite origen `*` o dominios específicos

**Si hay CORS:**
- Puede estar bloqueando peticiones desde Android
- Necesitas verificar los headers permitidos

---

### 5. Formato de Fechas/Timestamps

**Pregunta:** ¿Qué formato espera el backend para `lastSyncAt`?

**Opciones comunes:**
- ISO 8601: `"2024-12-01T10:30:00Z"`
- Timestamp Unix: `"1701427800"`
- Formato personalizado: `"2024-12-01 10:30:00"`

**Para verificar:**
- Revisa el código del backend donde se parsea `lastSyncAt`
- O prueba con diferentes formatos en curl

---

### 6. Respuestas de Error del Backend

**Necesitas saber:**
- ¿Qué estructura tienen los errores?
- ¿Qué códigos HTTP devuelve en diferentes situaciones?
- ¿Hay mensajes de error específicos?

**Ejemplo de lo que necesitas:**
```json
// Error 400
{
  "error": "Bad Request",
  "message": "userId is required"
}

// Error 500
{
  "error": "Internal Server Error",
  "message": "Database connection failed"
}
```

---

### 7. Variables de Entorno del Backend

**Para verificar en Cloud Run:**
```bash
gcloud run services describe mysyncapp-backend \
  --region us-central1 \
  --format="value(spec.template.spec.containers[0].env)"
```

**Información útil:**
- `SPRING_PROFILES_ACTIVE` - ¿Qué perfil está activo?
- `DB_PASSWORD` - No necesario para Android, pero confirma que está configurado
- `JWT_SECRET` - Solo si usas autenticación JWT

---

### 8. Logs del Backend

**Para verificar qué está pasando:**
1. Ve a Google Cloud Console
2. Cloud Run > mysyncapp-backend > Logs
3. Busca errores cuando Android hace peticiones

**Qué buscar:**
- Errores 403 (Forbidden)
- Errores 400 (Bad Request)
- Errores de parsing de JSON
- Errores de base de datos

---

## 📝 Checklist de Verificación

### Backend Spring Boot:
- [ ] Verificar `SecurityConfig.java` - ¿permite acceso sin autenticación?
- [ ] Verificar estructura de `SyncRequest.java` - ¿coincide con Android?
- [ ] Verificar estructura de `SyncResponse.java` - ¿coincide con Android?
- [ ] Verificar estructura de `FavoritePOIDto.java` - ¿coincide con Android?
- [ ] Verificar formato de fechas/timestamps esperado
- [ ] Verificar configuración de CORS (si aplica)
- [ ] Probar endpoints con curl/Postman

### Cloud Run:
- [ ] Verificar que el servicio esté desplegado y funcionando
- [ ] Verificar variables de entorno configuradas
- [ ] Verificar logs para errores
- [ ] Verificar que `--allow-unauthenticated` esté configurado

### Pruebas:
- [ ] Probar endpoint PULL con curl
- [ ] Probar endpoint PUSH con curl
- [ ] Verificar respuestas de éxito y error
- [ ] Verificar formato JSON de request/response

---

## 🔧 Comandos Útiles para Obtener Información

### Ver configuración de Cloud Run:
```bash
gcloud run services describe mysyncapp-backend \
  --region us-central1 \
  --format="yaml"
```

### Ver logs del backend:
```bash
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=mysyncapp-backend" \
  --limit 50 \
  --format json
```

### Probar endpoint directamente:
```bash
# PULL
curl -v -X GET "https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/pull?userId=test123&lastSyncAt=" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"

# PUSH
curl -v -X POST "https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/push" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "deviceId": "test-device-123",
    "userId": "test-user-123",
    "lastSyncAt": null,
    "favorites": [
      {
        "poiId": "poi-1",
        "userId": "test-user-123",
        "nombre": "Test POI",
        "descripcion": "Test description",
        "categoria": "RESTAURANTE",
        "direccion": "Test Address",
        "lat": 40.7128,
        "lon": -74.0060,
        "calificacion": 4.5,
        "deleted": false
      }
    ],
    "cached": [],
    "searchHistory": []
  }'
```

---

## 📧 Información que Debes Compartir

Si necesitas ayuda para configurar Android, comparte:

1. **Respuesta del curl de PULL** (completa, incluyendo headers)
2. **Respuesta del curl de PUSH** (completa, incluyendo headers)
3. **Código de `SecurityConfig.java`** (especialmente la parte de `/api/v1/sync/**`)
4. **Estructura de los DTOs del backend** (`SyncRequest.java`, `SyncResponse.java`, `FavoritePOIDto.java`)
5. **Logs del backend** cuando Android hace una petición (últimos 20-30 líneas)
6. **Cualquier error específico** que aparezca en los logs de Cloud Run

---

## 🎯 Prioridad de Verificación

### Alta Prioridad (Crítico):
1. ✅ **Configuración de SecurityConfig** - Determina si necesitas autenticación
2. ✅ **Estructura de DTOs** - Debe coincidir exactamente con Android
3. ✅ **Prueba con curl** - Confirma que los endpoints funcionan

### Media Prioridad:
4. Formato de fechas/timestamps
5. Configuración de CORS
6. Variables de entorno

### Baja Prioridad:
7. Logs detallados
8. Estructura de errores

---

**Última actualización:** Diciembre 2024

