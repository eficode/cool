
@echo OFF

set program=%1
set args=%*
set package=net.praqma.cli

IF NOT "%program%"=="" GOTO programok

echo.
echo The program was not given
echo.

EXIT /B 1

:programok

IF NOT "%COOL_HOME%"=="" GOTO coolhomeok

echo.
echo COOL_HOME is not set
echo.

EXIT /B 1

:coolhomeok


if exist "%COOL_HOME%\build\COOL-0.2.1-jar-with-dependencies.jar" goto start


:start

set cool=java -Dtest="1" -classpath %COOL_HOME% %package%.%program% %args%

rem echo %cool%

call %cool%


@GOTO :EOF


:WRONG_PARAMS
ECHO %1 is missing


EXIT /B 
GOTO :EOF