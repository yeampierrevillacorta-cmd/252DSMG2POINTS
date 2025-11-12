# Points - Aplicación de Puntos de Interés

Una aplicación Android moderna desarrollada con Jetpack Compose para gestionar y descubrir puntos de interés en tu ciudad.

## 🚀 Características

- **Mapas Interactivos**: Visualización de POIs en Google Maps
- **Búsqueda Avanzada**: Filtros por categoría, distancia y características
- **Gestión de POIs**: Crear, editar y administrar puntos de interés
- **Sistema de Calificaciones**: Calificar y revisar lugares
- **Navegación**: Integración con Google Maps para direcciones
- **Imágenes**: Subir y visualizar fotos de los POIs
- **Ubicación en Tiempo Real**: Detección automática de ubicación

## 🛠️ Tecnologías

- **Android**: Jetpack Compose, Material Design 3
- **Maps**: Google Maps Android API
- **Backend**: Firebase (Firestore, Storage, Auth)
- **Arquitectura**: MVVM con ViewModels
- **Navegación**: Jetpack Navigation Compose
- **Imágenes**: Coil para carga asíncrona
- **Variables de Entorno**: Configuración segura de claves API

## 📋 Requisitos

- Android Studio Hedgehog o superior
- Android SDK 24+
- Cuenta de Google Cloud Platform (para Google Maps)
- Proyecto Firebase configurado

## ⚙️ Configuración

### 1. Clonar el Repositorio
```bash
git clone <repository-url>
cd 252DSMG2POINTS
```

### 2. Configurar Variables de Entorno

**Opción A: Usar el script automático (Recomendado)**

En Windows (PowerShell):
```powershell
.\setup-env.ps1
```

En Linux/Mac:
```bash
chmod +x setup-env.sh
./setup-env.sh
```

**Opción B: Copiar manualmente**
```bash
# Copiar archivo de ejemplo
cp .env.example .env

# Editar con tus claves reales
nano .env
```

Después de crear el archivo `.env`, edítalo y reemplaza todos los valores `tu_clave_...` con tus claves reales.

**Variables requeridas:**
- `GOOGLE_MAPS_API_KEY`: Clave de API de Google Maps
- `FIREBASE_PROJECT_ID`: ID del proyecto Firebase
- `FIREBASE_API_KEY`: Clave de API de Firebase
- `FIREBASE_APP_ID`: ID de la aplicación Firebase
- `OPENWEATHER_API_KEY`: Clave de API de OpenWeatherMap (para funcionalidad de clima)

### 3. Configurar Google Maps
1. Crear proyecto en [Google Cloud Console](https://console.cloud.google.com/)
2. Habilitar Google Maps Android API
3. Crear credenciales (API Key)
4. Configurar restricciones de aplicación
5. Agregar la clave al archivo `.env`

### 4. Configurar Firebase
1. Crear proyecto en [Firebase Console](https://console.firebase.google.com/)
2. Agregar aplicación Android
3. Descargar `google-services.json`
4. Configurar Firestore, Storage y Authentication
5. Actualizar variables en `.env`

### 5. Compilar y Ejecutar
```bash
./gradlew assembleDebug
```

## 📁 Estructura del Proyecto

```
app/
├── src/main/
│   ├── java/com/example/points/
│   │   ├── screens/          # Pantallas principales
│   │   ├── components/       # Componentes reutilizables
│   │   ├── viewmodel/        # ViewModels
│   │   ├── models/           # Modelos de datos
│   │   ├── utils/            # Utilidades
│   │   └── services/         # Servicios
│   ├── res/                  # Recursos Android
│   └── assets/               # Archivos estáticos
├── .env                      # Variables de entorno (NO committear)
├── .env.example             # Plantilla de variables
└── google-services.json     # Configuración Firebase
```

## 🔐 Seguridad

- ✅ Variables de entorno para claves API
- ✅ Archivo `.env` excluido del control de versiones
- ✅ Validación de configuración en tiempo de ejecución
- ✅ Logs seguros (sin claves sensibles)

## 📱 Pantallas Principales

- **Mapa de POIs**: Visualización interactiva en Google Maps
- **Lista de POIs**: Búsqueda y filtrado de puntos de interés
- **Detalles de POI**: Información completa con imágenes
- **Crear POI**: Formulario para agregar nuevos puntos
- **Perfil**: Gestión de usuario y configuraciones

## 🎨 Diseño

- **Material Design 3**: Componentes modernos y accesibles
- **Tema Dinámico**: Soporte para modo claro/oscuro
- **Animaciones**: Transiciones fluidas con Lottie
- **Responsive**: Adaptable a diferentes tamaños de pantalla

## 🧪 Testing

```bash
# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests de instrumentación
./gradlew connectedAndroidTest

# Generar reporte de cobertura
./gradlew jacocoTestReport
```

## 📦 Build Variants

- **Debug**: Configuración de desarrollo con logs detallados
- **Release**: Optimizado para producción

## 🚀 Despliegue

1. Configurar variables de entorno para producción
2. Actualizar `google-services.json` con configuración de producción
3. Generar APK firmado:
   ```bash
   ./gradlew assembleRelease
   ```

## 🤝 Contribución

1. Fork el proyecto
2. Crear rama para feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abrir Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver `LICENSE` para más detalles.

## 📞 Soporte

Para soporte técnico o preguntas:
- Crear un issue en GitHub
- Revisar la documentación en `ENVIRONMENT_SETUP.md`
- Consultar logs de la aplicación en modo debug

---

**Desarrollado con ❤️ usando Jetpack Compose y Firebase**
