#!/bin/bash

# Скрипт запуска Minecraft сервера

JAVA_OPTS="-Xmx4G -Xms2G"
JAR_FILE="paper.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "Ошибка: Файл $JAR_FILE не найден"
    echo "Пожалуйста, скачайте PaperMC сервер и переименуйте его в paper.jar"
    exit 1
fi

echo "Запуск Minecraft сервера Minene..."
java $JAVA_OPTS -jar $JAR_FILE nogui

