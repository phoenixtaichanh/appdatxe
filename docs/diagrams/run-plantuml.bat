@echo off
REM PlantUML Local Runner for DoAn3
REM ================================
REM No internet required - generates diagrams offline
REM Usage:
REM   run-plantuml.bat android    - Android Data Layer
REM   run-plantuml.bat backend    - Backend Structure
REM   run-plantuml.bat database   - Database Schema
REM   run-plantuml.bat sequence   - Sequence Diagram
REM   run-plantuml.bat communication - Communication Diagram
REM   run-plantuml.bat c4         - C4 Model
REM   run-plantuml.bat all        - All diagrams
REM   run-plantuml.bat <file>     - Specific file

setlocal

set "SCRIPT_DIR=%~dp0"
set "DIAGRAMS_DIR=%SCRIPT_DIR%"
set "PLANTUML_JAR=%USERPROFILE%\.local\share\plantuml\plantuml.jar"

if "%~1"=="" goto usage
if "%~1"=="android" (
    set "INPUT=%DIAGRAMS_DIR%\01_Class_Diagram_Android.puml"
) else if "%~1"=="backend" (
    set "INPUT=%DIAGRAMS_DIR%\02_Class_Diagram_Backend.puml"
) else if "%~1"=="database" (
    set "INPUT=%DIAGRAMS_DIR%\03_Class_Diagram_Database.puml"
) else if "%~1"=="sequence" (
    set "INPUT=%DIAGRAMS_DIR%\02_Sequence_Diagram.puml"
) else if "%~1"=="communication" (
    set "INPUT=%DIAGRAMS_DIR%\03_Communication_Diagram.puml"
) else if "%~1"=="c4" (
    set "INPUT=%DIAGRAMS_DIR%\04_C4_Model.puml"
) else if "%~1"=="all" (
    goto all
) else (
    set "INPUT=%~1"
)

:run
echo.
echo Generating: %INPUT%
java -jar "%PLANTUML_JAR%" -o "%DIAGRAMS_DIR%" "%INPUT%" 2>&1
goto end

:all
echo ===============================================
echo  Generating ALL DoAn3 Diagrams...
echo ===============================================
echo.
echo [1/6] Android Data Layer...
java -jar "%PLANTUML_JAR%" -o "%DIAGRAMS_DIR%" "%DIAGRAMS_DIR%\01_Class_Diagram_Android.puml"
echo [2/6] Backend Structure...
java -jar "%PLANTUML_JAR%" -o "%DIAGRAMS_DIR%" "%DIAGRAMS_DIR%\02_Class_Diagram_Backend.puml"
echo [3/6] Database Schema...
java -jar "%PLANTUML_JAR%" -o "%DIAGRAMS_DIR%" "%DIAGRAMS_DIR%\03_Class_Diagram_Database.puml"
echo [4/6] Sequence Diagram...
java -jar "%PLANTUML_JAR%" -o "%DIAGRAMS_DIR%" "%DIAGRAMS_DIR%\02_Sequence_Diagram.puml"
echo [5/6] Communication Diagram...
java -jar "%PLANTUML_JAR%" -o "%DIAGRAMS_DIR%" "%DIAGRAMS_DIR%\03_Communication_Diagram.puml"
echo [6/6] C4 Model...
java -jar "%PLANTUML_JAR%" -o "%DIAGRAMS_DIR%" "%DIAGRAMS_DIR%\04_C4_Model.puml"
echo.
echo ===============================================
echo  Done! Check *.png files in this folder.
echo ===============================================
goto end

:usage
echo.
echo PlantUML Local Runner
echo =====================
echo Usage:
echo   run-plantuml.bat android      - Android Data Layer
echo   run-plantuml.bat backend      - Backend Structure
echo   run-plantuml.bat database     - Database Schema
echo   run-plantuml.bat sequence     - Sequence Diagram
echo   run-plantuml.bat communication - Communication Diagram
echo   run-plantuml.bat c4           - C4 Model
echo   run-plantuml.bat all          - All diagrams
echo   run-plantuml.bat ^<file^>     - Custom file
echo.

:end
endlocal
pause
