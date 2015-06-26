@echo off 
setLocal EnableDelayedExpansion 
for /f "tokens=* delims= " %%a in (%1) do ( 
set var=%%a 
) 
echo %1	!var! 
