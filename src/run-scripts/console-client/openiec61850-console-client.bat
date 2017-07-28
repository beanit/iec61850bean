::BATCH file for windows

set BATDIR=%~dp0
set LIBDIR=%BATDIR%..\..\build\libs-all

java  -Djava.ext.dirs=%LIBDIR% org.openmuc.openiec61850.app.ConsoleClient %*
