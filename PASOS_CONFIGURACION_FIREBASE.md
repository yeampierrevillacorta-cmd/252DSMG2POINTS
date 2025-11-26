# 🔥 Pasos de Configuración en Firebase Console

## 📋 Lo que DEBES HACER fuera del Proyecto

Este documento detalla **paso a paso** qué debes hacer en Firebase Console para habilitar las notificaciones de incidentes cercanos.

---

## ✅ Paso 1: Habilitar Firebase Cloud Messaging (FCM)

### 1.1 Abrir Firebase Console

1. Ve a: https://console.firebase.google.com/
2. Inicia sesión con tu cuenta de Google
3. Selecciona tu proyecto (o créalo si no existe)

### 1.2 Habilitar Cloud Messaging

1. En el menú lateral izquierdo, ve a: **⚙️ Project Settings** (Configuración del proyecto)
2. Ve a la pestaña **Cloud Messaging**
3. Si Cloud Messaging no está habilitado:
   - Haz clic en **"Enable Cloud Messaging"** o **"Habilitar Cloud Messaging"**
   - Espera a que se habilite (puede tomar unos segundos)

### 1.3 Obtener Credenciales (Importante)

**En la misma página de Cloud Messaging, encontrarás:**

1. **Sender ID**: Cópialo, lo necesitarás para Cloud Functions (si las usas)
2. **Server Key**: Cópialo, lo necesitarás para Cloud Functions (si las usas)
3. **Cloud Messaging API (V1)**: Asegúrate de que esté habilitada

**⚠️ Nota:** Guarda estas credenciales en un lugar seguro. **NO las compartas públicamente**.

---

## ✅ Paso 2: Configurar Reglas de Seguridad de Firestore

### 2.1 Abrir Firestore Database

1. En Firebase Console, ve a: **Firestore Database** (Base de datos Firestore)
2. Ve a la pestaña **Rules** (Reglas)

### 2.2 Agregar Reglas de Seguridad

**Reemplaza las reglas existentes con estas:**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Usuarios: solo el usuario puede leer/escribir su propio documento
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Configuración de notificaciones: solo el usuario
      match /notificationSettings {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Token FCM: solo el usuario
      match /fcmToken {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Ubicación del usuario: solo el usuario
      match /location {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
    
    // Incidentes: lectura pública de confirmados, escritura autenticada
    match /incidentes/{incidentId} {
      // Cualquiera puede leer incidentes confirmados
      allow read: if resource.data.estado == "Confirmado";
      
      // Solo usuarios autenticados pueden crear incidentes
      allow create: if request.auth != null;
      
      // Solo administradores pueden actualizar/eliminar
      allow update, delete: if request.auth != null 
        && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.tipoUsuario == "MODERADOR";
    }
  }
}
```

### 2.3 Publicar Reglas

1. Haz clic en **"Publish"** (Publicar)
2. Espera a que se publiquen las reglas (puede tomar unos segundos)

---

## ✅ Paso 3: Configurar Índices de Firestore (Opcional pero Recomendado)

### 3.1 Crear Índice Compuesto

**Para mejorar el rendimiento de las consultas:**

1. En Firestore Database, ve a la pestaña **Indexes** (Índices)
2. Haz clic en **"Create Index"** (Crear Índice)
3. Configura el índice:
   - **Collection ID**: `incidentes`
   - **Fields to index**:
     - `estado` (Ascending)
     - `ubicacion.lat` (Ascending)
     - `ubicacion.lon` (Ascending)
     - `fechaHora` (Descending)
   - **Query scope**: Collection
4. Haz clic en **"Create"** (Crear)
5. Espera a que se cree el índice (puede tomar varios minutos)

---

## ✅ Paso 4: Configurar Firebase Cloud Functions (Opcional)

### 4.1 Instalar Firebase CLI

**En tu computadora (fuera de Android Studio):**

```bash
# Instalar Node.js si no lo tienes
# Descarga desde: https://nodejs.org/

# Instalar Firebase CLI
npm install -g firebase-tools

# Verificar instalación
firebase --version
```

### 4.2 Iniciar Sesión en Firebase

```bash
firebase login
```

### 4.3 Inicializar Firebase Functions

```bash
# Navegar a la raíz de tu proyecto Android
cd C:\Users\USER\Desktop\github\252DSMG2POINTS

# Inicializar Firebase Functions
firebase init functions

# Selecciona:
# - TypeScript o JavaScript (recomiendo JavaScript para simplicidad)
# - Instalar dependencias? (Sí)
```

### 4.4 Crear Función para Notificaciones

**Archivo:** `functions/index.js`

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendIncidentNotifications = functions.firestore
  .document('incidentes/{incidentId}')
  .onCreate(async (snap, context) => {
    const incident = snap.data();
    
    // Solo enviar notificaciones para incidentes confirmados
    if (incident.estado !== 'Confirmado') {
      console.log('Incidente no confirmado, no se envía notificación');
      return null;
    }
    
    console.log('Procesando incidente confirmado:', context.params.incidentId);
    
    try {
      // Obtener todos los usuarios con notificaciones habilitadas
      const usersSnapshot = await admin.firestore()
        .collectionGroup('notificationSettings')
        .where('enabled', '==', true)
        .get();
      
      console.log(`Usuarios con notificaciones habilitadas: ${usersSnapshot.size}`);
      
      const nearbyUsers = [];
      
      // Para cada usuario, verificar si está cerca del incidente
      for (const userDoc of usersSnapshot.docs) {
        const userId = userDoc.ref.parent.parent.id;
        const settings = userDoc.data();
        
        // Obtener ubicación del usuario
        const userLocationDoc = await admin.firestore()
          .doc(`users/${userId}/location`)
          .get();
        
        if (!userLocationDoc.exists) {
          console.log(`Usuario ${userId} no tiene ubicación registrada`);
          continue;
        }
        
        const userLocation = userLocationDoc.data();
        
        // Calcular distancia
        const distance = calculateDistance(
          userLocation.lat,
          userLocation.lon,
          incident.ubicacion.lat,
          incident.ubicacion.lon
        );
        
        console.log(`Usuario ${userId}: distancia = ${distance.toFixed(2)} km, radio = ${settings.radiusKm} km`);
        
        // Verificar si está dentro del radio
        if (distance <= settings.radiusKm) {
          // Verificar que el tipo de incidente esté habilitado
          const enabledCategories = settings.enabledCategories || [];
          if (enabledCategories.includes(incident.tipo) || enabledCategories.length === 0) {
            // Obtener token FCM
            const tokenDoc = await admin.firestore()
              .doc(`users/${userId}/fcmToken`)
              .get();
            
            if (tokenDoc.exists) {
              const token = tokenDoc.data().token;
              nearbyUsers.push({
                userId,
                token,
                distance
              });
            }
          }
        }
      }
      
      console.log(`Usuarios cercanos encontrados: ${nearbyUsers.length}`);
      
      // Enviar notificaciones
      if (nearbyUsers.length > 0) {
        const messages = nearbyUsers.map(user => ({
          notification: {
            title: `⚠️ Incidente Cercano: ${incident.tipo}`,
            body: incident.descripcion.length > 100 
              ? incident.descripcion.substring(0, 100) + '...'
              : incident.descripcion
          },
          data: {
            incidentId: context.params.incidentId,
            tipo: incident.tipo,
            descripcion: incident.descripcion,
            lat: incident.ubicacion.lat.toString(),
            lon: incident.ubicacion.lon.toString(),
            direccion: incident.ubicacion.direccion || ''
          },
          token: user.token,
          android: {
            priority: 'high'
          }
        }));
        
        const response = await admin.messaging().sendAll(messages);
        console.log(`Notificaciones enviadas: ${response.successCount}/${messages.length}`);
        
        if (response.failureCount > 0) {
          console.error('Errores al enviar notificaciones:', response.responses
            .map((resp, idx) => resp.success ? null : `Usuario ${nearbyUsers[idx].userId}: ${resp.error}`)
            .filter(Boolean)
          );
        }
      }
      
      return null;
    } catch (error) {
      console.error('Error en función de notificaciones:', error);
      return null;
    }
  });

// Función auxiliar para calcular distancia (fórmula de Haversine)
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371; // Radio de la Tierra en km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}
```

### 4.5 Instalar Dependencias

```bash
cd functions
npm install firebase-functions firebase-admin
```

### 4.6 Desplegar Función

```bash
# Desde la raíz del proyecto
firebase deploy --only functions
```

**⚠️ Nota:** La primera vez que despliegues, puede tardar varios minutos.

---

## ✅ Paso 5: Verificar Configuración

### 5.1 Verificar Cloud Messaging

1. En Firebase Console, ve a: **Cloud Messaging**
2. Verifica que esté habilitado
3. Verifica que tengas las credenciales (Sender ID, Server Key)

### 5.2 Verificar Reglas de Firestore

1. En Firestore Database, ve a: **Rules**
2. Verifica que las reglas estén publicadas
3. Prueba las reglas con el simulador (opcional)

### 5.3 Verificar Cloud Functions (si las usas)

1. En Firebase Console, ve a: **Functions**
2. Verifica que la función `sendIncidentNotifications` esté desplegada
3. Verifica que esté activa (status: "Active")

---

## 📊 Estructura de Datos en Firestore

### Colección: `incidentes`

```json
{
  "id": "incident123",
  "tipo": "Inseguridad",
  "descripcion": "Robo en la calle principal",
  "ubicacion": {
    "lat": 40.7128,
    "lon": -74.0060,
    "direccion": "Calle Principal 123"
  },
  "estado": "Confirmado",
  "fechaHora": "2024-01-01T00:00:00Z",
  "usuarioId": "user123"
}
```

### Colección: `users/{userId}/notificationSettings`

```json
{
  "enabled": true,
  "radiusKm": 3.0,
  "enabledCategories": ["Inseguridad", "Accidente de Tránsito"],
  "lastCheckedTimestamp": 1234567890
}
```

### Colección: `users/{userId}/fcmToken`

```json
{
  "token": "fcm_token_aqui",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### Colección: `users/{userId}/location`

```json
{
  "lat": 40.7128,
  "lon": -74.0060,
  "timestamp": 1234567890
}
```

---

## 🎯 Resumen de Pasos

### ✅ Pasos Obligatorios:

1. ✅ Habilitar Firebase Cloud Messaging
2. ✅ Configurar reglas de seguridad de Firestore
3. ✅ Obtener credenciales (Sender ID, Server Key)

### ✅ Pasos Opcionales (pero Recomendados):

4. ✅ Crear índices de Firestore
5. ✅ Configurar Firebase Cloud Functions

---

## 🔍 Verificación Post-Configuración

### Checklist de Verificación:

- [ ] Cloud Messaging está habilitado
- [ ] Reglas de Firestore están publicadas
- [ ] Índices de Firestore están creados (si aplica)
- [ ] Cloud Functions están desplegadas (si aplica)
- [ ] Credenciales están guardadas de forma segura

---

## 📝 Notas Importantes

### ⚠️ Seguridad

1. **NO compartas** las credenciales (Server Key, Sender ID) públicamente
2. **Guarda** las credenciales en un lugar seguro
3. **No las incluyas** en el código fuente
4. **Usa** variables de entorno o Firebase Functions para manejar credenciales

### ⚠️ Costos

1. **Firebase Cloud Messaging**: Gratis hasta 10,000 mensajes/día
2. **Firestore**: Primeros 50,000 lecturas/día son gratis
3. **Cloud Functions**: Primeros 2 millones de invocaciones/mes son gratis

### ⚠️ Limitaciones

1. **WorkManager**: Puede tener retrasos en la ejecución (no es tiempo real)
2. **FCM**: Requiere conexión a Internet
3. **Ubicación en segundo plano**: Puede consumir batería

---

## 🚀 Próximos Pasos

Después de completar estos pasos en Firebase Console:

1. ✅ **Notifícame** cuando hayas completado los pasos
2. ✅ **Yo implementaré** el código en Android Studio
3. ✅ **Probaremos** la funcionalidad completa

---

*Documento creado para guiar la configuración de Firebase Console para notificaciones de incidentes cercanos.*

