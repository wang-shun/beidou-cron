@echo off
  
set url=%1
set file=%2

snap_shot.exe %url% %file% >> %3

@echo on