# 📸 Flujo Completo: Análisis de Imágenes con IA

## 📋 Resumen del Flujo

```
Usuario selecciona foto → Sube a Firebase → Analiza con IA → Guarda en Firebase → Admin ve prioridad ALTA
```

---

## 📦 DEPENDENCIAS UTILIZADAS

### ⚠️ **Importante: No se agregaron nuevas dependencias**

Para el análisis de imágenes con IA, **se reutilizaron las dependencias que ya existían** en el proyecto. No fue necesario agregar ninguna librería nueva.

### ✅ **Dependencias Reutilizadas**

Estas dependencias ya estaban configuradas en `app/build.gradle.kts` para otros servicios (WeatherApiService, GeminiApiService):

- **Retrofit** (`com.squareup.retrofit2:retrofit:2.9.0`): Para realizar llamadas HTTP al servidor de IA
- **OkHttp** (`com.squareup.okhttp3:okhttp:4.12.0`): Cliente HTTP con soporte para multipart (envío de imágenes), conectarse a servidores, hacer solicitudes HTTP y recibir respuestas.
- **OkHttp Logging Interceptor** (`com.squareup.okhttp3:logging-interceptor:4.12.0`): Para logging de requests HTTP
- **Kotlinx Serialization** (`org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3`): Para parsear las respuestas JSON del servidor
- **Retrofit Converter** (`com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0`): Convierte respuestas JSON a objetos Kotlin

### ✨ **Lo que se creó nuevo**

- **`DetectionApiService.kt`**: Nueva interfaz Retrofit para comunicarse con el servidor de IA
- **`DetectionResponse.kt`**: Modelos de datos para parsear la respuesta del servidor
- Configuración adicional en `DefaultAppContainer.kt`: Nueva instancia de Retrofit para el servidor de IA

---

## 🔄 FLUJO DETALLADO PASO A PASO

### **PASO 1: Usuario Selecciona la Foto**
**Archivo:** `app/src/main/java/com/example/points/screens/CreateIncidentScreen.kt`

**Líneas:** 266-279

```kotlin
OutlinedButton(
    onClick = { imagePickerLauncher.launch("image/*") }
) {
    Icon(Icons.Default.CameraAlt)
    Text("Seleccionar imagen")
}
```

**¿Qué hace?**
- Muestra un botón "Seleccionar imagen"
- Al presionarlo, abre el selector de imágenes del sistema Android
- Cuando el usuario selecciona una foto, se guarda en el estado del ViewModel

**Código relacionado:**
- `imagePickerLauncher` (línea 49-53): Abre el selector de archivos
- `updateSelectedImage()` (línea 165): Guarda la URI de la imagen seleccionada

---

### **PASO 2: Usuario Presiona "Reportar Incidente"**
**Archivo:** `app/src/main/java/com/example/points/screens/CreateIncidentScreen.kt`

**Línea:** 332

```kotlin
Button(
    onClick = { viewModel.createIncident(context) }
) {
    Text("Reportar Incidente")
}
```

**¿Qué hace?**
- El usuario completa el formulario (tipo, descripción, ubicación)
- Presiona el botón "Reportar Incidente"
- Se ejecuta `viewModel.createIncident(context)`

---

### **PASO 3: ViewModel Inicia el Proceso**
**Archivo:** `app/src/main/java/com/example/points/viewmodel/IncidentViewModel.kt`

**Método:** `createIncident(context: Context)` - Línea 168

**¿Qué hace?**
1. Valida que tenga descripción y ubicación
2. Si hay imagen seleccionada, inicia el proceso de análisis

---

### **PASO 4: Subir Imagen a Firebase Storage**
**Archivo:** `app/src/main/java/com/example/points/repository/IncidentRepository.kt`

**Método:** `uploadImage(uri: Uri)` - Línea 238

**¿Qué hace?**
```kotlin
suspend fun uploadImage(uri: Uri): Result<String> {
    val fileName = "incidents/${UUID.randomUUID()}.jpg"
    val storageRef = storage.reference.child(fileName)
    val uploadTask = storageRef.putFile(uri).await()
    val downloadUrl = storageRef.downloadUrl.await()
    return Result.success(downloadUrl.toString())
}
```

**Proceso:**
1. Genera un nombre único para la imagen
2. Sube la imagen a Firebase Storage
3. Obtiene la URL pública de descarga
4. Retorna la URL para guardarla en el incidente

**Llamado desde:** `IncidentViewModel.kt` línea 201

---

### **PASO 5: Analizar Imagen con IA**
**Archivo:** `app/src/main/java/com/example/points/repository/IncidentRepository.kt`

**Método:** `analyzeImageForThreats(uri: Uri, context: Context)` - Línea 252

**¿Qué hace?**

1. **Preparar la imagen:**
   ```kotlin
   // Convierte URI a archivo temporal
   val inputStream = context.contentResolver.openInputStream(uri)
   val tempFile = File.createTempFile("detection_", ".jpg", context.cacheDir)
   inputStream.copyTo(tempFile.outputStream())
   ```

2. **Crear el request multipart:**
   ```kotlin
   val requestFile = tempFile.asRequestBody(mimeType.toMediaType())
   val imagePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
   ```

3. **Enviar a tu servidor:**
   ```kotlin
   val response = detectionApiService.detectarAmenazas(apiKey, imagePart)
   ```

**Archivo del servicio:** `app/src/main/java/com/example/points/network/DetectionApiService.kt`

```kotlin
@Multipart
@POST("detectar")
suspend fun detectarAmenazas(
    @Header("X-API-Key") apiKey: String,
    @Part imagePart: MultipartBody.Part
): DetectionResponse
```

**¿Qué envía?**
- **URL:** `https://api-seguridad-ciudadana-1044569579247.us-central1.run.app/detectar`
- **Método:** POST
- **Header:** `X-API-Key: Jhonell042799*`
- **Body:** Multipart con el archivo de imagen

---

### **PASO 6: Procesar Respuesta del Servidor**
**Archivo:** `app/src/main/java/com/example/points/models/detection/DetectionResponse.kt`

**Modelo de respuesta:**
```kotlin
data class DetectionResponse(
    val cantidad_amenazas: Int,        // Ej: 1
    val cantidad_objetos: Int,         // Ej: 1
    val detalles: List<DetalleDeteccion>, // Array con objetos detectados
    val estado: String                 // "exito"
)

data class DetalleDeteccion(
    val objeto: String,      // "knife"
    val confianza: Double,   // 0.88
    val es_amenaza: Boolean  // true
)
```

**Ejemplo de respuesta real:**
```json
{
  "cantidad_amenazas": 1,
  "cantidad_objetos": 1,
  "detalles": [
    {
      "objeto": "knife",
      "confianza": 0.88,
      "es_amenaza": true
    }
  ],
  "estado": "exito"
}
```

**Archivo:** `app/src/main/java/com/example/points/viewmodel/IncidentViewModel.kt`

**Líneas:** 216-228

```kotlin
if (analysisResult.isSuccess) {
    val detection = analysisResult.getOrNull()
    if (detection != null) {
        // Determinar prioridad basada en cantidad de amenazas
        if (detection.cantidad_amenazas > 0) {
            prioridad = "ALTA"
            val primeraAmenaza = detection.detalles.firstOrNull()
            etiqueta_ia = primeraAmenaza?.objeto  // "knife"
        } else {
            prioridad = "BAJA"
        }
    }
}
```

**¿Qué determina?**
- Si `cantidad_amenazas > 0` → `prioridad = "ALTA"` y `etiqueta_ia = "knife"`
- Si `cantidad_amenazas == 0` → `prioridad = "BAJA"` y `etiqueta_ia = null`

---

### **PASO 7: Guardar Incidente en Firebase**
**Archivo:** `app/src/main/java/com/example/points/viewmodel/IncidentViewModel.kt`

**Líneas:** 258-268

```kotlin
val incident = Incident(
    tipo = currentState.tipo.displayName,
    descripcion = currentState.descripcion,
    fotoUrl = imageUrl,              // URL de Firebase Storage
    ubicacion = currentState.ubicacion,
    fechaHora = Timestamp.now(),
    estado = EstadoIncidente.PENDIENTE,
    prioridad = prioridad,           // "ALTA" o "BAJA"
    etiqueta_ia = etiqueta_ia        // "knife" o null
)

repository.createIncident(incident)
```

**Archivo del modelo:** `app/src/main/java/com/example/points/models/Incident.kt`

**Campos nuevos agregados:**
```kotlin
data class Incident(
    // ... campos existentes ...
    val prioridad: String? = null,      // "ALTA", "MEDIA", "BAJA"
    val etiqueta_ia: String? = null     // "knife", "pistol", etc.
)
```

**Archivo:** `app/src/main/java/com/example/points/repository/IncidentRepository.kt`

**Método:** `createIncident(incident: Incident)` - Línea 202

**¿Qué hace?**
- Guarda el incidente en Firestore (colección "incidentes")
- Incluye los campos `prioridad` y `etiqueta_ia`

**Datos guardados en Firebase:**
```json
{
  "tipo": "Inseguridad",
  "descripcion": "...",
  "fotoUrl": "https://firebasestorage.../image.jpg",
  "ubicacion": { "lat": ..., "lon": ... },
  "fechaHora": Timestamp,
  "estado": "Pendiente",
  "prioridad": "ALTA",        ← NUEVO
  "etiqueta_ia": "knife"      ← NUEVO
}
```

---

### **PASO 8: Panel de Administrador - Ordenar por Prioridad**
**Archivo:** `app/src/main/java/com/example/points/screens/AdminIncidentsScreen.kt`

**Líneas:** 46-64

**¿Qué hace?**
```kotlin
val filteredIncidents = remember(...) {
    val filtered = uiState.incidents.filter { ... }
    
    // Ordenar por prioridad (ALTA primero) y luego por fecha
    filtered.sortedWith(
        compareByDescending<Incident> { incident ->
            when (incident.prioridad?.uppercase()) {
                "ALTA" -> 3    // Los de ALTA aparecen primero
                "MEDIA" -> 2
                "BAJA" -> 1
                else -> 0
            }
        }.thenByDescending { it.fechaHora.toDate().time }
    )
}
```

**Resultado:** Los incidentes con `prioridad: "ALTA"` aparecen al inicio de la lista.

---

### **PASO 9: Mostrar Badge de Prioridad**
**Archivo:** `app/src/main/java/com/example/points/screens/AdminIncidentsScreen.kt`

**Función:** `IncidentAdminCard()` - Línea 329

**Líneas:** 410-450 (aproximadamente)

**¿Qué hace?**
- Muestra un badge rojo "Prioridad: ALTA" para incidentes con amenazas
- Muestra un badge "IA: knife" indicando qué objeto detectó la IA
- Usa colores diferentes según la prioridad:
  - **ALTA** = Rojo
  - **MEDIA** = Naranja
  - **BAJA** = Verde

```kotlin
// Badge de prioridad
incident.prioridad?.let { prioridad ->
    val priorityColor = when (prioridad.uppercase()) {
        "ALTA" -> Color(0xFFFF5252)
        "MEDIA" -> Color(0xFFFFA726)
        "BAJA" -> Color(0xFF66BB6A)
    }
    // Muestra badge con color
}

// Badge de etiqueta IA
incident.etiqueta_ia?.let { etiqueta ->
    // Muestra "IA: knife"
}
```

---

## 📁 ARCHIVOS CLAVE INVOLUCRADOS

### **1. Interfaz de Usuario (UI)**
- **`CreateIncidentScreen.kt`**: Pantalla donde el usuario selecciona la foto y presiona "Reportar"

### **2. Lógica de Negocio (ViewModel)**
- **`IncidentViewModel.kt`**: Orquesta todo el proceso (subir, analizar, guardar)

### **3. Acceso a Datos (Repository)**
- **`IncidentRepository.kt`**: 
  - `uploadImage()`: Sube a Firebase Storage
  - `analyzeImageForThreats()`: Envía a tu servidor IA
  - `createIncident()`: Guarda en Firestore

### **4. Comunicación con Servidor**
- **`DetectionApiService.kt`**: Define la interfaz Retrofit para llamar a tu API
- **`DefaultAppContainer.kt`**: Configura Retrofit con la URL base

### **5. Modelos de Datos**
- **`Incident.kt`**: Modelo con campos `prioridad` y `etiqueta_ia`
- **`DetectionResponse.kt`**: Modelo para parsear respuesta del servidor

### **6. Visualización (Admin)**
- **`AdminIncidentsScreen.kt`**: Ordena y muestra incidentes con badges de prioridad

---

## 🔗 FLUJO COMPLETO RESUMIDO

```
1. Usuario → CreateIncidentScreen.kt
   └─> Selecciona foto de galería

2. Usuario presiona "Reportar Incidente"
   └─> CreateIncidentScreen.kt → IncidentViewModel.createIncident()

3. IncidentViewModel.kt
   ├─> repository.uploadImage() → Sube a Firebase Storage
   │   └─> IncidentRepository.kt (uploadImage)
   │
   └─> repository.analyzeImageForThreats() → Analiza con IA
       └─> IncidentRepository.kt (analyzeImageForThreats)
           ├─> Prepara imagen (URI → File temporal)
           ├─> DetectionApiService.detectarAmenazas()
           │   └─> POST a: https://api-seguridad-ciudadana-1044569579247.us-central1.run.app/detectar
           │       └─> Tu servidor Python/YOLO analiza
           │           └─> Retorna: { cantidad_amenazas: 1, detalles: [{ objeto: "knife" }] }
           │
           └─> Procesa respuesta
               └─> Si cantidad_amenazas > 0 → prioridad = "ALTA", etiqueta_ia = "knife"

4. IncidentViewModel.kt
   └─> Crea objeto Incident con prioridad y etiqueta_ia
       └─> repository.createIncident() → Guarda en Firestore

5. Firestore
   └─> Documento guardado con:
       {
         prioridad: "ALTA",
         etiqueta_ia: "knife",
         fotoUrl: "...",
         ...
       }

6. AdminIncidentsScreen.kt
   ├─> getAllIncidents() lee de Firestore (incluye prioridad y etiqueta_ia)
   ├─> Ordena: ALTA primero (valor 3), luego MEDIA (2), luego BAJA (1)
   └─> Muestra badges visuales:
       └─> "Prioridad: ALTA" (badge rojo)
       └─> "IA: knife" (badge informativo)
```

---

## 🎯 PUNTOS CLAVE PARA EXPLICAR

1. **Análisis automático:** Cuando el usuario presiona "Reportar", la app automáticamente envía la foto a tu servidor de IA.

2. **Detección inteligente:** Tu servidor Python/YOLO analiza la imagen y detecta si hay armas u objetos peligrosos.

3. **Priorización automática:** Si detecta amenazas (`cantidad_amenazas > 0`), el incidente se marca como `prioridad: "ALTA"`.

4. **Información detallada:** Se guarda qué objeto se detectó (`etiqueta_ia: "knife"`).

5. **Visualización para admin:** El panel de administrador ordena automáticamente mostrando primero los incidentes con prioridad ALTA.

6. **Badges visuales:** Se muestran badges rojos para alertar al administrador sobre incidentes urgentes.

---

## 📊 DIAGRAMA DEL FLUJO

```
[Usuario]
    ↓ Selecciona foto
[CreateIncidentScreen]
    ↓ Presiona "Reportar Incidente"
[IncidentViewModel.createIncident()]
    ↓
    ├─→ [IncidentRepository.uploadImage()]
    │       ↓
    │   [Firebase Storage] ← Imagen guardada
    │
    └─→ [IncidentRepository.analyzeImageForThreats()]
            ↓
        [DetectionApiService]
            ↓ POST con imagen
        [Tu Servidor Python/YOLO]
            ↓ Analiza imagen
            ↓ Detecta: knife, confianza: 0.88
        [DetectionResponse]
            ↓ cantidad_amenazas: 1
        [IncidentViewModel]
            ↓ Calcula: prioridad = "ALTA", etiqueta_ia = "knife"
        [IncidentRepository.createIncident()]
            ↓
        [Firestore]
            ↓ Documento guardado con prioridad y etiqueta_ia
        [AdminIncidentsScreen]
            ↓ Ordena por prioridad (ALTA primero)
            ↓ Muestra badges visuales
        [Administrador ve incidente urgente]
```

---

## 🛠️ TECNOLOGÍAS UTILIZADAS

- **Retrofit:** Para llamadas HTTP a tu servidor
- **OkHttp:** Cliente HTTP con soporte multipart
- **Firebase Storage:** Almacenamiento de imágenes
- **Firestore:** Base de datos para incidentes
- **Kotlin Coroutines:** Operaciones asíncronas
- **Kotlinx Serialization:** Parseo de JSON



## ✅ RESULTADO FINAL

El administrador ve:
1. ✅ Incidentes ordenados por prioridad (ALTA primero)
2. ✅ Badge rojo "Prioridad: ALTA" en incidentes urgentes
3. ✅ Badge "IA: knife" indicando qué detectó la inteligencia artificial
4. ✅ Incidentes sin amenazas con prioridad BAJA aparecen al final

