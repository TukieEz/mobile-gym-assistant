#!/usr/bin/env python3
"""Pre-edit hook for Android/Kotlin project validation."""

import sys
import json
import re

def validate_kotlin_file(file_path: str, content: str) -> list[str]:
    """Validate Kotlin file conventions."""
    errors = []
    
    # Check file length
    lines = content.split('\n')
    if len(lines) > 300:
        errors.append(f"File too long: {len(lines)} lines (max 300)")
    
    # Check for hardcoded strings in UI
    if '/ui/' in file_path or '/screens/' in file_path:
        hardcoded = re.findall(r'text\s*=\s*"[^"]{10,}"', content)
        if hardcoded:
            errors.append(f"Hardcoded strings in UI: {len(hardcoded)} found. Use stringResource()")
    
    # Check for blocking calls in Composables
    if '@Composable' in content:
        if 'runBlocking' in content:
            errors.append("runBlocking in Composable! Use LaunchedEffect or ViewModel")
        if 'Thread.sleep' in content:
            errors.append("Thread.sleep in Composable! Use delay() in coroutine")
    
    # Check ViewModel conventions
    if 'ViewModel' in file_path:
        if 'MutableLiveData' in content and 'MutableStateFlow' not in content:
            errors.append("Prefer StateFlow over LiveData in new code")
    
    # Check Entity conventions
    if 'Entity' in file_path:
        if '@Entity' in content and '@PrimaryKey' not in content:
            errors.append("Entity missing @PrimaryKey")
    
    return errors

def main():
    try:
        input_data = json.loads(sys.stdin.read())
        tool_input = input_data.get('tool_input', {})
        file_path = tool_input.get('file_path', '')
        content = tool_input.get('content', '')
        
        # Only validate Kotlin files
        if not file_path.endswith('.kt'):
            print(json.dumps({}))
            return
        
        errors = validate_kotlin_file(file_path, content)
        
        if errors:
            result = {
                "decision": "block",
                "reason": "Kotlin validation failed:\n" + "\n".join(f"• {e}" for e in errors)
            }
        else:
            result = {}
        
        print(json.dumps(result))
        
    except Exception as e:
        print(json.dumps({"error": str(e)}), file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
