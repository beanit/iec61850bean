::BATCH file to windows

set BATDIR=%~dp0
set LIBDIR=%BATDIR%..\..\build\libs-all

java -Dlogback.configurationFile=logback.xml -Djava.ext.dirs=%LIBDIR% com.beanit.openiec61850.app.ConsoleServer %*
