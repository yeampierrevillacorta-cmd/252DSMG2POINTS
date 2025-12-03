# ✅ Checklist: Verificación del Backend para Android

## 🎯 Información Crítica que Necesitas Verificar

### 1. ⚠️ Configuración de Seguridad (MÁS IMPORTANTE)

**Archivo a revisar en el backend:** `SecurityConfig.java`

**Busca esta línea:**
```java
.requestMatchers("/api/v1/sync/**")
```

**¿Qué dice después?**
- ❌ Si dice `.authenticated()` → **Problema:** Necesitas cambiar a `.permitAll()` o agregar autenticación JWT
- ✅ Si dice `.permitAll()` → **Correcto:** No necesitas cambios adicionales

**Solución rápida (para desarrollo):**
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/sync/**").permitAll()  // ← Cambiar aquí
    .anyRequest().permitAll()
)
```

---

### 2. 📋 Estructura de los DTOs del Backend

**Archivos a revisar en el backend:**
- `SyncRequest.java`
- `SyncResponse.java`
- `FavoritePOIDto.java`

**Compara con Android (ya tenemos en `SyncApiService.kt`):**

#### SyncRequest - Verificar campos:
```java
// Backend (Java)
public class SyncRequest {
    private String deviceId;        // ✅ Coincide
    private String userId;          // ✅ Coincide
    private String lastSyncAt;      // ⚠️ Verificar tipo (String vs LocalDateTime)
    private List<FavoritePOIDto> favorites;  // ✅ Coincide
    // ... otros campos
}
```

#### FavoritePOIDto - Verificar campos:
```java
// Backend (Java)
public class FavoritePOIDto {
    private String poiId;           // ✅ Coincide
    private String userId;          // ✅ Coincide
    private String nombre;          // ✅ Coincide
    private String descripcion;     // ✅ Coincide (nullable)
    private String categoria;      // ✅ Coincide (nullable)
    // ... otros campos
}
```

**⚠️ Verificar especialmente:**
- Nombres de campos (case-sensitive: `poiId` vs `poi_id`)
- Tipos de datos (String, Double, Boolean)
- Campos opcionales vs requeridos

---

### 3. 🔗 Probar Endpoints con curl

**Ejecuta estos comandos y comparte las respuestas:**

#### Probar PULL:
```bash
curl -v -X GET "https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/pull?userId=test123&lastSyncAt=" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"
```

**¿Qué respuesta obtienes?**
- ✅ 200 OK → Endpoint funciona
- ❌ 403 Forbidden → Problema de seguridad (ver punto 1)
- ❌ 404 Not Found → URL incorrecta
- ❌ 500 Error → Problema en el backend

#### Probar PUSH:
```bash
curl -v -X POST "https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/push" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "deviceId": "test-device",
    "userId": "test-user",
    "favorites": []
  }'
```

**Comparte:**
- Código HTTP de respuesta
- Body de la respuesta (si hay)
- Headers de respuesta

---

### 4. 📅 Formato de Fechas

**Pregunta:** ¿Qué formato espera el backend para `lastSyncAt`?

**Opciones comunes:**
- ISO 8601: `"2024-12-01T10:30:00Z"` o `"2024-12-01T10:30:00"`
- Timestamp: `"1701427800"`
- Formato personalizado: `"2024-12-01 10:30:00"`

**Para verificar:**
- Revisa el código del backend donde se parsea `lastSyncAt`
- O prueba con diferentes formatos en curl

**En Android actualmente usamos:**
- Formato ISO 8601: `Instant.now().toString()` → `"2024-12-01T10:30:00Z"`

---

### 5. 🔍 Logs del Backend

**En Google Cloud Console:**
1. Ve a **Cloud Run** > **mysyncapp-backend** > **Logs**
2. Busca errores cuando Android hace peticiones
3. Comparte los últimos 20-30 logs relacionados con `/api/v1/sync/`

**Qué buscar:**
- Errores 403 (Forbidden)
- Errores de parsing JSON
- Errores de base de datos
- Mensajes de Spring Security

---

## 📤 Información que Debes Compartir

Para poder ayudarte a configurar Android correctamente, comparte:

### Mínimo Necesario:
1. ✅ **Código de `SecurityConfig.java`** (especialmente la parte de `/api/v1/sync/**`)
2. ✅ **Respuesta del curl de PULL** (código HTTP y body)
3. ✅ **Respuesta del curl de PUSH** (código HTTP y body)

### Muy Útil:
4. Estructura completa de `SyncRequest.java` del backend
5. Estructura completa de `SyncResponse.java` del backend
6. Estructura completa de `FavoritePOIDto.java` del backend
7. Logs del backend cuando Android hace una petición

### Opcional pero Útil:
8. Formato de fechas esperado
9. Configuración de CORS (si existe)
10. Variables de entorno de Cloud Run

---

## 🚀 Comandos Rápidos para Obtener Info

### Ver configuración de Cloud Run:
```bash
gcloud run services describe mysyncapp-backend \
  --region us-central1 \
  --format="value(spec.template.spec.containers[0].env)"
```

### Ver logs recientes:
```bash
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=mysyncapp-backend" \
  --limit 20 \
  --format="table(timestamp,severity,textPayload)"
```

### Verificar si permite acceso sin autenticación:
```bash
gcloud run services get-iam-policy mysyncapp-backend \
  --region us-central1
```

---

## ✅ Una Vez que Tengas la Info

Con esa información podré:
1. Ajustar los DTOs de Android para que coincidan exactamente
2. Configurar autenticación si es necesaria
3. Ajustar el formato de fechas
4. Corregir cualquier problema de mapeo de datos
5. Agregar manejo de errores específico del backend

---

**Última actualización:** Diciembre 2024

