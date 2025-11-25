# Исправление проблемы с MineneWorld

## Проблема
Плагин MineneWorld не загружается с ошибкой:
```
Cannot find main class `ru.minene.world.MineneWorld`
```

## Решение

Проблема была в конфигурации `maven-shade-plugin`. Он был удален, так как у MineneWorld нет зависимостей для включения.

### Шаги исправления:

1. Пересоберите только плагин MineneWorld:
```bash
cd plugins/MineneWorld
mvn clean package
cd ../..
```

2. Или пересоберите все плагины:
```bash
./build-all.sh
```

3. Установите обновленный плагин:
```bash
./install-plugins.sh [путь_к_серверу]
```

4. Перезапустите сервер

## Проверка

После пересборки проверьте, что файл `plugins/MineneWorld/target/MineneWorld-1.0.0.jar` существует и содержит классы:
```bash
jar -tf plugins/MineneWorld/target/MineneWorld-1.0.0.jar | grep MineneWorld.class
```

Должна быть строка: `ru/minene/world/MineneWorld.class`

