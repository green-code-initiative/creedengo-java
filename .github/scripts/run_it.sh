#!/bin/bash
# =============================================================================
# creedengo-java Integration Tests Runner
# =============================================================================
# This script runs the SonarQube integration tests using the Maven Failsafe
# plugin and the SonarQube Orchestrator. It downloads SonarQube automatically,
# installs the built plugin, analyses a test project, and verifies detected issues.
#
# Prerequisites:
#   - JDK 17+ (JDK 21 required for SonarQube >= 26.x)
#   - Maven wrapper (mvnw) available at project root
#   - Internet access (to download SonarQube on first run; cached in ~/.sonar/orchestrator)
#
# Usage: ./run_it.sh [--rule GCIXXX] [--keep-running] [--skip-build]
#
# Options:
#   --rule GCIXXX     Run only IT tests matching the given rule ID (e.g. GCI27)
#   --keep-running    Keep SonarQube running after tests (for manual inspection on port 33333)
#   --skip-build      Skip the clean+compile step (reuse existing target/ from a previous build)
#
# How it works:
#   This script uses the Maven lifecycle phase "verify" with -Dskip.unit.tests=true.
#   This property is defined in pom.xml and wired only to maven-surefire-plugin's
#   <skip> configuration, so unit tests (Surefire) are skipped while integration
#   tests (Failsafe) still run. All systemPropertyVariables defined in the pom.xml
#   for the maven-failsafe-plugin are correctly injected (orchestrator URL,
#   SonarQube version, plugin paths, etc.). Using standalone goals like
#   "failsafe:integration-test" would NOT inject those properties.
#
#   IMPORTANT: Do NOT use -DskipTests=true here. Since Maven Failsafe 3.x,
#   -DskipTests=true also skips integration tests, producing 0 tests run.
#   Use -Dskip.unit.tests=true instead (custom property wired to surefire only).
#
#   - Without --skip-build: runs "mvnw clean verify -Dskip.unit.tests=true"
#     (full clean build + IT)
#   - With --skip-build: runs "mvnw verify -Dskip.unit.tests=true" (no clean;
#     Maven detects compiled classes are up-to-date, repackages quickly, runs IT)
#
#   If review_pr.sh was already run, the target/ directory contains the JAR.
#   Using --skip-build avoids a full recompile (only shade+IT run, ~5s overhead).
#
# Outputs:
#   - it_report.txt          Structured text report with pass/fail per test
#   - it_build_output.log    Full Maven output of the IT run
#
# Exit codes:
#   0 = All IT tests passed
#   1 = One or more IT tests failed
#   2 = Build/infrastructure error (could not run tests)
# =============================================================================

set -uo pipefail

# --- Configuration ---
REPORT_FILE="it_report.txt"
LOG_FILE="it_build_output.log"
RULE_FILTER=""
KEEP_RUNNING=false
SKIP_BUILD=false
FAILSAFE_REPORTS_DIR="target/failsafe-reports"

# --- Parse arguments ---
while [[ $# -gt 0 ]]; do
    case "$1" in
        --rule) RULE_FILTER="$2"; shift 2 ;;
        --keep-running) KEEP_RUNNING=true; shift ;;
        --skip-build) SKIP_BUILD=true; shift ;;
        *) echo "Unknown option: $1"; exit 2 ;;
    esac
done

# --- Helper functions ---
log() { echo "[IT-Runner] $*"; }
error() { echo "[IT-Runner] ERROR: $*" >&2; }

# --- Validate environment ---
if ! command -v java &> /dev/null; then
    error "Java not found. JDK 17+ is required."
    exit 2
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | sed 's/.*"\([0-9]*\).*/\1/')
if [[ "$JAVA_VERSION" -lt 17 ]]; then
    error "JDK 17+ required, found JDK $JAVA_VERSION"
    exit 2
fi

if [[ ! -f "./mvnw" ]]; then
    error "Maven wrapper (mvnw) not found. Run from project root."
    exit 2
fi

# --- Check for existing JAR when --skip-build ---
if [[ "$SKIP_BUILD" == "true" ]]; then
    if ! ls target/creedengo-*.jar &> /dev/null; then
        error "No plugin JAR found in target/. Run without --skip-build first."
        exit 2
    fi
    log "Reusing existing JAR from target/."
fi

# --- Compose Maven command ---
# Use the Maven lifecycle "verify" so that all pom.xml systemPropertyVariables
# are correctly injected into the failsafe plugin execution.
# IMPORTANT: Do NOT use -DskipTests=true (skips failsafe too since 3.x).
MVN_CMD="verify -Dskip.unit.tests=true"

if [[ "$SKIP_BUILD" == "false" ]]; then
    MVN_CMD="clean $MVN_CMD"
fi

if [[ "$KEEP_RUNNING" == "true" ]]; then
    MVN_CMD="$MVN_CMD -Dtest-it.sonarqube.keepRunning=true"
    log "SonarQube will remain running after tests."
fi

if [[ -n "$RULE_FILTER" ]]; then
    MVN_CMD="$MVN_CMD -Dit.test=GCIRulesIT#test${RULE_FILTER}*"
    log "Filtering tests for rule: $RULE_FILTER"
fi

# --- Run integration tests ---
log "Running integration tests..."
log "Command: ./mvnw $MVN_CMD"

./mvnw $MVN_CMD 2>&1 | tee "$LOG_FILE"
MVN_EXIT=${PIPESTATUS[0]}

# --- Step 4: Parse results ---
log "Parsing test results..."

# Initialize report
{
    echo "============================================================"
    echo "  CREEDENGO-JAVA INTEGRATION TESTS REPORT"
    echo "  Date: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "  Rule filter: ${RULE_FILTER:-none (all rules)}"
    echo "  SonarQube version: $(grep -m1 'test-it.sonarqube.version' pom.xml | grep -v '<!--' | sed 's/.*>\(.*\)<.*/\1/' || echo 'unknown')"
    echo "============================================================"
    echo ""
} > "$REPORT_FILE"

# Parse Failsafe XML reports
TOTAL_TESTS=0
TOTAL_FAILURES=0
TOTAL_ERRORS=0
TOTAL_SKIPPED=0

if [[ -d "$FAILSAFE_REPORTS_DIR" ]]; then
    # Find all XML test result files
    for xml_file in "$FAILSAFE_REPORTS_DIR"/TEST-*.xml; do
        [[ -f "$xml_file" ]] || continue

        # Extract summary from XML attributes
        tests=$(grep -oP 'tests="\K[0-9]+' "$xml_file" | head -1 || echo 0)
        failures=$(grep -oP 'failures="\K[0-9]+' "$xml_file" | head -1 || echo 0)
        errors=$(grep -oP 'errors="\K[0-9]+' "$xml_file" | head -1 || echo 0)
        skipped=$(grep -oP 'skipped="\K[0-9]+' "$xml_file" | head -1 || echo 0)

        TOTAL_TESTS=$((TOTAL_TESTS + tests))
        TOTAL_FAILURES=$((TOTAL_FAILURES + failures))
        TOTAL_ERRORS=$((TOTAL_ERRORS + errors))
        TOTAL_SKIPPED=$((TOTAL_SKIPPED + skipped))
    done

    # Extract individual test results
    {
        echo "--- SUMMARY ---"
        echo "Total tests:   $TOTAL_TESTS"
        echo "Passed:        $((TOTAL_TESTS - TOTAL_FAILURES - TOTAL_ERRORS - TOTAL_SKIPPED))"
        echo "Failed:        $TOTAL_FAILURES"
        echo "Errors:        $TOTAL_ERRORS"
        echo "Skipped:       $TOTAL_SKIPPED"
        echo ""
        echo "--- DETAIL PER TEST ---"
    } >> "$REPORT_FILE"

    # Parse individual testcase results from XML
    for xml_file in "$FAILSAFE_REPORTS_DIR"/TEST-*.xml; do
        [[ -f "$xml_file" ]] || continue

        # Extract test method names and their status
        while IFS= read -r line; do
            if echo "$line" | grep -q '<testcase '; then
                test_name=$(echo "$line" | grep -oP 'name="\K[^"]+')
                test_class=$(echo "$line" | grep -oP 'classname="\K[^"]+')
                # Check if this testcase has a failure/error child
                if echo "$line" | grep -q '/>'; then
                    # Self-closing = passed
                    echo "  PASS  ${test_class}#${test_name}" >> "$REPORT_FILE"
                fi
            elif echo "$line" | grep -q '</testcase>'; then
                # Has children — check what came before
                :
            elif echo "$line" | grep -q '<failure'; then
                failure_msg=$(echo "$line" | grep -oP 'message="\K[^"]*' | head -1)
                echo "  FAIL  ${test_class}#${test_name} — ${failure_msg}" >> "$REPORT_FILE"
            elif echo "$line" | grep -q '<error'; then
                error_msg=$(echo "$line" | grep -oP 'message="\K[^"]*' | head -1)
                echo "  ERROR ${test_class}#${test_name} — ${error_msg}" >> "$REPORT_FILE"
            fi
        done < "$xml_file"
    done
else
    {
        echo "--- SUMMARY ---"
        echo "ERROR: No failsafe reports found in $FAILSAFE_REPORTS_DIR"
        echo "The integration tests may not have run at all."
    } >> "$REPORT_FILE"
fi

# --- Step 5: Final verdict ---
{
    echo ""
    echo "--- VERDICT ---"
    if [[ $MVN_EXIT -eq 0 && $TOTAL_FAILURES -eq 0 && $TOTAL_ERRORS -eq 0 ]]; then
        echo "RESULT=SUCCESS"
        echo "All integration tests passed. The rule(s) are correctly integrated in SonarQube."
    else
        echo "RESULT=FAILURE"
        if [[ $MVN_EXIT -ne 0 ]]; then
            echo "Maven exited with code $MVN_EXIT."
        fi
        if [[ $TOTAL_FAILURES -gt 0 || $TOTAL_ERRORS -gt 0 ]]; then
            echo "$TOTAL_FAILURES failure(s), $TOTAL_ERRORS error(s) detected."
            echo "One or more rules did not behave as expected in SonarQube."
        fi
    fi
} >> "$REPORT_FILE"

# --- Output summary to stdout ---
echo ""
echo "========================================"
cat "$REPORT_FILE"
echo "========================================"
echo ""
log "Full Maven output: $LOG_FILE"
log "Report: $REPORT_FILE"

# --- Exit ---
if [[ $TOTAL_FAILURES -gt 0 || $TOTAL_ERRORS -gt 0 || $MVN_EXIT -ne 0 ]]; then
    exit 1
fi
exit 0
