@echo off
REM =============================================================================
REM creedengo-java Integration Tests Runner (Windows)
REM =============================================================================
REM This script runs the SonarQube integration tests using the Maven Failsafe
REM plugin and the SonarQube Orchestrator. It downloads SonarQube automatically,
REM installs the built plugin, analyses a test project, and verifies detected issues.
REM
REM Prerequisites:
REM   - JDK 17+ (JDK 21 required for SonarQube >= 26.x)
REM   - Maven wrapper (mvnw.cmd) available at project root
REM   - Internet access (to download SonarQube on first run; cached in ~/.sonar/orchestrator)
REM
REM Usage: run_it.bat [--rule GCIXXX] [--keep-running] [--skip-build]
REM
REM Options:
REM   --rule GCIXXX     Run only IT tests matching the given rule ID (e.g. GCI27)
REM   --keep-running    Keep SonarQube running after tests (for manual inspection on port 33333)
REM   --skip-build      Skip the clean+compile step (reuse existing target/ from a previous build)
REM
REM How it works:
REM   This script uses the Maven lifecycle phase "verify" with -DskipTests=true.
REM   This ensures that all systemPropertyVariables defined in the pom.xml for
REM   the maven-failsafe-plugin are correctly injected (orchestrator URL,
REM   SonarQube version, plugin paths, etc.). Using standalone goals like
REM   "failsafe:integration-test" would NOT inject those properties.
REM
REM   - Without --skip-build: runs "mvnw clean verify -DskipTests=true"
REM     (full clean build + IT)
REM   - With --skip-build: runs "mvnw verify -DskipTests=true" (no clean;
REM     Maven detects compiled classes are up-to-date, repackages quickly, runs IT)
REM
REM   If review_pr.bat was already run, the target/ directory contains the JAR.
REM   Using --skip-build avoids a full recompile (only shade+IT run, ~5s overhead).
REM
REM Outputs:
REM   - it_report.txt          Structured text report with pass/fail per test
REM   - it_build_output.log    Full Maven output of the IT run
REM
REM Exit codes:
REM   0 = All IT tests passed
REM   1 = One or more IT tests failed
REM   2 = Build/infrastructure error (could not run tests)
REM =============================================================================

setlocal enabledelayedexpansion

REM --- Configuration ---
set "REPORT_FILE=it_report.txt"
set "LOG_FILE=it_build_output.log"
set "RULE_FILTER="
set "KEEP_RUNNING=false"
set "SKIP_BUILD=false"
set "FAILSAFE_REPORTS_DIR=target\failsafe-reports"

REM --- Parse arguments ---
:parse_args
if "%~1"=="" goto :end_parse
if "%~1"=="--rule" (set "RULE_FILTER=%~2" & shift & shift & goto :parse_args)
if "%~1"=="--keep-running" (set "KEEP_RUNNING=true" & shift & goto :parse_args)
if "%~1"=="--skip-build" (set "SKIP_BUILD=true" & shift & goto :parse_args)
shift
goto :parse_args
:end_parse

REM --- Validate environment ---
where java >nul 2>&1
if errorlevel 1 (
    echo [IT-Runner] ERROR: Java not found. JDK 17+ is required.
    exit /b 2
)

if not exist "mvnw.cmd" (
    echo [IT-Runner] ERROR: Maven wrapper not found. Run from project root.
    exit /b 2
)

REM --- Check for existing JAR when --skip-build ---
if "%SKIP_BUILD%"=="true" (
    dir /b target\creedengo-*.jar >nul 2>&1
    if errorlevel 1 (
        echo [IT-Runner] ERROR: No plugin JAR found in target\. Run without --skip-build first.
        exit /b 2
    )
    echo [IT-Runner] Reusing existing JAR from target\.
)

REM --- Compose Maven command ---
REM Use the Maven lifecycle "verify" so that all pom.xml systemPropertyVariables
REM are correctly injected into the failsafe plugin execution.
set "MVN_CMD=verify -DskipTests=true"

if "%SKIP_BUILD%"=="false" set "MVN_CMD=clean %MVN_CMD%"

if "%KEEP_RUNNING%"=="true" (
    set "MVN_CMD=!MVN_CMD! -Dtest-it.sonarqube.keepRunning=true"
    echo [IT-Runner] SonarQube will remain running after tests.
)

if not "%RULE_FILTER%"=="" (
    set "MVN_CMD=!MVN_CMD! -Dit.test=GCIRulesIT#test%RULE_FILTER%*"
    echo [IT-Runner] Filtering tests for rule: %RULE_FILTER%
)

REM --- Run integration tests ---
echo [IT-Runner] Running integration tests...
echo [IT-Runner] Command: mvnw.cmd %MVN_CMD%

call mvnw.cmd %MVN_CMD% > "%LOG_FILE%" 2>&1
set "MVN_EXIT=%errorlevel%"

REM --- Parse results ---
echo [IT-Runner] Parsing test results...

REM Initialize report
echo ============================================================> "%REPORT_FILE%"
echo   CREEDENGO-JAVA INTEGRATION TESTS REPORT>> "%REPORT_FILE%"
echo   Date: %date% %time%>> "%REPORT_FILE%"
echo   Rule filter: %RULE_FILTER%>> "%REPORT_FILE%"
echo   Maven exit code: !MVN_EXIT!>> "%REPORT_FILE%"
echo ============================================================>> "%REPORT_FILE%"
echo.>> "%REPORT_FILE%"

REM Parse Failsafe summary from Maven output
set "TOTAL_TESTS=0"
set "TOTAL_FAILURES=0"
set "TOTAL_ERRORS=0"
set "TOTAL_SKIPPED=0"

REM Parse from failsafe-summary.xml if it exists (most reliable source)
if exist "%FAILSAFE_REPORTS_DIR%\failsafe-summary.xml" (
    for /f "tokens=2 delims=<>" %%a in ('findstr "completed" "%FAILSAFE_REPORTS_DIR%\failsafe-summary.xml"') do set /a "TOTAL_TESTS=%%a"
    for /f "tokens=2 delims=<>" %%a in ('findstr "failures" "%FAILSAFE_REPORTS_DIR%\failsafe-summary.xml"') do set /a "TOTAL_FAILURES=%%a"
    for /f "tokens=2 delims=<>" %%a in ('findstr "errors" "%FAILSAFE_REPORTS_DIR%\failsafe-summary.xml"') do set /a "TOTAL_ERRORS=%%a"
    for /f "tokens=2 delims=<>" %%a in ('findstr "skipped" "%FAILSAFE_REPORTS_DIR%\failsafe-summary.xml"') do set /a "TOTAL_SKIPPED=%%a"
)

set /a "TOTAL_PASSED=TOTAL_TESTS - TOTAL_FAILURES - TOTAL_ERRORS - TOTAL_SKIPPED"

echo --- SUMMARY --->> "%REPORT_FILE%"
echo Total tests:   !TOTAL_TESTS!>> "%REPORT_FILE%"
echo Passed:        !TOTAL_PASSED!>> "%REPORT_FILE%"
echo Failed:        !TOTAL_FAILURES!>> "%REPORT_FILE%"
echo Errors:        !TOTAL_ERRORS!>> "%REPORT_FILE%"
echo Skipped:       !TOTAL_SKIPPED!>> "%REPORT_FILE%"
echo.>> "%REPORT_FILE%"

REM --- Extract individual test results from text reports ---
echo --- DETAIL PER TEST --->> "%REPORT_FILE%"
if exist "%FAILSAFE_REPORTS_DIR%" (
    for %%f in (%FAILSAFE_REPORTS_DIR%\*.txt) do (
        findstr /c:"FAILED" "%%f" >nul 2>&1
        if not errorlevel 1 (
            echo   FAIL  %%~nf>> "%REPORT_FILE%"
            findstr /c:"FAILED" "%%f" >> "%REPORT_FILE%"
        ) else (
            findstr /c:"Tests run:" "%%f" >nul 2>&1
            if not errorlevel 1 (
                echo   PASS  %%~nf>> "%REPORT_FILE%"
            )
        )
    )
)

REM --- Final verdict ---
echo.>> "%REPORT_FILE%"
echo --- VERDICT --->> "%REPORT_FILE%"

if !MVN_EXIT! equ 0 if !TOTAL_FAILURES! equ 0 if !TOTAL_ERRORS! equ 0 (
    echo RESULT=SUCCESS>> "%REPORT_FILE%"
    echo All integration tests passed.>> "%REPORT_FILE%"
    goto :display
)

echo RESULT=FAILURE>> "%REPORT_FILE%"
if !MVN_EXIT! neq 0 echo Maven exited with code !MVN_EXIT!.>> "%REPORT_FILE%"
if !TOTAL_FAILURES! gtr 0 echo !TOTAL_FAILURES! test failure^(s^) detected.>> "%REPORT_FILE%"
if !TOTAL_ERRORS! gtr 0 echo !TOTAL_ERRORS! error^(s^) detected.>> "%REPORT_FILE%"
echo One or more rules did not behave as expected in SonarQube.>> "%REPORT_FILE%"

:display
REM --- Output summary to stdout ---
echo.
echo ========================================
type "%REPORT_FILE%"
echo ========================================
echo.
echo [IT-Runner] Full Maven output: %LOG_FILE%
echo [IT-Runner] Report: %REPORT_FILE%

REM --- Exit ---
if !TOTAL_FAILURES! gtr 0 exit /b 1
if !TOTAL_ERRORS! gtr 0 exit /b 1
if !MVN_EXIT! neq 0 exit /b 1
exit /b 0
