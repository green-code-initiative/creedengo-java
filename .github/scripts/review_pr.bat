@echo off
REM =============================================================================
REM creedengo-java PR Review - Automated Checks Script (Windows)
REM =============================================================================
REM This script performs deterministic checks on a creedengo-java PR branch.
REM It expects to be run from the root of the creedengo-java repository,
REM on the branch of the PR to review.
REM
REM Usage: review_pr.bat [--skip-build] [--pr-number N] [--base-branch branch]
REM
REM Options:
REM   --skip-build      [DEBUG ONLY] Skip Maven build and tests (for script debugging)
REM   --pr-number N     PR number (for report metadata)
REM   --base-branch b   Base branch to diff against (default: main)
REM
REM Exit codes:
REM   0 = All checks passed
REM   1 = Critical issues found
REM   2 = Warnings found (non-blocking)
REM =============================================================================

setlocal enabledelayedexpansion

REM --- Configuration ---
set "REPORT_FILE=pr_review_report.txt"
set "BASE_BRANCH=main"
set "SKIP_BUILD=false"
set "PR_NUMBER="
set "CRITICAL_COUNT=0"
set "WARNING_COUNT=0"
set "INFO_COUNT=0"

REM --- Parse arguments ---
:parse_args
if "%~1"=="" goto :end_parse
if "%~1"=="--skip-build" (set "SKIP_BUILD=true" & shift & goto :parse_args)
if "%~1"=="--pr-number" (set "PR_NUMBER=%~2" & shift & shift & goto :parse_args)
if "%~1"=="--base-branch" (set "BASE_BRANCH=%~2" & shift & shift & goto :parse_args)
shift
goto :parse_args
:end_parse

REM --- Initialize report ---
echo. > "%REPORT_FILE%"
echo ============================================================>> "%REPORT_FILE%"
echo   CREEDENGO-JAVA PR AUTOMATED REVIEW REPORT>> "%REPORT_FILE%"
echo   Date: %date% %time%>> "%REPORT_FILE%"
echo   PR: %PR_NUMBER%>> "%REPORT_FILE%"
echo   Base branch: %BASE_BRANCH%>> "%REPORT_FILE%"
echo ============================================================>> "%REPORT_FILE%"
echo.>> "%REPORT_FILE%"

REM --- Verify we're in the right repository ---
if not exist "pom.xml" (
    echo [CRITICAL] Not in creedengo-java root directory ^(pom.xml not found^)
    echo [CRITICAL] Not in creedengo-java root directory>> "%REPORT_FILE%"
    set /a CRITICAL_COUNT+=1
    goto :summary
)

findstr /c:"creedengo-java-plugin" pom.xml >nul 2>&1
if errorlevel 1 (
    echo [CRITICAL] pom.xml does not appear to be creedengo-java
    echo [CRITICAL] pom.xml does not appear to be creedengo-java>> "%REPORT_FILE%"
    set /a CRITICAL_COUNT+=1
    goto :summary
)

REM =============================================================================
REM CHANGED FILES DETECTION
REM =============================================================================
echo.
echo === CHANGED FILES DETECTION ===
echo.>> "%REPORT_FILE%"
echo === CHANGED FILES DETECTION ===>> "%REPORT_FILE%"

git diff --name-only "%BASE_BRANCH%"...HEAD > changed_files.tmp 2>nul
if errorlevel 1 (
    git diff --name-only "origin/%BASE_BRANCH%"...HEAD > changed_files.tmp 2>nul
)

REM --- Sanitize line endings in changed_files.tmp (git may output LF-only) ---
if exist changed_files.tmp (
    move /y changed_files.tmp changed_files_raw.tmp >nul 2>&1
    for /f "tokens=*" %%L in (changed_files_raw.tmp) do (
        echo %%L>> changed_files.tmp
    )
    del changed_files_raw.tmp >nul 2>&1
)

REM --- Identify changed Java source files and rule IDs ---
set "RULE_IDS="
set "NEW_CHECK_FILES="
set "HAS_CHANGELOG=false"

for /f "tokens=*" %%f in (changed_files.tmp) do (
    echo   - %%f>> "%REPORT_FILE%"
    
    REM Check if CHANGELOG is modified
    echo %%f | findstr /c:"CHANGELOG.md" >nul 2>&1 && set "HAS_CHANGELOG=true"
    
    REM Check if it's a source Java file
    echo %%f | findstr /b /c:"src/main/java/" | findstr /c:".java" >nul 2>&1
    if not errorlevel 1 (
        if exist "%%f" (
            findstr /c:"@Rule" "%%f" >nul 2>&1
            if not errorlevel 1 (
                set "NEW_CHECK_FILES=!NEW_CHECK_FILES! %%f"
                for /f "tokens=2 delims==" %%r in ('findstr /r /c:"@Rule(key.*GCI" "%%f" 2^>nul') do (
                    for /f "tokens=1 delims=^)" %%i in ("%%r") do (
                        set "RULE_ID=%%i"
                        set "RULE_ID=!RULE_ID:"=!"
                        set "RULE_IDS=!RULE_IDS! !RULE_ID!"
                    )
                )
            )
        )
    )
)

echo Detected rules: %RULE_IDS%
echo Detected rules: %RULE_IDS%>> "%REPORT_FILE%"

REM =============================================================================
REM CHECK 1: LICENSE HEADER
REM =============================================================================
echo.
echo === CHECK 1: LICENSE HEADERS ===
echo.>> "%REPORT_FILE%"
echo === CHECK 1: LICENSE HEADERS ===>> "%REPORT_FILE%"

set "LICENSE_ISSUES=0"
for /f "tokens=*" %%f in ('findstr /b /c:"src/main/java/" changed_files.tmp 2^>nul') do (
    REM Convert forward slashes to backslashes (type command requires backslashes)
    set "LFILE=%%f"
    set "LFILE=!LFILE:/=\!"
    if exist "!LFILE!" (
        type "!LFILE!" 2>nul | findstr /c:"creedengo - Java language" >nul 2>&1
        if errorlevel 1 (
            echo [CRITICAL] Missing license header: %%f
            echo [CRITICAL] Missing license header: %%f>> "%REPORT_FILE%"
            set /a CRITICAL_COUNT+=1
            set /a LICENSE_ISSUES+=1
        )
    )
)
if %LICENSE_ISSUES% equ 0 (
    echo [OK] All source files have correct license headers
    echo [OK] All source files have correct license headers>> "%REPORT_FILE%"
    set /a INFO_COUNT+=1
)

REM =============================================================================
REM CHECK 2: CHANGELOG.md UPDATED
REM =============================================================================
echo.
echo === CHECK 2: CHANGELOG.md ===
echo.>> "%REPORT_FILE%"
echo === CHECK 2: CHANGELOG.md ===>> "%REPORT_FILE%"

if "%HAS_CHANGELOG%"=="true" (
    echo [OK] CHANGELOG.md has been modified
    echo [OK] CHANGELOG.md has been modified>> "%REPORT_FILE%"
    set /a INFO_COUNT+=1
) else (
    echo [CRITICAL] CHANGELOG.md has NOT been modified
    echo [CRITICAL] CHANGELOG.md has NOT been modified>> "%REPORT_FILE%"
    set /a CRITICAL_COUNT+=1
)

REM =============================================================================
REM CHECK 3: RULE REGISTRATION (JavaCheckRegistrar)
REM =============================================================================
echo.
echo === CHECK 3: RULE REGISTRATION ===
echo.>> "%REPORT_FILE%"
echo === CHECK 3: RULE REGISTRATION ===>> "%REPORT_FILE%"

set "REGISTRAR_FILE=src\main\java\org\greencodeinitiative\creedengo\java\JavaCheckRegistrar.java"

if defined NEW_CHECK_FILES (
    for %%f in (%NEW_CHECK_FILES%) do (
        for %%n in (%%~nf) do (
            findstr /c:"%%n" "%REGISTRAR_FILE%" >nul 2>&1
            if errorlevel 1 (
                echo [CRITICAL] Rule class '%%n' is NOT registered in JavaCheckRegistrar
                echo [CRITICAL] Rule class '%%n' is NOT registered in JavaCheckRegistrar>> "%REPORT_FILE%"
                set /a CRITICAL_COUNT+=1
            ) else (
                echo [OK] Rule class '%%n' is registered in JavaCheckRegistrar
                echo [OK] Rule class '%%n' is registered in JavaCheckRegistrar>> "%REPORT_FILE%"
                set /a INFO_COUNT+=1
            )
        )
    )
) else (
    echo [OK] No new rule classes detected ^(registration check skipped^)
    echo [OK] No new rule classes detected>> "%REPORT_FILE%"
    set /a INFO_COUNT+=1
)

REM =============================================================================
REM CHECK 4: QUALITY PROFILE (creedengo_way_profile.json)
REM =============================================================================
echo.
echo === CHECK 4: QUALITY PROFILE ===
echo.>> "%REPORT_FILE%"
echo === CHECK 4: QUALITY PROFILE ===>> "%REPORT_FILE%"

set "PROFILE_FILE="
for /f "tokens=*" %%p in ('dir /b /s src\main\resources\*creedengo_way_profile.json 2^>nul') do set "PROFILE_FILE=%%p"

if defined PROFILE_FILE (
    for %%r in (%RULE_IDS%) do (
        findstr /c:"\"%%r\"" "%PROFILE_FILE%" >nul 2>&1
        if errorlevel 1 (
            echo [CRITICAL] Rule '%%r' is NOT in creedengo_way_profile.json
            echo [CRITICAL] Rule '%%r' is NOT in creedengo_way_profile.json>> "%REPORT_FILE%"
            set /a CRITICAL_COUNT+=1
        ) else (
            echo [OK] Rule '%%r' is listed in creedengo_way_profile.json
            echo [OK] Rule '%%r' is listed in creedengo_way_profile.json>> "%REPORT_FILE%"
            set /a INFO_COUNT+=1
        )
    )
)

REM =============================================================================
REM CHECK 5: UNIT TESTS PRESENT
REM =============================================================================
echo.
echo === CHECK 5: UNIT TESTS ===
echo.>> "%REPORT_FILE%"
echo === CHECK 5: UNIT TESTS ===>> "%REPORT_FILE%"

for %%r in (%RULE_IDS%) do (
    set "TEST_FOUND=false"
    for /f "tokens=*" %%t in ('dir /b /s "src\test\java\*%%r*" 2^>nul') do set "TEST_FOUND=true"
    if "!TEST_FOUND!"=="true" (
        echo [OK] Unit test directory/file found for %%r
        echo [OK] Unit test found for %%r>> "%REPORT_FILE%"
        set /a INFO_COUNT+=1
    ) else (
        echo [CRITICAL] No unit test found for rule %%r
        echo [CRITICAL] No unit test found for %%r>> "%REPORT_FILE%"
        set /a CRITICAL_COUNT+=1
    )
)

REM =============================================================================
REM CHECK 6: INTEGRATION TEST (GCIRulesIT)
REM =============================================================================
echo.
echo === CHECK 6: INTEGRATION TEST ===
echo.>> "%REPORT_FILE%"
echo === CHECK 6: INTEGRATION TEST ===>> "%REPORT_FILE%"

set "IT_FILE="
for /f "tokens=*" %%p in ('dir /b /s "src\it\*GCIRulesIT.java" 2^>nul') do set "IT_FILE=%%p"

if defined IT_FILE (
    for %%r in (%RULE_IDS%) do (
        findstr /c:"creedengo-java:%%r" "%IT_FILE%" >nul 2>&1
        if errorlevel 1 (
            echo [CRITICAL] No integration test for %%r in GCIRulesIT.java
            echo [CRITICAL] No integration test for %%r in GCIRulesIT.java>> "%REPORT_FILE%"
            set /a CRITICAL_COUNT+=1
        ) else (
            echo [OK] Integration test found for %%r
            echo [OK] Integration test found for %%r>> "%REPORT_FILE%"
            set /a INFO_COUNT+=1
        )
    )
) else (
    if defined RULE_IDS (
        echo [WARNING] GCIRulesIT.java not found
        echo [WARNING] GCIRulesIT.java not found>> "%REPORT_FILE%"
        set /a WARNING_COUNT+=1
    )
)

REM =============================================================================
REM CHECK 7: CODE STYLE - var usage
REM =============================================================================
echo.
echo === CHECK 7: CODE STYLE ===
echo.>> "%REPORT_FILE%"
echo === CHECK 7: CODE STYLE ===>> "%REPORT_FILE%"

set "STYLE_ISSUES=0"
for /f "tokens=*" %%f in ('findstr /b /c:"src/main/java/" changed_files.tmp 2^>nul') do (
    if exist "%%f" (
        REM Check for 'var' usage
        findstr /n /r "^\s*var " "%%f" >nul 2>&1
        if not errorlevel 1 (
            echo [WARNING] Usage of 'var' detected in %%f ^(forbidden by CODE_STYLE^)
            echo [WARNING] var usage in %%f>> "%REPORT_FILE%"
            set /a WARNING_COUNT+=1
            set /a STYLE_ISSUES+=1
        )

        REM Check extends IssuableSubscriptionVisitor for @Rule files
        findstr /c:"@Rule" "%%f" >nul 2>&1
        if not errorlevel 1 (
            findstr /c:"extends IssuableSubscriptionVisitor" "%%f" >nul 2>&1
            if errorlevel 1 (
                echo [WARNING] %%f does not extend IssuableSubscriptionVisitor
                echo [WARNING] %%f does not extend IssuableSubscriptionVisitor>> "%REPORT_FILE%"
                set /a WARNING_COUNT+=1
                set /a STYLE_ISSUES+=1
            )
        )
    )
)

if %STYLE_ISSUES% equ 0 (
    echo [OK] No code style violations detected
    echo [OK] No code style violations detected>> "%REPORT_FILE%"
    set /a INFO_COUNT+=1
)

REM =============================================================================
REM CHECK 8: TEST FILES IN CORRECT LOCATION (not old src/test/files/)
REM =============================================================================
echo.
echo === CHECK 8: TEST FILES LOCATION ===
echo.>> "%REPORT_FILE%"
echo === CHECK 8: TEST FILES LOCATION ===>> "%REPORT_FILE%"

REM Real reviewer feedback (PR #103, #106): test files should be in the NEW location
set "OLD_LOCATION_ISSUES=0"
findstr /b /c:"src/test/files/" changed_files.tmp >nul 2>&1
if not errorlevel 1 (
    echo [CRITICAL] Test files added in DEPRECATED location 'src/test/files/'
    echo [CRITICAL] Test files in deprecated src/test/files/ location>> "%REPORT_FILE%"
    echo   Move to src/it/test-projects/.../checks/GCI{N}/ instead>> "%REPORT_FILE%"
    set /a CRITICAL_COUNT+=1
    set /a OLD_LOCATION_ISSUES+=1
)
if %OLD_LOCATION_ISSUES% equ 0 (
    echo [OK] No test files in deprecated location
    echo [OK] No test files in deprecated location>> "%REPORT_FILE%"
    set /a INFO_COUNT+=1
)

REM =============================================================================
REM CHECK 9: RULE ID CONSISTENCY ACROSS FILES
REM =============================================================================
echo.
echo === CHECK 9: RULE ID CONSISTENCY ===
echo.>> "%REPORT_FILE%"
echo === CHECK 9: RULE ID CONSISTENCY ===>> "%REPORT_FILE%"

REM Real reviewer feedback (PR #106): Rule ID must be consistent everywhere
for %%r in (%RULE_IDS%) do (
    REM Check CHANGELOG mentions the rule
    if "%HAS_CHANGELOG%"=="true" (
        findstr /c:"%%r" CHANGELOG.md >nul 2>&1
        if errorlevel 1 (
            echo [WARNING] CHANGELOG.md does not mention rule %%r
            echo [WARNING] CHANGELOG.md does not mention rule %%r>> "%REPORT_FILE%"
            set /a WARNING_COUNT+=1
        ) else (
            echo [OK] Rule %%r mentioned in CHANGELOG.md
            echo [OK] Rule %%r mentioned in CHANGELOG.md>> "%REPORT_FILE%"
            set /a INFO_COUNT+=1
        )
    )
)

REM =============================================================================
REM CHECK 10: TEST FILE PACKAGES
REM =============================================================================
echo.
echo === CHECK 10: TEST FILE PACKAGES ===
echo.>> "%REPORT_FILE%"
echo === CHECK 10: TEST FILE PACKAGES ===>> "%REPORT_FILE%"

REM Real reviewer feedback (PR #103): compile errors due to wrong/missing packages
REM Note: IT test files intentionally may use a parent package (omitting GCI{N} subdir)
set "PKG_ISSUES=0"
for /f "tokens=*" %%f in ('findstr /b /c:"src/it/test-projects/" changed_files.tmp 2^>nul ^| findstr /c:".java"') do (
    set "ITFILE=%%f"
    set "ITFILE=!ITFILE:/=\!"
    if exist "!ITFILE!" (
        type "!ITFILE!" 2>nul | findstr /b /c:"package " >nul 2>&1
        if errorlevel 1 (
            echo [WARNING] Missing package declaration in %%f
            echo [WARNING] Missing package declaration in %%f>> "%REPORT_FILE%"
            set /a WARNING_COUNT+=1
            set /a PKG_ISSUES+=1
        )
        REM Check class name matches filename using a flag
        set "CLASS_FOUND=0"
        for %%n in (%%~nf) do (
            type "!ITFILE!" 2>nul | findstr /c:"class %%n" >nul 2>&1
            if not errorlevel 1 set "CLASS_FOUND=1"
            if "!CLASS_FOUND!"=="0" (
                type "!ITFILE!" 2>nul | findstr /c:"interface %%n" >nul 2>&1
                if not errorlevel 1 set "CLASS_FOUND=1"
            )
            if "!CLASS_FOUND!"=="0" (
                type "!ITFILE!" 2>nul | findstr /c:"enum %%n" >nul 2>&1
                if not errorlevel 1 set "CLASS_FOUND=1"
            )
            if "!CLASS_FOUND!"=="0" (
                echo [WARNING] Class name does not match filename in %%f
                echo [WARNING] Class/filename mismatch in %%f>> "%REPORT_FILE%"
                set /a WARNING_COUNT+=1
                set /a PKG_ISSUES+=1
            )
        )
    )
)
if %PKG_ISSUES% equ 0 (
    echo [OK] All test file packages and class names correct
    echo [OK] All test file packages and class names correct>> "%REPORT_FILE%"
    set /a INFO_COUNT+=1
)

REM =============================================================================
REM CHECK 11: RULES-SPECIFICATIONS REFERENCE
REM =============================================================================
echo.
echo === CHECK 11: RULES-SPECS REFERENCE ===
echo.>> "%REPORT_FILE%"
echo === CHECK 11: RULES-SPECS REFERENCE ===>> "%REPORT_FILE%"

REM Real reviewer feedback (PR #103, #106): always reference rules-specifications
if defined RULE_IDS (
    git log "%BASE_BRANCH%"..HEAD --oneline 2>nul | findstr /i "rules-spec creedengo-rules-spec" >nul 2>&1
    if errorlevel 1 (
        echo [WARNING] No reference to creedengo-rules-specifications in commits
        echo [WARNING] No creedengo-rules-specifications reference in commits>> "%REPORT_FILE%"
        set /a WARNING_COUNT+=1
    ) else (
        echo [OK] Reference to creedengo-rules-specifications found
        echo [OK] creedengo-rules-specifications reference found>> "%REPORT_FILE%"
        set /a INFO_COUNT+=1
    )
)

REM =============================================================================
REM CHECK 12: MERGE CONFLICT MARKERS
REM =============================================================================
echo.
echo === CHECK 12: MERGE CONFLICT MARKERS ===
echo.>> "%REPORT_FILE%"
echo === CHECK 12: MERGE CONFLICT MARKERS ===>> "%REPORT_FILE%"

REM Real reviewer feedback (PR #105): utarwyn asked "Can you correct the conflict in the CHANGELOG file?"
set "CONFLICT_ISSUES=0"
for /f "tokens=*" %%f in (changed_files.tmp) do (
    if exist "%%f" (
        findstr /b /c:"<<<<<<< " "%%f" >nul 2>&1
        if not errorlevel 1 (
            echo [CRITICAL] Merge conflict markers found in %%f
            echo [CRITICAL] Merge conflict markers found in %%f>> "%REPORT_FILE%"
            set /a CRITICAL_COUNT+=1
            set /a CONFLICT_ISSUES+=1
        )
    )
)
if %CONFLICT_ISSUES% equ 0 (
    echo [OK] No merge conflict markers found
    echo [OK] No merge conflict markers found>> "%REPORT_FILE%"
    set /a INFO_COUNT+=1
)

REM =============================================================================
REM CHECK 13: BRANCH UP-TO-DATE
REM =============================================================================
echo.
echo === CHECK 13: BRANCH UP-TO-DATE ===
echo.>> "%REPORT_FILE%"
echo === CHECK 13: BRANCH UP-TO-DATE ===>> "%REPORT_FILE%"

REM Real reviewer feedback (PR #44): utarwyn asked "Can you rebase your branch on the main branch?"
set "BEHIND_COUNT=0"
for /f %%c in ('git rev-list --count "HEAD..%BASE_BRANCH%" 2^>nul') do set "BEHIND_COUNT=%%c"
if %BEHIND_COUNT% equ 0 (
    for /f %%c in ('git rev-list --count "HEAD..origin/%BASE_BRANCH%" 2^>nul') do set "BEHIND_COUNT=%%c"
)
if %BEHIND_COUNT% gtr 0 (
    echo [WARNING] Branch is %BEHIND_COUNT% commit^(s^) behind %BASE_BRANCH% ^(rebase may be needed^)
    echo [WARNING] Branch is %BEHIND_COUNT% commit^(s^) behind %BASE_BRANCH%>> "%REPORT_FILE%"
    set /a WARNING_COUNT+=1
) else (
    echo [OK] Branch is up-to-date with %BASE_BRANCH%
    echo [OK] Branch is up-to-date with %BASE_BRANCH%>> "%REPORT_FILE%"
    set /a INFO_COUNT+=1
)

REM =============================================================================
REM CHECK 14: @DeprecatedRuleKey FOR RENAMES
REM =============================================================================
echo.
echo === CHECK 14: @DeprecatedRuleKey ===
echo.>> "%REPORT_FILE%"
echo === CHECK 14: @DeprecatedRuleKey ===>> "%REPORT_FILE%"

REM Real reviewer feedback (PR #75): @DeprecatedRuleKey mandatory when renaming ECXXX to GCIXXX
for %%f in (%NEW_CHECK_FILES%) do (
    if exist "%%f" (
        for /f "tokens=*" %%k in ('findstr /r "Rule.*key.*GCI" "%%f" 2^>nul') do (
            findstr /c:"@DeprecatedRuleKey" "%%f" >nul 2>&1
            if errorlevel 1 (
                echo [WARNING] %%f has @Rule with GCI key but no @DeprecatedRuleKey ^(check if EC predecessor existed^)
                echo [WARNING] %%f missing @DeprecatedRuleKey>> "%REPORT_FILE%"
                set /a WARNING_COUNT+=1
            ) else (
                echo [OK] %%f has @DeprecatedRuleKey for backward compatibility
                echo [OK] %%f has @DeprecatedRuleKey>> "%REPORT_FILE%"
                set /a INFO_COUNT+=1
            )
        )
    )
)

REM =============================================================================
REM CHECK 15: BUILD AND TESTS
REM =============================================================================
echo.
echo === CHECK 15: BUILD AND TESTS ===
echo.>> "%REPORT_FILE%"
echo === CHECK 15: BUILD AND TESTS ===>> "%REPORT_FILE%"

if "%SKIP_BUILD%"=="true" (
    echo [WARNING] Build skipped ^(--skip-build flag^)
    echo [WARNING] Build skipped>> "%REPORT_FILE%"
    set /a WARNING_COUNT+=1
    goto :summary
)

echo Running Maven build (this may take a few minutes)...
call mvnw.cmd -e -B verify -DskipITs=true > build_output.log 2>&1
set "BUILD_RESULT=!errorlevel!"

if !BUILD_RESULT! neq 0 (
    echo [CRITICAL] Maven build FAILED
    echo [CRITICAL] Maven build FAILED>> "%REPORT_FILE%"
    set /a CRITICAL_COUNT+=1
    echo   See build_output.log for details>> "%REPORT_FILE%"
    REM Extract error lines for the report
    findstr /c:"[ERROR]" build_output.log >> "%REPORT_FILE%" 2>nul
) else (
    echo [OK] Maven build SUCCESS ^(unit tests passed^)
    echo [OK] Maven build SUCCESS>> "%REPORT_FILE%"
    set /a INFO_COUNT+=1
    REM Extract test summary
    for /f "tokens=*" %%s in ('findstr /c:"Tests run:" build_output.log 2^>nul') do (
        echo   %%s
        echo   %%s>> "%REPORT_FILE%"
    )
)

REM --- Integration tests (skip if unit build failed) ---
if !BUILD_RESULT! neq 0 goto :summary
echo.
echo NOTE: Integration tests are NOT run by this script ^(require SonarQube Orchestrator^).
echo NOTE: IT tests skipped ^(require SonarQube env^)>> "%REPORT_FILE%"
echo To run IT tests: .github\scripts\run_it.bat --skip-build [--rule GCIXXX]>> "%REPORT_FILE%"

REM =============================================================================
REM SUMMARY
REM =============================================================================
:summary
echo.
echo.>> "%REPORT_FILE%"
echo === SUMMARY ===>> "%REPORT_FILE%"
echo Critical issues: %CRITICAL_COUNT%>> "%REPORT_FILE%"
echo Warnings: %WARNING_COUNT%>> "%REPORT_FILE%"
echo Passed checks: %INFO_COUNT%>> "%REPORT_FILE%"

echo.
echo =====================================
echo   Review complete.
echo   Critical: %CRITICAL_COUNT%
echo   Warnings: %WARNING_COUNT%
echo   Passed:   %INFO_COUNT%
echo =====================================
echo.
echo Full report saved to: %REPORT_FILE%

REM --- Cleanup ---
if exist changed_files.tmp del changed_files.tmp

REM --- Exit code ---
if %CRITICAL_COUNT% gtr 0 (
    echo RESULT: CRITICAL ISSUES FOUND - PR should NOT be merged>> "%REPORT_FILE%"
    exit /b 1
)
if %WARNING_COUNT% gtr 0 (
    echo RESULT: WARNINGS FOUND - Review needed>> "%REPORT_FILE%"
    exit /b 2
)
echo RESULT: ALL AUTOMATED CHECKS PASSED>> "%REPORT_FILE%"
exit /b 0
