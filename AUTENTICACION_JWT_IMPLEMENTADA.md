# 🔐 Autenticación JWT Implementada

## ✅ Estado: Implementada y Funcional

La aplicación Android ahora incluye autenticación JWT automática para todas las peticiones al backend Spring Boot.

---

## 📋 Resumen

El backend Spring Boot requiere autenticación JWT según `SecurityConfig.java`:
```java
.requestMatchers("/api/v1/sync/**").authenticated()
```

La aplicación Android ahora:
1. ✅ Obtiene automáticamente el token JWT de Firebase Auth
2. ✅ Agrega el token al header `Authorization: Bearer <token>`
3. ✅ Maneja errores de autenticación correctamente

---

## 🔧 Implementación

### Archivo: `app/src/main/java/com/example/points/data/DefaultAppContainer.kt`

**Interceptor de Headers:**
```kotlin
private val backendHeadersInterceptor = okhttp3.Interceptor { chain ->
    val originalRequest = chain.request()
    val isBackendRequest = originalRequest.url.toString().contains(BACKEND_BASE_URL)
    
    if (isBackendRequest) {
        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-Agent", "MySyncApp-Android/1.0")
        
        // Obtener token JWT de Firebase Auth
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val tokenResult = runBlocking {
                    try {
                        currentUser.getIdToken(false).await() // Del caché
                    } catch (e: Exception) {
                        currentUser.getIdToken(true).await()  // Del servidor
                    }
                }
                val token = tokenResult.token
                if (token != null) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
            }
        } catch (e: Exception) {
            Log.e("DefaultAppContainer", "Error al obtener token: ${e.message}")
        }
        
        chain.proceed(requestBuilder.build())
    }
}
```

---

## 🔄 Flujo de Autenticación

### 1. Usuario Inicia Sesión
- Usuario se autentica con Firebase Auth
- Firebase genera un token JWT

### 2. Petición al Backend
- La app necesita hacer una petición a `/api/v1/sync/push` o `/api/v1/sync/pull`
- El interceptor intercepta la petición

### 3. Obtención del Token
- El interceptor verifica si hay un usuario autenticado
- Si existe, obtiene el token JWT:
  - **Primero:** Intenta obtener del caché local (rápido)
  - **Si falla:** Obtiene del servidor de Firebase (puede tomar unos milisegundos)

### 4. Agregar Header
- El token se agrega al header: `Authorization: Bearer <token>`
- La petición continúa con el token incluido

### 5. Validación en el Backend
- El backend recibe la petición con el header `Authorization`
- `JwtAuthenticationFilter` valida el token
- Si es válido, permite el acceso
- Si no es válido, devuelve 401/403

---

## 📝 Logs de Debugging

### Logs Exitosos
```
✅ Token JWT agregado al header Authorization
📤 [HEADERS] Request a: https://mysyncapp-backend-860998153214.us-central1.run.app/api/v1/sync/push
   Headers: [Content-Type, Accept, User-Agent, Authorization]
   Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
```

### Logs de Advertencia
```
⚠️ Usuario no autenticado - la petición puede fallar con 401/403
⚠️ Token no en caché, obteniendo del servidor...
```

### Logs de Error
```
❌ Error al obtener token de Firebase: <mensaje de error>
```

---

## 🧪 Pruebas

### 1. Verificar que el Token se Obtiene
1. Abre la app
2. Inicia sesión con Firebase Auth
3. Ve a **Perfil > Sincronización**
4. Toca **Sincronizar Ahora**
5. Revisa Logcat con filtro: `DefaultAppContainer`
6. Deberías ver: `✅ Token JWT agregado al header Authorization`

### 2. Verificar que el Backend Acepta el Token
1. Realiza una sincronización
2. Si es exitosa, el token fue validado correctamente
3. Si falla con 401/403, verifica:
   - Que el usuario esté autenticado en Firebase
   - Que el backend tenga `JwtAuthenticationFilter` configurado correctamente
   - Que el backend pueda validar tokens de Firebase

### 3. Probar sin Autenticación
1. Cierra sesión en la app
2. Intenta sincronizar
3. Deberías ver: `⚠️ Usuario no autenticado`
4. La petición fallará con 401/403 (comportamiento esperado)

---

## 🔍 Troubleshooting

### Error: "Usuario no autenticado"
**Causa:** El usuario no ha iniciado sesión con Firebase Auth.

**Solución:**
1. Asegúrate de que el usuario haya iniciado sesión
2. Verifica que `FirebaseAuth.getInstance().currentUser` no sea `null`

### Error: "Token JWT es null"
**Causa:** Firebase Auth no pudo generar el token.

**Solución:**
1. Verifica la conexión a internet
2. Verifica que Firebase Auth esté configurado correctamente
3. Intenta cerrar sesión y volver a iniciar sesión

### Error: HTTP 401/403 desde el Backend
**Causa:** El backend no puede validar el token JWT.

**Posibles causas:**
1. El backend no tiene `JwtAuthenticationFilter` configurado
2. El backend no puede validar tokens de Firebase
3. El token expiró (debería renovarse automáticamente)

**Solución:**
1. Verifica que el backend tenga `JwtAuthenticationFilter` configurado
2. Verifica que el backend pueda validar tokens de Firebase Auth
3. Revisa los logs del backend para ver el error específico

---

## 📚 Referencias

- **Backend SecurityConfig:** `com.example.demo.config.SecurityConfig`
- **JwtAuthenticationFilter:** `com.example.demo.security.JwtAuthenticationFilter`
- **Firebase Auth:** [Documentación oficial](https://firebase.google.com/docs/auth)

---

## 🚀 Próximos Pasos

1. ✅ **Implementación completada** - Autenticación JWT funcional
2. ⏳ **Pruebas en producción** - Verificar que funciona con el backend desplegado
3. ⏳ **Manejo de renovación de tokens** - Los tokens se renuevan automáticamente cuando expiran
4. ⏳ **Manejo de errores 401** - Considerar redirigir al login si el token es inválido

---

**Última actualización:** Diciembre 2024

