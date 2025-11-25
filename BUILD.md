# Инструкция по сборке плагинов

## Требования

- Java 17 или выше
- Maven 3.6 или выше
- Git (опционально)

## Сборка плагинов

### Автоматическая сборка всех плагинов

```bash
# Перейти в корневую директорию проекта
cd Minene

# Собрать все плагины
./build-all.sh
```

### Ручная сборка каждого плагина

```bash
# MineneAuth
cd plugins/MineneAuth
mvn clean package
cd ../..

# MineneLobby
cd plugins/MineneLobby
mvn clean package
cd ../..

# MineneProtection
cd plugins/MineneProtection
mvn clean package
cd ../..

# MineneRulers
cd plugins/MineneRulers
mvn clean package
cd ../..

# MineneWorld
cd plugins/MineneWorld
mvn clean package
cd ../..
```

Собранные JAR файлы будут находиться в папках `plugins/[PluginName]/target/`.

## Установка на сервер

1. Скопируйте все собранные JAR файлы в папку `plugins/` вашего Minecraft сервера
2. Запустите сервер один раз для создания конфигурационных файлов
3. Настройте конфигурационные файлы в папках `plugins/[PluginName]/`
4. Перезапустите сервер

## Порядок загрузки плагинов

Плагины имеют следующие зависимости:
- **MineneAuth** - независимый
- **MineneLobby** - зависит от MineneAuth
- **MineneProtection** - независимый
- **MineneRulers** - зависит от MineneProtection
- **MineneWorld** - независимый

Сервер автоматически загрузит плагины в правильном порядке.

## Решение проблем

### Ошибка компиляции

Убедитесь, что:
- Java 17+ установлена: `java -version`
- Maven установлен: `mvn -version`
- Все зависимости загружены: `mvn dependency:resolve`

### Плагины не загружаются

Проверьте:
- Версия сервера совместима (1.20.1+)
- Все зависимости установлены
- Логи сервера на наличие ошибок

### База данных не создается

Убедитесь, что:
- Плагин MineneAuth имеет права на запись в папку `plugins/MineneAuth/`
- SQLite драйвер включен в JAR файл

