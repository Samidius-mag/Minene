# Инструкция по установке плагинов

## Автоматическая установка

После сборки плагинов используйте скрипт для автоматической установки:

### Linux/Mac

```bash
# Установка в текущую директорию (если сервер в той же папке)
chmod +x install-plugins.sh
./install-plugins.sh

# Установка в указанную папку сервера
./install-plugins.sh /path/to/minecraft-server
```

### Windows

```cmd
REM Установка в текущую директорию
install-plugins.bat

REM Установка в указанную папку сервера
install-plugins.bat C:\path\to\minecraft-server
```

## Что делает скрипт

1. Проверяет наличие папки `plugins/` в указанной директории (создает, если нет)
2. Ищет собранные JAR файлы в `plugins/[PluginName]/target/`
3. Копирует найденные плагины в папку `plugins/` сервера
4. Выводит отчет о результате (сколько скопировано, сколько не найдено)

## Пример использования

```bash
# 1. Собрать плагины
./build-all.sh

# 2. Установить плагины на сервер
./install-plugins.sh ../minecraft-server

# 3. Запустить сервер
cd ../minecraft-server
java -Xmx4G -Xms2G -jar paper.jar nogui
```

## Ручная установка

Если скрипт не работает, скопируйте файлы вручную:

```bash
# Linux/Mac
cp plugins/MineneAuth/target/MineneAuth-1.0.0.jar minecraft-server/plugins/
cp plugins/MineneLobby/target/MineneLobby-1.0.0.jar minecraft-server/plugins/
cp plugins/MineneProtection/target/MineneProtection-1.0.0.jar minecraft-server/plugins/
cp plugins/MineneRulers/target/MineneRulers-1.0.0.jar minecraft-server/plugins/
cp plugins/MineneWorld/target/MineneWorld-1.0.0.jar minecraft-server/plugins/
```

```cmd
REM Windows
copy plugins\MineneAuth\target\MineneAuth-1.0.0.jar minecraft-server\plugins\
copy plugins\MineneLobby\target\MineneLobby-1.0.0.jar minecraft-server\plugins\
copy plugins\MineneProtection\target\MineneProtection-1.0.0.jar minecraft-server\plugins\
copy plugins\MineneRulers\target\MineneRulers-1.0.0.jar minecraft-server\plugins\
copy plugins\MineneWorld\target\MineneWorld-1.0.0.jar minecraft-server\plugins\
```

## Решение проблем

### Ошибка: "Permission denied" (Linux/Mac)
```bash
chmod +x install-plugins.sh
```

### Плагины не найдены
Убедитесь, что плагины собраны:
```bash
./build-all.sh
```

### Папка plugins не создается
Проверьте права доступа к целевой директории:
```bash
ls -la /path/to/minecraft-server
```

