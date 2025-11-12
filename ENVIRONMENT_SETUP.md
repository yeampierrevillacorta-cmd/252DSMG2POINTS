# Configuración de Variables de Entorno

Este proyecto utiliza variables de entorno para manejar de forma segura las claves de API y configuraciones sensibles.

## Configuración Inicial

1. **Copia el archivo de ejemplo:**
   ```bash
   cp .env.example .env
   ```

2. **Edita el archivo `.env` con tus claves reales:**
   ```bash
   # Google Maps API Key
   GOOGLE_MAPS_API_KEY=tu_clave_de_google_maps_aqui
   
   # Firebase Configuration
   FIREBASE_PROJECT_ID=tu_proyecto_firebase
   FIREBASE_PROJECT_NUMBER=tu_numero_de_proyecto
   FIREBASE_STORAGE_BUCKET=tu_bucket_de_storage
   FIREBASE_API_KEY=tu_clave_de_firebase
   FIREBASE_APP_ID=tu_app_id_de_firebase
   
   # OpenWeatherMap API Key
   OPENWEATHER_API_KEY=tu_clave_de_openweathermap_aqui
   
   # Environment Configuration
   ENVIRONMENT=development
   DEBUG_MODE=true
   ```

## Seguridad

- ✅ El archivo `.env` está incluido en `.gitignore` y **NO** se sube al repositorio
- ✅ El archivo `.env.example` contiene plantillas sin claves reales
- ✅ Las claves se cargan dinámicamente en tiempo de compilación

## Estructura de Archivos

```
├── .env                    # Variables de entorno (NO committear)
├── .env.example           # Plantilla de variables (SÍ committear)
├── app/src/main/assets/.env  # Copia para Android (generada automáticamente)
└── app/src/main/java/com/example/points/utils/EnvironmentConfig.kt
```

## Uso en el Código

```kotlin
import com.example.points.utils.EnvironmentConfig

// Obtener claves de API
val mapsKey = EnvironmentConfig.GOOGLE_MAPS_API_KEY
val firebaseKey = EnvironmentConfig.FIREBASE_API_KEY

// Verificar configuración
if (EnvironmentConfig.isConfigurationValid()) {
    // Configuración válida
}

// Información de debugging (sin claves sensibles)
val configInfo = EnvironmentConfig.getConfigurationInfo()
```

## Generación Automática

El sistema genera automáticamente los archivos XML de configuración de Google Maps desde las variables de entorno durante la compilación:

- `app/src/debug/res/values/google_maps_api.xml`
- `app/src/release/res/values/google_maps_api.xml`

## Troubleshooting

### Error: "Variable de entorno no encontrada"
- Verifica que el archivo `.env` existe en la raíz del proyecto
- Asegúrate de que la variable esté definida en el archivo `.env`
- Revisa que no haya espacios alrededor del signo `=`

### Error: "Archivo .env no se encuentra"
- Ejecuta `cp .env.example .env` para crear el archivo
- Verifica que estés en el directorio raíz del proyecto

### Error de compilación
- Limpia el proyecto: `./gradlew clean`
- Reconstruye: `./gradlew build`

## Variables Disponibles

| Variable | Descripción | Requerida |
|----------|-------------|-----------|
| `GOOGLE_MAPS_API_KEY` | Clave de API de Google Maps | ✅ |
| `FIREBASE_PROJECT_ID` | ID del proyecto Firebase | ✅ |
| `FIREBASE_PROJECT_NUMBER` | Número del proyecto Firebase | ✅ |
| `FIREBASE_STORAGE_BUCKET` | Bucket de almacenamiento Firebase | ✅ |
| `FIREBASE_API_KEY` | Clave de API de Firebase | ✅ |
| `FIREBASE_APP_ID` | ID de la aplicación Firebase | ✅ |
| `OPENWEATHER_API_KEY` | Clave de API de OpenWeatherMap | ✅ |
| `ENVIRONMENT` | Entorno (development/production) | ❌ |
| `DEBUG_MODE` | Modo debug (true/false) | ❌ |
