@echo off
REM Скрипт сборки всех плагинов Minene для Windows

echo === Сборка плагинов Minene ===
echo.

set PLUGINS=MineneAuth MineneLobby MineneProtection MineneRulers MineneWorld

for %%p in (%PLUGINS%) do (
    echo Сборка %%p...
    cd plugins\%%p
    
    if not exist pom.xml (
        echo Ошибка: pom.xml не найден для %%p
        cd ..\..
        continue
    )
    
    call mvn clean package
    
    if %errorlevel% equ 0 (
        echo [OK] %%p собран успешно
    ) else (
        echo [ERROR] Ошибка при сборке %%p
    )
    
    cd ..\..
    echo.
)

echo === Сборка завершена ===
echo.
echo JAR файлы находятся в:
for %%p in (%PLUGINS%) do (
    echo   - plugins\%%p\target\%%p-1.0.0.jar
)

pause

