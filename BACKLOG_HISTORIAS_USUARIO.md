# 📋 Backlog de Historias de Usuario - Points App

## Documento de Product Backlog

Este documento contiene el backlog completo de historias de usuario para la aplicación **Points App**, generado a partir de los Casos de Uso del Sistema (CUS-01 al CUS-10).

**Versión:** 1.0  
**Fecha:** 2025  
**Product Owner:** Análisis de Producto

---

## 📑 Índice

- [Historias de Autenticación (CUS-01)](#historias-de-autenticación-cus-01)
- [Historias de POIs (CUS-02)](#historias-de-pois-cus-02)
- [Historias de Incidentes (CUS-03)](#historias-de-incidentes-cus-03)
- [Historias de Eventos (CUS-04)](#historias-de-eventos-cus-04)
- [Historias de Moderación (CUS-05)](#historias-de-moderación-cus-05)
- [Historias de Gestión de Usuarios (CUS-06)](#historias-de-gestión-de-usuarios-cus-06)
- [Historias de Dashboards (CUS-07)](#historias-de-dashboards-cus-07)
- [Historias de Sincronización (CUS-08)](#historias-de-sincronización-cus-08)
- [Historias de Mapas (CUS-09)](#historias-de-mapas-cus-09)
- [Historias de Perfil (CUS-10)](#historias-de-perfil-cus-10)

---

## Historias de Autenticación (CUS-01)

### HU-001: Registro de Nuevo Usuario

* **Relacionado a:** CUS-01

* **Prioridad:** Alta

* **Descripción:**

  > "Como **usuario nuevo**, quiero **registrarme en el sistema con email y contraseña**, para **acceder a las funcionalidades de la aplicación**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que todos los campos obligatorios estén completos (nombre, teléfono, email, contraseña)
  - [ ] El sistema debe validar que las contraseñas coincidan en los campos "Contraseña" y "Confirmar Contraseña"
  - [ ] El sistema debe validar la fortaleza de la contraseña: mínimo 8 caracteres, al menos una mayúscula, un número y un símbolo
  - [ ] El sistema debe validar el formato del email usando `android.util.Patterns.EMAIL_ADDRESS`
  - [ ] Si el email ya existe en Firebase Authentication, el sistema debe mostrar error "El correo ya está registrado"
  - [ ] El sistema debe crear la cuenta en Firebase Authentication con `createUserWithEmailAndPassword()`
  - [ ] Si el usuario selecciona foto de perfil, el sistema debe subirla a Firebase Storage antes de crear el documento en Firestore
  - [ ] El sistema debe crear el documento del usuario en Firestore con rol "CIUDADANO" por defecto
  - [ ] El sistema debe redirigir al usuario a la pantalla ClientHome después del registro exitoso
  - [ ] El sistema debe mostrar mensaje de éxito "Usuario registrado exitosamente"

---

### HU-002: Inicio de Sesión de Usuario

* **Relacionado a:** CUS-01

* **Prioridad:** Alta

* **Descripción:**

  > "Como **usuario registrado**, quiero **iniciar sesión con email y contraseña**, para **acceder a mi cuenta y las funcionalidades según mi rol**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar el formato del email antes de intentar autenticación
  - [ ] El sistema debe autenticar las credenciales con Firebase Authentication usando `signInWithEmailAndPassword()`
  - [ ] Si las credenciales son incorrectas, el sistema debe mostrar el mensaje de error de Firebase
  - [ ] El sistema debe obtener el tipo de usuario desde Firestore después de autenticación exitosa
  - [ ] El sistema debe redirigir según el rol:
    - Administrador → AdminHome
    - Moderador → AdminHome
    - Ciudadano → ClientHome
  - [ ] Si el usuario activa "Recordar credenciales", el sistema debe guardarlas localmente usando CredentialsStorage
  - [ ] El sistema debe mostrar mensaje de éxito "Inicio de sesión exitoso"
  - [ ] El sistema debe persistir las preferencias del usuario después del login

---

### HU-003: Recuperación de Contraseña

* **Relacionado a:** CUS-01

* **Prioridad:** Media

* **Descripción:**

  > "Como **usuario que olvidé mi contraseña**, quiero **recibir un email para restablecer mi contraseña**, para **poder acceder nuevamente a mi cuenta**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el email ingresado tenga formato válido
  - [ ] El sistema debe enviar email de recuperación usando `FirebaseAuth.sendPasswordResetEmail()`
  - [ ] Si el email no existe, el sistema debe mostrar mensaje genérico (por seguridad)
  - [ ] El sistema debe mostrar mensaje de confirmación "Se ha enviado un email de recuperación"
  - [ ] El sistema debe permitir al usuario volver a la pantalla de login

---

## Historias de POIs (CUS-02)

### HU-004: Crear Punto de Interés

* **Relacionado a:** CUS-02

* **Prioridad:** Alta

* **Descripción:**

  > "Como **ciudadano**, quiero **crear un nuevo punto de interés con información completa**, para **compartirlo con la comunidad después de aprobación**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario esté autenticado antes de permitir crear POI
  - [ ] El sistema debe validar campos obligatorios: nombre, descripción, categoría, dirección
  - [ ] El sistema debe validar que la ubicación tenga coordenadas válidas (lat != 0.0 y lon != 0.0)
  - [ ] El sistema debe realizar geocodificación inversa para obtener coordenadas GPS desde la dirección ingresada
  - [ ] Si la geocodificación falla, el sistema debe permitir seleccionar ubicación manualmente en el mapa
  - [ ] El sistema debe permitir subir múltiples imágenes (mínimo 1, máximo configurable)
  - [ ] Las imágenes deben subirse a Firebase Storage antes de crear el documento en Firestore
  - [ ] El sistema debe crear el documento del POI en Firestore con estado "PENDIENTE"
  - [ ] El sistema debe asociar el POI al usuario creador mediante campo `usuarioId`
  - [ ] El sistema debe establecer `fechaCreacion` y `fechaActualizacion` con `Timestamp.now()`
  - [ ] El sistema debe mostrar mensaje de confirmación "POI creado exitosamente, pendiente de aprobación"
  - [ ] El sistema debe redirigir a la lista de POIs después de creación exitosa

---

### HU-005: Generar Descripción de POI con IA

* **Relacionado a:** CUS-02

* **Prioridad:** Media

* **Descripción:**

  > "Como **ciudadano**, quiero **generar automáticamente la descripción de un POI usando IA**, para **ahorrar tiempo y tener descripciones atractivas**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe mostrar botón "Generar con IA" en el formulario de creación de POI
  - [ ] El botón debe estar habilitado solo si el usuario ha ingresado nombre y categoría
  - [ ] El sistema debe llamar a Google Gemini API con el nombre, categoría y dirección (si está disponible)
  - [ ] El sistema debe mostrar indicador de carga mientras se genera la descripción
  - [ ] Si la API de Gemini está disponible y responde exitosamente, el sistema debe pegar la descripción generada en el campo
  - [ ] Si la API falla o no está configurada, el sistema debe generar una descripción predeterminada basada en nombre y categoría
  - [ ] El usuario debe poder editar la descripción generada antes de enviar
  - [ ] El sistema no debe sobrescribir una descripción ya escrita por el usuario sin confirmación

---

### HU-006: Buscar y Filtrar POIs

* **Relacionado a:** CUS-02

* **Prioridad:** Alta

* **Descripción:**

  > "Como **usuario**, quiero **buscar y filtrar puntos de interés por texto, categoría y proximidad**, para **encontrar rápidamente los lugares que me interesan**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe cargar automáticamente todos los POIs con estado "APROBADO" desde Firestore al abrir la pantalla
  - [ ] El sistema debe usar Firestore listeners (`addSnapshotListener`) para actualizaciones en tiempo real
  - [ ] El sistema debe permitir búsqueda por texto que filtre por nombre, descripción o dirección
  - [ ] El sistema debe permitir filtrar por categoría (Comida, Cultura, Deporte, etc.)
  - [ ] El sistema debe permitir filtrar por proximidad desde la ubicación actual del usuario (radio configurable: 1km, 5km, 10km)
  - [ ] El sistema debe calcular distancias usando la fórmula de Haversine o similar
  - [ ] El sistema debe permitir ordenar por: distancia, calificación (descendente), fecha de creación (descendente)
  - [ ] Los filtros deben poder combinarse (ej: buscar "restaurante" + categoría "Comida" + radio 5km)
  - [ ] El sistema debe mostrar indicador de carga mientras se obtienen los datos
  - [ ] Si no hay resultados, el sistema debe mostrar mensaje "No se encontraron POIs"

---

### HU-007: Visualizar Detalles de POI

* **Relacionado a:** CUS-02

* **Prioridad:** Alta

* **Descripción:**

  > "Como **usuario**, quiero **ver información completa de un punto de interés**, para **conocer todos los detalles antes de visitarlo**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe mostrar toda la información del POI: nombre, descripción, categoría, dirección, teléfono, email, sitio web
  - [ ] El sistema debe mostrar todas las imágenes del POI en un carrusel o galería
  - [ ] El sistema debe mostrar horarios por día de la semana (si están configurados)
  - [ ] El sistema debe mostrar características especiales seleccionadas (accesibilidad, WiFi, estacionamiento, etc.)
  - [ ] El sistema debe mostrar rango de precio si está configurado
  - [ ] El sistema debe mostrar calificación promedio y total de calificaciones
  - [ ] El sistema debe obtener y mostrar el clima actual en la ubicación del POI usando OpenWeatherMap API
  - [ ] Si la API de clima no está disponible, el sistema debe ocultar la sección de clima sin mostrar error
  - [ ] El sistema debe mostrar un mapa integrado con la ubicación del POI marcada
  - [ ] El sistema debe permitir abrir la ubicación en Google Maps para navegación
  - [ ] El sistema debe mostrar botón de favorito para agregar/quitar de favoritos

---

### HU-008: Calificar POI

* **Relacionado a:** CUS-02

* **Prioridad:** Media

* **Descripción:**

  > "Como **usuario**, quiero **calificar un punto de interés con estrellas**, para **ayudar a otros usuarios a conocer la calidad del lugar**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe mostrar sistema de calificación con estrellas (1 a 5 estrellas) en la pantalla de detalles
  - [ ] El sistema debe permitir al usuario seleccionar una calificación
  - [ ] El sistema debe actualizar la calificación promedio del POI en Firestore
  - [ ] El sistema debe incrementar el contador `totalCalificaciones` en Firestore
  - [ ] El sistema debe mostrar la calificación actualizada inmediatamente en la interfaz
  - [ ] El sistema debe validar que el usuario esté autenticado antes de permitir calificar
  - [ ] El sistema debe mostrar mensaje de confirmación "Calificación guardada"

---

### HU-009: Agregar POI a Favoritos

* **Relacionado a:** CUS-02, CUS-08

* **Prioridad:** Media

* **Descripción:**

  > "Como **usuario**, quiero **agregar un POI a mis favoritos**, para **acceder rápidamente a mis lugares preferidos**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe mostrar botón de favorito (estrella o corazón) en la pantalla de detalles del POI
  - [ ] El sistema debe guardar el POI en la base de datos local Room (tabla `FavoritePOI`) cuando se presiona el botón
  - [ ] El sistema debe actualizar la interfaz inmediatamente para indicar que está en favoritos
  - [ ] El sistema debe permitir quitar de favoritos presionando el botón nuevamente
  - [ ] El sistema debe validar que el POI esté aprobado antes de permitir agregar a favoritos
  - [ ] El POI agregado a favoritos debe quedar marcado para sincronización en la próxima sincronización

---

## Historias de Incidentes (CUS-03)

### HU-010: Reportar Incidente Urbano

* **Relacionado a:** CUS-03

* **Prioridad:** Alta

* **Descripción:**

  > "Como **ciudadano**, quiero **reportar un incidente urbano con ubicación, descripción y evidencia visual**, para **alertar a las autoridades y otros ciudadanos sobre problemas en la ciudad**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario esté autenticado
  - [ ] El sistema debe permitir seleccionar tipo de incidente: Inseguridad, Accidente de Tránsito, Incendio, Inundación, Vandalismo, Basura, Infraestructura, Otro
  - [ ] El sistema debe validar que la descripción no esté vacía
  - [ ] El sistema debe detectar automáticamente la ubicación GPS del dispositivo si los permisos están otorgados
  - [ ] El sistema debe permitir ingresar ubicación manualmente si el GPS no está disponible
  - [ ] El sistema debe realizar geocodificación para convertir coordenadas en dirección legible
  - [ ] El sistema debe permitir adjuntar una o múltiples fotos como evidencia
  - [ ] El sistema debe permitir adjuntar un video como evidencia (opcional)
  - [ ] Las imágenes y videos deben subirse a Firebase Storage antes de crear el documento
  - [ ] El sistema debe crear el documento del incidente en Firestore con estado "PENDIENTE"
  - [ ] El sistema debe asociar el incidente al usuario reportante mediante campo `usuarioId`
  - [ ] El sistema debe establecer `fechaHora` con `Timestamp.now()`
  - [ ] El sistema debe mostrar mensaje de confirmación "Incidente reportado exitosamente"

---

### HU-011: Visualizar Lista de Incidentes

* **Relacionado a:** CUS-03

* **Prioridad:** Alta

* **Descripción:**

  > "Como **usuario**, quiero **ver una lista de incidentes reportados**, para **estar informado sobre problemas en la ciudad**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe cargar incidentes desde Firestore según los permisos del usuario:
    - Ciudadanos: solo incidentes con estado "CONFIRMADO" o "RESUELTO"
    - Moderadores/Administradores: todos los incidentes
  - [ ] El sistema debe usar Firestore listeners para actualizaciones en tiempo real
  - [ ] El sistema debe permitir filtrar por tipo de incidente
  - [ ] El sistema debe permitir filtrar por estado (para moderadores/administradores)
  - [ ] El sistema debe permitir filtrar por fecha (hoy, esta semana, este mes)
  - [ ] El sistema debe mostrar información resumida: tipo, descripción, ubicación, fecha, estado
  - [ ] El sistema debe mostrar indicador visual del tipo de incidente (color o icono)
  - [ ] El sistema debe mostrar indicador de carga mientras se obtienen los datos
  - [ ] Si no hay incidentes, el sistema debe mostrar mensaje apropiado

---

### HU-012: Visualizar Mapa de Incidentes

* **Relacionado a:** CUS-03, CUS-09

* **Prioridad:** Media

* **Descripción:**

  > "Como **usuario**, quiero **ver los incidentes en un mapa interactivo**, para **visualizar la distribución geográfica de problemas en la ciudad**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe cargar todos los incidentes visibles según el rol del usuario
  - [ ] El sistema debe mostrar cada incidente como un marcador en Google Maps
  - [ ] Los marcadores deben tener colores diferentes según el tipo de incidente
  - [ ] El usuario debe poder tocar un marcador para ver información básica del incidente
  - [ ] El usuario debe poder filtrar los incidentes visibles en el mapa por tipo o estado
  - [ ] El sistema debe centrar el mapa en la ubicación del usuario si los permisos están otorgados
  - [ ] El sistema debe actualizar los marcadores en tiempo real cuando hay cambios en Firestore

---

### HU-013: Gestionar Estado de Incidente

* **Relacionado a:** CUS-03, CUS-05

* **Prioridad:** Alta

* **Descripción:**

  > "Como **moderador o administrador**, quiero **cambiar el estado de un incidente reportado**, para **gestionar el flujo de trabajo de resolución de problemas**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario tenga rol Moderador o Administrador
  - [ ] El sistema debe mostrar todos los incidentes pendientes o en revisión en la pantalla de gestión
  - [ ] El moderador debe poder cambiar el estado a: "EN_REVISION", "CONFIRMADO", "RECHAZADO" o "RESUELTO"
  - [ ] El sistema debe permitir agregar comentarios de moderación al cambiar el estado
  - [ ] El sistema debe actualizar el estado del incidente en Firestore
  - [ ] El sistema debe registrar información de moderación (moderadorId, fechaModeracion, comentariosModeracion)
  - [ ] El sistema debe mostrar el cambio de estado en tiempo real para todos los usuarios
  - [ ] El usuario reportante debe poder ver el estado actualizado de su reporte

---

## Historias de Eventos (CUS-04)

### HU-014: Crear Evento Urbano

* **Relacionado a:** CUS-04

* **Prioridad:** Alta

* **Descripción:**

  > "Como **ciudadano**, quiero **crear un evento urbano con información completa**, para **compartirlo con la comunidad después de aprobación**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario esté autenticado
  - [ ] El sistema debe validar campos obligatorios: nombre, descripción, categoría, dirección, fecha inicio, fecha fin, hora inicio, hora fin
  - [ ] El sistema debe validar que la fecha de fin sea posterior o igual a la fecha de inicio
  - [ ] Si son el mismo día, el sistema debe validar que la hora de fin sea posterior a la hora de inicio
  - [ ] El sistema debe realizar geocodificación inversa para obtener coordenadas GPS desde la dirección
  - [ ] El sistema debe permitir marcar el evento como recurrente (diario, semanal, mensual, anual)
  - [ ] Si es recurrente, el sistema debe validar que se seleccione frecuencia y fecha de fin de recurrencia
  - [ ] El sistema debe validar que si el evento no es gratuito, se ingrese un precio válido
  - [ ] El sistema debe validar que la capacidad sea un número válido mayor a 0 (si se especifica)
  - [ ] El sistema debe permitir subir múltiples imágenes del evento
  - [ ] Las imágenes deben subirse a Firebase Storage antes de crear el documento
  - [ ] El sistema debe crear el documento del evento en Firestore con estado "PENDIENTE"
  - [ ] El sistema debe asociar el evento al usuario creador mediante campo `usuarioId`
  - [ ] El sistema debe mostrar mensaje de confirmación "Evento creado exitosamente, pendiente de aprobación"

---

### HU-015: Buscar y Filtrar Eventos

* **Relacionado a:** CUS-04

* **Prioridad:** Alta

* **Descripción:**

  > "Como **usuario**, quiero **buscar y filtrar eventos por texto, categoría, fecha y ubicación**, para **encontrar eventos que me interesen**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe cargar automáticamente todos los eventos con estado "APROBADO" desde Firestore
  - [ ] El sistema debe usar Firestore listeners para actualizaciones en tiempo real
  - [ ] El sistema debe permitir búsqueda por texto (nombre, descripción)
  - [ ] El sistema debe permitir filtrar por categoría (Cultural, Deportivo, Educativo, etc.)
  - [ ] El sistema debe permitir filtrar por fecha: eventos futuros, pasados, en un rango específico
  - [ ] El sistema debe permitir filtrar por ubicación (proximidad)
  - [ ] El sistema debe permitir ordenar por: fecha (ascendente/descendente), nombre, categoría
  - [ ] Los filtros deben poder combinarse
  - [ ] El sistema debe mostrar indicador de carga mientras se obtienen los datos
  - [ ] Si no hay resultados, el sistema debe mostrar mensaje "No se encontraron eventos"

---

### HU-016: Visualizar Detalles de Evento

* **Relacionado a:** CUS-04

* **Prioridad:** Alta

* **Descripción:**

  > "Como **usuario**, quiero **ver información completa de un evento**, para **conocer todos los detalles antes de asistir**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe mostrar toda la información del evento: nombre, descripción, categoría, fechas, horarios, ubicación
  - [ ] El sistema debe mostrar todas las imágenes del evento
  - [ ] El sistema debe mostrar información del organizador y contacto (teléfono, email)
  - [ ] El sistema debe mostrar precio (gratuito o rango de precios)
  - [ ] El sistema debe mostrar capacidad máxima y número de inscripciones actuales (si aplica)
  - [ ] El sistema debe mostrar características especiales (accesibilidad, estacionamiento, transporte público)
  - [ ] El sistema debe mostrar enlaces a redes sociales si están configurados
  - [ ] El sistema debe mostrar un mapa integrado con la ubicación del evento
  - [ ] El sistema debe permitir abrir la ubicación en Google Maps para navegación
  - [ ] Si el evento requiere inscripción, el sistema debe mostrar botón "Inscribirse" si hay capacidad disponible

---

### HU-017: Inscribirse en Evento

* **Relacionado a:** CUS-04

* **Prioridad:** Media

* **Descripción:**

  > "Como **usuario**, quiero **inscribirme en un evento que requiere registro**, para **reservar mi lugar y recibir información**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario esté autenticado
  - [ ] El sistema debe validar que el evento requiera inscripción (`requiereInscripcion = true`)
  - [ ] El sistema debe validar que haya capacidad disponible (inscripciones < capacidad)
  - [ ] El sistema debe incrementar el contador `inscripciones` en Firestore
  - [ ] El sistema debe mostrar confirmación "Te has inscrito exitosamente en el evento"
  - [ ] El sistema debe actualizar la interfaz para reflejar el nuevo número de inscripciones
  - [ ] Si se alcanza la capacidad máxima, el sistema debe deshabilitar el botón de inscripción

---

## Historias de Moderación (CUS-05)

### HU-018: Revisar Contenido Pendiente de Moderación

* **Relacionado a:** CUS-05

* **Prioridad:** Alta

* **Descripción:**

  > "Como **moderador o administrador**, quiero **ver una lista de contenido pendiente de moderación**, para **revisar y aprobar o rechazar contenido creado por ciudadanos**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario tenga rol Moderador o Administrador
  - [ ] El sistema debe cargar automáticamente todos los elementos con estado "PENDIENTE" o "EN_REVISION" desde Firestore
  - [ ] El sistema debe mostrar lista separada por tipo: POIs, Eventos, Incidentes
  - [ ] El sistema debe ordenar los elementos por fecha de creación (más antiguos primero)
  - [ ] El sistema debe mostrar información resumida: nombre/título, categoría, fecha de creación, usuario creador
  - [ ] El sistema debe usar Firestore listeners para actualizaciones en tiempo real
  - [ ] El sistema debe mostrar indicador de cantidad de elementos pendientes
  - [ ] Si no hay elementos pendientes, el sistema debe mostrar mensaje "No hay contenido pendiente"

---

### HU-019: Aprobar Contenido

* **Relacionado a:** CUS-05

* **Prioridad:** Alta

* **Descripción:**

  > "Como **moderador o administrador**, quiero **aprobar contenido después de revisarlo**, para **que quede visible públicamente para todos los usuarios**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario tenga rol Moderador o Administrador
  - [ ] El moderador debe poder agregar comentarios opcionales de moderación
  - [ ] El sistema debe actualizar el estado del elemento a "APROBADO" en Firestore
  - [ ] El sistema debe registrar `moderadorId` con el UID del usuario que aprueba
  - [ ] El sistema debe registrar `fechaModeracion` con `Timestamp.now()`
  - [ ] El sistema debe registrar `comentariosModeracion` si fueron proporcionados
  - [ ] El sistema debe actualizar `fechaActualizacion` con `Timestamp.now()`
  - [ ] El elemento debe quedar visible públicamente para todos los usuarios inmediatamente
  - [ ] El sistema debe mostrar mensaje de confirmación "Contenido aprobado exitosamente"
  - [ ] El usuario creador debe poder ver que su contenido fue aprobado

---

### HU-020: Rechazar Contenido

* **Relacionado a:** CUS-05

* **Prioridad:** Alta

* **Descripción:**

  > "Como **moderador o administrador**, quiero **rechazar contenido que no cumple con las políticas**, para **mantener la calidad del contenido en la plataforma**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario tenga rol Moderador o Administrador
  - [ ] El sistema debe requerir que el moderador ingrese comentarios explicando el motivo del rechazo (obligatorio)
  - [ ] El sistema debe validar que los comentarios no estén vacíos antes de permitir rechazar
  - [ ] El sistema debe actualizar el estado del elemento a "RECHAZADO" en Firestore
  - [ ] El sistema debe registrar `moderadorId`, `fechaModeracion` y `comentariosModeracion`
  - [ ] El elemento no debe quedar visible públicamente
  - [ ] El sistema debe mostrar mensaje de confirmación "Contenido rechazado"
  - [ ] El usuario creador debe poder ver el estado "RECHAZADO" y los comentarios de rechazo en su historial

---

### HU-021: Suspender Contenido

* **Relacionado a:** CUS-05

* **Prioridad:** Media

* **Descripción:**

  > "Como **administrador**, quiero **suspender contenido previamente aprobado**, para **retirar contenido inapropiado que fue aprobado por error**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario tenga rol Administrador (no Moderador)
  - [ ] El sistema debe permitir suspender contenido con estado "APROBADO"
  - [ ] El sistema debe actualizar el estado a "SUSPENDIDO" en Firestore
  - [ ] El sistema debe registrar información de moderación (administradorId, fechaModeracion, comentariosModeracion)
  - [ ] El elemento debe dejar de ser visible públicamente inmediatamente
  - [ ] El sistema debe mostrar mensaje de confirmación "Contenido suspendido"

---

## Historias de Gestión de Usuarios (CUS-06)

### HU-022: Ver Lista de Usuarios

* **Relacionado a:** CUS-06

* **Prioridad:** Media

* **Descripción:**

  > "Como **administrador**, quiero **ver una lista de todos los usuarios del sistema**, para **gestionar usuarios y sus roles**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario tenga rol Administrador
  - [ ] Si el usuario no es administrador, el sistema debe mostrar mensaje "Acceso Denegado"
  - [ ] El sistema debe cargar todos los usuarios registrados desde Firestore
  - [ ] El sistema debe mostrar información básica: nombre, email, rol, estado (activo/inactivo)
  - [ ] El sistema debe permitir búsqueda por texto (nombre, email)
  - [ ] El sistema debe permitir filtrar por rol (Ciudadano, Moderador, Administrador)
  - [ ] El sistema debe permitir filtrar para mostrar solo usuarios activos
  - [ ] El sistema debe mostrar estadísticas agregadas: total de usuarios, distribución por rol
  - [ ] El sistema debe usar Firestore listeners para actualizaciones en tiempo real

---

### HU-023: Cambiar Rol de Usuario

* **Relacionado a:** CUS-06

* **Prioridad:** Alta

* **Descripción:**

  > "Como **administrador**, quiero **cambiar el rol de un usuario**, para **asignar permisos de moderador o administrador según sea necesario**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario tenga rol Administrador
  - [ ] El sistema debe mostrar un diálogo de edición con el rol actual del usuario
  - [ ] El administrador debe poder seleccionar nuevo rol: Ciudadano, Moderador, Administrador
  - [ ] El sistema debe validar que no se pueda cambiar el rol del propio administrador actual (opcional, para seguridad)
  - [ ] El sistema debe actualizar el campo "tipo" del usuario en Firestore
  - [ ] El sistema debe mostrar mensaje de confirmación "Rol actualizado exitosamente"
  - [ ] El usuario afectado verá cambios en sus permisos al iniciar sesión nuevamente
  - [ ] El sistema debe actualizar la lista de usuarios en tiempo real

---

### HU-024: Eliminar Usuario

* **Relacionado a:** CUS-06

* **Prioridad:** Media

* **Descripción:**

  > "Como **administrador**, quiero **eliminar un usuario del sistema**, para **remover usuarios que violan las políticas o ya no necesitan acceso**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario tenga rol Administrador
  - [ ] El sistema debe mostrar un diálogo de confirmación antes de eliminar
  - [ ] El sistema debe validar que no se pueda eliminar el propio administrador actual
  - [ ] El sistema debe eliminar el documento del usuario de Firestore
  - [ ] El sistema debe mostrar mensaje de confirmación "Usuario eliminado exitosamente"
  - [ ] El usuario eliminado no podrá iniciar sesión nuevamente
  - [ ] El sistema debe actualizar la lista de usuarios inmediatamente

---

### HU-025: Ver Estadísticas de Usuario

* **Relacionado a:** CUS-06

* **Prioridad:** Baja

* **Descripción:**

  > "Como **administrador**, quiero **ver estadísticas de actividad de un usuario**, para **evaluar su participación en la plataforma**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe mostrar cantidad de POIs creados por el usuario
  - [ ] El sistema debe mostrar cantidad de eventos creados por el usuario
  - [ ] El sistema debe mostrar cantidad de incidentes reportados por el usuario
  - [ ] El sistema debe mostrar fecha de registro del usuario
  - [ ] Las estadísticas deben calcularse desde Firestore en tiempo real

---

## Historias de Dashboards (CUS-07)

### HU-026: Visualizar Dashboard de Analíticas

* **Relacionado a:** CUS-07

* **Prioridad:** Media

* **Descripción:**

  > "Como **moderador o administrador**, quiero **ver dashboards con estadísticas del sistema**, para **tomar decisiones basadas en datos**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario tenga rol Moderador o Administrador
  - [ ] El sistema debe cargar todos los datos necesarios desde Firestore (incidentes, eventos, POIs)
  - [ ] El sistema debe mostrar gráfico de barras con distribución de incidentes por tipo
  - [ ] El sistema debe mostrar gráfico de pastel con porcentaje de cada tipo de incidente
  - [ ] El sistema debe mostrar distribución mensual de incidentes, eventos y POIs
  - [ ] El sistema debe mostrar distribución por estado (Pendiente, Aprobado, Rechazado, etc.)
  - [ ] El sistema debe usar Firestore listeners para actualizar los gráficos en tiempo real
  - [ ] El sistema debe mostrar indicador de carga mientras se obtienen los datos
  - [ ] Los gráficos deben ser interactivos (usando librería `tehras-charts` o similar)

---

### HU-027: Filtrar Datos del Dashboard por Período

* **Relacionado a:** CUS-07

* **Prioridad:** Baja

* **Descripción:**

  > "Como **moderador o administrador**, quiero **filtrar los datos del dashboard por rango de fechas**, para **analizar tendencias en períodos específicos**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe permitir seleccionar un rango de fechas (fecha inicio y fecha fin)
  - [ ] El sistema debe validar que la fecha fin sea posterior o igual a la fecha inicio
  - [ ] El sistema debe recalcular todos los gráficos según el filtro temporal aplicado
  - [ ] El sistema debe actualizar: gráficos de incidentes, eventos y POIs
  - [ ] El sistema debe mostrar el período seleccionado en la interfaz
  - [ ] El sistema debe permitir limpiar el filtro para ver todos los datos

---

## Historias de Sincronización (CUS-08)

### HU-028: Agregar POI a Favoritos (Local)

* **Relacionado a:** CUS-08

* **Prioridad:** Media

* **Descripción:**

  > "Como **usuario**, quiero **agregar un POI a mis favoritos localmente**, para **acceder rápidamente sin conexión a internet**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe guardar el POI en la base de datos local Room (tabla `FavoritePOI`)
  - [ ] El sistema debe validar que el POI esté aprobado antes de permitir agregar a favoritos
  - [ ] El sistema debe actualizar la interfaz inmediatamente para indicar que está en favoritos
  - [ ] El sistema debe permitir quitar de favoritos presionando el botón nuevamente
  - [ ] El POI debe quedar disponible incluso sin conexión a internet
  - [ ] El POI debe quedar marcado para sincronización en la próxima sincronización

---

### HU-029: Ver Lista de Favoritos

* **Relacionado a:** CUS-08

* **Prioridad:** Media

* **Descripción:**

  > "Como **usuario**, quiero **ver todos mis POIs favoritos**, para **acceder rápidamente a mis lugares preferidos**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe cargar todos los POIs favoritos desde la base de datos local Room
  - [ ] El sistema debe mostrar la lista de favoritos con información básica (nombre, categoría, dirección)
  - [ ] El usuario debe poder tocar un favorito para ver sus detalles completos
  - [ ] El usuario debe poder eliminar favoritos desde esta pantalla
  - [ ] Si no hay favoritos, el sistema debe mostrar mensaje "No tienes favoritos aún"
  - [ ] La lista debe funcionar sin conexión a internet (datos locales)

---

### HU-030: Sincronización Manual de Favoritos

* **Relacionado a:** CUS-08

* **Prioridad:** Alta

* **Descripción:**

  > "Como **usuario**, quiero **sincronizar manualmente mis favoritos con el servidor**, para **mantener mis datos actualizados en todos mis dispositivos**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe validar que el usuario esté autenticado
  - [ ] El sistema debe obtener el token JWT del usuario autenticado usando `FirebaseAuth.currentUser.getIdToken()`
  - [ ] El sistema debe agregar el token al header `Authorization: Bearer <token>` en las peticiones HTTP
  - [ ] **Paso PULL:** El sistema debe realizar petición GET a `/api/v1/sync/pull` del backend Spring Boot
  - [ ] El backend debe retornar los favoritos del usuario desde PostgreSQL
  - [ ] El sistema debe actualizar la base de datos local Room con los favoritos recibidos del servidor
  - [ ] **Paso PUSH:** El sistema debe obtener todos los favoritos locales desde Room
  - [ ] El sistema debe realizar petición POST a `/api/v1/sync/push` con los favoritos locales
  - [ ] El backend debe guardar los favoritos en PostgreSQL
  - [ ] El sistema debe guardar el timestamp de última sincronización en DataStore
  - [ ] El sistema debe mostrar mensaje de éxito o error según el resultado
  - [ ] Si hay error de autenticación (401/403), el sistema debe mostrar mensaje apropiado

---

### HU-031: Configurar Sincronización Automática

* **Relacionado a:** CUS-08

* **Prioridad:** Media

* **Descripción:**

  > "Como **usuario**, quiero **configurar sincronización automática de favoritos**, para **mantener mis datos sincronizados sin intervención manual**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe permitir activar/desactivar sincronización automática
  - [ ] El sistema debe permitir configurar frecuencia: 15 min, 30 min, 1 hora, 2 horas, 4 horas
  - [ ] El sistema debe permitir configurar restricción de red: solo WiFi o cualquier conexión
  - [ ] El sistema debe guardar la configuración en DataStore
  - [ ] El sistema debe programar un trabajo periódico con WorkManager cuando se activa
  - [ ] WorkManager debe ejecutar `SyncWorker` según la frecuencia configurada
  - [ ] El sistema debe respetar la restricción de red configurada (solo WiFi si está activada)
  - [ ] El sistema debe mostrar el estado de la sincronización automática (activa/inactiva)
  - [ ] El sistema debe mostrar la última fecha y hora de sincronización

---

### HU-032: Sincronización Automática con WorkManager

* **Relacionado a:** CUS-08

* **Prioridad:** Media

* **Descripción:**

  > "Como **sistema**, quiero **ejecutar sincronización automática en segundo plano**, para **mantener los datos del usuario actualizados periódicamente**."

* **Criterios de Aceptación:**

  - [ ] `SyncWorker` debe ejecutarse según la frecuencia configurada en WorkManager
  - [ ] El worker debe verificar que haya conexión a internet antes de sincronizar
  - [ ] El worker debe respetar la restricción de red (solo WiFi si está configurada)
  - [ ] El worker debe realizar el proceso completo de sincronización (PULL y PUSH)
  - [ ] El worker debe actualizar el timestamp de última sincronización en DataStore
  - [ ] El worker debe manejar errores de red y reintentar según la política de WorkManager
  - [ ] El worker debe notificar al usuario si hay errores persistentes (opcional)
  - [ ] El worker debe cancelarse si el usuario desactiva la sincronización automática

---

## Historias de Mapas (CUS-09)

### HU-033: Visualizar POIs en Mapa

* **Relacionado a:** CUS-09

* **Prioridad:** Alta

* **Descripción:**

  > "Como **usuario**, quiero **ver todos los POIs aprobados en un mapa interactivo**, para **visualizar su distribución geográfica**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe solicitar permisos de ubicación si no están otorgados
  - [ ] El sistema debe detectar automáticamente la ubicación actual del usuario (si los permisos están otorgados)
  - [ ] El sistema debe centrar el mapa en la ubicación del usuario o en una ubicación predeterminada
  - [ ] El sistema debe cargar todos los POIs aprobados desde Firestore
  - [ ] El sistema debe mostrar cada POI como un marcador en Google Maps
  - [ ] Los marcadores deben tener iconos diferentes según la categoría del POI
  - [ ] El usuario debe poder tocar un marcador para ver información básica del POI
  - [ ] El usuario debe poder tocar "Ver detalles" para navegar a la pantalla de detalles completa
  - [ ] El sistema debe actualizar los marcadores en tiempo real cuando hay cambios en Firestore
  - [ ] El sistema debe manejar errores de Google Maps API apropiadamente

---

### HU-034: Navegar a Ubicación desde Detalles

* **Relacionado a:** CUS-09

* **Prioridad:** Media

* **Descripción:**

  > "Como **usuario**, quiero **abrir la ubicación de un POI o evento en Google Maps**, para **obtener direcciones y navegar hasta el lugar**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe mostrar botón "Abrir en Maps" o "Navegar" en la pantalla de detalles
  - [ ] El sistema debe construir una URI de Google Maps con las coordenadas del lugar
  - [ ] El sistema debe abrir Google Maps en el dispositivo con la ruta hacia el destino
  - [ ] El usuario debe poder iniciar navegación desde Google Maps
  - [ ] Si Google Maps no está instalado, el sistema debe abrir en el navegador web

---

### HU-035: Seleccionar Ubicación en Mapa

* **Relacionado a:** CUS-09

* **Prioridad:** Media

* **Descripción:**

  > "Como **usuario**, quiero **seleccionar una ubicación tocando en el mapa**, para **especificar la ubicación de un POI, evento o incidente que estoy creando**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe mostrar un mapa interactivo en la pantalla de selección de ubicación
  - [ ] El usuario debe poder tocar en el mapa para seleccionar una ubicación
  - [ ] El sistema debe obtener las coordenadas (lat, lon) del punto seleccionado
  - [ ] El sistema debe realizar geocodificación inversa para obtener la dirección desde las coordenadas
  - [ ] El sistema debe actualizar los campos de ubicación en el formulario (coordenadas y dirección)
  - [ ] El sistema debe mostrar un marcador en el punto seleccionado
  - [ ] El usuario debe poder mover el marcador para ajustar la ubicación
  - [ ] El sistema debe validar que se haya seleccionado una ubicación antes de permitir continuar

---

### HU-036: Filtrar por Proximidad en Mapa

* **Relacionado a:** CUS-09

* **Prioridad:** Baja

* **Descripción:**

  > "Como **usuario**, quiero **filtrar POIs o incidentes por proximidad desde el mapa**, para **ver solo elementos cercanos a mi ubicación**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe permitir seleccionar un radio de búsqueda (1 km, 5 km, 10 km)
  - [ ] El sistema debe calcular la distancia desde la ubicación actual del usuario usando fórmula de Haversine
  - [ ] El sistema debe mostrar solo los elementos dentro del radio seleccionado
  - [ ] El sistema debe actualizar los marcadores en el mapa según el filtro
  - [ ] El sistema debe mostrar el radio seleccionado visualmente en el mapa (círculo)

---

## Historias de Perfil (CUS-10)

### HU-037: Ver Perfil de Usuario

* **Relacionado a:** CUS-10

* **Prioridad:** Media

* **Descripción:**

  > "Como **usuario**, quiero **ver mi información de perfil**, para **revisar mis datos personales y configuración**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe cargar la información del usuario actual desde Firestore
  - [ ] El sistema debe mostrar: nombre, email, teléfono, foto de perfil, rol
  - [ ] El sistema debe mostrar estado de preferencias de notificaciones
  - [ ] El sistema debe mostrar fecha de última sincronización (si aplica)
  - [ ] El sistema debe mostrar botones para editar perfil, cambiar foto, configurar sincronización y cerrar sesión

---

### HU-038: Editar Información de Perfil

* **Relacionado a:** CUS-10

* **Prioridad:** Media

* **Descripción:**

  > "Como **usuario**, quiero **editar mi nombre y teléfono**, para **mantener mi información actualizada**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe mostrar formulario con campos editables: nombre, teléfono
  - [ ] El sistema debe validar que el nombre no esté vacío
  - [ ] El sistema debe validar formato del teléfono (opcional, si hay validación específica)
  - [ ] El sistema debe actualizar el documento del usuario en Firestore
  - [ ] El sistema debe mostrar mensaje de confirmación "Perfil actualizado exitosamente"
  - [ ] El sistema debe actualizar la interfaz inmediatamente con los nuevos datos
  - [ ] El email no debe ser editable (se gestiona desde Firebase Authentication)

---

### HU-039: Cambiar Foto de Perfil

* **Relacionado a:** CUS-10

* **Prioridad:** Baja

* **Descripción:**

  > "Como **usuario**, quiero **cambiar mi foto de perfil**, para **personalizar mi cuenta**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe mostrar opciones: seleccionar de galería o tomar foto con cámara
  - [ ] El sistema debe solicitar permisos de almacenamiento o cámara según la opción
  - [ ] El sistema debe validar que la imagen seleccionada no exceda un tamaño máximo (ej: 5MB)
  - [ ] El sistema debe subir la imagen a Firebase Storage
  - [ ] El sistema debe actualizar el campo `photoUrl` del usuario en Firestore
  - [ ] El sistema debe mostrar la nueva foto de perfil inmediatamente
  - [ ] El sistema debe mostrar mensaje de confirmación "Foto de perfil actualizada"

---

### HU-040: Gestionar Preferencias de Notificaciones

* **Relacionado a:** CUS-10

* **Prioridad:** Baja

* **Descripción:**

  > "Como **usuario**, quiero **activar o desactivar las notificaciones**, para **controlar qué tipo de alertas recibo**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe mostrar un switch o toggle para activar/desactivar notificaciones
  - [ ] El sistema debe actualizar el campo "notificaciones" del usuario en Firestore
  - [ ] El sistema debe guardar la preferencia inmediatamente sin necesidad de confirmación adicional
  - [ ] El sistema debe reflejar el cambio en la interfaz inmediatamente

---

### HU-041: Cerrar Sesión

* **Relacionado a:** CUS-10

* **Prioridad:** Alta

* **Descripción:**

  > "Como **usuario**, quiero **cerrar sesión en la aplicación**, para **proteger mi cuenta cuando uso un dispositivo compartido**."

* **Criterios de Aceptación:**

  - [ ] El sistema debe mostrar un diálogo de confirmación antes de cerrar sesión
  - [ ] El sistema debe cerrar la sesión en Firebase Authentication usando `FirebaseAuth.signOut()`
  - [ ] El sistema debe limpiar las credenciales almacenadas localmente (si existen en CredentialsStorage)
  - [ ] El sistema debe navegar a la pantalla de inicio de sesión
  - [ ] El sistema debe limpiar el estado de la aplicación relacionado con el usuario
  - [ ] El usuario no debe poder acceder a funcionalidades que requieren autenticación después de cerrar sesión

---

## 📊 Resumen del Backlog

### Estadísticas

- **Total de Historias de Usuario:** 41
- **Prioridad Alta:** 18 historias
- **Prioridad Media:** 17 historias
- **Prioridad Baja:** 6 historias

### Distribución por Rol

- **Ciudadano:** 20 historias
- **Moderador:** 8 historias
- **Administrador:** 6 historias
- **Sistema/General:** 7 historias

### Distribución por CUS

- **CUS-01 (Autenticación):** 3 historias
- **CUS-02 (POIs):** 6 historias
- **CUS-03 (Incidentes):** 4 historias
- **CUS-04 (Eventos):** 4 historias
- **CUS-05 (Moderación):** 4 historias
- **CUS-06 (Gestión Usuarios):** 4 historias
- **CUS-07 (Dashboards):** 2 historias
- **CUS-08 (Sincronización):** 5 historias
- **CUS-09 (Mapas):** 4 historias
- **CUS-10 (Perfil):** 5 historias

---

## 🎯 Recomendaciones de Sprint Planning

### Sprint 1 (MVP - Funcionalidad Core)
- HU-001, HU-002 (Autenticación)
- HU-004, HU-006, HU-007 (POIs básicos)
- HU-010, HU-011 (Incidentes básicos)
- HU-041 (Cerrar sesión)

### Sprint 2 (Contenido y Moderación)
- HU-005 (IA para descripciones)
- HU-008, HU-009 (Calificaciones y favoritos)
- HU-014, HU-015, HU-016 (Eventos)
- HU-018, HU-019, HU-020 (Moderación)

### Sprint 3 (Gestión y Sincronización)
- HU-013 (Gestión de incidentes)
- HU-022, HU-023 (Gestión de usuarios)
- HU-030, HU-031 (Sincronización)

### Sprint 4 (Mejoras y Analíticas)
- HU-026 (Dashboards)
- HU-033, HU-034 (Mapas mejorados)
- HU-037, HU-038 (Perfil completo)

---

**Documento generado mediante análisis exhaustivo de los Casos de Uso del Sistema y la lógica de negocio del proyecto Points App.**

