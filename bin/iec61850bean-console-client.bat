::BATCH file to windows
@echo off

set BATDIR=%~dp0
set LIBDIR="%BATDIR%..\build\libs-all\*"

java -cp %LIBDIR% com.beanit.iec61850bean.app.ConsoleClient %*
