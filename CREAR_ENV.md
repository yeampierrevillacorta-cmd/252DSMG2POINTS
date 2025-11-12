# 📝 Cómo Crear el Archivo .env

El archivo `.env` **NO** viene incluido en el repositorio por razones de seguridad (contiene claves API sensibles). Debes crearlo manualmente.

## 🚀 Método 1: Copiar desde .env.example (Recomendado)

Si existe un archivo `.env.example` en el repositorio:

### En Windows (PowerShell):
```powershell
Copy-Item .env.example .env
```

### En Windows (CMD):
```cmd
copy .env.example .env
```

### En Linux/Mac:
```bash
cp .env.example .env
```

## ✏️ Método 2: Crear Manualmente

Si no existe `.env.example`, crea el archivo `.env` en la **raíz del proyecto** con el siguiente contenido:

```env
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

## 📍 Ubicación del Archivo

El archivo `.env` debe estar en la **raíz del proyecto**, al mismo nivel que:
- `build.gradle.kts`
- `settings.gradle.kts`
- `README.md`
- Carpeta `app/`

```
252DSMG2POINTS/
├── .env              ← AQUÍ debe estar
├── .gitignore
├── build.gradle.kts
├── app/
└── ...
```

## 🔑 Cómo Obtener las Claves API

### Google Maps API Key
1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea o selecciona un proyecto
3. Habilita "Maps SDK for Android"
4. Ve a "Credenciales" → "Crear credenciales" → "Clave de API"
5. Copia la clave generada

### Firebase Configuration
1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Ve a "Configuración del proyecto" (⚙️)
4. En "Tus aplicaciones", selecciona tu app Android
5. Copia los valores de `projectId`, `apiKey`, `appId`, etc.

### OpenWeatherMap API Key
1. Ve a [OpenWeatherMap](https://openweathermap.org/api)
2. Regístrate (es gratuito)
3. Ve a "API keys" en tu cuenta
4. Copia tu API key (puede tardar unos minutos en activarse)

## ✅ Verificar que Funciona

Después de crear el archivo `.env`:

1. **Asegúrate de que el archivo existe:**
   ```powershell
   # En PowerShell
   Test-Path .env
   # Debe retornar: True
   ```

2. **Verifica el contenido (sin mostrar claves completas):**
   ```powershell
   # En PowerShell
   Get-Content .env | Select-String "API_KEY"
   ```

3. **Ejecuta la app** y revisa los logs en Logcat. Deberías ver:
   ```
   PointsApp: Configuración inicializada:
   PointsApp: Environment: development
   PointsApp: Debug Mode: true
   ```

## ⚠️ Importante

- ❌ **NUNCA** subas el archivo `.env` al repositorio
- ✅ El archivo `.env` está en `.gitignore` por seguridad
- ✅ Reemplaza todos los valores `tu_clave_...` con tus claves reales
- ✅ No dejes espacios alrededor del signo `=` en el archivo `.env`

## 🆘 Problemas Comunes

### "No se encuentra el archivo .env"
- Verifica que estás en la raíz del proyecto
- Asegúrate de que el archivo se llama exactamente `.env` (con el punto al inicio)
- En Windows, algunos editores pueden agregar extensión automáticamente (`.env.txt`). Asegúrate de que sea solo `.env`

### "Las variables no se cargan"
- Verifica que no hay espacios alrededor del `=`
- Asegúrate de que cada variable está en una línea separada
- No uses comillas alrededor de los valores (a menos que la clave tenga espacios)

### "Error al compilar"
- Limpia el proyecto: `./gradlew clean`
- Sincroniza Gradle en Android Studio
- Reinicia Android Studio

