@echo off
REM Скрипт копирования собранных плагинов на сервер для Windows

REM Путь к папке сервера (по умолчанию - текущая директория)
set SERVER_DIR=%1
if "%SERVER_DIR%"=="" set SERVER_DIR=.

REM Проверка наличия папки plugins в сервере
if not exist "%SERVER_DIR%\plugins" (
    echo Создание папки plugins в %SERVER_DIR%...
    mkdir "%SERVER_DIR%\plugins"
)

echo === Копирование плагинов Minene ===
echo Целевая папка: %SERVER_DIR%\plugins
echo.

set COPIED=0
set MISSING=0

REM MineneAuth
if exist "plugins\MineneAuth\target\MineneAuth-1.0.0.jar" (
    copy "plugins\MineneAuth\target\MineneAuth-1.0.0.jar" "%SERVER_DIR%\plugins\" >nul
    if %errorlevel% equ 0 (
        echo [OK] Скопирован: MineneAuth-1.0.0.jar
        set /a COPIED+=1
    ) else (
        echo [ERROR] Ошибка при копировании: MineneAuth
    )
) else (
    echo [MISSING] Не найден: plugins\MineneAuth\target\MineneAuth-1.0.0.jar
    set /a MISSING+=1
)

REM MineneLobby
if exist "plugins\MineneLobby\target\MineneLobby-1.0.0.jar" (
    copy "plugins\MineneLobby\target\MineneLobby-1.0.0.jar" "%SERVER_DIR%\plugins\" >nul
    if %errorlevel% equ 0 (
        echo [OK] Скопирован: MineneLobby-1.0.0.jar
        set /a COPIED+=1
    ) else (
        echo [ERROR] Ошибка при копировании: MineneLobby
    )
) else (
    echo [MISSING] Не найден: plugins\MineneLobby\target\MineneLobby-1.0.0.jar
    set /a MISSING+=1
)

REM MineneProtection
if exist "plugins\MineneProtection\target\MineneProtection-1.0.0.jar" (
    copy "plugins\MineneProtection\target\MineneProtection-1.0.0.jar" "%SERVER_DIR%\plugins\" >nul
    if %errorlevel% equ 0 (
        echo [OK] Скопирован: MineneProtection-1.0.0.jar
        set /a COPIED+=1
    ) else (
        echo [ERROR] Ошибка при копировании: MineneProtection
    )
) else (
    echo [MISSING] Не найден: plugins\MineneProtection\target\MineneProtection-1.0.0.jar
    set /a MISSING+=1
)

REM MineneRulers
if exist "plugins\MineneRulers\target\MineneRulers-1.0.0.jar" (
    copy "plugins\MineneRulers\target\MineneRulers-1.0.0.jar" "%SERVER_DIR%\plugins\" >nul
    if %errorlevel% equ 0 (
        echo [OK] Скопирован: MineneRulers-1.0.0.jar
        set /a COPIED+=1
    ) else (
        echo [ERROR] Ошибка при копировании: MineneRulers
    )
) else (
    echo [MISSING] Не найден: plugins\MineneRulers\target\MineneRulers-1.0.0.jar
    set /a MISSING+=1
)

REM MineneWorld
if exist "plugins\MineneWorld\target\MineneWorld-1.0.0.jar" (
    copy "plugins\MineneWorld\target\MineneWorld-1.0.0.jar" "%SERVER_DIR%\plugins\" >nul
    if %errorlevel% equ 0 (
        echo [OK] Скопирован: MineneWorld-1.0.0.jar
        set /a COPIED+=1
    ) else (
        echo [ERROR] Ошибка при копировании: MineneWorld
    )
) else (
    echo [MISSING] Не найден: plugins\MineneWorld\target\MineneWorld-1.0.0.jar
    set /a MISSING+=1
)

echo.
echo === Результат ===
echo Скопировано: %COPIED% плагинов
if %MISSING% gtr 0 (
    echo Не найдено: %MISSING% плагинов
    echo.
    echo Для сборки всех плагинов выполните: build-all.bat
)

if %COPIED% gtr 0 (
    echo.
    echo Плагины успешно установлены в %SERVER_DIR%\plugins\
    echo Теперь можно запускать сервер!
)

pause

