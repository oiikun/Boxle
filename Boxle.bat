@echo off
chcp 65001 >nul
title Boxle
cd /d "%~dp0backend"

netstat -ano | findstr ":8080 " | findstr "LISTENING" >nul
if %errorlevel%==0 (
    echo O Boxle já está ligado. Abrindo o navegador...
    start "" http://localhost:8080
    exit /b
)

where java >nul 2>nul
if %errorlevel% neq 0 if not defined JAVA_HOME (
    echo Java não encontrado. Instale o JDK 17 ou mais novo e abra de novo.
    pause
    exit /b
)

echo Ligando o Boxle. O navegador abre sozinho quando estiver pronto.
echo Na primeira vez demora mais, porque baixa as bibliotecas.
echo Para desligar, feche esta janela.
echo.
call .\mvnw.cmd -q spring-boot:run
pause
