# 🔒 Solución para Error HTTP 403 (Forbidden)

## Problema

El backend Spring Boot está devolviendo error **403 Forbidden** en las peticiones de sincronización. Esto generalmente ocurre porque:

1. **Spring Security está bloqueando las peticiones** sin autenticación
2. El backend requiere autenticación JWT pero no se está enviando el token
3. La configuración de seguridad del backend está restringiendo el acceso

## ✅ Soluciones

### Opción 1: Permitir Acceso Sin Autenticación (Para Desarrollo)

**Modificar `SecurityConfig.java` en el backend Spring Boot:**

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
                .anyRequest().permitAll() // O cambiar a authenticated() para otros endpoints
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
}
```

**Cambios clave:**
- Cambiar `.requestMatchers("/api/v1/sync/**").authenticated()` 
- Por `.requestMatchers("/api/v1/sync/**").permitAll()`

### Opción 2: Autenticación JWT (✅ IMPLEMENTADA)

**✅ Estado:** Ya está implementada en `DefaultAppContainer.kt`

El interceptor automáticamente:
1. **Obtiene el token JWT desde Firebase Auth** (del caché o del servidor)
2. **Agrega el token en el header Authorization** con formato `Bearer <token>`

**Código implementado:**
```kotlin
// En DefaultAppContainer.kt
private val backendHeadersInterceptor = okhttp3.Interceptor { chain ->
    // ... código de headers ...
    
    // Obtener token JWT de Firebase Auth
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        val tokenResult = runBlocking {
            try {
                currentUser.getIdToken(false).await() // Intentar del caché
            } catch (e: Exception) {
                currentUser.getIdToken(true).await()  // Obtener del servidor
            }
        }
        val token = tokenResult.token
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
    }
    
    chain.proceed(requestBuilder.build())
}
```

**Cómo funciona:**
- El token se obtiene automáticamente antes de cada petición al backend
- Si el token está en caché, se usa inmediatamente (rápido)
- Si no está en caché, se obtiene del servidor (puede tomar unos milisegundos)
- El token se agrega al header `Authorization: Bearer <token>`
- El backend valida el token usando `JwtAuthenticationFilter`

**Verificación:**
- Revisa los logs de Android para ver: `✅ Token JWT agregado al header Authorization`
- Si no hay usuario autenticado, verás: `⚠️ Usuario no autenticado - la petición puede fallar con 401/403`

### Opción 3: Verificar Configuración de Cloud Run

Asegúrate de que Cloud Run esté configurado para permitir acceso sin autenticación:

```bash
# Verificar configuración actual
gcloud run services describe mysyncapp-backend \
  --region us-central1 \
  --format="value(spec.template.spec.containers[0].env)"

# Si no está permitido, actualizar:
gcloud run services update mysyncapp-backend \
  --region us-central1 \
  --allow-unauthenticated
```

## 🔍 Verificación

### 1. Probar el Endpoint Directamente

Usa curl o Postman para probar si el endpoint está accesible:

```bash
# Probar endpoint de pull
curl -X GET "https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/pull?userId=test&lastSyncAt=" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json"

# Probar endpoint de push
curl -X POST "https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/push" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"deviceId":"test","userId":"test","favorites":[]}'
```

### 2. Revisar Logs del Backend

En Google Cloud Console:
1. Ir a **Cloud Run** > **mysyncapp-backend** > **Logs**
2. Buscar errores relacionados con autenticación o Spring Security

### 3. Verificar Headers en Android

Con los logs mejorados, deberías ver en Logcat:
```
📤 [HEADERS] Request a: https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/push
   Headers: Content-Type: application/json, Accept: application/json, User-Agent: MySyncApp-Android/1.0
```

## 📝 Recomendación

**Para desarrollo/testing:**
- Usa la **Opción 1** (permitir acceso sin autenticación)
- Es más simple y permite probar la funcionalidad rápidamente

**Para producción:**
- Implementa la **Opción 2** (autenticación JWT)
- Es más seguro y sigue mejores prácticas

## 🚀 Pasos Siguientes

1. **Modificar el backend** para permitir acceso sin autenticación (temporalmente)
2. **Re-desplegar el backend** en Cloud Run
3. **Probar la sincronización** desde la app Android
4. **Verificar los logs** para confirmar que funciona
5. **Implementar autenticación JWT** cuando esté listo para producción

---

**Última actualización:** Diciembre 2024

