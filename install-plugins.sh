#!/bin/bash

# Скрипт копирования собранных плагинов на сервер

# Путь к папке сервера (по умолчанию - текущая директория)
SERVER_DIR="${1:-.}"

# Проверка наличия папки plugins в сервере
if [ ! -d "$SERVER_DIR/plugins" ]; then
    echo "Создание папки plugins в $SERVER_DIR..."
    mkdir -p "$SERVER_DIR/plugins"
fi

# Массив плагинов
PLUGINS=("MineneAuth" "MineneLobby" "MineneProtection" "MineneRulers" "MineneWorld")

echo "=== Копирование плагинов Minene ==="
echo "Целевая папка: $SERVER_DIR/plugins"
echo ""

COPIED=0
MISSING=0

for plugin in "${PLUGINS[@]}"; do
    JAR_FILE="plugins/$plugin/target/$plugin-1.0.0.jar"
    
    if [ -f "$JAR_FILE" ]; then
        cp "$JAR_FILE" "$SERVER_DIR/plugins/"
        if [ $? -eq 0 ]; then
            echo "✓ Скопирован: $plugin-1.0.0.jar"
            ((COPIED++))
        else
            echo "✗ Ошибка при копировании: $plugin"
        fi
    else
        echo "✗ Не найден: $JAR_FILE"
        echo "  Убедитесь, что плагин собран: cd plugins/$plugin && mvn clean package"
        ((MISSING++))
    fi
done

echo ""
echo "=== Результат ==="
echo "Скопировано: $COPIED плагинов"
if [ $MISSING -gt 0 ]; then
    echo "Не найдено: $MISSING плагинов"
    echo ""
    echo "Для сборки всех плагинов выполните: ./build-all.sh"
fi

if [ $COPIED -gt 0 ]; then
    echo ""
    echo "Плагины успешно установлены в $SERVER_DIR/plugins/"
    echo "Теперь можно запускать сервер!"
fi

