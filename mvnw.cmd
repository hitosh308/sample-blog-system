@ECHO OFF
SETLOCAL
IF EXIST "%MAVEN_HOME%\bin\mvn.cmd" (
  CALL "%MAVEN_HOME%\bin\mvn.cmd" %*
) ELSE IF EXIST "%M2_HOME%\bin\mvn.cmd" (
  CALL "%M2_HOME%\bin\mvn.cmd" %*
) ELSE (
  WHERE mvn >NUL 2>&1
  IF %ERRORLEVEL% EQU 0 (
    mvn %*
  ) ELSE (
    ECHO Maven (mvn) が見つかりません。Maven をインストールしてください。
    EXIT /B 1
  )
)
ENDLOCAL
