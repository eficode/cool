
@echo OFF

set program=%1
set args=%*
set package=net.praqma.cli

IF "%program%"=="" CALL :WRONG_PARAMS program & EXIT /B 1
IF "%COOL_HOME%"=="" CALL :WRONG_PARAMS COOL_HOME & EXIT /B 1

set cool=java -classpath %COOL_HOME% %package%.%program% %args%


rem echo %cool%

call %cool%


@GOTO :EOF


:WRONG_PARAMS
ECHO %1 is missing


EXIT /B 
GOTO :EOF