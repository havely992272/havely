#!/bin/bash
echo "🔧 Setting up Havely Server in Termux..."

# Проверяем Node.js
if ! command -v node &> /dev/null; then
    echo "📦 Installing Node.js..."
    pkg install nodejs -y
fi

# Устанавливаем зависимости
echo "📦 Installing dependencies..."
npm install

echo "🚀 Starting Havely Server..."
node src/server.js
