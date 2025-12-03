# 🔒 Solución Detallada para Error HTTP 403

## 🔍 Diagnóstico del Problema

Según tus logs:
```
❌ [PULL] Error HTTP 403: {"timestamp":"2025-11-30T17:55:08.118Z","status":403,"error":"Forbidden","path":"/api/v1/sync/pull"}
❌ [PUSH] Error HTTP 403: {"timestamp":"2025-11-30T17:55:08.346Z","status":403,"error":"Forbidden","path":"/api/v1/sync/push"}
```

**Causa:** El backend tiene `SecurityConfig` configurado con `.authenticated()`, pero el `JwtAuthenticationFilter` **NO puede validar tokens de Firebase Auth**.

---

## ✅ Soluciones

### Opción 1: Permitir Acceso Sin Autenticación (RECOMENDADO PARA DESARROLLO)

**Esta es la solución más rápida para probar la sincronización.**

#### Paso 1: Modificar `SecurityConfig.java` en el Backend

**Archivo:** `src/main/java/com/example/demo/config/SecurityConfig.java`

**Cambiar de:**
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
                .requestMatchers("/api/v1/sync/**").authenticated()  // ← ESTO CAUSA EL 403
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

**A:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/sync/**").permitAll()  // ← PERMITIR SIN AUTENTICACIÓN
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
}
```

**Cambios:**
- ✅ Eliminar `@Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;`
- ✅ Cambiar `.requestMatchers("/api/v1/sync/**").authenticated()` 
- ✅ Por `.requestMatchers("/api/v1/sync/**").permitAll()`
- ✅ Eliminar `.addFilterBefore(jwtAuthenticationFilter, ...)`

#### Paso 2: Recompilar y Redesplegar el Backend

```bash
# En el directorio del backend
mvn clean package

# Redesplegar a Cloud Run
gcloud run deploy mysyncapp-backend \
  --source . \
  --region us-central1 \
  --allow-unauthenticated
```

#### Paso 3: Probar desde Android

1. Abre la app Android
2. Ve a **Perfil > Sincronización**
3. Toca **"Sincronizar Ahora"**
4. Debería funcionar sin error 403

---

### Opción 2: Configurar Validación de Tokens Firebase (PARA PRODUCCIÓN)

**Esta opción es más compleja pero más segura para producción.**

#### Requisitos:
1. El backend debe poder validar tokens JWT de Firebase
2. Necesitas configurar Firebase Admin SDK en el backend
3. El `JwtAuthenticationFilter` debe validar tokens de Firebase

#### Implementación en el Backend:

**1. Agregar dependencia Firebase Admin SDK:**

En `pom.xml`:
```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

**2. Crear `FirebaseConfig.java`:**

```java
package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Opción 1: Desde archivo JSON (recomendado para producción)
        FileInputStream serviceAccount = new FileInputStream("path/to/firebase-service-account.json");
        
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();
        
        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }
}
```

**3. Modificar `JwtAuthenticationFilter.java` para validar tokens de Firebase:**

```java
package com.example.demo.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                // Validar token con Firebase
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                String uid = decodedToken.getUid();
                
                // Crear autenticación
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        uid, 
                        null, 
                        null
                    );
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
            } catch (FirebaseAuthException e) {
                logger.error("Error al validar token Firebase: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

**4. Mantener `SecurityConfig.java` con autenticación:**

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
                .requestMatchers("/api/v1/sync/**").authenticated()
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

---

## 🎯 Recomendación

**Para desarrollo/testing inmediato:**
- ✅ Usa **Opción 1** (permitir sin autenticación)
- Es rápido y te permite probar la funcionalidad ahora mismo

**Para producción:**
- ✅ Implementa **Opción 2** (validación Firebase)
- Es más seguro y sigue mejores prácticas

---

## 🔍 Verificación

### Después de aplicar Opción 1:

1. **Redesplegar el backend**
2. **Probar desde Android:**
   - Ve a Perfil > Sincronización
   - Toca "Sincronizar Ahora"
   - Deberías ver: `✅ [PUSH] Push completado exitosamente`

3. **Verificar logs:**
   ```
   ✅ [PUSH] Push completado exitosamente (código: 200)
   ✅ [PULL] Pull completado exitosamente (código: 200)
   ```

### Si sigue fallando:

1. **Verifica que el backend se haya redesplegado correctamente:**
   ```bash
   gcloud run services describe mysyncapp-backend \
     --region us-central1 \
     --format="value(status.url)"
   ```

2. **Prueba el endpoint directamente con curl:**
   ```bash
   curl -X POST "https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/push" \
     -H "Content-Type: application/json" \
     -d '{"favorites":[]}'
   ```
   
   Debería devolver `200 OK` (no `403 Forbidden`)

3. **Revisa los logs del backend en Google Cloud Console:**
   - Cloud Run > mysyncapp-backend > Logs
   - Busca errores relacionados con Spring Security

---

## 📝 Resumen

**Problema:** Backend rechaza peticiones con 403 porque requiere autenticación pero no puede validar tokens de Firebase.

**Solución rápida:** Modificar `SecurityConfig.java` para permitir acceso sin autenticación a `/api/v1/sync/**`.

**Solución completa:** Configurar Firebase Admin SDK en el backend para validar tokens de Firebase Auth.

---

**Última actualización:** Diciembre 2024

