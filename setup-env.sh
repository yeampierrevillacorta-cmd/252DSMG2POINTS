#!/bin/bash
# Script de configuración inicial para Linux/Mac
# Este script crea el archivo .env desde .env.example

echo "🚀 Configurando variables de entorno..."

# Verificar si .env.example existe
if [ ! -f ".env.example" ]; then
    echo "❌ Error: No se encuentra el archivo .env.example"
    echo "   Asegúrate de estar en la raíz del proyecto."
    exit 1
fi

# Verificar si .env ya existe
if [ -f ".env" ]; then
    echo "⚠️  El archivo .env ya existe."
    read -p "¿Deseas sobrescribirlo? (S/N): " response
    if [ "$response" != "S" ] && [ "$response" != "s" ]; then
        echo "❌ Operación cancelada."
        exit 0
    fi
fi

# Copiar .env.example a .env
cp .env.example .env

echo "✅ Archivo .env creado exitosamente!"
echo ""
echo "📝 Próximos pasos:"
echo "   1. Abre el archivo .env en tu editor"
echo "   2. Reemplaza todos los valores 'tu_clave_...' con tus claves reales"
echo "   3. Guarda el archivo"
echo ""
echo "💡 Tip: El archivo .env está en .gitignore y NO se subirá al repositorio."

