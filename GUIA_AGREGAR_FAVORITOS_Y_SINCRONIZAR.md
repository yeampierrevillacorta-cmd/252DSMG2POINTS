# 📖 Guía: Cómo Agregar Favoritos y Sincronizar

## 🔧 Problema Corregido

**Error en el log:** El log mostraba `Authorization: Bearer Bearer ...` (duplicado)
**Solución:** Corregido el formato del log para mostrar correctamente el header.

---

## ✅ Cómo Agregar un Punto Favorito

### Opción 1: Desde la Pantalla de Detalles del POI (Recomendado)

1. **Abrir la aplicación**
2. **Navegar a la lista de POIs** (pantalla principal con el mapa o lista)
3. **Tocar un POI** para abrir su pantalla de detalles
4. **Buscar el botón de favorito** (ícono de estrella ⭐ o corazón ❤️)
5. **Tocar el botón** para agregar a favoritos
6. El POI se guardará automáticamente en la base de datos local

### Opción 2: Desde la Pantalla de Favoritos

1. **Ir a la pantalla de Favoritos** (desde el menú o navegación)
2. Si no hay favoritos, la pantalla mostrará un mensaje
3. **Volver a la lista de POIs** y agregar favoritos desde allí

---

## 🔄 Cómo Sincronizar con el Backend

### Sincronización Manual

1. **Ir a Perfil** (desde el menú de navegación)
2. **Tocar "Sincronización"** o buscar la opción de sincronización
3. **Tocar "Sincronizar Ahora"**
4. La aplicación:
   - Primero hará **PULL** (obtiene cambios del servidor)
   - Luego hará **PUSH** (envía tus favoritos locales al servidor)

### Sincronización Automática

La sincronización automática está configurada con WorkManager:

1. **Ir a Perfil > Sincronización**
2. **Activar "Sincronización automática"**
3. **Configurar el intervalo** (ej: cada 6 horas)
4. **Opcional:** Activar "Solo en WiFi" para ahorrar datos

---

## 📋 Verificación de Sincronización

### 1. Verificar Logs en Logcat

Filtra por estos tags:
```
DefaultSyncRepository
DefaultAppContainer
SyncSettingsViewModel
```

### 2. Logs Esperados (Exitosos)

```
✅ Token JWT agregado al header Authorization
📤 [HEADERS] Request a: https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/push
   Headers: [Accept, Authorization, Content-Type, User-Agent]
   Authorization: Bearer eyJhbGciOiJSU... (sin duplicado)
🔄 [PUSH] Iniciando push de cambios para usuario: [userId]
📦 [PUSH] Obtenidos X favoritos locales
✅ [PUSH] Push completado exitosamente
```

### 3. Verificar en la UI

- **Pantalla de Sincronización:**
  - Debe mostrar "Última sincronización: [fecha/hora]"
  - Estado: "Sincronización exitosa" (verde)
  
- **Pantalla de Favoritos:**
  - Debe mostrar los favoritos que agregaste
  - Después de sincronizar, deberían aparecer favoritos del servidor (si hay)

---

## 🐛 Solución de Problemas

### Problema: "No hay favoritos para sincronizar"

**Solución:**
1. Agrega al menos un favorito desde la pantalla de detalles de un POI
2. Verifica que el favorito aparezca en la pantalla de Favoritos
3. Intenta sincronizar nuevamente

### Problema: Error 401/403 en sincronización

**Causas posibles:**
- Usuario no autenticado en Firebase
- Token JWT expirado o inválido
- Backend rechazando el token

**Solución:**
1. Cierra sesión y vuelve a iniciar sesión
2. Verifica que estés autenticado en Firebase
3. Revisa los logs para ver si el token se está agregando correctamente

### Problema: "Bearer Bearer" en los logs

**Estado:** ✅ **CORREGIDO**

El log ahora muestra correctamente:
```
Authorization: Bearer eyJhbGciOiJSU...
```

---

## 📝 Flujo Completo de Prueba

### Paso 1: Agregar Favorito
1. Abre la app
2. Ve a la lista de POIs
3. Toca un POI
4. Toca el botón de favorito ⭐
5. Verifica que aparezca en "Favoritos"

### Paso 2: Sincronizar
1. Ve a **Perfil > Sincronización**
2. Toca **"Sincronizar Ahora"**
3. Espera a que termine (verás un indicador de carga)
4. Verifica el mensaje de éxito

### Paso 3: Verificar en Logs
1. Abre Logcat
2. Filtra por: `DefaultSyncRepository|DefaultAppContainer`
3. Busca mensajes:
   - `✅ [PUSH] Push completado exitosamente`
   - `✅ [PULL] Pull completado exitosamente`
   - `✅ [SYNC] Sincronización completada`

### Paso 4: Verificar en el Backend (Opcional)
1. Revisa los logs del backend en Google Cloud Console
2. Deberías ver las peticiones POST a `/api/v1/sync/push`
3. Verifica que los favoritos se hayan guardado en la base de datos

---

## 🎯 Resumen

1. **Agregar favorito:** Toca el botón ⭐ en la pantalla de detalles del POI
2. **Sincronizar:** Ve a Perfil > Sincronización > "Sincronizar Ahora"
3. **Verificar:** Revisa los logs y la UI para confirmar éxito

---

**Última actualización:** Diciembre 2024

