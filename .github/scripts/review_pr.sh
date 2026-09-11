#!/bin/bash
# =============================================================================
# creedengo-java PR Review - Automated Checks Script
# =============================================================================
# This script performs deterministic checks on a creedengo-java PR branch.
# It expects to be run from the root of the creedengo-java repository,
# on the branch of the PR to review.
#
# Usage: ./review_pr.sh [--skip-build] [--pr-number <N>] [--base-branch <branch>]
#
# Options:
#   --skip-build      [DEBUG ONLY] Skip Maven build (for script debugging only)
#   --pr-number <N>   PR number (for report metadata)
#   --base-branch <b> Base branch to diff against (default: main)
#
# Exit codes:
#   0 = All checks passed
#   1 = Critical issues found
#   2 = Warnings found (non-blocking)
# =============================================================================

set -euo pipefail

# --- Configuration ---
REPORT_FILE="pr_review_report.txt"
BASE_BRANCH="main"
SKIP_BUILD=false
PR_NUMBER=""
CRITICAL_COUNT=0
WARNING_COUNT=0
INFO_COUNT=0

# --- Colors (for terminal output) ---
RED='\033[0;31m'
YELLOW='\033[0;33m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# --- Parse arguments ---
while [[ $# -gt 0 ]]; do
    case "$1" in
        --skip-build) SKIP_BUILD=true; shift ;;
        --pr-number) PR_NUMBER="$2"; shift 2 ;;
        --base-branch) BASE_BRANCH="$2"; shift 2 ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

# --- Helper functions ---
report() {
    echo "$1" >> "$REPORT_FILE"
}

critical() {
    echo -e "${RED}[CRITICAL]${NC} $1"
    report "[CRITICAL] $1"
    CRITICAL_COUNT=$((CRITICAL_COUNT + 1))
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
    report "[WARNING] $1"
    WARNING_COUNT=$((WARNING_COUNT + 1))
}

info() {
    echo -e "${GREEN}[OK]${NC} $1"
    report "[OK] $1"
    INFO_COUNT=$((INFO_COUNT + 1))
}

section() {
    echo ""
    echo -e "${BLUE}=== $1 ===${NC}"
    echo ""
    report ""
    report "=== $1 ==="
    report ""
}

# --- Initialize report ---
echo "" > "$REPORT_FILE"
report "============================================================"
report "  CREEDENGO-JAVA PR AUTOMATED REVIEW REPORT"
report "  Date: $(date '+%Y-%m-%d %H:%M:%S')"
report "  PR: ${PR_NUMBER:-N/A}"
report "  Base branch: $BASE_BRANCH"
report "============================================================"
report ""

# --- Verify we're in the right repository ---
if [[ ! -f "pom.xml" ]]; then
    critical "Not in creedengo-java root directory (pom.xml not found)"
    exit 1
fi

if ! grep -q "creedengo-java-plugin" pom.xml 2>/dev/null; then
    critical "pom.xml does not appear to be creedengo-java (artifactId mismatch)"
    exit 1
fi

# --- Get list of changed files ---
section "CHANGED FILES DETECTION"

if git rev-parse --verify "$BASE_BRANCH" >/dev/null 2>&1; then
    CHANGED_FILES=$(git diff --name-only "$BASE_BRANCH"...HEAD 2>/dev/null || git diff --name-only "$BASE_BRANCH" HEAD 2>/dev/null || echo "")
else
    # If base branch doesn't exist locally, try origin
    CHANGED_FILES=$(git diff --name-only "origin/$BASE_BRANCH"...HEAD 2>/dev/null || echo "")
fi

if [[ -z "$CHANGED_FILES" ]]; then
    warning "Could not determine changed files. Will analyze all source files."
    CHANGED_FILES=$(find src -name "*.java" -type f 2>/dev/null || echo "")
fi

report "Changed files:"
echo "$CHANGED_FILES" | while read -r f; do report "  - $f"; done

# --- Extract rule IDs from changed files ---
RULE_IDS=""
NEW_CHECK_FILES=""
CHANGED_JAVA_SRC=$(echo "$CHANGED_FILES" | grep "^src/main/java/.*\.java$" || true)
CHANGED_TEST_FILES=$(echo "$CHANGED_FILES" | grep "^src/test/\|^src/it/" || true)

# Find @Rule annotations in changed files
for f in $CHANGED_JAVA_SRC; do
    if [[ -f "$f" ]] && grep -q '@Rule' "$f" 2>/dev/null; then
        NEW_CHECK_FILES="$NEW_CHECK_FILES $f"
        RULE_ID=$(grep -oP '@Rule\(key\s*=\s*"(GCI\d+)"' "$f" | grep -oP 'GCI\d+' || true)
        if [[ -n "$RULE_ID" ]]; then
            RULE_IDS="$RULE_IDS $RULE_ID"
        fi
    fi
done

RULE_IDS=$(echo "$RULE_IDS" | tr ' ' '\n' | sort -u | tr '\n' ' ' | xargs)
report "Detected rule IDs: ${RULE_IDS:-none}"
report "New/modified check files: ${NEW_CHECK_FILES:-none}"

# =============================================================================
# CHECK 1: LICENSE HEADER
# =============================================================================
section "CHECK 1: LICENSE HEADERS"

LICENSE_PATTERN="creedengo - Java language"
LICENSE_ISSUES=0

for f in $CHANGED_JAVA_SRC; do
    if [[ -f "$f" ]]; then
        # Only check src/main/java files (not test-projects)
        if [[ "$f" == src/main/java/* ]]; then
            if ! head -20 "$f" | grep -q "$LICENSE_PATTERN" 2>/dev/null; then
                critical "Missing license header: $f"
                LICENSE_ISSUES=$((LICENSE_ISSUES + 1))
            fi
        fi
    fi
done

if [[ $LICENSE_ISSUES -eq 0 ]]; then
    info "All source files have correct license headers"
fi

# =============================================================================
# CHECK 2: CHANGELOG.md UPDATED
# =============================================================================
section "CHECK 2: CHANGELOG.md"

if echo "$CHANGED_FILES" | grep -q "^CHANGELOG.md$"; then
    # Check if it's in the Unreleased section
    if grep -q "\[Unreleased\]" CHANGELOG.md 2>/dev/null; then
        # Check for PR link
        if [[ -n "$PR_NUMBER" ]]; then
            if grep -q "#$PR_NUMBER\|pull/$PR_NUMBER" CHANGELOG.md 2>/dev/null; then
                info "CHANGELOG.md updated with PR link (#$PR_NUMBER)"
            else
                warning "CHANGELOG.md updated but PR link #$PR_NUMBER not found"
            fi
        else
            info "CHANGELOG.md has been modified"
        fi
    else
        warning "CHANGELOG.md modified but no [Unreleased] section found"
    fi
else
    critical "CHANGELOG.md has NOT been modified"
fi

# =============================================================================
# CHECK 3: RULE REGISTRATION (JavaCheckRegistrar)
# =============================================================================
section "CHECK 3: RULE REGISTRATION"

REGISTRAR_FILE="src/main/java/org/greencodeinitiative/creedengo/java/JavaCheckRegistrar.java"

if [[ -n "$NEW_CHECK_FILES" ]]; then
    for f in $NEW_CHECK_FILES; do
        CLASS_NAME=$(basename "$f" .java)
        if [[ -f "$REGISTRAR_FILE" ]]; then
            if grep -q "$CLASS_NAME" "$REGISTRAR_FILE" 2>/dev/null; then
                info "Rule class '$CLASS_NAME' is registered in JavaCheckRegistrar"
            else
                critical "Rule class '$CLASS_NAME' is NOT registered in JavaCheckRegistrar"
            fi
        fi
    done
else
    info "No new rule classes detected (registration check skipped)"
fi

# =============================================================================
# CHECK 4: QUALITY PROFILE (creedengo_way_profile.json)
# =============================================================================
section "CHECK 4: QUALITY PROFILE"

PROFILE_FILE=$(find src/main/resources -name "creedengo_way_profile.json" 2>/dev/null | head -1)

if [[ -n "$PROFILE_FILE" && -f "$PROFILE_FILE" ]]; then
    for rule_id in $RULE_IDS; do
        if grep -q "\"$rule_id\"" "$PROFILE_FILE" 2>/dev/null; then
            info "Rule '$rule_id' is listed in creedengo_way_profile.json"
        else
            critical "Rule '$rule_id' is NOT listed in creedengo_way_profile.json"
        fi
    done
else
    if [[ -n "$RULE_IDS" ]]; then
        warning "Could not find creedengo_way_profile.json"
    fi
fi

# =============================================================================
# CHECK 5: UNIT TESTS PRESENT
# =============================================================================
section "CHECK 5: UNIT TESTS"

TEST_DIR="src/test/java"
IT_TEST_PROJECT_DIR="src/it/test-projects"

for rule_id in $RULE_IDS; do
    # Check for test class
    TEST_FOUND=$(find "$TEST_DIR" -path "*/$rule_id/*" -name "*Test*.java" 2>/dev/null | head -1)
    if [[ -n "$TEST_FOUND" ]]; then
        info "Unit test found for $rule_id: $TEST_FOUND"
    else
        # Search more broadly
        TEST_FOUND=$(find "$TEST_DIR" -name "*Test*.java" -exec grep -l "$rule_id\|$(echo "$NEW_CHECK_FILES" | xargs -n1 basename 2>/dev/null | sed 's/\.java//')" {} \; 2>/dev/null | head -1)
        if [[ -n "$TEST_FOUND" ]]; then
            info "Unit test found for $rule_id: $TEST_FOUND"
        else
            critical "No unit test found for rule $rule_id"
        fi
    fi

    # Check for test resource files (compliant and noncompliant)
    TEST_RESOURCES=$(find "$IT_TEST_PROJECT_DIR" -path "*/$rule_id/*" -name "*.java" 2>/dev/null || true)
    if [[ -n "$TEST_RESOURCES" ]]; then
        NONCOMPLIANT_FOUND=false
        COMPLIANT_POSSIBLE=false
        while IFS= read -r res_file; do
            if grep -q "// Noncompliant" "$res_file" 2>/dev/null; then
                NONCOMPLIANT_FOUND=true
            fi
            # A file without Noncompliant markers is likely a compliant test
            if ! grep -q "// Noncompliant" "$res_file" 2>/dev/null; then
                COMPLIANT_POSSIBLE=true
            fi
        done <<< "$TEST_RESOURCES"

        if $NONCOMPLIANT_FOUND; then
            info "Noncompliant test cases found for $rule_id"
        else
            warning "No '// Noncompliant' markers found in test files for $rule_id"
        fi
    else
        warning "No test resource files found in $IT_TEST_PROJECT_DIR for $rule_id"
    fi
done

# =============================================================================
# CHECK 6: INTEGRATION TEST (GCIRulesIT)
# =============================================================================
section "CHECK 6: INTEGRATION TEST"

IT_FILE=$(find src/it -name "GCIRulesIT.java" 2>/dev/null | head -1)

if [[ -n "$IT_FILE" && -f "$IT_FILE" ]]; then
    for rule_id in $RULE_IDS; do
        RULE_NUM=$(echo "$rule_id" | grep -oP '\d+')
        if grep -q "testGCI${RULE_NUM}\|test${rule_id}\|\"creedengo-java:${rule_id}\"" "$IT_FILE" 2>/dev/null; then
            info "Integration test found for $rule_id in GCIRulesIT.java"
        else
            critical "No integration test found for $rule_id in GCIRulesIT.java"
        fi
    done
else
    if [[ -n "$RULE_IDS" ]]; then
        warning "GCIRulesIT.java not found"
    fi
fi

# =============================================================================
# CHECK 7: CODE STYLE VIOLATIONS
# =============================================================================
section "CHECK 7: CODE STYLE"

STYLE_ISSUES=0

for f in $CHANGED_JAVA_SRC; do
    if [[ ! -f "$f" ]]; then continue; fi

    # Check for 'var' usage (local variable type inference forbidden)
    VAR_LINES=$(grep -n '^\s*var\s\|[\s(]var\s' "$f" 2>/dev/null | grep -v '//\|/\*\|\*' || true)
    if [[ -n "$VAR_LINES" ]]; then
        warning "Usage of 'var' detected in $f (forbidden by CODE_STYLE):"
        echo "$VAR_LINES" | while read -r line; do report "    $line"; done
        STYLE_ISSUES=$((STYLE_ISSUES + 1))
    fi

    # Check for lines > 120 characters
    LONG_LINES=$(awk 'length > 120 {print NR": "$0}' "$f" 2>/dev/null | grep -v '^\s*//' | head -5 || true)
    if [[ -n "$LONG_LINES" ]]; then
        warning "Lines exceeding 120 chars in $f:"
        echo "$LONG_LINES" | while read -r line; do report "    $line"; done
        STYLE_ISSUES=$((STYLE_ISSUES + 1))
    fi

    # Check for single-character variable names (excluding loop counters i, j, k)
    SINGLE_CHAR=$(grep -nP '(^|\s)(int|long|String|Object|boolean|double|float|char|byte|short)\s+[a-hlo-zA-Z]\s*[=;,)]' "$f" 2>/dev/null | grep -v '//\|/\*' || true)
    if [[ -n "$SINGLE_CHAR" ]]; then
        warning "Single-character variable names in $f (discouraged):"
        echo "$SINGLE_CHAR" | head -3 | while read -r line; do report "    $line"; done
        STYLE_ISSUES=$((STYLE_ISSUES + 1))
    fi
done

# Check @Rule annotated files for specific patterns
for f in $NEW_CHECK_FILES; do
    if [[ ! -f "$f" ]]; then continue; fi

    # Check for @Override on visitNode/leaveNode
    if grep -q "public void visitNode\|public List.*nodesToVisit\|public void leaveNode" "$f" 2>/dev/null; then
        METHODS_WITHOUT_OVERRIDE=$(grep -B1 "public void visitNode\|public List.*nodesToVisit\|public void leaveNode" "$f" 2>/dev/null | grep -v "@Override" | grep "public" || true)
        if [[ -n "$METHODS_WITHOUT_OVERRIDE" ]]; then
            warning "Missing @Override annotation in $f:"
            echo "$METHODS_WITHOUT_OVERRIDE" | while read -r line; do report "    $line"; done
            STYLE_ISSUES=$((STYLE_ISSUES + 1))
        fi
    fi

    # Check extends IssuableSubscriptionVisitor
    if ! grep -q "extends IssuableSubscriptionVisitor" "$f" 2>/dev/null; then
        warning "$f does not extend IssuableSubscriptionVisitor (unusual for a rule class)"
        STYLE_ISSUES=$((STYLE_ISSUES + 1))
    fi

    # Check for protected static final String message constant
    if ! grep -qP 'protected static final String\s+(MESSAGERULE|RULE_MESSAGE|MESSAGE)' "$f" 2>/dev/null; then
        if ! grep -q 'protected static final String' "$f" 2>/dev/null; then
            warning "$f has no 'protected static final String' message constant"
            STYLE_ISSUES=$((STYLE_ISSUES + 1))
        fi
    fi
done

if [[ $STYLE_ISSUES -eq 0 ]]; then
    info "No code style violations detected"
fi

# =============================================================================
# CHECK 8: POTENTIAL ClassCastException RISKS
# =============================================================================
section "CHECK 8: ClassCastException RISKS"

CAST_RISKS=0

for f in $CHANGED_JAVA_SRC; do
    if [[ ! -f "$f" ]]; then continue; fi

    # Look for unchecked casts without instanceof
    CASTS=$(grep -nP '\(\s*([\w.]+Tree|[\w.]+Expression|[\w.]+Statement)\s*\)' "$f" 2>/dev/null || true)
    if [[ -n "$CASTS" ]]; then
        while IFS= read -r cast_line; do
            LINE_NUM=$(echo "$cast_line" | cut -d: -f1)
            # Check if there's an instanceof check nearby (within 5 lines before)
            START_LINE=$((LINE_NUM > 5 ? LINE_NUM - 5 : 1))
            CONTEXT=$(sed -n "${START_LINE},${LINE_NUM}p" "$f" 2>/dev/null)
            CAST_TYPE=$(echo "$cast_line" | grep -oP '\(\s*([\w.]+)\s*\)' | head -1 | tr -d '() ')
            if [[ -n "$CAST_TYPE" ]] && ! echo "$CONTEXT" | grep -q "instanceof.*$CAST_TYPE\|\.is(\|\.isKind(" 2>/dev/null; then
                warning "Potential unchecked cast at $f:$LINE_NUM - verify instanceof guard exists"
                report "    $cast_line"
                CAST_RISKS=$((CAST_RISKS + 1))
            fi
        done <<< "$CASTS"
    fi
done

if [[ $CAST_RISKS -eq 0 ]]; then
    info "No obvious ClassCastException risks detected"
else
    warning "$CAST_RISKS potential cast risk(s) found - requires manual verification"
fi

# =============================================================================
# CHECK 9: NONCOMPLIANT MESSAGE CONSISTENCY
# =============================================================================
section "CHECK 9: MESSAGE CONSISTENCY"

for f in $NEW_CHECK_FILES; do
    if [[ ! -f "$f" ]]; then continue; fi

    # Extract the error message from the rule class
    RULE_MSG=$(grep -oP 'protected static final String\s+\w+\s*=\s*"([^"]+)"' "$f" 2>/dev/null | grep -oP '"[^"]+"' | tr -d '"' | head -1)

    if [[ -n "$RULE_MSG" ]]; then
        report "  Rule message in $(basename "$f"): \"$RULE_MSG\""

        # Find corresponding test files
        RULE_ID=$(grep -oP '@Rule\(key\s*=\s*"(GCI\d+)"' "$f" | grep -oP 'GCI\d+' || true)
        if [[ -n "$RULE_ID" ]]; then
            TEST_RESOURCES=$(find "$IT_TEST_PROJECT_DIR" -path "*/$RULE_ID/*" -name "*.java" 2>/dev/null || true)
            if [[ -n "$TEST_RESOURCES" ]]; then
                while IFS= read -r test_file; do
                    NONCOMPLIANT_MSGS=$(grep -oP '// Noncompliant \{\{([^}]+)\}\}' "$test_file" 2>/dev/null | grep -oP '\{\{[^}]+\}\}' | tr -d '{}' || true)
                    if [[ -n "$NONCOMPLIANT_MSGS" ]]; then
                        while IFS= read -r msg; do
                            if [[ "$msg" != "$RULE_MSG" ]]; then
                                warning "Message mismatch in $test_file:"
                                report "    Expected: \"$RULE_MSG\""
                                report "    Found:    \"$msg\""
                            fi
                        done <<< "$NONCOMPLIANT_MSGS"
                    fi
                done <<< "$TEST_RESOURCES"
            fi
        fi
    fi
done

# =============================================================================
# CHECK 10: TEST FILES COMPILATION (basic import check)
# =============================================================================
section "CHECK 10: TEST FILES IMPORT CHECK"

IMPORT_ISSUES=0
CHANGED_IT_FILES=$(echo "$CHANGED_FILES" | grep "^src/it/test-projects/.*\.java$" || true)

for f in $CHANGED_IT_FILES; do
    if [[ ! -f "$f" ]]; then continue; fi

    # Check if file uses classes without imports
    USED_TYPES=$(grep -oP '(?<!\w)(List|Map|Set|ArrayList|HashMap|HashSet|Optional|Stream|Arrays|Collections)\s*[<(]' "$f" 2>/dev/null | grep -oP '^\w+' | sort -u || true)
    IMPORTS=$(grep "^import " "$f" 2>/dev/null || true)

    for type in $USED_TYPES; do
        if ! echo "$IMPORTS" | grep -q "$type" 2>/dev/null; then
            # Check if it's not in java.lang (implicit import)
            if [[ "$type" != "String" && "$type" != "Object" && "$type" != "System" && "$type" != "Integer" ]]; then
                warning "Potentially missing import for '$type' in $f"
                IMPORT_ISSUES=$((IMPORT_ISSUES + 1))
            fi
        fi
    done
done

if [[ $IMPORT_ISSUES -eq 0 ]]; then
    info "No obvious import issues in test files"
fi

# =============================================================================
# CHECK 11: @DeprecatedRuleKey usage
# =============================================================================
section "CHECK 11: @DeprecatedRuleKey USAGE"

for f in $NEW_CHECK_FILES; do
    if [[ ! -f "$f" ]]; then continue; fi
    if grep -q "@DeprecatedRuleKey" "$f" 2>/dev/null; then
        # Check if this is actually a new file (not existing)
        IS_NEW=$(git diff --name-only --diff-filter=A "$BASE_BRANCH"...HEAD 2>/dev/null | grep "$(basename "$f")" || \
                 git diff --name-only --diff-filter=A "origin/$BASE_BRANCH"...HEAD 2>/dev/null | grep "$(basename "$f")" || true)
        if [[ -n "$IS_NEW" ]]; then
            warning "@DeprecatedRuleKey found on new file $f - verify this is intentional"
        else
            info "@DeprecatedRuleKey in $f (existing file modification - OK)"
        fi
    fi
done

# =============================================================================
# CHECK 12: TEST FILES IN CORRECT LOCATION (not old src/test/files/)
# =============================================================================
section "CHECK 12: TEST FILES LOCATION"

# Real reviewer feedback (PR #103, #106): test files should be in the NEW location
# src/it/test-projects/.../checks/GCI{N}/ and NOT in the old src/test/files/
OLD_TEST_LOCATION="src/test/files"
NEW_FILES_IN_OLD_LOCATION=$(echo "$CHANGED_FILES" | grep "^${OLD_TEST_LOCATION}/" || true)

if [[ -n "$NEW_FILES_IN_OLD_LOCATION" ]]; then
    critical "Test files added in DEPRECATED location '$OLD_TEST_LOCATION/':"
    echo "$NEW_FILES_IN_OLD_LOCATION" | while read -r f; do
        report "    $f"
        critical "  -> Move to src/it/test-projects/.../checks/GCI{N}/ instead"
    done

    # Check for duplication (same file in both old and new location) - PR #110 pattern
    while IFS= read -r old_file; do
        OLD_BASENAME=$(basename "$old_file")
        DUPLICATE=$(echo "$CHANGED_FILES" | grep "^src/it/test-projects/.*${OLD_BASENAME}$" || true)
        if [[ -n "$DUPLICATE" ]]; then
            warning "Test file '$OLD_BASENAME' exists in BOTH old and new locations (remove from src/test/files/)"
        fi
    done <<< "$NEW_FILES_IN_OLD_LOCATION"
else
    info "No test files in deprecated location (src/test/files/)"
fi

# Check for missing trailing newline (PR #110 pattern)
NEWLINE_ISSUES=0
for f in $CHANGED_JAVA_SRC $CHANGED_IT_FILES; do
    if [[ -f "$f" ]]; then
        if [[ -n "$(tail -c 1 "$f" 2>/dev/null)" ]]; then
            warning "Missing trailing newline in $f"
            NEWLINE_ISSUES=$((NEWLINE_ISSUES + 1))
        fi
    fi
done

if [[ $NEWLINE_ISSUES -eq 0 ]]; then
    info "All changed files have proper trailing newline"
fi

# =============================================================================
# CHECK 13: RULE ID CONSISTENCY ACROSS FILES
# =============================================================================
section "CHECK 13: RULE ID CONSISTENCY"

# Real reviewer feedback (PR #106): Rule ID must be consistent across ALL files
# (CHANGELOG, registrar, profile, test, IT, rule class)
for rule_id in $RULE_IDS; do
    INCONSISTENCIES=0

    # Check CHANGELOG mentions the same rule ID
    if echo "$CHANGED_FILES" | grep -q "^CHANGELOG.md$"; then
        if ! grep -q "$rule_id" CHANGELOG.md 2>/dev/null; then
            warning "CHANGELOG.md does not mention rule $rule_id"
            INCONSISTENCIES=$((INCONSISTENCIES + 1))
        fi
    fi

    # Check IT test uses the correct rule ID format "creedengo-java:{RULE_ID}"
    IT_FILE=$(find src/it -name "GCIRulesIT.java" 2>/dev/null | head -1)
    if [[ -n "$IT_FILE" && -f "$IT_FILE" ]]; then
        if echo "$CHANGED_FILES" | grep -q "GCIRulesIT"; then
            RULE_ID_IN_IT=$(grep -oP '"creedengo-java:(GCI\d+)"' "$IT_FILE" 2>/dev/null | grep "$rule_id" || true)
            if [[ -z "$RULE_ID_IN_IT" ]]; then
                warning "GCIRulesIT.java does not reference \"creedengo-java:$rule_id\""
                INCONSISTENCIES=$((INCONSISTENCIES + 1))
            fi
        fi
    fi

    # Check that the rule ID in the @Rule annotation matches everywhere
    for f in $NEW_CHECK_FILES; do
        FILE_RULE_ID=$(grep -oP '@Rule\(key\s*=\s*"(GCI\d+)"' "$f" 2>/dev/null | grep -oP 'GCI\d+' || true)
        if [[ -n "$FILE_RULE_ID" && "$FILE_RULE_ID" != "$rule_id" ]]; then
            critical "Rule ID mismatch: $f has @Rule(key=\"$FILE_RULE_ID\") but expected $rule_id"
            INCONSISTENCIES=$((INCONSISTENCIES + 1))
        fi
    done

    if [[ $INCONSISTENCIES -eq 0 ]]; then
        info "Rule ID $rule_id is consistent across all files"
    fi
done

# =============================================================================
# CHECK 14: TEST FILE PACKAGE DECLARATION
# =============================================================================
section "CHECK 14: TEST FILE PACKAGES"

# Real reviewer feedback (PR #103): compile errors due to wrong/missing packages
PACKAGE_ISSUES=0
ALL_IT_TEST_FILES=$(echo "$CHANGED_FILES" | grep "^src/it/test-projects/.*\.java$" || true)

for f in $ALL_IT_TEST_FILES; do
    if [[ ! -f "$f" ]]; then continue; fi

    # Check package declaration exists
    PACKAGE_DECL=$(grep "^package " "$f" 2>/dev/null || true)
    if [[ -z "$PACKAGE_DECL" ]]; then
        warning "Missing package declaration in $f"
        PACKAGE_ISSUES=$((PACKAGE_ISSUES + 1))
    else
        # Verify package matches directory structure
        # Note: IT test files are in GCI{N}/ subdirectories but intentionally
        # use the parent package (without GCI{N}) for CheckVerifier compatibility.
        # We check that the declared package is a valid prefix of the dir structure.
        EXPECTED_PKG=$(echo "$f" | sed 's|src/it/test-projects/[^/]*/src/main/java/||' | sed 's|/[^/]*\.java$||' | tr '/' '.')
        ACTUAL_PKG=$(echo "$PACKAGE_DECL" | grep -oP 'package\s+([^;]+)' | sed 's/package\s*//' || true)
        # Allow the actual package to be a prefix of expected (e.g., omitting GCI{N} subdir)
        if [[ -n "$EXPECTED_PKG" && -n "$ACTUAL_PKG" && "$EXPECTED_PKG" != "$ACTUAL_PKG" ]]; then
            # Check if actual is a valid prefix of expected (IT files in GCI{N}/ subdir)
            if [[ "$EXPECTED_PKG" != "$ACTUAL_PKG."* ]]; then
                warning "Package mismatch in $f:"
                report "    Expected: $EXPECTED_PKG"
                report "    Found:    $ACTUAL_PKG"
                PACKAGE_ISSUES=$((PACKAGE_ISSUES + 1))
            fi
        fi
    fi

    # Check for class declaration matching filename
    CLASS_NAME=$(basename "$f" .java)
    if ! grep -q "class $CLASS_NAME\|interface $CLASS_NAME\|enum $CLASS_NAME" "$f" 2>/dev/null; then
        warning "Class name does not match filename in $f (expected 'class $CLASS_NAME')"
        PACKAGE_ISSUES=$((PACKAGE_ISSUES + 1))
    fi
done

if [[ $PACKAGE_ISSUES -eq 0 ]]; then
    info "All test file packages and class names are correct"
fi

# =============================================================================
# CHECK 15: RULES-SPECIFICATIONS REFERENCE
# =============================================================================
section "CHECK 15: RULES-SPECIFICATIONS REFERENCE"

# Real reviewer feedback (PR #103, #106): always link to creedengo-rules-specifications PR
if [[ -n "$RULE_IDS" ]]; then
    # Check if PR description or commits mention creedengo-rules-specifications
    SPECS_REF=$(git log "$BASE_BRANCH"..HEAD --oneline 2>/dev/null | grep -i "rules-spec\|creedengo-rules-spec" || \
                git log "origin/$BASE_BRANCH"..HEAD --oneline 2>/dev/null | grep -i "rules-spec\|creedengo-rules-spec" || true)
    if [[ -n "$SPECS_REF" ]]; then
        info "Reference to creedengo-rules-specifications found in commits"
    else
        warning "No reference to creedengo-rules-specifications PR in commits (reviewer will ask for it)"
    fi
fi

# =============================================================================
# CHECK 16: UNUSED IMPORTS IN SOURCE FILES
# =============================================================================
section "CHECK 16: UNUSED IMPORTS"

# Real reviewer feedback (PR #175): github-code-quality detects unused code
UNUSED_IMPORT_ISSUES=0

for f in $CHANGED_JAVA_SRC; do
    if [[ ! -f "$f" ]]; then continue; fi

    # Get all imports
    IMPORTS=$(grep "^import " "$f" 2>/dev/null | grep -v "^import static" | sed 's/import //; s/;//' || true)

    for imp in $IMPORTS; do
        # Get the simple class name (last part after .)
        SIMPLE_NAME=$(echo "$imp" | grep -oP '[^.]+$')
        # Skip wildcard imports
        if [[ "$SIMPLE_NAME" == "*" ]]; then continue; fi
        # Check if the class name is used anywhere else in the file (excluding the import line itself)
        USAGE=$(grep -c "\b$SIMPLE_NAME\b" "$f" 2>/dev/null || echo "0")
        # It should appear at least twice (import + usage)
        if [[ "$USAGE" -lt 2 ]]; then
            warning "Potentially unused import '$imp' in $f"
            UNUSED_IMPORT_ISSUES=$((UNUSED_IMPORT_ISSUES + 1))
        fi
    done
done

if [[ $UNUSED_IMPORT_ISSUES -eq 0 ]]; then
    info "No obviously unused imports detected"
fi

# =============================================================================
# CHECK 17: MERGE CONFLICT MARKERS
# =============================================================================
section "CHECK 17: MERGE CONFLICT MARKERS"

# Real reviewer feedback (PR #105): utarwyn asked "Can you correct the conflict in the CHANGELOG file?"
CONFLICT_ISSUES=0
for f in $CHANGED_FILES; do
    if [[ -f "$f" ]]; then
        CONFLICTS=$(grep -n "^<<<<<<< \|^=======$\|^>>>>>>> " "$f" 2>/dev/null || true)
        if [[ -n "$CONFLICTS" ]]; then
            critical "Merge conflict markers found in $f:"
            echo "$CONFLICTS" | head -5 | while read -r line; do report "    $line"; done
            CONFLICT_ISSUES=$((CONFLICT_ISSUES + 1))
        fi
    fi
done

if [[ $CONFLICT_ISSUES -eq 0 ]]; then
    info "No merge conflict markers found"
fi

# =============================================================================
# CHECK 18: BRANCH UP-TO-DATE WITH BASE
# =============================================================================
section "CHECK 18: BRANCH UP-TO-DATE"

# Real reviewer feedback (PR #44): utarwyn asked "Can you rebase your branch on the main branch?"
BEHIND_COUNT=$(git rev-list --count HEAD.."$BASE_BRANCH" 2>/dev/null || git rev-list --count HEAD.."origin/$BASE_BRANCH" 2>/dev/null || echo "0")
if [[ "$BEHIND_COUNT" -gt 0 ]]; then
    warning "Branch is $BEHIND_COUNT commit(s) behind $BASE_BRANCH (rebase may be needed)"
else
    info "Branch is up-to-date with $BASE_BRANCH"
fi

# =============================================================================
# CHECK 19: CHANGELOG FORMAT VALIDATION
# =============================================================================
section "CHECK 19: CHANGELOG FORMAT"

# Real reviewer feedback (PR #42, #105, #110): CHANGELOG format is very specific
if echo "$CHANGED_FILES" | grep -q "^CHANGELOG.md$"; then
    CHANGELOG_ISSUES=0

    # Check entry format: - [#N](url) GCIXXX - description
    UNRELEASED_SECTION=$(sed -n '/## \[Unreleased\]/,/## \[/p' CHANGELOG.md 2>/dev/null | head -30)

    # Check for entries without PR link format [#N](url)
    ENTRIES_WITHOUT_LINK=$(echo "$UNRELEASED_SECTION" | grep "^- " | grep -v "\[#[0-9]" || true)
    if [[ -n "$ENTRIES_WITHOUT_LINK" ]]; then
        warning "CHANGELOG entries without PR link format '[#N](url)':"
        echo "$ENTRIES_WITHOUT_LINK" | head -3 | while read -r line; do report "    $line"; done
        CHANGELOG_ISSUES=$((CHANGELOG_ISSUES + 1))
    fi

    # Check for entries without rule ID (GCI) for rule-related PRs
    if [[ -n "$RULE_IDS" ]]; then
        for rule_id in $RULE_IDS; do
            if ! echo "$UNRELEASED_SECTION" | grep -q "$rule_id" 2>/dev/null; then
                warning "CHANGELOG [Unreleased] section does not mention $rule_id"
                CHANGELOG_ISSUES=$((CHANGELOG_ISSUES + 1))
            fi
        done
    fi

    if [[ $CHANGELOG_ISSUES -eq 0 ]]; then
        info "CHANGELOG format looks correct"
    fi
fi

# =============================================================================
# CHECK 20: @DeprecatedRuleKey FOR RENAMED RULES
# =============================================================================
section "CHECK 20: @DeprecatedRuleKey FOR RENAMES"

# Real reviewer feedback (PR #75): when renaming ECXXX to GCIXXX, @DeprecatedRuleKey is mandatory
for f in $NEW_CHECK_FILES; do
    if [[ ! -f "$f" ]]; then continue; fi

    # Check if the file has a @Rule annotation with GCI ID
    RULE_KEY=$(grep -oP '@Rule\(key\s*=\s*"(GCI\d+)"' "$f" 2>/dev/null | grep -oP 'GCI\d+' || true)
    if [[ -n "$RULE_KEY" ]]; then
        # Extract the numeric part
        RULE_NUM=$(echo "$RULE_KEY" | grep -oP '\d+')

        # Rules below GCI100 had an ECXXX predecessor - they SHOULD have @DeprecatedRuleKey
        if [[ "$RULE_NUM" -lt 100 ]]; then
            if ! grep -q "@DeprecatedRuleKey" "$f" 2>/dev/null; then
                warning "$f has $RULE_KEY (< GCI100) but no @DeprecatedRuleKey for backward compatibility (EC$RULE_NUM)"
            else
                info "$f has @DeprecatedRuleKey for backward compatibility"
            fi
        fi
    fi
done

# =============================================================================
# CHECK 21: BUILD AND TESTS
# =============================================================================
section "CHECK 21: BUILD AND TESTS"

if [[ "$SKIP_BUILD" == "true" ]]; then
    warning "Build skipped (--skip-build flag)"
else
    report "Running Maven build..."
    echo -e "${BLUE}Running Maven build (this may take a few minutes)...${NC}"

    BUILD_OUTPUT_FILE="build_output.log"

    if ./mvnw -e -B verify -DskipITs=true > "$BUILD_OUTPUT_FILE" 2>&1; then
        info "Maven build SUCCESS (unit tests passed)"
        # Extract test summary
        TEST_SUMMARY=$(grep "Tests run:" "$BUILD_OUTPUT_FILE" | grep -v "Time elapsed" | tail -1 || true)
        if [[ -n "$TEST_SUMMARY" ]]; then
            report "  Test summary: $TEST_SUMMARY"
        fi
        # Extract total test count
        TOTAL_TESTS=$(grep -oP 'Tests run: \d+' "$BUILD_OUTPUT_FILE" | tail -1 || true)
        report "  $TOTAL_TESTS"
    else
        critical "Maven build FAILED"
        # Extract error info
        ERRORS=$(grep -A5 "\[ERROR\]" "$BUILD_OUTPUT_FILE" | head -30 || true)
        report "  Build errors (first 30 lines):"
        echo "$ERRORS" | while read -r line; do report "    $line"; done
    fi

    # Note: Integration tests require SonarQube Orchestrator and are NOT run by this script
    report ""
    report "NOTE: Integration tests (IT) require a SonarQube Orchestrator environment."
    report "They are NOT run by this script. To run IT tests:"
    report "  .github/scripts/run_it.sh --skip-build [--rule GCIXXX]"
fi

# =============================================================================
# SUMMARY
# =============================================================================
section "SUMMARY"

report "Critical issues: $CRITICAL_COUNT"
report "Warnings: $WARNING_COUNT"
report "Passed checks: $INFO_COUNT"
report ""

echo ""
echo -e "${BLUE}=====================================${NC}"
echo -e "  Review complete."
echo -e "  ${RED}Critical: $CRITICAL_COUNT${NC}"
echo -e "  ${YELLOW}Warnings: $WARNING_COUNT${NC}"
echo -e "  ${GREEN}Passed:   $INFO_COUNT${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""
echo "Full report saved to: $REPORT_FILE"

if [[ $CRITICAL_COUNT -gt 0 ]]; then
    report "RESULT: CRITICAL ISSUES FOUND - PR should NOT be merged"
    exit 1
elif [[ $WARNING_COUNT -gt 0 ]]; then
    report "RESULT: WARNINGS FOUND - Review needed"
    exit 2
else
    report "RESULT: ALL AUTOMATED CHECKS PASSED"
    exit 0
fi
