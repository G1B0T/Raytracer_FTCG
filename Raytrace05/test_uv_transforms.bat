@echo off
echo Testing UV transformations for R2-D2 textures...
cd /d "d:\FTCG\Raytrace05\Raytrace05"

echo.
echo =================================
echo TEST 1: Original UV (u, v)
echo =================================

copy /y "src\de\hskl\imst\i\cgma\raytracer\texture\Texture.java" "src\de\hskl\imst\i\cgma\raytracer\texture\Texture_original.java"

echo Kompiliere...
javac -cp src src/de/hskl/imst/i/cgma/raytracer/*.java src/de/hskl/imst/i/cgma/raytracer/texture/*.java src/de/hskl/imst/i/cgma/raytracer/file/*.java src/de/hskl/imst/i/cgma/raytracer/gui/*.java -d bin

if %errorlevel% equ 0 (
    echo Kompilierung erfolgreich! 
    echo Starte Raytracer...
    timeout /t 2 /nobreak >nul
    echo Sie können jetzt das Rendering testen.
    echo.
    echo Drücken Sie eine beliebige Taste um mit dem nächsten Test fortzufahren...
    pause >nul
) else (
    echo Fehler bei der Kompilierung!
    pause
)

echo.
echo =================================
echo TEST 2: V gespiegelt (u, 1-v)
echo =================================
