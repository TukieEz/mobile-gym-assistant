#!/usr/bin/env python3
"""Post-edit hook for Android/Kotlin project."""

import sys
import json
import subprocess
from pathlib import Path

def run_ktlint_check(file_path: str) -> str | None:
    """Run ktlint on the file if available."""
    try:
        result = subprocess.run(
            ['./gradlew', 'ktlintCheck', '-q'],
            capture_output=True,
            text=True,
            timeout=30,
            cwd=Path(file_path).parent.parent.parent.parent.parent  # Go to project root
        )
        if result.returncode != 0:
            return result.stdout + result.stderr
    except (subprocess.TimeoutExpired, FileNotFoundError):
        pass
    return None

def main():
    try:
        input_data = json.loads(sys.stdin.read())
        tool_input = input_data.get('tool_input', {})
        file_path = tool_input.get('file_path', '')
        
        # Only process Kotlin files
        if not file_path.endswith('.kt'):
            print(json.dumps({}))
            return
        
        # Log the edit
        result = {
            "message": f"✓ Edited: {Path(file_path).name}"
        }
        
        # Optionally run ktlint (disabled by default for speed)
        # lint_errors = run_ktlint_check(file_path)
        # if lint_errors:
        #     result["warning"] = f"ktlint issues:\n{lint_errors}"
        
        print(json.dumps(result))
        
    except Exception as e:
        print(json.dumps({"error": str(e)}), file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
