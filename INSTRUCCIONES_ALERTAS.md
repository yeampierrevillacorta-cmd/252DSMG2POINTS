# 🔔 Instrucciones para Activar Alertas y Notificaciones

## ⚠️ Problema Identificado y Solucionado

El sistema de alertas **NO se activa automáticamente**. Debes activarlo manualmente desde la pantalla de Alertas.

## 📋 Pasos para Activar las Alertas

### 1. **Ir a la Pantalla de Alertas**
   - Abre la aplicación Points
   - Navega a la pantalla de **"Alertas y Notificaciones"** (botón de alertas en el menú)

### 2. **Configurar las Alertas**
   - Toca el botón de **configuración (⚙️)** en la esquina superior derecha
   - Se abrirá un diálogo de configuración

### 3. **Configurar Parámetros**
   - **Radio de búsqueda**: Desliza para seleccionar el radio (1-50 km)
     - Recomendado: 5-10 km para empezar
   - **Tipos de alertas**: Marca las casillas para:
     - ✅ **Incidentes**: Recibir alertas sobre incidentes cercanos
     - ✅ **Eventos**: Recibir alertas sobre eventos cercanos

### 4. **Activar las Alertas**
   - Presiona el botón **"Activar"** o **"Actualizar"**
   - Las alertas se programarán automáticamente

### 5. **Verificar Inmediatamente (Opcional)**
   - Presiona **"Verificar ahora"** para ejecutar una verificación inmediata
   - Esto te permitirá ver si hay incidentes/eventos cercanos sin esperar el intervalo programado

## 🔐 Permisos Necesarios

### **Ubicación (OBLIGATORIO)**
   - La aplicación necesita acceso a tu ubicación para detectar incidentes/eventos cercanos
   - **Cómo verificar**:
     1. Ve a **Configuración** del teléfono
     2. **Aplicaciones** → **Points**
     3. **Permisos** → **Ubicación**
     4. Asegúrate de que esté en **"Permitir todo el tiempo"** o **"Permitir solo mientras se usa la app"**

### **Notificaciones (Android 13+)**
   - En Android 13 y superior, necesitas conceder permisos de notificaciones
   - **Cómo verificar**:
     1. Ve a **Configuración** del teléfono
     2. **Aplicaciones** → **Points**
     3. **Permisos** → **Notificaciones**
     4. Asegúrate de que esté **activado**

### **GPS/Ubicación del Dispositivo**
   - El GPS debe estar activado en tu dispositivo
   - **Cómo verificar**:
     1. Ve a **Configuración** del teléfono
     2. **Ubicación** o **Localización**
     3. Asegúrate de que esté **activado**

## ⏰ Intervalo de Verificación

- **Intervalo mínimo**: 15 minutos (limitación de Android WorkManager)
- Las alertas se verifican automáticamente cada 15 minutos cuando:
  - ✅ Tienes conexión a internet
  - ✅ El dispositivo está encendido
  - ✅ Las alertas están activadas

## 🧪 Cómo Probar que Funciona

### **Método 1: Verificación Inmediata**
1. Activa las alertas con un radio de 50 km (para maximizar resultados)
2. Presiona **"Verificar ahora"** en el diálogo de configuración
3. Espera unos segundos
4. Revisa la pantalla de alertas para ver si aparecen notificaciones

### **Método 2: Crear un Incidente/Evento de Prueba**
1. Crea un incidente o evento desde la aplicación
2. Asegúrate de que esté cerca de tu ubicación actual
3. Espera hasta 15 minutos (o usa "Verificar ahora")
4. Deberías recibir una notificación

### **Método 3: Verificar Logs (Desarrolladores)**
- Abre **Logcat** en Android Studio
- Filtra por: `AlertWorker` o `AlertWorkManager`
- Deberías ver logs como:
  ```
  AlertWorker: Iniciando verificación de alertas
  AlertWorker: Ubicación del usuario: lat=..., lon=...
  AlertWorker: Encontrados X incidentes cercanos
  AlertWorker: Verificación completada. X nuevas notificaciones creadas
  ```

## ❌ Problemas Comunes y Soluciones

### **No recibo notificaciones**
1. ✅ Verifica que las alertas estén **activadas** (botón de configuración)
2. ✅ Verifica **permisos de ubicación** (Configuración → Points → Permisos)
3. ✅ Verifica **permisos de notificaciones** (Android 13+)
4. ✅ Verifica que el **GPS esté activado**
5. ✅ Verifica que tengas **conexión a internet**
6. ✅ Usa **"Verificar ahora"** para probar inmediatamente

### **"No se pudo obtener ubicación"**
- **Causa**: Permisos de ubicación no concedidos o GPS desactivado
- **Solución**: 
  1. Ve a Configuración → Points → Permisos → Ubicación
  2. Concede permisos
  3. Activa el GPS en Configuración del dispositivo

### **"Usuario no autenticado"**
- **Causa**: No has iniciado sesión en la aplicación
- **Solución**: Inicia sesión en la aplicación

### **Las alertas se desactivan solas**
- **Causa**: Puede ser por restricciones del sistema o batería baja
- **Solución**: 
  1. Revisa la configuración de ahorro de batería
  2. Desactiva la optimización de batería para Points
  3. Reactiva las alertas manualmente

## 📱 Configuración Recomendada

Para mejor funcionamiento:
- **Radio de búsqueda**: 5-10 km (suficiente para la mayoría de casos)
- **Tipos de alertas**: Ambos activados (Incidentes y Eventos)
- **Permisos de ubicación**: "Permitir todo el tiempo" (para verificación en segundo plano)
- **Optimización de batería**: Desactivada para Points

## 🔄 Estado de las Alertas

Puedes verificar el estado de las alertas en la pantalla de Alertas:
- **✅ Activado**: Las alertas están programadas y funcionando
- **❌ Desactivado**: Necesitas activarlas manualmente

## 📝 Notas Importantes

1. **Las alertas NO se activan automáticamente** - Debes activarlas manualmente la primera vez
2. **El intervalo mínimo es 15 minutos** - No recibirás notificaciones inmediatas, sino cada 15 minutos como mínimo
3. **Solo se notifican incidentes/eventos nuevos** - Si ya recibiste una notificación sobre un incidente/evento, no recibirás otra
4. **Requiere conexión a internet** - Las alertas no funcionan sin internet
5. **Solo incidentes CONFIRMADOS** - Solo recibirás alertas de incidentes que estén en estado "Confirmado"
6. **Solo eventos APROBADOS** - Solo recibirás alertas de eventos que estén en estado "Aprobado"

## 🆘 Si Nada Funciona

1. **Reinicia la aplicación** completamente
2. **Desactiva y reactiva las alertas** desde el diálogo de configuración
3. **Verifica los logs** en Logcat para ver errores específicos
4. **Asegúrate de tener incidentes/eventos cercanos** en la base de datos
5. **Prueba con "Verificar ahora"** para ejecución inmediata

---

**Última actualización**: Después de corregir permisos y restricciones del WorkManager

