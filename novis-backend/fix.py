import os
import re
import glob

def find_files(directory, extension):
    out = []
    for root, dirs, files in os.walk(directory):
        for f in files:
            if f.endswith(extension):
                out.append(os.path.join(root, f))
    return out

def remove_lombok_imports(content):
    lines = content.split('\n')
    new_lines = [l for l in lines if not l.strip().startswith('import lombok.')]
    return '\n'.join(new_lines)

def fix_required_args_constructor(content, class_name):
    if '@RequiredArgsConstructor' not in content:
        return content
    content = content.replace('@RequiredArgsConstructor\n', '').replace('@RequiredArgsConstructor', '')
    
    # find all private final fields
    pattern = r'private\s+final\s+([A-Za-z0-9_<>]+)\s+([A-Za-z0-9_]+)\s*;'
    fields = re.findall(pattern, content)
    
    if not fields:
        return content
        
    constructor_args = ', '.join([f"{t} {n}" for t, n in fields])
    constructor_body = '\n'.join([f"        this.{n} = {n};" for t, n in fields])
    
    constructor_str = f"""
    public {class_name}({constructor_args}) {{
{constructor_body}
    }}
"""
    # Insert it before the last closing brace
    last_brace_idx = content.rfind('}')
    if last_brace_idx != -1:
        content = content[:last_brace_idx] + constructor_str + content[last_brace_idx:]
    return content

def fix_slf4j(content, class_name):
    if '@Slf4j' not in content:
        return content
    content = content.replace('@Slf4j\n', '').replace('@Slf4j', '')
    
    # insert import if not present
    if 'import org.slf4j.Logger;' not in content:
        content = re.sub(r'(package .*?;)', r'\1\n\nimport org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;', content, 1)
        
    logger_field = f"\n    private static final Logger log = LoggerFactory.getLogger({class_name}.class);\n"
    
    # insert after class definition
    class_def_pattern = r'(public\s+(?:class|interface|record)\s+' + class_name + r'[\s\S]*?\{)'
    match = re.search(class_def_pattern, content)
    if match:
        idx = match.end()
        content = content[:idx] + logger_field + content[idx:]
    return content

def process_services():
    java_files = find_files('src/main/java', '.java')
    for f in java_files:
        with open(f, 'r') as file:
            content = file.read()
            
        original_content = content
        
        # remove lombok imports
        content = remove_lombok_imports(content)
        
        class_name_match = re.search(r'public\s+(?:class|interface|record)\s+([A-Za-z0-9_]+)', content)
        if class_name_match:
            class_name = class_name_match.group(1)
            content = fix_required_args_constructor(content, class_name)
            content = fix_slf4j(content, class_name)
            
            if content != original_content:
                with open(f, 'w') as file:
                    file.write(content)

process_services()
