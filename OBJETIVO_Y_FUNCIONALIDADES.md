# 📱 Points App - Objetivo, Funcionalidades y Propósito

## 📋 Tabla de Contenidos

1. [Objetivo del Programa](#objetivo-del-programa)
2. [¿Para Qué Sirve?](#para-qué-sirve)
3. [Funcionalidades Principales](#funcionalidades-principales)
4. [Roles de Usuario](#roles-de-usuario)
5. [Características Técnicas](#características-técnicas)
6. [Casos de Uso](#casos-de-uso)

---

## 🎯 Objetivo del Programa

**Points App** es una **plataforma móvil de gestión urbana inteligente** desarrollada para Android que permite a los ciudadanos, administradores y moderadores colaborar en la mejora de sus ciudades mediante:

- 📍 **Gestión de Puntos de Interés (POIs)**: Descubrir, compartir y gestionar lugares importantes de la ciudad
- 🚨 **Reporte de Incidentes Urbanos**: Sistema de reportes en tiempo real para problemas urbanos
- 🎉 **Gestión de Eventos**: Organización y descubrimiento de eventos locales
- 📊 **Analíticas y Dashboards**: Visualización de datos y estadísticas para toma de decisiones
- 👥 **Sistema de Moderación**: Control de calidad y aprobación de contenido

### Visión

Crear una **comunidad conectada** donde ciudadanos y autoridades trabajen juntos para:
- Mejorar la calidad de vida urbana
- Fomentar el turismo y la economía local
- Facilitar la toma de decisiones basada en datos
- Promover la participación ciudadana

---

## 🎯 ¿Para Qué Sirve?

### Para Ciudadanos 👥

1. **Descubrir Lugares de Interés**
   - Encontrar restaurantes, parques, museos, centros de salud, etc.
   - Ver información detallada: horarios, precios, características
   - Consultar el clima actual en la ubicación del POI
   - Leer calificaciones y reseñas de otros usuarios
   - Ver fotos y obtener direcciones

2. **Reportar Problemas Urbanos**
   - Reportar incidentes en tiempo real (inseguridad, accidentes, vandalismo, etc.)
   - Adjuntar fotos y videos como evidencia
   - Seguir el estado de sus reportes
   - Ver el mapa de incidentes cercanos

3. **Descubrir Eventos Locales**
   - Ver eventos culturales, deportivos, educativos, etc.
   - Filtrar por categoría, fecha y ubicación
   - Inscribirse en eventos que requieren registro
   - Ver información de contacto y redes sociales

4. **Navegación y Mapas**
   - Visualizar POIs e incidentes en mapas interactivos
   - Obtener direcciones con Google Maps
   - Encontrar lugares cercanos a su ubicación

### Para Administradores 🛡️

1. **Gestión Completa del Sistema**
   - Moderar y aprobar/rechazar POIs, eventos e incidentes
   - Gestionar usuarios: cambiar roles, activar/desactivar cuentas
   - Ver estadísticas y analíticas en tiempo real
   - Configurar parámetros del sistema

2. **Analíticas y Reportes**
   - Dashboard con gráficos de barras y pastel
   - Distribución de incidentes, eventos y POIs por mes
   - Distribución por estado (atendido, denegado, en revisión)
   - Tendencias y patrones de uso

3. **Moderación de Contenido**
   - Revisar y aprobar contenido pendiente
   - Rechazar contenido inapropiado con comentarios
   - Gestionar estados de moderación

### Para Moderadores 🔍

1. **Moderación de Contenido**
   - Revisar POIs, eventos e incidentes pendientes
   - Aprobar o rechazar con comentarios
   - Cambiar estados de moderación

2. **Visualización de Datos**
   - Acceder a dashboards y analíticas
   - Ver estadísticas de contenido moderado

---

## 🚀 Funcionalidades Principales

### 1. Sistema de Puntos de Interés (POIs) 📍

#### Categorías Disponibles
- 🍽️ **Comida**: Restaurantes, cafeterías, bares
- 🎬 **Entretenimiento**: Cines, teatros, centros de entretenimiento
- 🎨 **Cultura**: Museos, galerías, bibliotecas
- ⚽ **Deporte**: Gimnasios, canchas, centros deportivos
- 🏥 **Salud**: Hospitales, clínicas, farmacias
- 🎓 **Educación**: Escuelas, universidades, centros educativos
- 🚌 **Transporte**: Paradas, estaciones, terminales
- 🔧 **Servicios**: Bancos, oficinas públicas, servicios
- 🗺️ **Turismo**: Atracciones turísticas, miradores
- ⚡ **Recarga Eléctrica**: Estaciones de carga para vehículos
- 🌳 **Parques**: Parques, plazas, áreas verdes
- 🛒 **Shopping**: Centros comerciales, tiendas

#### Características de los POIs
- ✅ **Información Completa**: Nombre, descripción, dirección, teléfono, email, sitio web
- ✅ **Ubicación GPS**: Coordenadas precisas con geocodificación
- ✅ **Imágenes Múltiples**: Subir varias fotos del lugar
- ✅ **Horarios**: Horarios de apertura por día de la semana
- ✅ **Calificaciones**: Sistema de estrellas con total de calificaciones
- ✅ **Características Especiales**: 
  - Accesibilidad en silla de ruedas
  - Estacionamiento gratuito/de pago
  - WiFi gratuito
  - Mascotas bienvenidas
  - Terraza, aire acondicionado
  - Música en vivo, delivery
  - Opciones vegetarianas/veganas/sin gluten
  - Y más...
- ✅ **Rango de Precio**: Gratis, Económico, Moderado, Caro, Muy Caro
- ✅ **Clima en Tiempo Real**: Integración con OpenWeatherMap API
- ✅ **Generación de Descripción con IA**: Usando Google Gemini API
- ✅ **Estados de Moderación**: Pendiente, En Revisión, Aprobado, Rechazado, Suspendido

#### Funcionalidades de Búsqueda y Filtrado
- 🔍 **Búsqueda por Texto**: Nombre, descripción o dirección
- 🎯 **Filtro por Categoría**: Filtrar por tipo de POI
- 📍 **Búsqueda por Proximidad**: Encontrar POIs cercanos (radio configurable)
- 🗺️ **Visualización en Mapa**: Ver todos los POIs en Google Maps
- 📊 **Ordenamiento**: Por distancia, calificación, fecha de creación

---

### 2. Sistema de Incidentes Urbanos 🚨

#### Tipos de Incidentes
- 🚨 **Inseguridad**: Robos, asaltos, situaciones sospechosas
- 🚗 **Accidente de Tránsito**: Colisiones, atropellos
- 🔥 **Incendio**: Incendios en edificios, vehículos, áreas verdes
- 💧 **Inundación**: Inundaciones, desbordes, anegamientos
- 🎨 **Vandalismo**: Grafitis, daños a propiedad pública
- 🗑️ **Basura**: Acumulación de basura, contenedores llenos
- 🚧 **Infraestructura**: Baches, semáforos dañados, postes caídos
- ⚠️ **Otro**: Cualquier otro problema urbano

#### Características
- ✅ **Reporte en Tiempo Real**: Reportar incidentes al instante
- ✅ **Ubicación GPS**: Detección automática o manual de ubicación
- ✅ **Multimedia**: Adjuntar fotos y videos como evidencia
- ✅ **Descripción Detallada**: Campo de texto para detalles adicionales
- ✅ **Estados de Seguimiento**:
  - **Pendiente**: Recién reportado, esperando revisión
  - **En Revisión**: Siendo evaluado por moderadores
  - **Confirmado**: Verificado y aceptado
  - **Rechazado**: No válido o duplicado
  - **Resuelto**: Problema solucionado
- ✅ **Historial de Reportes**: Ver todos los reportes del usuario
- ✅ **Mapa de Incidentes**: Visualizar todos los incidentes en un mapa
- ✅ **Filtros**: Por tipo, estado, fecha

---

### 3. Sistema de Eventos Urbanos 🎉

#### Categorías de Eventos
- 🎨 **Cultural**: Conciertos, exposiciones, festivales
- ⚽ **Deportivo**: Partidos, maratones, competencias
- 🎓 **Educativo**: Conferencias, talleres, seminarios
- 🎪 **Entretenimiento**: Shows, espectáculos, ferias
- 🏛️ **Comunitario**: Asambleas, reuniones vecinales
- 🍽️ **Gastronómico**: Ferias de comida, degustaciones
- 🛍️ **Comercial**: Ferias, mercados, ventas
- 🎭 **Arte**: Exposiciones, obras de teatro, performances
- 🌱 **Ambiental**: Limpiezas, plantaciones, charlas
- 🎊 **Otro**: Cualquier otro tipo de evento

#### Características
- ✅ **Información Completa**: Nombre, descripción, ubicación, fechas
- ✅ **Horarios**: Fecha y hora de inicio/fin
- ✅ **Eventos Recurrentes**: Soporte para eventos que se repiten
- ✅ **Frecuencias**: Diario, semanal, mensual, anual
- ✅ **Precios**: Gratis o con rango de precios
- ✅ **Capacidad e Inscripciones**: Control de aforo
- ✅ **Contacto**: Teléfono, email, nombre de contacto
- ✅ **Redes Sociales**: Enlaces a Facebook, Instagram, Twitter
- ✅ **Sitio Web**: URL del evento
- ✅ **Características Especiales**:
  - Accesibilidad
  - Estacionamiento
  - Transporte público cercano
  - Edad mínima/máxima
  - Requiere inscripción
- ✅ **Imágenes**: Múltiples fotos del evento
- ✅ **Estados**: Pendiente, Aprobado, Rechazado, Cancelado
- ✅ **Etiquetas**: Sistema de tags para búsqueda

---

### 4. Sistema de Dashboards y Analíticas 📊

#### Dashboard Principal
- 📊 **Gráfico de Barras**: Distribución de incidentes por tipo
- 🥧 **Gráfico de Pastel**: Porcentaje de cada tipo de incidente
- 📅 **Distribución Mensual**: 
  - Cantidad de incidentes por mes
  - Cantidad de eventos por mes
  - Cantidad de POIs por mes
- 📈 **Distribución por Estado**:
  - Incidentes: Atendidos, Denegados, En Revisión
  - Eventos: Atendidos, Denegados, En Revisión
  - POIs: Atendidos, Denegados, En Revisión

#### Características
- ✅ **Tiempo Real**: Los datos se actualizan automáticamente
- ✅ **Visualización Interactiva**: Gráficos interactivos con la librería `tehras-charts`
- ✅ **Filtros Temporales**: Ver datos por período específico
- ✅ **Exportación**: (Futuro) Exportar reportes en PDF/Excel

---

### 5. Sistema de Autenticación y Roles 👥

#### Autenticación
- ✅ **Registro de Usuarios**: Email y contraseña
- ✅ **Inicio de Sesión**: Login seguro con Firebase Auth
- ✅ **Recuperación de Contraseña**: Envío de email para reset
- ✅ **Perfil de Usuario**: Editar información personal
- ✅ **Foto de Perfil**: Subir y actualizar foto

#### Roles del Sistema

##### 👤 Ciudadano
- **Permisos**:
  - ✅ Crear y reportar incidentes
  - ✅ Crear POIs (requiere aprobación)
  - ✅ Crear eventos (requiere aprobación)
  - ✅ Ver POIs, eventos e incidentes aprobados
  - ✅ Calificar POIs
  - ✅ Ver su propio historial
  - ❌ No puede moderar contenido
  - ❌ No puede ver dashboards administrativos

##### 🔍 Moderador
- **Permisos**:
  - ✅ Todos los permisos de Ciudadano
  - ✅ Moderar POIs (aprobar/rechazar)
  - ✅ Moderar eventos (aprobar/rechazar)
  - ✅ Moderar incidentes (cambiar estados)
  - ✅ Ver dashboards y analíticas
  - ❌ No puede gestionar usuarios
  - ❌ No puede cambiar roles

##### 🛡️ Administrador
- **Permisos**:
  - ✅ Todos los permisos de Moderador
  - ✅ Gestionar usuarios (cambiar roles, activar/desactivar)
  - ✅ Eliminar contenido
  - ✅ Acceso completo a dashboards
  - ✅ Configuración del sistema
  - ✅ Ver todas las estadísticas

---

### 6. Integraciones con APIs Externas 🌐

#### OpenWeatherMap API ☁️
- **Propósito**: Mostrar clima actual en la ubicación de POIs
- **Funcionalidad**:
  - Temperatura actual
  - Sensación térmica
  - Condición climática (soleado, nublado, lluvia, etc.)
  - Icono del clima
- **Ubicación**: Se muestra en la pantalla de detalles del POI

#### Google Gemini API 🤖
- **Propósito**: Generar descripciones automáticas para POIs usando IA
- **Funcionalidad**:
  - Genera descripciones atractivas basadas en:
    - Nombre del POI
    - Categoría
    - Dirección (si está disponible)
  - Fallback automático: Si la API falla, genera descripción predeterminada
- **Ubicación**: Botón "Generar con IA" en el formulario de creación de POI

#### Google Maps API 🗺️
- **Propósito**: Visualización de mapas y navegación
- **Funcionalidades**:
  - Mostrar POIs en el mapa
  - Mostrar incidentes en el mapa
  - Obtener direcciones
  - Navegación integrada

---

### 7. Sistema de Ubicación 📍

#### Características
- ✅ **Detección Automática**: Obtiene ubicación GPS del dispositivo
- ✅ **Geocodificación**: Convierte coordenadas en direcciones legibles
- ✅ **Geocodificación Inversa**: Convierte direcciones en coordenadas
- ✅ **Permisos**: Solicita permisos de ubicación de forma segura
- ✅ **Fallback Manual**: Si no hay GPS, permite ingresar ubicación manualmente

---

### 8. Gestión de Imágenes 📸

#### Características
- ✅ **Subida Múltiple**: Subir varias imágenes por POI/evento
- ✅ **Almacenamiento en Firebase Storage**: Imágenes seguras en la nube
- ✅ **Carga Asíncrona**: Usa Coil para carga eficiente de imágenes
- ✅ **Vista Previa**: Ver imágenes antes de subir
- ✅ **Optimización**: Compresión automática de imágenes

---

## 🎯 Casos de Uso

### Caso de Uso 1: Ciudadano Descubre un Restaurante 🍽️

1. **Ciudadano abre la app** → Ve la pantalla principal
2. **Navega a "Lugares de Interés"** → Ve lista de POIs
3. **Filtra por categoría "Comida"** → Ve solo restaurantes
4. **Selecciona un restaurante** → Ve detalles completos:
   - Nombre, descripción, dirección
   - Horarios de apertura
   - Calificación y reseñas
   - Fotos del lugar
   - Clima actual en esa ubicación
   - Características (WiFi, estacionamiento, etc.)
5. **Presiona "Abrir en Maps"** → Se abre Google Maps con la ruta
6. **Visita el restaurante** → Puede calificarlo después

---

### Caso de Uso 2: Ciudadano Reporta un Bache 🚧

1. **Ciudadano ve un bache en la calle** → Abre la app
2. **Navega a "Reportar Incidente"** → Abre formulario
3. **Selecciona tipo "Infraestructura"** → Completa descripción
4. **Toma una foto del bache** → Adjunta como evidencia
5. **La app detecta su ubicación GPS** → Confirma la ubicación
6. **Envía el reporte** → Estado: "Pendiente"
7. **Moderador revisa el reporte** → Cambia estado a "En Revisión"
8. **Administrador confirma** → Cambia estado a "Confirmado"
9. **Autoridades resuelven** → Estado cambia a "Resuelto"
10. **Ciudadano recibe notificación** → Ve que su reporte fue resuelto

---

### Caso de Uso 3: Ciudadano Crea un POI 🏛️

1. **Ciudadano encuentra un lugar interesante** → Abre la app
2. **Navega a "Agregar POI"** → Abre formulario
3. **Ingresa nombre y categoría** → Selecciona "Cultura"
4. **Presiona "Generar con IA"** → Gemini genera descripción automática
5. **La descripción se pega automáticamente** → Puede editarla si quiere
6. **Agrega dirección, teléfono, horarios** → Completa información
7. **Sube fotos del lugar** → Adjunta imágenes
8. **Selecciona características** → WiFi, accesibilidad, etc.
9. **Envía el POI** → Estado: "Pendiente"
10. **Moderador revisa** → Aprueba o rechaza
11. **Si es aprobado** → Aparece en la lista pública de POIs

---

### Caso de Uso 4: Administrador Revisa Analíticas 📊

1. **Administrador inicia sesión** → Ve pantalla de administración
2. **Navega a "Analíticas"** → Ve dashboard
3. **Revisa gráficos**:
   - Ve que hay muchos incidentes de "Basura" en marzo
   - Ve que los POIs de "Comida" son los más populares
   - Ve que hay 15 eventos pendientes de aprobación
4. **Toma decisiones**:
   - Asigna más recursos para limpieza en marzo
   - Promueve más POIs de comida
   - Revisa eventos pendientes

---

### Caso de Uso 5: Moderador Aprueba Contenido ✅

1. **Moderador inicia sesión** → Ve pantalla de moderación
2. **Ve lista de POIs pendientes** → Revisa cada uno
3. **Selecciona un POI** → Ve toda la información
4. **Verifica que cumple con las políticas** → Revisa fotos, descripción
5. **Aprueba el POI** → Agrega comentario opcional
6. **El POI aparece públicamente** → Ciudadanos pueden verlo

---

## 🛠️ Características Técnicas

### Arquitectura
- ✅ **MVVM (Model-View-ViewModel)**: Separación clara de responsabilidades
- ✅ **Jetpack Compose**: UI moderna y declarativa
- ✅ **Kotlin Coroutines**: Programación asíncrona
- ✅ **StateFlow**: Estado reactivo y observable
- ✅ **Flow**: Flujos de datos en tiempo real

### Backend
- ✅ **Firebase Firestore**: Base de datos NoSQL en tiempo real
- ✅ **Firebase Storage**: Almacenamiento de imágenes
- ✅ **Firebase Authentication**: Autenticación segura
- ✅ **Retrofit**: Cliente HTTP para APIs externas
- ✅ **Kotlinx Serialization**: Serialización JSON

### Seguridad
- ✅ **Variables de Entorno**: Claves API en archivo `.env` (no commiteado)
- ✅ **Autenticación Firebase**: Login seguro
- ✅ **Roles y Permisos**: Control de acceso basado en roles
- ✅ **Validación de Datos**: Validación en cliente y servidor

### UX/UI
- ✅ **Material Design 3**: Componentes modernos y accesibles
- ✅ **Modo Claro/Oscuro**: Soporte para temas
- ✅ **Navegación Intuitiva**: Bottom navigation y navegación por gestos
- ✅ **Feedback Visual**: Loading states, mensajes de éxito/error
- ✅ **Offline Support**: (Futuro) Funcionalidad offline con Room

---

## 📈 Beneficios del Sistema

### Para la Ciudad 🏙️
- ✅ **Mejora la Gestión Urbana**: Datos centralizados y accesibles
- ✅ **Participación Ciudadana**: Los ciudadanos se involucran activamente
- ✅ **Toma de Decisiones Basada en Datos**: Dashboards con información real
- ✅ **Turismo**: Promoción de lugares de interés
- ✅ **Economía Local**: Apoyo a negocios locales

### Para los Ciudadanos 👥
- ✅ **Descubrimiento**: Encuentran lugares y eventos nuevos
- ✅ **Participación**: Pueden reportar problemas y crear contenido
- ✅ **Información Útil**: Clima, horarios, características de lugares
- ✅ **Navegación**: Fácil acceso a direcciones y mapas
- ✅ **Comunidad**: Se sienten parte de la mejora de su ciudad

### Para Administradores 🛡️
- ✅ **Control Total**: Gestionan usuarios y contenido
- ✅ **Analíticas**: Ven tendencias y patrones
- ✅ **Moderación Eficiente**: Herramientas para aprobar/rechazar contenido
- ✅ **Escalabilidad**: Sistema preparado para crecer

---

## 🎯 Resumen Ejecutivo

**Points App** es una **plataforma integral de gestión urbana** que conecta ciudadanos, moderadores y administradores para:

1. **Descubrir y compartir** lugares de interés en la ciudad
2. **Reportar y resolver** problemas urbanos de forma colaborativa
3. **Organizar y descubrir** eventos locales
4. **Analizar y tomar decisiones** basadas en datos reales
5. **Fomentar la participación ciudadana** en la mejora de su entorno

Con tecnología moderna, interfaz intuitiva y funcionalidades robustas, Points App se posiciona como una herramienta esencial para ciudades inteligentes y comunidades conectadas.

---

**Desarrollado con ❤️ para mejorar la vida urbana**  
**Última actualización: 2025**

