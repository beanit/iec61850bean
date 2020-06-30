::BATCH file to windows

set BATDIR=%~dp0
set LIBDIR="%BATDIR%..\build\libs-all"

java -Djava.ext.dirs=%LIBDIR% com.beanit.iec61850bean.clientgui.ClientGui %*
