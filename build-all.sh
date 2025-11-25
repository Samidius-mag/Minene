#!/bin/bash

# Скрипт сборки всех плагинов Minene

echo "=== Сборка плагинов Minene ==="
echo ""

PLUGINS=("MineneAuth" "MineneLobby" "MineneProtection" "MineneRulers" "MineneWorld")

for plugin in "${PLUGINS[@]}"; do
    echo "Сборка $plugin..."
    cd "plugins/$plugin"
    
    if [ ! -f "pom.xml" ]; then
        echo "Ошибка: pom.xml не найден для $plugin"
        cd ../..
        continue
    fi
    
    mvn clean package
    
    if [ $? -eq 0 ]; then
        echo "✓ $plugin собран успешно"
    else
        echo "✗ Ошибка при сборке $plugin"
    fi
    
    cd ../..
    echo ""
done

echo "=== Сборка завершена ==="
echo ""
echo "JAR файлы находятся в:"
for plugin in "${PLUGINS[@]}"; do
    echo "  - plugins/$plugin/target/$plugin-1.0.0.jar"
done
echo ""
echo "Для установки плагинов на сервер выполните:"
echo "  ./install-plugins.sh [путь_к_серверу]"

