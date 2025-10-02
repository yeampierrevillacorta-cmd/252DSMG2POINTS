# Configuración de Google Maps API

## Pasos necesarios para habilitar Google Maps en la aplicación POINTS

### 1. Obtener API Key de Google Maps

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita las siguientes APIs:
   - Maps SDK for Android
   - Places API
   - Geocoding API

### 2. Crear API Key

1. Ve a "APIs & Services" > "Credentials"
2. Haz clic en "Create Credentials" > "API Key"
3. Copia la API key generada

### 3. Configurar la API Key en la aplicación

Reemplaza `YOUR_API_KEY_HERE` en el archivo `app/src/main/AndroidManifest.xml` con tu API key real:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="TU_API_KEY_AQUI" />
```

### 4. Restricciones de seguridad (Recomendado)

1. En Google Cloud Console, ve a tu API key
2. Agrega restricciones de aplicación Android:
   - Nombre del paquete: `com.example.points`
   - Huella SHA-1 de tu certificado de depuración

### 5. Obtener huella SHA-1

Para obtener la huella SHA-1 de tu certificado de depuración:

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### 6. Funcionalidades implementadas

✅ **Visualización de incidentes en mapa**: Los incidentes se muestran como marcadores en Google Maps
✅ **Filtros por tipo**: Filtrar incidentes por tipo (Inseguridad, Accidente, etc.)
✅ **Ubicación actual**: Botón para centrar el mapa en la ubicación del usuario
✅ **Crear incidentes**: Formulario completo para reportar nuevos incidentes
✅ **Subida de imágenes**: Capacidad de adjuntar fotos a los reportes
✅ **Geolocalización**: Obtener automáticamente la ubicación para nuevos reportes

### 7. Permisos necesarios

La aplicación solicita los siguientes permisos:
- `ACCESS_FINE_LOCATION`: Para obtener ubicación precisa
- `ACCESS_COARSE_LOCATION`: Para ubicación aproximada
- `CAMERA`: Para tomar fotos (opcional)
- `READ_EXTERNAL_STORAGE`: Para seleccionar imágenes de la galería

### 8. Estructura de datos en Firebase

Los incidentes se guardan en Firestore con la siguiente estructura:

```
incidentes/
  {incident_id}/
    - tipo: "Inseguridad"
    - descripcion: "Descripción del incidente"
    - fotoUrl: "https://storage.googleapis.com/..."
    - ubicacion:
        - lat: -12.0464
        - lon: -77.0428
        - direccion: "Dirección legible"
    - fechaHora: Timestamp
    - estado: "Pendiente"
    - usuarioId: "user_id"
```

### 9. Estados de incidentes

- **Pendiente**: Recién reportado, esperando revisión
- **En Revisión**: Siendo evaluado por administradores
- **Confirmado**: Validado por administradores
- **Rechazado**: No cumple criterios o es falso
- **Resuelto**: Problema solucionado

### ⚠️ Nota importante

Sin configurar correctamente la API key de Google Maps, la funcionalidad del mapa no funcionará. Asegúrate de seguir todos los pasos anteriores antes de probar la aplicación.
