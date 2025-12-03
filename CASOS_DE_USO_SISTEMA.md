# 📋 Casos de Uso del Sistema (CUS) - Points App

## Documento de Análisis de Casos de Uso

Este documento describe los Casos de Uso del Sistema (CUS) de alto nivel identificados para la aplicación **Points App**, una plataforma móvil de gestión urbana inteligente desarrollada para Android.

**Versión:** 1.0  
**Fecha:** 2025  
**Autor:** Análisis de Sistemas

---

## 📑 Índice de Casos de Uso

1. [CUS-01: Autenticación y Registro de Usuarios](#cus-01-autenticación-y-registro-de-usuarios)
2. [CUS-02: Gestión de Puntos de Interés (POIs)](#cus-02-gestión-de-puntos-de-interés-pois)
3. [CUS-03: Reporte y Gestión de Incidentes Urbanos](#cus-03-reporte-y-gestión-de-incidentes-urbanos)
4. [CUS-04: Gestión de Eventos Urbanos](#cus-04-gestión-de-eventos-urbanos)
5. [CUS-05: Sistema de Moderación de Contenido](#cus-05-sistema-de-moderación-de-contenido)
6. [CUS-06: Gestión de Usuarios y Roles](#cus-06-gestión-de-usuarios-y-roles)
7. [CUS-07: Visualización de Dashboards y Analíticas](#cus-07-visualización-de-dashboards-y-analíticas)
8. [CUS-08: Sistema de Favoritos y Sincronización](#cus-08-sistema-de-favoritos-y-sincronización)
9. [CUS-09: Navegación y Visualización en Mapas](#cus-09-navegación-y-visualización-en-mapas)
10. [CUS-10: Gestión de Perfil de Usuario](#cus-10-gestión-de-perfil-de-usuario)

---

## CUS-01: Autenticación y Registro de Usuarios

* **Actor Principal:** Usuario no autenticado

* **Descripción:** Permite a un usuario nuevo registrarse en el sistema o a un usuario existente iniciar sesión para acceder a las funcionalidades de la aplicación. El sistema gestiona la autenticación mediante Firebase Authentication, almacena información del usuario en Firestore y asigna roles predeterminados (Ciudadano por defecto).

* **Precondiciones:** 
  - La aplicación debe estar instalada y configurada correctamente
  - Debe existir conexión a internet
  - Firebase Authentication debe estar configurado y operativo
  - Para registro: el usuario no debe tener una cuenta existente con el mismo email

* **Flujo Básico:**
  1. El usuario abre la aplicación y se presenta la pantalla de inicio de sesión
  2. El usuario selecciona la opción "Registrarse" o "Iniciar Sesión"
  3. **Para Registro:**
     - El usuario ingresa nombre completo, teléfono, email y contraseña
     - El usuario confirma la contraseña
     - Opcionalmente, el usuario puede seleccionar una foto de perfil
     - El sistema valida que todos los campos obligatorios estén completos
     - El sistema valida que las contraseñas coincidan
     - El sistema valida la fortaleza de la contraseña (mínimo 8 caracteres, mayúscula, número, símbolo)
     - El sistema valida el formato del email
     - El sistema crea la cuenta en Firebase Authentication con email y contraseña
     - Si hay foto de perfil, el sistema la sube a Firebase Storage
     - El sistema crea el documento del usuario en Firestore con rol "CIUDADANO" por defecto
     - El sistema redirige al usuario a la pantalla principal correspondiente a su rol
  4. **Para Inicio de Sesión:**
     - El usuario ingresa email y contraseña
     - Opcionalmente, el usuario puede activar "Recordar credenciales"
     - El sistema valida el formato del email
     - El sistema autentica las credenciales con Firebase Authentication
     - El sistema obtiene el tipo de usuario desde Firestore
     - El sistema redirige al usuario según su rol (Administrador → AdminHome, Moderador → AdminHome, Ciudadano → ClientHome)
  5. El sistema muestra un mensaje de éxito y actualiza la interfaz

* **Post-condiciones:**
  - El usuario queda autenticado en el sistema
  - Se crea o actualiza la sesión de usuario en Firebase Authentication
  - El usuario tiene acceso a las funcionalidades según su rol asignado
  - Si se activó "Recordar credenciales", estas se almacenan localmente de forma segura
  - El usuario puede acceder a todas las funcionalidades disponibles para su rol

---

## CUS-02: Gestión de Puntos de Interés (POIs)

* **Actor Principal:** Usuario autenticado (Ciudadano, Moderador, Administrador)

* **Descripción:** Permite a los usuarios crear, buscar, visualizar, calificar y gestionar puntos de interés en la ciudad. Los POIs incluyen información detallada como ubicación GPS, categoría, horarios, características especiales, imágenes y calificaciones. Los POIs creados por ciudadanos requieren aprobación de moderadores antes de ser visibles públicamente.

* **Precondiciones:**
  - El usuario debe estar autenticado en el sistema
  - Para crear POIs: el usuario debe tener rol Ciudadano, Moderador o Administrador
  - Para visualizar POIs: el usuario debe tener rol válido
  - Debe existir conexión a internet para operaciones que requieran Firebase
  - Para geocodificación: debe estar disponible el servicio de Google Maps Geocoding API

* **Flujo Básico:**
  1. **Crear POI:**
     - El usuario navega a la pantalla de creación de POI
     - El usuario ingresa nombre, categoría y dirección del POI
     - Opcionalmente, el usuario puede solicitar generación automática de descripción usando Google Gemini API
     - El sistema realiza geocodificación inversa para obtener coordenadas GPS desde la dirección
     - El usuario completa información adicional: teléfono, email, sitio web, horarios por día de la semana
     - El usuario selecciona características especiales (accesibilidad, WiFi, estacionamiento, etc.)
     - El usuario selecciona rango de precio
     - El usuario sube una o múltiples imágenes del lugar
     - El sistema valida que los campos obligatorios estén completos
     - El sistema sube las imágenes a Firebase Storage
     - El sistema crea el documento del POI en Firestore con estado "PENDIENTE"
     - El sistema asocia el POI al usuario creador mediante usuarioId
     - El sistema muestra mensaje de confirmación
  2. **Buscar y Filtrar POIs:**
     - El usuario accede a la pantalla de lista de POIs
     - El sistema carga automáticamente todos los POIs con estado "APROBADO" desde Firestore
     - El usuario puede realizar búsqueda por texto (nombre, descripción, dirección)
     - El usuario puede filtrar por categoría
     - El usuario puede filtrar por proximidad (radio configurable desde ubicación actual)
     - El usuario puede ordenar por distancia, calificación o fecha de creación
     - El sistema actualiza la lista en tiempo real mediante Flow y Firestore listeners
  3. **Visualizar Detalles de POI:**
     - El usuario selecciona un POI de la lista
     - El sistema navega a la pantalla de detalles
     - El sistema carga información completa del POI
     - El sistema obtiene y muestra el clima actual en la ubicación del POI mediante OpenWeatherMap API
     - El sistema muestra todas las imágenes, horarios, características y calificaciones
     - El usuario puede ver la ubicación en el mapa integrado
     - El usuario puede abrir la ubicación en Google Maps para navegación
  4. **Calificar POI:**
     - El usuario accede a la pantalla de detalles de un POI aprobado
     - El usuario selecciona una calificación (sistema de estrellas)
     - El sistema actualiza la calificación promedio y el total de calificaciones en Firestore
     - El sistema muestra la calificación actualizada
  5. **Agregar a Favoritos:**
     - El usuario accede a la pantalla de detalles de un POI
     - El usuario presiona el botón de favorito
     - El sistema guarda el POI en la base de datos local (Room) como favorito
     - El sistema actualiza la interfaz para indicar que está en favoritos

* **Post-condiciones:**
  - Si se creó un POI: queda almacenado en Firestore con estado "PENDIENTE" y asociado al usuario creador
  - Si se calificó un POI: la calificación promedio y total se actualizan en Firestore
  - Si se agregó a favoritos: el POI queda almacenado localmente y disponible para sincronización
  - Los cambios se reflejan en tiempo real para todos los usuarios mediante Firestore listeners
  - El POI creado queda disponible para moderación por parte de moderadores o administradores

---

## CUS-03: Reporte y Gestión de Incidentes Urbanos

* **Actor Principal:** Usuario autenticado (Ciudadano, Moderador, Administrador)

* **Descripción:** Permite a los ciudadanos reportar incidentes urbanos en tiempo real (inseguridad, accidentes, vandalismo, infraestructura dañada, etc.) con ubicación GPS, descripción, fotos y videos. Los moderadores y administradores pueden gestionar estos reportes cambiando su estado (Pendiente, En Revisión, Confirmado, Rechazado, Resuelto).

* **Precondiciones:**
  - El usuario debe estar autenticado en el sistema
  - Para reportar: el usuario debe tener rol Ciudadano, Moderador o Administrador
  - Para gestionar: el usuario debe tener rol Moderador o Administrador
  - Debe existir conexión a internet
  - Para detección automática de ubicación: el dispositivo debe tener GPS activo y permisos de ubicación otorgados

* **Flujo Básico:**
  1. **Reportar Incidente:**
     - El usuario navega a la pantalla de reporte de incidentes
     - El usuario selecciona el tipo de incidente (Inseguridad, Accidente de Tránsito, Incendio, Inundación, Vandalismo, Basura, Infraestructura, Otro)
     - El usuario ingresa una descripción detallada del incidente
     - El sistema detecta automáticamente la ubicación GPS del dispositivo o permite ingresarla manualmente
     - El sistema realiza geocodificación para convertir coordenadas en dirección legible
     - El usuario puede adjuntar una o múltiples fotos como evidencia
     - El usuario puede adjuntar un video como evidencia (opcional)
     - El sistema valida que los campos obligatorios estén completos
     - El sistema sube las imágenes y videos a Firebase Storage
     - El sistema crea el documento del incidente en Firestore con estado "PENDIENTE"
     - El sistema asocia el incidente al usuario reportante mediante usuarioId
     - El sistema muestra mensaje de confirmación
  2. **Visualizar Incidentes:**
     - El usuario accede a la pantalla de lista de incidentes
     - El sistema carga todos los incidentes desde Firestore según los permisos del usuario
     - Los ciudadanos ven solo incidentes con estado "CONFIRMADO" o "RESUELTO"
     - Los moderadores y administradores ven todos los incidentes
     - El usuario puede filtrar por tipo de incidente
     - El usuario puede filtrar por estado
     - El usuario puede filtrar por fecha
     - El sistema actualiza la lista en tiempo real mediante Flow y Firestore listeners
  3. **Visualizar Mapa de Incidentes:**
     - El usuario accede a la pantalla de mapa de incidentes
     - El sistema carga todos los incidentes visibles según el rol del usuario
     - El sistema muestra los incidentes como marcadores en Google Maps
     - El usuario puede ver detalles de cada incidente al tocar un marcador
     - El usuario puede filtrar los incidentes visibles en el mapa
  4. **Gestionar Estado de Incidente (Moderadores/Administradores):**
     - El moderador o administrador accede a la pantalla de gestión de incidentes
     - El moderador selecciona un incidente pendiente o en revisión
     - El moderador revisa la información, fotos y videos del incidente
     - El moderador puede cambiar el estado a "EN_REVISION", "CONFIRMADO", "RECHAZADO" o "RESUELTO"
     - El moderador puede agregar comentarios de moderación
     - El sistema actualiza el estado del incidente en Firestore
     - El sistema muestra el cambio de estado en tiempo real para todos los usuarios

* **Post-condiciones:**
  - Si se reportó un incidente: queda almacenado en Firestore con estado "PENDIENTE", asociado al usuario reportante y con ubicación GPS registrada
  - Si se cambió el estado: el incidente queda actualizado en Firestore y visible según las reglas de permisos
  - Los cambios se reflejan en tiempo real mediante Firestore listeners
  - El usuario reportante puede ver el estado actualizado de su reporte
  - Los incidentes confirmados son visibles para todos los ciudadanos en el mapa

---

## CUS-04: Gestión de Eventos Urbanos

* **Actor Principal:** Usuario autenticado (Ciudadano, Moderador, Administrador)

* **Descripción:** Permite a los usuarios crear, buscar, visualizar e inscribirse en eventos urbanos (culturales, deportivos, educativos, etc.). Los eventos incluyen información completa como fechas, horarios, ubicación, capacidad, precios, contacto y características especiales. Los eventos creados por ciudadanos requieren aprobación de moderadores antes de ser visibles públicamente.

* **Precondiciones:**
  - El usuario debe estar autenticado en el sistema
  - Para crear eventos: el usuario debe tener rol Ciudadano, Moderador o Administrador
  - Para visualizar eventos: el usuario debe tener rol válido
  - Debe existir conexión a internet
  - Para geocodificación: debe estar disponible el servicio de Google Maps Geocoding API

* **Flujo Básico:**
  1. **Crear Evento:**
     - El usuario navega a la pantalla de creación de evento
     - El usuario ingresa nombre, descripción, categoría y dirección del evento
     - El sistema realiza geocodificación inversa para obtener coordenadas GPS desde la dirección
     - El usuario selecciona fecha y hora de inicio y fin del evento
     - El usuario puede marcar el evento como recurrente (diario, semanal, mensual, anual)
     - Si es recurrente, el usuario selecciona frecuencia y fecha de fin de recurrencia
     - El usuario completa información adicional: organizador, contacto (teléfono, email), sitio web
     - El usuario configura precio (gratuito o con rango de precios)
     - El usuario puede configurar capacidad máxima y si requiere inscripción
     - El usuario puede establecer edad mínima y máxima
     - El usuario selecciona características especiales (accesibilidad, estacionamiento, transporte público)
     - El usuario puede agregar enlaces a redes sociales (Facebook, Instagram, Twitter)
     - El usuario sube una o múltiples imágenes del evento
     - El usuario puede agregar etiquetas para búsqueda
     - El sistema valida que los campos obligatorios estén completos
     - El sistema valida que la fecha de fin sea posterior a la fecha de inicio
     - El sistema sube las imágenes a Firebase Storage
     - El sistema crea el documento del evento en Firestore con estado "PENDIENTE"
     - El sistema asocia el evento al usuario creador mediante usuarioId
     - El sistema muestra mensaje de confirmación
  2. **Buscar y Filtrar Eventos:**
     - El usuario accede a la pantalla de lista de eventos
     - El sistema carga automáticamente todos los eventos con estado "APROBADO" desde Firestore
     - El usuario puede realizar búsqueda por texto (nombre, descripción)
     - El usuario puede filtrar por categoría
     - El usuario puede filtrar por fecha (eventos futuros, pasados, en un rango)
     - El usuario puede filtrar por ubicación (proximidad)
     - El usuario puede ordenar por fecha, nombre o categoría
     - El sistema actualiza la lista en tiempo real mediante Flow y Firestore listeners
  3. **Visualizar Detalles de Evento:**
     - El usuario selecciona un evento de la lista
     - El sistema navega a la pantalla de detalles
     - El sistema carga información completa del evento
     - El sistema muestra todas las imágenes, fechas, horarios, ubicación y características
     - El usuario puede ver la ubicación en el mapa integrado
     - El usuario puede abrir la ubicación en Google Maps para navegación
     - Si el evento requiere inscripción, el usuario puede ver el número de inscripciones actuales y la capacidad
  4. **Inscribirse en Evento:**
     - El usuario accede a la pantalla de detalles de un evento aprobado
     - Si el evento requiere inscripción y hay capacidad disponible
     - El usuario presiona el botón "Inscribirse"
     - El sistema incrementa el contador de inscripciones en Firestore
     - El sistema muestra confirmación de inscripción
  5. **Gestionar Estado de Evento (Moderadores/Administradores):**
     - El moderador o administrador accede a la pantalla de gestión de eventos
     - El moderador selecciona un evento pendiente
     - El moderador revisa toda la información del evento
     - El moderador puede aprobar, rechazar o cancelar el evento
     - El moderador puede agregar comentarios de moderación
     - El sistema actualiza el estado del evento en Firestore
     - Si se aprueba, el evento queda visible para todos los usuarios

* **Post-condiciones:**
  - Si se creó un evento: queda almacenado en Firestore con estado "PENDIENTE" y asociado al usuario creador
  - Si se inscribió en un evento: el contador de inscripciones se actualiza en Firestore
  - Si se cambió el estado: el evento queda actualizado y visible según las reglas de permisos
  - Los cambios se reflejan en tiempo real mediante Firestore listeners
  - El evento aprobado queda disponible para visualización y búsqueda por todos los usuarios

---

## CUS-05: Sistema de Moderación de Contenido

* **Actor Principal:** Moderador o Administrador

* **Descripción:** Permite a moderadores y administradores revisar, aprobar, rechazar o suspender contenido creado por ciudadanos (POIs, eventos e incidentes). El sistema mantiene un historial de moderación con comentarios y fechas de revisión.

* **Precondiciones:**
  - El usuario debe estar autenticado en el sistema
  - El usuario debe tener rol Moderador o Administrador
  - Debe existir contenido pendiente de moderación en el sistema
  - Debe existir conexión a internet

* **Flujo Básico:**
  1. **Acceder a Contenido Pendiente:**
     - El moderador accede a la pantalla de gestión correspondiente (POIs, Eventos o Incidentes)
     - El sistema carga automáticamente todos los elementos con estado "PENDIENTE" o "EN_REVISION" desde Firestore
     - El sistema muestra la lista de elementos pendientes ordenados por fecha de creación
  2. **Revisar Contenido:**
     - El moderador selecciona un elemento de la lista
     - El sistema muestra todos los detalles del elemento (información completa, imágenes, ubicación, etc.)
     - El moderador revisa que el contenido cumpla con las políticas del sistema
     - El moderador verifica la veracidad y calidad de la información
  3. **Aprobar Contenido:**
     - El moderador presiona el botón "Aprobar"
     - El sistema muestra un diálogo de moderación
     - El moderador puede agregar comentarios opcionales de moderación
     - El moderador confirma la aprobación
     - El sistema actualiza el estado del elemento a "APROBADO" en Firestore
     - El sistema registra el moderadorId, fechaModeracion y comentariosModeracion
     - El sistema actualiza fechaActualizacion
     - El elemento queda visible públicamente para todos los usuarios
     - El sistema muestra mensaje de confirmación
  4. **Rechazar Contenido:**
     - El moderador presiona el botón "Rechazar"
     - El sistema muestra un diálogo de moderación
     - El moderador debe ingresar comentarios explicando el motivo del rechazo (obligatorio)
     - El moderador confirma el rechazo
     - El sistema actualiza el estado del elemento a "RECHAZADO" en Firestore
     - El sistema registra el moderadorId, fechaModeracion y comentariosModeracion
     - El elemento no queda visible públicamente
     - El usuario creador puede ver el estado y los comentarios de rechazo
  5. **Suspender Contenido (Solo Administradores):**
     - El administrador puede suspender contenido previamente aprobado
     - El sistema actualiza el estado a "SUSPENDIDO"
     - El elemento deja de ser visible públicamente
  6. **Cambiar Estado de Incidente:**
     - Para incidentes, el moderador puede cambiar el estado a "EN_REVISION", "CONFIRMADO", "RECHAZADO" o "RESUELTO"
     - El sistema actualiza el estado en Firestore
     - El sistema registra la información de moderación

* **Post-condiciones:**
  - El contenido moderado queda con su estado actualizado en Firestore
  - Se registra información de moderación (moderadorId, fechaModeracion, comentariosModeracion)
  - Si fue aprobado: el contenido queda visible públicamente y accesible en búsquedas
  - Si fue rechazado o suspendido: el contenido deja de ser visible públicamente
  - Los cambios se reflejan en tiempo real mediante Firestore listeners
  - El usuario creador puede ver el estado actualizado de su contenido

---

## CUS-06: Gestión de Usuarios y Roles

* **Actor Principal:** Administrador

* **Descripción:** Permite a los administradores gestionar usuarios del sistema, incluyendo cambiar roles (Ciudadano, Moderador, Administrador), activar/desactivar cuentas, ver estadísticas de usuarios y eliminar usuarios. Solo los administradores tienen acceso a esta funcionalidad.

* **Precondiciones:**
  - El usuario debe estar autenticado en el sistema
  - El usuario debe tener rol Administrador
  - Debe existir conexión a internet
  - Debe haber usuarios registrados en el sistema

* **Flujo Básico:**
  1. **Acceder a Gestión de Usuarios:**
     - El administrador accede a la pantalla de gestión de usuarios desde el menú administrativo
     - El sistema verifica que el usuario tenga rol Administrador
     - El sistema carga todos los usuarios registrados desde Firestore
     - El sistema muestra la lista de usuarios con información básica (nombre, email, rol, estado)
  2. **Buscar y Filtrar Usuarios:**
     - El administrador puede realizar búsqueda por texto (nombre, email)
     - El administrador puede filtrar por rol (Ciudadano, Moderador, Administrador)
     - El administrador puede filtrar para mostrar solo usuarios activos
     - El sistema actualiza la lista en tiempo real
  3. **Ver Detalles de Usuario:**
     - El administrador selecciona un usuario de la lista
     - El sistema muestra información completa del usuario (nombre, email, teléfono, rol, fecha de registro, foto de perfil)
     - El sistema muestra estadísticas del usuario (cantidad de POIs creados, eventos creados, incidentes reportados)
  4. **Cambiar Rol de Usuario:**
     - El administrador selecciona un usuario
     - El administrador presiona el botón "Editar"
     - El sistema muestra un diálogo de edición
     - El administrador selecciona el nuevo rol (Ciudadano, Moderador, Administrador)
     - El administrador confirma el cambio
     - El sistema actualiza el campo "tipo" del usuario en Firestore
     - El sistema muestra mensaje de confirmación
     - El usuario afectado verá cambios en sus permisos al iniciar sesión nuevamente
  5. **Eliminar Usuario:**
     - El administrador selecciona un usuario
     - El administrador presiona el botón "Eliminar"
     - El sistema muestra un diálogo de confirmación
     - El administrador confirma la eliminación
     - El sistema elimina el documento del usuario de Firestore
     - El sistema muestra mensaje de confirmación
  6. **Ver Estadísticas de Usuarios:**
     - El sistema muestra estadísticas agregadas: total de usuarios, distribución por rol, usuarios activos vs inactivos
     - El administrador puede ver estas estadísticas en la pantalla de gestión

* **Post-condiciones:**
  - Si se cambió el rol: el usuario queda con el nuevo rol asignado en Firestore y tendrá los permisos correspondientes
  - Si se eliminó un usuario: el documento del usuario se elimina de Firestore (el usuario no podrá iniciar sesión)
  - Los cambios se reflejan en tiempo real
  - El usuario afectado verá cambios en sus permisos en la próxima sesión
  - Las estadísticas se actualizan automáticamente

---

## CUS-07: Visualización de Dashboards y Analíticas

* **Actor Principal:** Moderador o Administrador

* **Descripción:** Permite a moderadores y administradores visualizar dashboards interactivos con estadísticas y analíticas del sistema, incluyendo distribución de incidentes por tipo, eventos por mes, POIs por categoría, y distribución por estados de moderación. Los datos se actualizan en tiempo real desde Firestore.

* **Precondiciones:**
  - El usuario debe estar autenticado en el sistema
  - El usuario debe tener rol Moderador o Administrador
  - Debe existir conexión a internet
  - Debe haber datos en el sistema (incidentes, eventos, POIs)

* **Flujo Básico:**
  1. **Acceder al Dashboard:**
     - El moderador o administrador accede a la pantalla de analíticas desde el menú administrativo
     - El sistema verifica que el usuario tenga permisos de moderador o administrador
     - El sistema carga todos los datos necesarios desde Firestore (incidentes, eventos, POIs)
  2. **Visualizar Gráficos de Incidentes:**
     - El sistema muestra un gráfico de barras con la distribución de incidentes por tipo (Inseguridad, Accidente, Incendio, etc.)
     - El sistema muestra un gráfico de pastel con el porcentaje de cada tipo de incidente
     - El sistema muestra la distribución mensual de incidentes (cantidad por mes)
     - El sistema muestra la distribución de incidentes por estado (Pendiente, En Revisión, Confirmado, Rechazado, Resuelto)
  3. **Visualizar Gráficos de Eventos:**
     - El sistema muestra la distribución mensual de eventos (cantidad por mes)
     - El sistema muestra la distribución de eventos por estado (Pendiente, Aprobado, Rechazado, Cancelado)
     - El sistema muestra la distribución de eventos por categoría
  4. **Visualizar Gráficos de POIs:**
     - El sistema muestra la distribución mensual de POIs (cantidad por mes)
     - El sistema muestra la distribución de POIs por estado (Pendiente, En Revisión, Aprobado, Rechazado, Suspendido)
     - El sistema muestra la distribución de POIs por categoría
  5. **Filtrar Datos:**
     - El usuario puede seleccionar un rango de fechas para filtrar los datos
     - El sistema recalcula y actualiza todos los gráficos según el filtro temporal
  6. **Actualización en Tiempo Real:**
     - El sistema utiliza Firestore listeners para actualizar los datos automáticamente
     - Cuando hay cambios en incidentes, eventos o POIs, los gráficos se actualizan sin necesidad de recargar

* **Post-condiciones:**
  - Los dashboards muestran información actualizada en tiempo real
  - Los gráficos reflejan la distribución actual de datos en el sistema
  - El usuario puede tomar decisiones basadas en los datos visualizados
  - Los datos se mantienen sincronizados mediante Firestore listeners

---

## CUS-08: Sistema de Favoritos y Sincronización

* **Actor Principal:** Usuario autenticado

* **Descripción:** Permite a los usuarios agregar POIs a favoritos, almacenarlos localmente en la base de datos Room, y sincronizarlos con el backend Spring Boot mediante un sistema bidireccional (push/pull). Incluye sincronización automática programada con WorkManager y sincronización manual bajo demanda.

* **Precondiciones:**
  - El usuario debe estar autenticado en el sistema
  - Debe existir conexión a internet para sincronización
  - El backend Spring Boot debe estar disponible y configurado
  - El usuario debe tener un token JWT válido de Firebase Authentication
  - La base de datos local Room debe estar inicializada

* **Flujo Básico:**
  1. **Agregar POI a Favoritos:**
     - El usuario accede a la pantalla de detalles de un POI aprobado
     - El usuario presiona el botón de favorito (estrella o corazón)
     - El sistema guarda el POI en la base de datos local Room (tabla FavoritePOI)
     - El sistema actualiza la interfaz para indicar que está en favoritos
     - El POI queda marcado para sincronización en la próxima sincronización
  2. **Ver Favoritos:**
     - El usuario accede a la pantalla de favoritos
     - El sistema carga todos los POIs favoritos desde la base de datos local Room
     - El sistema muestra la lista de favoritos
     - El usuario puede eliminar favoritos desde esta pantalla
  3. **Sincronización Manual:**
     - El usuario accede a la pantalla de configuración de sincronización desde el perfil
     - El usuario presiona el botón "Sincronizar Ahora"
     - El sistema obtiene el token JWT del usuario autenticado
     - El sistema agrega el token al header Authorization de las peticiones HTTP
     - **Paso PULL:** El sistema realiza petición GET al endpoint `/api/v1/sync/pull` del backend
     - El backend retorna los favoritos del usuario desde PostgreSQL
     - El sistema actualiza la base de datos local con los favoritos recibidos
     - **Paso PUSH:** El sistema obtiene todos los favoritos locales
     - El sistema realiza petición POST al endpoint `/api/v1/sync/push` con los favoritos locales
     - El backend guarda los favoritos en PostgreSQL
     - El sistema guarda el timestamp de última sincronización en DataStore
     - El sistema muestra mensaje de éxito o error
  4. **Sincronización Automática:**
     - El usuario activa la sincronización automática en la pantalla de configuración
     - El usuario configura la frecuencia (15 min, 30 min, 1 hora, 2 horas, 4 horas)
     - El usuario puede configurar restricción de red (solo WiFi o cualquier conexión)
     - El sistema programa un trabajo periódico con WorkManager
     - WorkManager ejecuta SyncWorker según la frecuencia configurada
     - SyncWorker realiza el mismo proceso de sincronización (PULL y PUSH)
     - El sistema actualiza el timestamp de última sincronización
  5. **Sincronización de Caché e Historial:**
     - Además de favoritos, el sistema sincroniza POIs en caché y historial de búsquedas
     - El proceso es similar: PULL para obtener datos del servidor, PUSH para enviar datos locales

* **Post-condiciones:**
  - Si se agregó a favoritos: el POI queda almacenado localmente en Room y marcado para sincronización
  - Si se sincronizó: los favoritos locales y del servidor quedan sincronizados
  - El timestamp de última sincronización se actualiza en DataStore
  - Si hay sincronización automática activa: el trabajo periódico queda programado en WorkManager
  - Los datos quedan disponibles tanto localmente como en el backend PostgreSQL

---

## CUS-09: Navegación y Visualización en Mapas

* **Actor Principal:** Usuario autenticado

* **Descripción:** Permite a los usuarios visualizar POIs e incidentes en mapas interactivos de Google Maps, obtener direcciones, navegar a ubicaciones y filtrar elementos visibles en el mapa. Incluye detección automática de ubicación del usuario y geocodificación de direcciones.

* **Precondiciones:**
  - El usuario debe estar autenticado en el sistema
  - Debe existir conexión a internet
  - Google Maps API debe estar configurada y operativa
  - Para detección automática: el dispositivo debe tener GPS activo y permisos de ubicación otorgados
  - Debe haber POIs aprobados o incidentes confirmados en el sistema

* **Flujo Básico:**
  1. **Visualizar Mapa de POIs:**
     - El usuario accede a la pantalla de mapa de POIs
     - El sistema solicita permisos de ubicación si no están otorgados
     - El sistema detecta automáticamente la ubicación actual del usuario (si los permisos están otorgados)
     - El sistema centra el mapa en la ubicación del usuario o en una ubicación predeterminada
     - El sistema carga todos los POIs aprobados desde Firestore
     - El sistema muestra cada POI como un marcador en el mapa con icono según su categoría
     - El usuario puede tocar un marcador para ver información básica del POI
     - El usuario puede tocar "Ver detalles" para navegar a la pantalla de detalles completa
  2. **Visualizar Mapa de Incidentes:**
     - El usuario accede a la pantalla de mapa de incidentes
     - El sistema carga todos los incidentes visibles según el rol del usuario (ciudadanos ven solo confirmados/resueltos)
     - El sistema muestra cada incidente como un marcador en el mapa con color según el tipo
     - El usuario puede tocar un marcador para ver información del incidente
     - El usuario puede filtrar los incidentes visibles por tipo o estado
  3. **Obtener Direcciones y Navegar:**
     - El usuario accede a la pantalla de detalles de un POI o incidente
     - El usuario presiona el botón "Abrir en Maps" o "Navegar"
     - El sistema construye una URI de Google Maps con las coordenadas del lugar
     - El sistema abre Google Maps en el dispositivo con la ruta hacia el destino
     - El usuario puede iniciar navegación desde Google Maps
  4. **Buscar por Proximidad:**
     - El usuario puede filtrar POIs o incidentes por proximidad desde la pantalla de mapa
     - El usuario selecciona un radio de búsqueda (ej: 1 km, 5 km, 10 km)
     - El sistema calcula la distancia desde la ubicación actual del usuario
     - El sistema muestra solo los elementos dentro del radio seleccionado
  5. **Seleccionar Ubicación Manualmente:**
     - En pantallas de creación (POI, evento, incidente), el usuario puede seleccionar ubicación en el mapa
     - El sistema muestra un mapa interactivo
     - El usuario toca en el mapa para seleccionar una ubicación
     - El sistema obtiene las coordenadas del punto seleccionado
     - El sistema realiza geocodificación inversa para obtener la dirección
     - El sistema actualiza los campos de ubicación en el formulario

* **Post-condiciones:**
  - El mapa muestra todos los elementos visibles según los permisos del usuario
  - La ubicación del usuario se muestra en el mapa si los permisos están otorgados
  - Los marcadores se actualizan en tiempo real cuando hay cambios en Firestore
  - Si se seleccionó una ubicación: las coordenadas y dirección quedan disponibles para el formulario
  - Si se abrió Google Maps: la aplicación de mapas queda abierta con la ruta configurada

---

## CUS-10: Gestión de Perfil de Usuario

* **Actor Principal:** Usuario autenticado

* **Descripción:** Permite a los usuarios visualizar y editar su información de perfil, incluyendo nombre, teléfono, email, foto de perfil y preferencias de notificaciones. También permite acceder a configuración de sincronización y cerrar sesión.

* **Precondiciones:**
  - El usuario debe estar autenticado en el sistema
  - Debe existir conexión a internet para actualizar información en Firestore
  - Para cambiar foto: el dispositivo debe tener acceso a la galería o cámara

* **Flujo Básico:**
  1. **Acceder al Perfil:**
     - El usuario accede a la pantalla de perfil desde el menú de navegación
     - El sistema carga la información del usuario actual desde Firestore
     - El sistema muestra nombre, email, teléfono, foto de perfil y rol del usuario
  2. **Editar Información Personal:**
     - El usuario presiona el botón "Editar Perfil"
     - El sistema muestra un formulario con los campos editables (nombre, teléfono)
     - El usuario modifica los campos deseados
     - El sistema valida que los campos obligatorios estén completos
     - El usuario confirma los cambios
     - El sistema actualiza el documento del usuario en Firestore
     - El sistema muestra mensaje de confirmación
  3. **Cambiar Foto de Perfil:**
     - El usuario presiona el botón para cambiar foto de perfil
     - El sistema muestra opciones: seleccionar de galería o tomar foto
     - El usuario selecciona una imagen
     - El sistema sube la imagen a Firebase Storage
     - El sistema actualiza el campo photoUrl del usuario en Firestore
     - El sistema muestra la nueva foto de perfil
  4. **Gestionar Preferencias de Notificaciones:**
     - El usuario puede activar o desactivar las notificaciones
     - El sistema actualiza el campo "notificaciones" del usuario en Firestore
  5. **Acceder a Configuración de Sincronización:**
     - El usuario presiona el botón "Sincronización" en el perfil
     - El sistema navega a la pantalla de configuración de sincronización
     - El usuario puede configurar sincronización automática, frecuencia y restricciones de red
  6. **Cerrar Sesión:**
     - El usuario presiona el botón "Cerrar Sesión"
     - El sistema muestra un diálogo de confirmación
     - El usuario confirma
     - El sistema cierra la sesión en Firebase Authentication
     - El sistema limpia las credenciales almacenadas localmente (si existen)
     - El sistema navega a la pantalla de inicio de sesión

* **Post-condiciones:**
  - Si se editó información: los cambios quedan guardados en Firestore y visibles en el perfil
  - Si se cambió la foto: la nueva foto queda almacenada en Firebase Storage y visible en el perfil
  - Si se cerró sesión: el usuario queda desautenticado y debe iniciar sesión nuevamente para acceder
  - Los cambios se reflejan inmediatamente en la interfaz
  - Otros usuarios pueden ver los cambios en información pública del perfil (si aplica)

---

## 📊 Resumen de Actores

### Actores Identificados:

1. **Usuario no autenticado:** Usuario que no ha iniciado sesión en el sistema
2. **Ciudadano:** Usuario autenticado con rol Ciudadano (permisos básicos)
3. **Moderador:** Usuario autenticado con rol Moderador (puede moderar contenido)
4. **Administrador:** Usuario autenticado con rol Administrador (acceso completo)

---

## 🔄 Relaciones entre Casos de Uso

- **CUS-01** es prerrequisito para todos los demás casos de uso (excepto registro)
- **CUS-02, CUS-03, CUS-04** dependen de **CUS-01** y pueden generar contenido que requiere **CUS-05**
- **CUS-05** depende de **CUS-02, CUS-03, CUS-04** para tener contenido que moderar
- **CUS-06** solo es accesible para Administradores
- **CUS-07** es accesible para Moderadores y Administradores
- **CUS-08** puede utilizarse desde **CUS-02** (agregar favoritos) y **CUS-10** (configurar sincronización)
- **CUS-09** se utiliza en **CUS-02, CUS-03, CUS-04** para visualización en mapas
- **CUS-10** es independiente pero requiere **CUS-01**

---

**Documento generado mediante análisis exhaustivo del código fuente, documentación y estructura del proyecto Points App.**

