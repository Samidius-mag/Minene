#!/bin/bash

# Скрипт запуска Minecraft сервера

JAVA_OPTS="-Xmx4G -Xms2G"
JAR_FILE="paper.jar"
PROPERTIES_FILE="server.properties"

if [ ! -f "$JAR_FILE" ]; then
    echo "Ошибка: Файл $JAR_FILE не найден"
    echo "Пожалуйста, скачайте PaperMC сервер и переименуйте его в paper.jar"
    exit 1
fi

# Проверка наличия server.properties
if [ ! -f "$PROPERTIES_FILE" ]; then
    echo "Файл $PROPERTIES_FILE не найден. Создаю из шаблона..."
    if [ -f "../server.properties" ]; then
        cp ../server.properties .
    else
        echo "Предупреждение: server.properties не найден. Сервер создаст его автоматически при первом запуске."
    fi
fi

echo "Запуск Minecraft сервера Minene на порту 27015..."
echo "Для изменения порта отредактируйте файл server.properties (параметр server-port)"
java $JAVA_OPTS -jar $JAR_FILE nogui

