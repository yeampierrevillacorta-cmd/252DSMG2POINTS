# 🚀 Indicaciones para Implementar Backend Spring Boot + PostgreSQL + Google Cloud Run

Este documento contiene las indicaciones paso a paso para implementar el backend corporativo MySyncApp usando Spring Boot, PostgreSQL y desplegarlo en Google Cloud Run.

---

## 📋 Tabla de Contenidos

1. [Requisitos Previos](#requisitos-previos)
2. [Configuración del Proyecto Spring Boot](#configuración-del-proyecto-spring-boot)
3. [Configuración de PostgreSQL](#configuración-de-postgresql)
4. [Implementación de Capas del Backend](#implementación-de-capas-del-backend)
5. [Configuración de Seguridad](#configuración-de-seguridad)
6. [Despliegue en Google Cloud Run](#despliegue-en-google-cloud-run)
7. [Integración con Android (Retrofit)](#integración-con-android-retrofit)

---

## 1. Requisitos Previos

### Herramientas Necesarias

- **Java 17 o superior** (JDK)
- **Maven 3.8+** o **Gradle 7.5+**
- **PostgreSQL 15+** (local para desarrollo)
- **Google Cloud SDK** (`gcloud` CLI)
- **Docker** (para contenedores)
- **IDE** (IntelliJ IDEA, VS Code, etc.)

### Cuentas y Servicios

- Cuenta de **Google Cloud Platform** (GCP)
- Proyecto creado en GCP
- **Cloud SQL** habilitado (para PostgreSQL)
- **Cloud Run** habilitado
- **Container Registry** o **Artifact Registry** habilitado

---

## 2. Configuración del Proyecto Spring Boot

### 2.1 Crear Proyecto Base

**Opción A: Spring Initializr (Recomendado)**

1. Ir a https://start.spring.io/
2. Configurar:g.io/
2. Configurar:
   - **Project**: Maven o Gradle
   - **Language**: Java o Kotlin
   - **Spring Boot**: 3.2.x o superior
   - **Packaging**: Jar
   - **Java**: 17 o superior
3. Dependencias a seleccionar:
   - Spring Web
   - Spring Data JPA
   - PostgreSQL Driver
   - Spring Security
   - Validation
   - Lombok (opcional)
4. Generar y descargar el proyecto
5. Extraer y abrir en tu IDE

**Opción B: Manual**

```bash
mkdir mysyncapp-backend
cd mysyncapp-backend
# Crear estructura de carpetas manualmente
```

### 2.2 Estructura de Carpetas Recomendada

```
mysyncapp-backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/mysyncapp/
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── service/              # Lógica de negocio
│   │   │   ├── repository/          # JPA Repositories
│   │   │   ├── entity/              # Entidades JPA
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── config/              # Configuraciones
│   │   │   ├── security/            # Configuración de seguridad
│   │   │   └── MySyncAppApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Configuración principal
│   │       ├── application-dev.yml  # Configuración desarrollo
│   │       ├── application-prod.yml # Configuración producción
│   │       └── db/migration/        # Flyway migrations (opcional)
│   └── test/
└── pom.xml o build.gradle
```

---

## 3. Configuración de PostgreSQL

### 3.1 PostgreSQL Local (Desarrollo)

1. **Instalar PostgreSQL** en tu máquina local
2. **Crear base de datos**:
   ```sql
   CREATE DATABASE mysyncapp_db;
   CREATE USER mysyncapp_user WITH PASSWORD 'tu_password';
   GRANT ALL PRIVILEGES ON DATABASE mysyncapp_db TO mysyncapp_user;
   ```

3. **Configurar en `application-dev.yml`**:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/mysyncapp_db
       username: mysyncapp_user
       password: tu_password
       driver-class-name: org.postgresql.Driver
     jpa:
       hibernate:
         ddl-auto: update
       show-sql: true
       properties:
         hibernate:
           dialect: org.hibernate.dialect.PostgreSQLDialect
           format_sql: true
   ```

### 3.2 PostgreSQL en Google Cloud SQL

1. **Crear instancia Cloud SQL**:
   ```bash
   gcloud sql instances create mysyncapp-postgres \
     --database-version=POSTGRES_15 \
     --tier=db-f1-micro \
     --region=us-central1 \
     --root-password=tu_password_root
   ```

2. **Crear base de datos**:
   ```bash
   gcloud sql databases create mysyncapp_db \
     --instance=mysyncapp-postgres
   ```

3. **Crear usuario**:
   ```bash
   gcloud sql users create mysyncapp_user \
     --instance=mysyncapp-postgres \
     --password=tu_password
   ```

4. **Obtener IP de conexión**:
   ```bash
   gcloud sql instances describe mysyncapp-postgres
   ```

5. **Configurar en `application-prod.yml`**:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://IP_PUBLICA:5432/mysyncapp_db
       username: mysyncapp_user
       password: ${DB_PASSWORD}
       driver-class-name: org.postgresql.Driver
     jpa:
       hibernate:
         ddl-auto: validate
       show-sql: false
   ```

---

## 4. Implementación de Capas del Backend

### 4.1 Dependencias en `pom.xml` o `build.gradle`

**Maven (`pom.xml`)**:
```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

**Gradle (`build.gradle.kts`)**:
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
```

### 4.2 Crear Entidades JPA

**Ejemplo: `entity/FavoritePOI.java`**:
```java
@Entity
@Table(name = "poi_favorites")
public class FavoritePOI {
    @Id
    private String poiId;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String nombre;
    
    private String descripcion;
    private String categoria;
    private String direccion;
    private Double lat;
    private Double lon;
    private Double calificacion;
    private String imagenUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    // Getters, setters, constructores
}
```

### 4.3 Crear Repositorios JPA

**Ejemplo: `repository/FavoriteRepository.java`**:
```java
@Repository
public interface FavoriteRepository extends JpaRepository<FavoritePOI, String> {
    List<FavoritePOI> findByUserIdAndDeletedFalse(String userId);
    Optional<FavoritePOI> findByPoiIdAndUserId(String poiId, String userId);
    long countByUserIdAndDeletedFalse(String userId);
}
```

### 4.4 Crear DTOs

**Ejemplo: `dto/SyncRequest.java`**:
```java
public class SyncRequest {
    private String deviceId;
    private String userId;
    private LocalDateTime lastSyncAt;
    private List<FavoritePOIDto> favorites;
    private List<CachedPOIDto> cached;
    private List<SearchHistoryDto> searchHistory;
    
    // Getters, setters
}
```

### 4.5 Crear Services

**Ejemplo: `service/SyncService.java`**:
```java
@Service
public class SyncService {
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    public SyncResponse pullChanges(String userId, LocalDateTime lastSyncAt) {
        // Lógica para obtener cambios desde lastSyncAt
        List<FavoritePOI> changes = favoriteRepository
            .findByUserIdAndUpdatedAtAfter(userId, lastSyncAt);
        
        return SyncResponse.builder()
            .serverTimestamp(LocalDateTime.now())
            .favorites(changes)
            .build();
    }
    
    public void pushChanges(SyncRequest request) {
        // Lógica para guardar cambios del cliente
        for (FavoritePOIDto dto : request.getFavorites()) {
            FavoritePOI entity = mapDtoToEntity(dto);
            favoriteRepository.save(entity);
        }
    }
}
```

### 4.6 Crear Controllers REST

**Ejemplo: `controller/SyncController.java`**:
```java
@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {
    @Autowired
    private SyncService syncService;
    
    @PostMapping("/push")
    public ResponseEntity<?> pushChanges(@RequestBody SyncRequest request) {
        syncService.pushChanges(request);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/pull")
    public ResponseEntity<SyncResponse> pullChanges(
        @RequestParam String userId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastSyncAt
    ) {
        SyncResponse response = syncService.pullChanges(userId, lastSyncAt);
        return ResponseEntity.ok(response);
    }
}
```

---

## 5. Configuración de Seguridad

### 5.1 Configurar Spring Security

**⚠️ IMPORTANTE: Para permitir acceso sin autenticación (desarrollo/testing):**

**Archivo: `config/SecurityConfig.java`**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Permitir acceso sin autenticación a los endpoints de sincronización
                .requestMatchers("/api/v1/sync/**").permitAll()
                .anyRequest().permitAll() // O authenticated() para otros endpoints
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
}
```

**Nota:** Si cambias `.authenticated()` por `.permitAll()`, los endpoints estarán accesibles sin autenticación. Esto es útil para desarrollo pero **NO recomendado para producción**.

**Para producción con autenticación JWT:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/sync/**").authenticated()
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        
        return http.build();
    }
}
```

### 5.2 Implementar JWT (Opcional pero Recomendado)

1. **Crear `security/JwtTokenProvider.java`**
2. **Crear `security/JwtAuthenticationFilter.java`**
3. **Configurar secret key en `application.yml`**

---

## 6. Despliegue en Google Cloud Run

### 6.1 Crear Dockerfile

**Archivo: `Dockerfile`**:
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/mysyncapp-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 6.2 Construir Imagen Docker

```bash
# Construir imagen localmente
docker build -t gcr.io/TU_PROJECT_ID/mysyncapp-backend:latest .

# O usar Cloud Build
gcloud builds submit --tag gcr.io/TU_PROJECT_ID/mysyncapp-backend:latest
```

### 6.3 Desplegar en Cloud Run

```bash
gcloud run deploy mysyncapp-backend \
  --image gcr.io/TU_PROJECT_ID/mysyncapp-backend:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod,DB_PASSWORD=tu_password" \
  --add-cloudsql-instances TU_PROJECT_ID:us-central1:mysyncapp-postgres \
  --set-cloudsql-instances mysyncapp-postgres
```

**✅ Despliegue Realizado:**

- **Proyecto GCP**: `conexionpostgres`
- **Región**: `us-central1`
- **Instancia Cloud SQL**: `conexionpostgres:us-central1:mysyncapp-postgres`
- **Servicio Cloud Run**: `mysyncapp-backend`
- **URL del Servicio**: `https://mysyncapp-backend-860998153214.us-central1.run.app`

### 6.4 Configurar Variables de Entorno

En Cloud Run Console o vía CLI:
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_PASSWORD=tu_password`
- `JWT_SECRET=tu_secret_key`
- `CLOUD_SQL_CONNECTION_NAME=TU_PROJECT_ID:us-central1:mysyncapp-postgres`

**✅ Configuración Real:**

- **CLOUD_SQL_CONNECTION_NAME**: `conexionpostgres:us-central1:mysyncapp-postgres`
- **URL del Servicio**: `https://mysyncapp-backend-860998153214.us-central1.run.app`

---

## 7. Integración con Android (Retrofit)

### 7.1 Agregar Dependencias en Android

**En `app/build.gradle.kts`**:
```kotlin
dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
```

### 7.2 Crear Interfaz de API

**Archivo: `network/SyncApiService.kt`**:
```kotlin
interface SyncApiService {
    @POST("api/v1/sync/push")
    suspend fun pushChanges(@Body request: SyncRequest): Response<Unit>
    
    @GET("api/v1/sync/pull")
    suspend fun pullChanges(
        @Query("userId") userId: String,
        @Query("lastSyncAt") lastSyncAt: String
    ): Response<SyncResponse>
}
```

### 7.3 Configurar Retrofit

**Archivo: `network/RetrofitClient.kt`** (Ejemplo genérico):
```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://TU_CLOUD_RUN_URL/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${getToken()}")
                .build()
            chain.proceed(request)
        }
        .build()
    
    val apiService: SyncApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SyncApiService::class.java)
}
```

**✅ Implementación Real en el Proyecto:**

La configuración de Retrofit para el backend se encuentra en `DefaultAppContainer.kt`:

```kotlin
// Retrofit para Backend Spring Boot
private val backendRetrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(BACKEND_BASE_URL) // URL desde EnvironmentConfig
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}

private val syncApiService: SyncApiService by lazy {
    backendRetrofit.create(SyncApiService::class.java)
}
```

**URL de Producción Configurada:**
- `BACKEND_BASE_URL`: `https://mysyncapp-backend-860998153214.us-central1.run.app/`

**Nota:** La URL se puede configurar en el archivo `.env` del proyecto Android con la variable `BACKEND_BASE_URL`, o se usará la URL por defecto si no está configurada.

### 7.4 Usar en Repository Android

**✅ Implementación Real:**

Se ha creado un `SyncRepository` completo que maneja toda la lógica de sincronización:

**Archivo: `repository/SyncRepository.kt`** (Interfaz):
```kotlin
interface SyncRepository {
    suspend fun pushChanges(userId: String): Result<Unit>
    suspend fun pullChanges(userId: String): Result<SyncResult>
    suspend fun sync(userId: String): Result<SyncResult>
    suspend fun getLastSyncTimestamp(): String?
    suspend fun saveLastSyncTimestamp(timestamp: String)
}
```

**Archivo: `repository/DefaultSyncRepository.kt`** (Implementación):
- Maneja la conversión entre modelos locales y DTOs del servidor
- Gestiona la sincronización bidireccional
- Guarda el timestamp de la última sincronización
- Maneja errores de red y HTTP

**Ejemplo de uso:**

```kotlin
// Obtener el repositorio desde AppContainer
val syncRepository = appContainer.syncRepository

if (syncRepository != null) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        // Sincronización completa (pull + push)
        viewModelScope.launch {
            val result = syncRepository.sync(currentUser.uid)
            result.onSuccess { syncResult ->
                Log.d("Sync", "Sincronización exitosa: ${syncResult.message}")
            }.onFailure { error ->
                Log.e("Sync", "Error: ${error.message}")
            }
        }
    }
}
```

**Ver más ejemplos en:** `repository/SyncRepositoryExample.kt`

### 7.5 Sincronización Automática con WorkManager

**✅ Implementación Real:**

Se ha implementado sincronización automática en background usando WorkManager:

**Archivo: `worker/SyncWorker.kt`**:
- Worker que ejecuta la sincronización en background
- Se ejecuta periódicamente según la configuración
- Maneja errores de red y reintentos automáticos

**Archivo: `worker/SyncWorkManager.kt`**:
- Manager para configurar y gestionar la sincronización automática
- Permite configurar intervalo, restricciones (WiFi, batería, etc.)
- Métodos para iniciar, detener y reiniciar sincronización

**Configuración en `PreferencesManager.kt`**:
- `autoSyncEnabled`: Habilitar/deshabilitar sincronización automática
- `autoSyncIntervalHours`: Intervalo entre sincronizaciones (1-24 horas)
- `syncOnlyWifi`: Sincronizar solo cuando hay WiFi

**Inicialización automática:**
La sincronización se inicia automáticamente en `PointsApplication.onCreate()` si está habilitada.

**Ejemplo de uso:**

```kotlin
// Desde cualquier Activity o ViewModel
val app = application as PointsApplication

// Sincronización inmediata
app.syncNow()

// Reiniciar con nueva configuración
app.restartAutoSync()

// O usar directamente el SyncWorkManager
val syncWorkManager = SyncWorkManager(context, preferencesManager)
syncWorkManager.startPeriodicSync(
    intervalHours = 6,
    onlyWifi = false
)
```

**Ver más ejemplos en:** `worker/SyncWorkManagerExample.kt`

---

## ✅ Checklist de Implementación

### Fase 1: Desarrollo Local
- [ ] Proyecto Spring Boot creado
- [ ] PostgreSQL local configurado
- [ ] Entidades JPA creadas
- [ ] Repositorios implementados
- [ ] Services con lógica de negocio
- [ ] Controllers REST expuestos
- [ ] Seguridad básica configurada
- [ ] Pruebas unitarias escritas

### Fase 2: Cloud Setup
- [ ] Cloud SQL instancia creada
- [ ] Base de datos y usuario creados
- [ ] Dockerfile creado
- [ ] Imagen Docker construida
- [ ] Cloud Run servicio desplegado
- [ ] Variables de entorno configuradas
- [ ] Conexión Cloud SQL funcionando

### Fase 3: Integración Android
- [ ] Retrofit configurado en Android
- [ ] Interfaz API definida
- [ ] DTOs mapeados
- [ ] Repository remoto implementado
- [ ] WorkManager para sincronización
- [ ] Manejo de errores y reintentos

---

## 📚 Recursos Adicionales

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Google Cloud Run](https://cloud.google.com/run/docs)
- [Cloud SQL for PostgreSQL](https://cloud.google.com/sql/docs/postgres)
- [Retrofit Documentation](https://square.github.io/retrofit/)

---

## ⚠️ Notas Importantes

1. **Seguridad**: Nunca commitees contraseñas o secrets en el código. Usa variables de entorno o Secret Manager de GCP.

2. **Migraciones**: Considera usar Flyway o Liquibase para gestionar cambios de esquema de base de datos.

3. **Logging**: Configura logging apropiado para producción (Cloud Logging en GCP).

4. **Monitoreo**: Implementa health checks (`/actuator/health`) y métricas.

5. **CORS**: Si tu frontend está en otro dominio, configura CORS en Spring Security.

6. **Rate Limiting**: Considera implementar rate limiting para proteger tu API.

---

**Última actualización**: Diciembre 2024

