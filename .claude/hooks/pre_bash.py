#!/usr/bin/env python3
"""
Pre-bash hook for Android project.
Features:
- Dangerous command blocking
- Git push to main protection
"""

import json
import sys
import re

# === CONFIGURATION ===
DANGEROUS_PATTERNS = [
    (r"rm\s+(-[rf]+\s+)*[/~]", "Dangerous rm command near root/home"),
    (r"chmod\s+777", "Overly permissive chmod"),
    (r">\s*/etc/", "Writing to system directory"),
    (r"\|\s*sh\b", "Piping to shell"),
    (r"curl.*\|\s*bash", "Curl pipe to bash"),
    (r":(){ :|:& };:", "Fork bomb detected"),
]

GIT_PROTECTED_BRANCHES = ["main", "master", "production"]
# === END CONFIGURATION ===


def check_dangerous_patterns(command: str) -> tuple:
    """Check for dangerous shell patterns."""
    for pattern, message in DANGEROUS_PATTERNS:
        if re.search(pattern, command, re.IGNORECASE):
            return False, f"BLOCKED: {message}"
    return True, ""


def check_git_push(command: str) -> tuple:
    """Check for push to protected branches."""
    for branch in GIT_PROTECTED_BRANCHES:
        patterns = [
            rf"git\s+push\s+\S+\s+{branch}\b",
            rf"git\s+push\s+{branch}\b",
            rf"git\s+push.*\s+{branch}:",
        ]
        for pattern in patterns:
            if re.search(pattern, command, re.IGNORECASE):
                return False, f"BLOCKED: Direct push to '{branch}' is not allowed. Use PR workflow."
    return True, ""


def main():
    try:
        hook_data = json.load(sys.stdin)
        tool_input = hook_data.get("tool_input", {})
        command = tool_input.get("command", "")

        if not command:
            sys.exit(0)

        # Check dangerous patterns
        is_safe, error = check_dangerous_patterns(command)
        if not is_safe:
            print(error, file=sys.stderr)
            sys.exit(2)

        # Check git push to protected branches
        is_safe, error = check_git_push(command)
        if not is_safe:
            print(error, file=sys.stderr)
            sys.exit(2)

        sys.exit(0)

    except Exception as e:
        print(f"Hook error: {e}", file=sys.stderr)
        sys.exit(0)


if __name__ == "__main__":
    main()
