# Быстрый старт

## Сборка плагинов

### ⚠️ ВАЖНО: Первая сборка

При первой сборке Maven загрузит все зависимости из интернета. Это может занять **5-10 минут** в зависимости от скорости интернета.

**НЕ ПРЕРЫВАЙТЕ ПРОЦЕСС (Ctrl+C)** во время загрузки зависимостей!

### Шаги сборки:

```bash
# 1. Убедитесь, что у скрипта есть права на выполнение
chmod +x build-all.sh

# 2. Запустите сборку
./build-all.sh

# 3. ДОЖДИТЕСЬ завершения! Процесс может показаться зависшим, но это нормально.
#    Maven загружает файлы из интернета.
```

### Что происходит во время сборки:

1. **Загрузка зависимостей** (первый раз - долго, последующие разы - быстро)
   - Spigot API
   - SQLite JDBC драйвер
   - Другие библиотеки

2. **Компиляция Java кода**

3. **Создание JAR файлов**

### Если процесс кажется зависшим:

- Это нормально! Maven загружает файлы из интернета
- Проверьте индикатор загрузки в терминале
- Не нажимайте Ctrl+C, если видите сообщения о загрузке

### После успешной сборки:

JAR файлы будут находиться в:
- `plugins/MineneAuth/target/MineneAuth-1.0.0.jar`
- `plugins/MineneLobby/target/MineneLobby-1.0.0.jar`
- `plugins/MineneProtection/target/MineneProtection-1.0.0.jar`
- `plugins/MineneRulers/target/MineneRulers-1.0.0.jar`
- `plugins/MineneWorld/target/MineneWorld-1.0.0.jar`

### Установка плагинов на сервер:

**Автоматически (рекомендуется):**
```bash
# Linux/Mac
chmod +x install-plugins.sh
./install-plugins.sh [путь_к_серверу]

# Windows
install-plugins.bat [путь_к_серверу]
```

**Вручную:**
Скопируйте JAR файлы из `plugins/[PluginName]/target/` в папку `plugins/` вашего Minecraft сервера.

## Решение проблем

### Ошибка: "Permission denied"
```bash
chmod +x build-all.sh
```

### Ошибка: "Maven not found"
Установите Maven:
```bash
# Ubuntu/Debian
sudo apt-get install maven

# CentOS/RHEL
sudo yum install maven
```

### Ошибка: "Java not found"
Установите Java 17+:
```bash
# Ubuntu/Debian
sudo apt-get install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel
```

### Медленная загрузка зависимостей

Если загрузка идет очень медленно:
1. Проверьте подключение к интернету
2. Попробуйте использовать VPN
3. Настройте зеркало Maven (см. BUILD.md)

