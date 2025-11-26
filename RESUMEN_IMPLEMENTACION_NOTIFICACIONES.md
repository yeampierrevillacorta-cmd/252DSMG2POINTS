# 📱 Resumen: Implementación de Notificaciones de Incidentes Cercanos

## 🎯 Objetivo

Implementar un sistema de notificaciones automáticas que alerta a los usuarios sobre incidentes cercanos a su ubicación actual.

---

## 📋 Requisitos

1. ✅ Identificar incidentes cercanos (radio configurable: 1km, 3km, 5km)
2. ✅ Notificación con: tipo, descripción, ubicación
3. ✅ Configurar categorías de incidentes (todos o específicos)
4. ✅ Funcionar en segundo plano

---

## 🔄 Arquitectura de Solución

### Opción 1: Notificaciones Locales (Recomendada para empezar)

```
Usuario abre app
    ↓
WorkManager se programa (cada 15 minutos)
    ↓
Worker obtiene ubicación actual
    ↓
Worker consulta incidentes en Firestore
    ↓
Worker filtra incidentes cercanos
    ↓
Worker muestra notificación local
```

**Ventajas:**
- ✅ Más simple de implementar
- ✅ No requiere Cloud Functions
- ✅ Funciona sin servidor

**Desventajas:**
- ⚠️ No es tiempo real (hay retraso)
- ⚠️ Consume batería del dispositivo
- ⚠️ Requiere que la app esté instalada

### Opción 2: Notificaciones con FCM + Cloud Functions (Más escalable)

```
Admin confirma incidente en Firestore
    ↓
Cloud Function se activa automáticamente
    ↓
Cloud Function calcula usuarios cercanos
    ↓
Cloud Function envía notificación FCM
    ↓
Usuario recibe notificación push
```

**Ventajas:**
- ✅ Tiempo real (notificación inmediata)
- ✅ No consume batería del dispositivo
- ✅ Funciona aunque la app esté cerrada
- ✅ Más escalable

**Desventajas:**
- ⚠️ Requiere Cloud Functions (más complejo)
- ⚠️ Requiere configuración en Firebase Console

---

## 📝 Lo que TÚ debes hacer (Fuera del Proyecto)

### ✅ Paso 1: Firebase Console - Habilitar FCM

1. Ve a: https://console.firebase.google.com/
2. Selecciona tu proyecto
3. Ve a: **Project Settings** → **Cloud Messaging**
4. Haz clic en **"Enable Cloud Messaging"** (si no está habilitado)
5. **Copia y guarda:**
   - **Sender ID**
   - **Server Key** (si planeas usar Cloud Functions)

**📄 Documento detallado:** Ver `PASOS_CONFIGURACION_FIREBASE.md` (Paso 1)

---

### ✅ Paso 2: Firebase Console - Configurar Reglas de Firestore

1. En Firebase Console, ve a: **Firestore Database** → **Rules**
2. **Reemplaza** las reglas existentes con:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Usuarios: solo el usuario puede leer/escribir
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      match /notificationSettings {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /fcmToken {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /location {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
    
    // Incidentes: lectura pública de confirmados
    match /incidentes/{incidentId} {
      allow read: if resource.data.estado == "Confirmado";
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null;
    }
  }
}
```

3. Haz clic en **"Publish"** (Publicar)

**📄 Documento detallado:** Ver `PASOS_CONFIGURACION_FIREBASE.md` (Paso 2)

---

### ✅ Paso 3: (Opcional) Configurar Cloud Functions

**Solo si quieres notificaciones en tiempo real:**

1. Instalar Firebase CLI:
   ```bash
   npm install -g firebase-tools
   ```

2. Inicializar Functions:
   ```bash
   firebase login
   firebase init functions
   ```

3. Crear función (código proporcionado en `IMPLEMENTACION_NOTIFICACIONES_INCIDENTES.md`)

4. Desplegar:
   ```bash
   firebase deploy --only functions
   ```

**📄 Documento detallado:** Ver `PASOS_CONFIGURACION_FIREBASE.md` (Paso 4)

---

## 🛠️ Lo que YO implementaré (En el Proyecto)

### ✅ Fase 1: Dependencias y Permisos

**Archivos a modificar:**
1. `app/build.gradle.kts` - Agregar dependencias FCM y WorkManager
2. `app/src/main/AndroidManifest.xml` - Agregar permisos y servicios

**Cambios:**
- ✅ Agregar `firebase-messaging`
- ✅ Agregar `work-runtime-ktx`
- ✅ Agregar permisos de notificaciones y ubicación en segundo plano
- ✅ Registrar servicio de notificaciones FCM

---

### ✅ Fase 2: Modelos y Configuración

**Archivos a crear:**
1. `app/src/main/java/com/example/points/models/NotificationSettings.kt`
2. `app/src/main/java/com/example/points/models/NotificationRadius.kt`

**Funcionalidad:**
- ✅ Modelo de configuración de notificaciones
- ✅ Enum de radios (1km, 3km, 5km)
- ✅ Gestión de categorías habilitadas

---

### ✅ Fase 3: Servicios y Workers

**Archivos a crear:**
1. `app/src/main/java/com/example/points/services/IncidentNotificationService.kt`
2. `app/src/main/java/com/example/points/workers/IncidentMonitoringWorker.kt`
3. `app/src/main/java/com/example/points/utils/NotificationHelper.kt`
4. `app/src/main/java/com/example/points/utils/WorkManagerHelper.kt`

**Funcionalidad:**
- ✅ Servicio FCM para recibir notificaciones push
- ✅ Worker para monitorear incidentes en segundo plano
- ✅ Helper para mostrar notificaciones locales
- ✅ Helper para programar trabajos con WorkManager

---

### ✅ Fase 4: Repositorios

**Archivos a crear:**
1. `app/src/main/java/com/example/points/repository/NotificationSettingsRepository.kt`

**Funcionalidad:**
- ✅ Guardar/cargar configuración de notificaciones
- ✅ Guardar token FCM en Firestore
- ✅ Guardar ubicación del usuario en Firestore

---

### ✅ Fase 5: ViewModels y Pantallas

**Archivos a crear:**
1. `app/src/main/java/com/example/points/viewmodel/NotificationSettingsViewModel.kt`
2. `app/src/main/java/com/example/points/screens/NotificationSettingsScreen.kt`

**Funcionalidad:**
- ✅ ViewModel para gestión de configuración
- ✅ Pantalla de configuración de notificaciones
- ✅ Toggle para habilitar/deshabilitar
- ✅ Selector de radio (1km, 3km, 5km)
- ✅ Checkboxes para categorías

---

### ✅ Fase 6: Integración

**Archivos a modificar:**
1. `app/src/main/java/com/example/points/PointsApplication.kt`
2. `app/src/main/java/com/example/points/data/PreferencesManager.kt`
3. `app/src/main/java/com/example/points/repository/IncidentRepository.kt`

**Funcionalidad:**
- ✅ Inicializar WorkManager al iniciar la app
- ✅ Registrar token FCM al iniciar sesión
- ✅ Actualizar ubicación del usuario periódicamente
- ✅ Agregar función para calcular distancia geográfica

---

## 🔄 Flujo Completo de Funcionamiento

### Flujo 1: Configuración Inicial

```
1. Usuario inicia sesión
   ↓
2. App registra token FCM en Firestore
   ↓
3. App obtiene ubicación actual
   ↓
4. App guarda ubicación en Firestore
   ↓
5. App programa WorkManager (cada 15 minutos)
```

### Flujo 2: Monitoreo de Incidentes (WorkManager)

```
1. WorkManager ejecuta Worker (cada 15 minutos)
   ↓
2. Worker obtiene ubicación actual del usuario
   ↓
3. Worker obtiene configuración de notificaciones
   ↓
4. Worker consulta incidentes confirmados en Firestore
   ↓
5. Worker filtra incidentes cercanos (por radio y categoría)
   ↓
6. Worker filtra incidentes nuevos (no notificados antes)
   ↓
7. Worker muestra notificación para cada incidente nuevo
   ↓
8. Worker actualiza timestamp de última verificación
```

### Flujo 3: Notificación Push (si usas Cloud Functions)

```
1. Admin confirma incidente en Firestore
   ↓
2. Cloud Function se activa automáticamente
   ↓
3. Cloud Function obtiene usuarios con notificaciones habilitadas
   ↓
4. Cloud Function calcula distancia para cada usuario
   ↓
5. Cloud Function filtra usuarios cercanos
   ↓
6. Cloud Function envía notificación FCM a usuarios cercanos
   ↓
7. Usuario recibe notificación push
   ↓
8. Usuario hace clic en notificación
   ↓
9. App abre detalles del incidente
```

---

## 📊 Estructura de Datos

### Firestore: `users/{userId}/notificationSettings`

```json
{
  "enabled": true,
  "radiusKm": 3.0,
  "enabledCategories": ["Inseguridad", "Accidente de Tránsito"],
  "lastCheckedTimestamp": 1234567890
}
```

### Firestore: `users/{userId}/fcmToken`

```json
{
  "token": "fcm_token_aqui",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### Firestore: `users/{userId}/location`

```json
{
  "lat": 40.7128,
  "lon": -74.0060,
  "timestamp": 1234567890
}
```

---

## 🎯 Plan de Implementación

### Fase 1: Configuración de Firebase (TÚ)

- [ ] Habilitar Firebase Cloud Messaging
- [ ] Configurar reglas de seguridad de Firestore
- [ ] (Opcional) Configurar Cloud Functions

### Fase 2: Implementación Básica (YO)

- [ ] Agregar dependencias
- [ ] Agregar permisos
- [ ] Crear modelos de configuración
- [ ] Crear servicio de notificaciones FCM
- [ ] Crear Worker para monitoreo
- [ ] Crear helper de notificaciones

### Fase 3: Configuración de Usuario (YO)

- [ ] Crear repositorio de configuración
- [ ] Crear ViewModel de configuración
- [ ] Crear pantalla de configuración
- [ ] Integrar con la app existente

### Fase 4: Integración Completa (YO)

- [ ] Integrar con PointsApplication
- [ ] Registrar token FCM al iniciar sesión
- [ ] Actualizar ubicación periódicamente
- [ ] Programar WorkManager
- [ ] Probar funcionalidad completa

---

## 🚀 Pasos Inmediatos

### Para TÍ (Ahora):

1. ✅ **Abre Firebase Console**
2. ✅ **Habilita Cloud Messaging** (ver `PASOS_CONFIGURACION_FIREBASE.md`)
3. ✅ **Configura reglas de Firestore** (ver `PASOS_CONFIGURACION_FIREBASE.md`)
4. ✅ **Notifícame** cuando hayas completado estos pasos

### Para MÍ (Después de que completes los pasos):

1. ✅ **Implementaré** todo el código en Android Studio
2. ✅ **Crearé** todos los archivos necesarios
3. ✅ **Integraré** con la app existente
4. ✅ **Probaré** la funcionalidad completa

---

## 📚 Documentos de Referencia

1. **`IMPLEMENTACION_NOTIFICACIONES_INCIDENTES.md`** - Implementación completa detallada
2. **`PASOS_CONFIGURACION_FIREBASE.md`** - Pasos específicos en Firebase Console
3. **`RESUMEN_IMPLEMENTACION_NOTIFICACIONES.md`** - Este documento (resumen ejecutivo)

---

## ❓ Preguntas Frecuentes

### ¿Necesito Cloud Functions?

**Respuesta:** No es obligatorio. Puedes usar solo notificaciones locales con WorkManager. Cloud Functions es recomendado para notificaciones en tiempo real.

### ¿Cuánto tiempo tomará la implementación?

**Respuesta:** 
- Configuración en Firebase: 15-30 minutos
- Implementación en Android: 2-4 horas
- Pruebas: 1-2 horas

### ¿Funcionará si la app está cerrada?

**Respuesta:** 
- Con WorkManager: Sí, pero con retraso (cada 15 minutos)
- Con Cloud Functions + FCM: Sí, en tiempo real

### ¿Consumirá mucha batería?

**Respuesta:** 
- WorkManager: Consumo moderado (verifica cada 15 minutos)
- FCM: Consumo mínimo (solo cuando hay notificaciones)

---

## 🎯 Conclusión

### Lo que TÚ debes hacer:

1. ✅ Habilitar Firebase Cloud Messaging
2. ✅ Configurar reglas de Firestore
3. ✅ (Opcional) Configurar Cloud Functions

### Lo que YO implementaré:

1. ✅ Todo el código en Android Studio
2. ✅ Servicios, Workers, ViewModels, Pantallas
3. ✅ Integración completa con la app existente

### Resultado Final:

- ✅ Notificaciones automáticas de incidentes cercanos
- ✅ Configuración de radio y categorías
- ✅ Funcionamiento en segundo plano
- ✅ Integración completa con la app

---

*Documento creado como resumen ejecutivo de la implementación de notificaciones de incidentes cercanos.*

