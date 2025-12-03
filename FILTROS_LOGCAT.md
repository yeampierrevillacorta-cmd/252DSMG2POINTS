# 🔍 Filtros de Logcat para Debugging

Este documento contiene filtros útiles para el Logcat de Android Studio que te ayudarán a ver solo los logs relevantes durante el debugging.

## 📋 Filtros Recomendados

### 1. Filtro para Sincronización (Recomendado)

**Tag:** `DefaultSyncRepository|SyncSettingsViewModel|SyncWorker|DefaultAppContainer`

**Filtro en Logcat:**
```
package:mine tag:DefaultSyncRepository|SyncSettingsViewModel|SyncWorker|DefaultAppContainer
```

**O usar regex:**
```
^(DefaultSyncRepository|SyncSettingsViewModel|SyncWorker|DefaultAppContainer)
```

### 2. Filtro para Errores de Sincronización

**Filtro en Logcat:**
```
package:mine level:error tag:DefaultSyncRepository|SyncSettingsViewModel|SyncWorker
```

**O usar regex:**
```
^(DefaultSyncRepository|SyncSettingsViewModel|SyncWorker).*ERROR|^(DefaultSyncRepository|SyncSettingsViewModel|SyncWorker).*❌
```

### 3. Filtro para Retrofit (Requests/Responses)

**Tag:** `Retrofit|DefaultAppContainer`

**Filtro en Logcat:**
```
package:mine tag:Retrofit|DefaultAppContainer
```

### 4. Filtro Combinado: Sincronización + Retrofit

**Filtro en Logcat:**
```
package:mine tag:DefaultSyncRepository|SyncSettingsViewModel|SyncWorker|Retrofit|DefaultAppContainer
```

### 5. Filtro Solo Errores (Todos los Errores de la App)

**Filtro en Logcat:**
```
package:mine level:error
```

### 6. Filtro Personalizado: Solo Logs de Sincronización con Emojis

**Filtro en Logcat (regex):**
```
.*(🔄|📤|📥|✅|❌|⚠️|🔗|📍).*(Sync|sync|PUSH|PULL|SYNC)
```

## 🎯 Cómo Usar los Filtros en Android Studio

1. **Abrir Logcat** en Android Studio (pestaña inferior)
2. **Click en el icono de filtro** (🔍) o usar el campo de búsqueda
3. **Seleccionar "Edit Filter Configuration"**
4. **Crear un nuevo filtro:**
   - **Name:** "Sincronización"
   - **Log Tag:** `DefaultSyncRepository|SyncSettingsViewModel|SyncWorker`
   - **Log Level:** `Debug` o `Verbose`
   - **Package Name:** `com.example.points`
5. **Aplicar el filtro**

## 📱 Filtros desde ADB (Línea de Comandos)

### Ver solo logs de sincronización:
```bash
adb logcat -s DefaultSyncRepository:S SyncSettingsViewModel:S SyncWorker:S DefaultAppContainer:S
```

### Ver solo errores de sincronización:
```bash
adb logcat *:E DefaultSyncRepository:* SyncSettingsViewModel:* SyncWorker:*
```

### Ver logs con emojis de sincronización:
```bash
adb logcat | grep -E "(🔄|📤|📥|✅|❌|⚠️|🔗|📍|PUSH|PULL|SYNC)"
```

### Ver logs de Retrofit (requests/responses):
```bash
adb logcat -s Retrofit:D
```

## 🔧 Filtros Avanzados

### Ver solo errores HTTP:
```bash
adb logcat | grep -E "(Error HTTP|❌.*HTTP|HttpException)"
```

### Ver solo errores de conexión:
```bash
adb logcat | grep -E "(UnknownHostException|ConnectException|Error de conexión)"
```

### Ver timeline completo de sincronización:
```bash
adb logcat -s DefaultSyncRepository:D | grep -E "(🔄|📤|📥|✅|❌|⚠️)"
```

## 📊 Tags Principales para Sincronización

- `DefaultSyncRepository` - Logs del repositorio de sincronización
- `SyncSettingsViewModel` - Logs del ViewModel de configuración
- `SyncWorker` - Logs del Worker de sincronización automática
- `DefaultAppContainer` - Logs de configuración de Retrofit
- `Retrofit` - Logs de requests/responses HTTP

## 💡 Tips

1. **Usa el filtro "Show only selected application"** para ver solo logs de tu app
2. **Guarda filtros personalizados** para acceso rápido
3. **Usa regex** para filtros más complejos
4. **Combina múltiples filtros** usando `|` (OR) o `&` (AND)
5. **Exporta logs** cuando encuentres un error para análisis posterior

## 🐛 Debugging de Errores Comunes

### Error: "Tanto pull como push fallaron"
**Filtro:**
```
package:mine tag:DefaultSyncRepository level:error
```

### Error: "Unable to create @Body converter"
**Filtro:**
```
package:mine level:error | grep -i "converter\|serialization"
```

### Error: "No se pudo resolver el host"
**Filtro:**
```
package:mine | grep -i "host\|connection\|network"
```

---

**Última actualización:** Diciembre 2024

