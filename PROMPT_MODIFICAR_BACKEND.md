# 🔧 Prompt para Modificar el Backend

## 📋 Instrucciones para el Desarrollador del Backend

Copia y pega este prompt completo al desarrollador del backend o úsalo como referencia:

---

## 🎯 Objetivo

Modificar `SecurityConfig.java` para permitir acceso **sin autenticación** a los endpoints de sincronización (`/api/v1/sync/**`) temporalmente, para permitir que la aplicación Android se conecte correctamente.

---

## 📝 Cambios Requeridos

### Archivo a Modificar:
`src/main/java/com/example/demo/config/SecurityConfig.java`

### Cambio Específico:

**ANTES (Código Actual):**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/sync/**").authenticated()  // ← ESTA LÍNEA CAUSA EL 403
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**DESPUÉS (Código Modificado):**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/sync/**").permitAll()  // ← CAMBIAR A permitAll()
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
}
```

### Cambios Detallados:

1. **Eliminar esta línea:**
   ```java
   @Autowired
   private JwtAuthenticationFilter jwtAuthenticationFilter;
   ```

2. **Cambiar esta línea:**
   ```java
   .requestMatchers("/api/v1/sync/**").authenticated()
   ```
   **Por:**
   ```java
   .requestMatchers("/api/v1/sync/**").permitAll()
   ```

3. **Eliminar esta línea:**
   ```java
   .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
   ```

---

## ✅ Verificación

Después de hacer los cambios:

1. **Recompilar el proyecto:**
   ```bash
   mvn clean package
   ```

2. **Redesplegar a Cloud Run:**
   ```bash
   gcloud run deploy mysyncapp-backend \
     --source . \
     --region us-central1 \
     --allow-unauthenticated
   ```

3. **Probar el endpoint directamente:**
   ```bash
   curl -X POST "https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/push" \
     -H "Content-Type: application/json" \
     -d '{"favorites":[]}'
   ```
   
   **Resultado esperado:** `200 OK` (no `403 Forbidden`)

---

## 📌 Notas Importantes

- ⚠️ **Esta es una solución temporal para desarrollo/testing**
- ✅ Los endpoints `/api/v1/sync/**` estarán accesibles sin autenticación
- 🔒 **Para producción**, se recomienda implementar validación de tokens Firebase (ver `SOLUCION_ERROR_403_DETALLADA.md`)

---

## 🚀 Después del Cambio

Una vez que el backend esté redesplegado:

1. La aplicación Android podrá sincronizar sin error 403
2. Los logs mostrarán: `✅ [PUSH] Push completado exitosamente`
3. Los favoritos se guardarán correctamente en la base de datos del backend

---

**Fecha:** Diciembre 2024
**Prioridad:** Alta (bloquea la funcionalidad de sincronización)

