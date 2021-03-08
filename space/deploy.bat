@echo off
SETLOCAL
if not defined GS_HOME set GS_HOME=%~dp0..\..
call %GS_HOME%\bin\gs.bat pu deploy myspace %~dp0\target\myspace-0.1.jar %*