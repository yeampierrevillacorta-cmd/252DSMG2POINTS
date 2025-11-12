# Script de configuración inicial para Windows PowerShell
# Este script crea el archivo .env desde .env.example

Write-Host "🚀 Configurando variables de entorno..." -ForegroundColor Cyan

# Verificar si .env.example existe
if (-not (Test-Path ".env.example")) {
    Write-Host "❌ Error: No se encuentra el archivo .env.example" -ForegroundColor Red
    Write-Host "   Asegúrate de estar en la raíz del proyecto." -ForegroundColor Yellow
    exit 1
}

# Verificar si .env ya existe
if (Test-Path ".env") {
    Write-Host "⚠️  El archivo .env ya existe." -ForegroundColor Yellow
    $response = Read-Host "¿Deseas sobrescribirlo? (S/N)"
    if ($response -ne "S" -and $response -ne "s") {
        Write-Host "❌ Operación cancelada." -ForegroundColor Red
        exit 0
    }
}

# Copiar .env.example a .env
Copy-Item ".env.example" ".env" -Force

Write-Host "✅ Archivo .env creado exitosamente!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Próximos pasos:" -ForegroundColor Cyan
Write-Host "   1. Abre el archivo .env en tu editor" -ForegroundColor White
Write-Host "   2. Reemplaza todos los valores 'tu_clave_...' con tus claves reales" -ForegroundColor White
Write-Host "   3. Guarda el archivo" -ForegroundColor White
Write-Host ""
Write-Host "💡 Tip: El archivo .env está en .gitignore y NO se subirá al repositorio." -ForegroundColor Gray

