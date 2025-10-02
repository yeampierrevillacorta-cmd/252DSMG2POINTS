# Solución de Problemas - Mapa en Blanco

## 🔍 Diagnóstico Implementado

He agregado herramientas de debugging para identificar por qué el mapa se muestra en blanco:

### 1. **Logs de Debug Agregados**
- ✅ Logs en `IncidentRepository` para ver si Firebase está funcionando
- ✅ Logs en `IncidentViewModel` para verificar la carga de datos
- ✅ Logs en `IncidentsMapScreen` para confirmar renderizado del mapa
- ✅ Panel de debug visual en la esquina superior izquierda

### 2. **Panel de Debug Visual**
En la esquina superior izquierda del mapa verás:
- **Contador de incidentes**: Muestra cuántos incidentes se han cargado
- **Estado de carga**: Indica si está cargando datos
- **Mensajes de error**: Muestra errores si los hay
- **Botón "Crear datos de prueba"**: Si no hay incidentes, puedes crear algunos

## 🚨 Posibles Causas del Mapa en Blanco

### 1. **Problema con API Key de Google Maps**
**Síntomas**: Mapa completamente blanco, sin controles
**Soluciones**:
- Verifica que la API key sea válida: `AIzaSyA7evjmRwUEECBNBFaOiWgfxV8GEEzfBZQ`
- Asegúrate de que las APIs estén habilitadas en Google Cloud Console:
  - Maps SDK for Android
  - Places API (opcional)
  - Geocoding API (opcional)

### 2. **Restricciones de API Key**
**Síntomas**: Mapa se carga pero no funciona completamente
**Soluciones**:
- Verifica las restricciones de la API key
- Agrega el SHA-1 fingerprint de tu certificado de debug
- Verifica que el nombre del paquete sea `com.example.points`

### 3. **Problemas de Red/Firebase**
**Síntomas**: Mapa se carga pero no hay marcadores
**Soluciones**:
- Verifica conexión a internet
- Revisa la configuración de Firebase
- Usa el botón "Crear datos de prueba" para generar incidentes

### 4. **Permisos de Ubicación**
**Síntomas**: Mapa se carga pero no muestra ubicación actual
**Soluciones**:
- Acepta los permisos de ubicación cuando la app los solicite
- Verifica en configuración del dispositivo que la app tenga permisos

## 📱 Cómo Usar las Herramientas de Debug

### 1. **Revisar Logs en Android Studio**
```
Filtrar por: "IncidentRepository", "IncidentViewModel", "IncidentsMap"
```

Logs esperados:
```
D/IncidentRepository: Iniciando escucha de incidentes en Firebase
D/IncidentRepository: Snapshot recibido con X documentos
D/IncidentViewModel: Incidentes recibidos: X
D/IncidentsMap: Mapa de Google cargado correctamente
D/IncidentsMap: Renderizando X marcadores
```

### 2. **Usar Panel de Debug Visual**
1. Ve a la sección "INCIDENTES" en la app
2. Mira la esquina superior izquierda
3. Si dice "Incidentes: 0", haz clic en "Crear datos de prueba"
4. Deberían aparecer 5 marcadores en Lima, Perú

### 3. **Verificar Datos en Firebase Console**
1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Ve a Firestore Database
4. Busca la colección "incidentes"
5. Deberías ver documentos con la estructura correcta

## 🛠️ Datos de Prueba Incluidos

He creado 5 incidentes de prueba en Lima, Perú:
- 🚨 **Inseguridad** en Plaza de Armas
- 🚗 **Accidente** en Av. Abancay  
- 🔥 **Incendio** en Jr. de la Unión
- 🏛️ **Servicio Público** en San Juan de Lurigancho
- 🔨 **Vandalismo** en Miraflores

## ✅ Lista de Verificación

**Antes de probar:**
- [ ] API Key configurada correctamente
- [ ] Firebase configurado y conectado
- [ ] Permisos de internet y ubicación otorgados
- [ ] Conexión a internet activa

**Durante la prueba:**
- [ ] Panel de debug muestra "Incidentes: X" (X > 0)
- [ ] Mapa se carga (no está completamente blanco)
- [ ] Aparecen marcadores en el mapa
- [ ] Logs en Android Studio muestran actividad

**Si el mapa sigue en blanco:**
1. Revisa los logs de Android Studio
2. Verifica la API key en Google Cloud Console
3. Usa el botón "Crear datos de prueba"
4. Reinicia la aplicación

## 🔄 Próximos Pasos

1. **Ejecuta la app** y ve a la sección "INCIDENTES"
2. **Observa el panel de debug** en la esquina superior izquierda
3. **Revisa los logs** en Android Studio
4. **Crea datos de prueba** si no hay incidentes
5. **Reporta** qué información ves en el panel de debug

Con estas herramientas deberíamos poder identificar exactamente qué está causando el problema del mapa en blanco.
