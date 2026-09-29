import os
import re

def parse_fields(content):
    # Match fields like: private String name; or @Builder.Default private String name = "default";
    # We ignore static fields
    fields = []
    lines = content.split('\n')
    for line in lines:
        if 'private ' in line and 'static ' not in line and '(' not in line:
            # try to extract type, name and default
            match = re.search(r'private\s+([A-Za-z0-9_<>\[\]]+)\s+([A-Za-z0-9_]+)\s*(?:=\s*(.+?))?;', line)
            if match:
                f_type = match.group(1)
                f_name = match.group(2)
                f_def = match.group(3)
                is_builder_default = '@Builder.Default' in line
                fields.append({'type': f_type, 'name': f_name, 'default': f_def, 'builder_default': is_builder_default})
    return fields

def generate_getters_setters(fields):
    out = ""
    for f in fields:
        cap_name = f['name'][0].upper() + f['name'][1:]
        prefix = "is" if f['type'] == 'boolean' else "get"
        out += f"""
    public {f['type']} {prefix}{cap_name}() {{
        return {f['name']};
    }}

    public void set{cap_name}({f['type']} {f['name']}) {{
        this.{f['name']} = {f['name']};
    }}
"""
    return out

def generate_constructors(class_name, fields):
    # No-args
    no_args = f"\n    public {class_name}() {{\n"
    for f in fields:
        if f['builder_default'] and f['default']:
            no_args += f"        this.{f['name']} = {f['default']};\n"
    no_args += "    }\n"
    
    # All-args
    args = ", ".join([f"{f['type']} {f['name']}" for f in fields])
    all_args = f"\n    public {class_name}({args}) {{\n"
    for f in fields:
        all_args += f"        this.{f['name']} = {f['name']};\n"
    all_args += "    }\n"
    
    return no_args + all_args

def generate_builder(class_name, fields):
    out = f"""
    public static Builder builder() {{
        return new Builder();
    }}

    public static class Builder {{
"""
    for f in fields:
        if f['builder_default'] and f['default']:
            out += f"        private {f['type']} {f['name']} = {f['default']};\n"
        else:
            out += f"        private {f['type']} {f['name']};\n"
            
    for f in fields:
        out += f"""
        public Builder {f['name']}({f['type']} {f['name']}) {{
            this.{f['name']} = {f['name']};
            return this;
        }}
"""
    
    build_assignments = "\n".join([f"            obj.{f['name']} = this.{f['name']};" for f in fields])
    out += f"""
        public {class_name} build() {{
            {class_name} obj = new {class_name}();
{build_assignments}
            return obj;
        }}
    }}
"""
    return out

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
        
    if '@Data' not in content and '@Builder' not in content:
        return
        
    original = content
    # Remove imports
    lines = content.split('\n')
    lines = [l for l in lines if not l.strip().startswith('import lombok.')]
    content = '\n'.join(lines)
    
    # Remove annotations
    for ann in ['@Data', '@Builder', '@NoArgsConstructor', '@AllArgsConstructor', '@Builder.Default']:
        content = re.sub(ann + r'\s*', '', content)
        
    class_name_match = re.search(r'public\s+class\s+([A-Za-z0-9_]+)', content)
    if not class_name_match:
        return
        
    class_name = class_name_match.group(1)
    fields = parse_fields(original)
    
    getters_setters = generate_getters_setters(fields)
    constructors = generate_constructors(class_name, fields)
    builder = generate_builder(class_name, fields)
    
    # Insert before the last brace
    last_brace_idx = content.rfind('}')
    if last_brace_idx != -1:
        new_content = content[:last_brace_idx] + getters_setters + constructors + builder + content[last_brace_idx:]
        with open(filepath, 'w') as f:
            f.write(new_content)

for root, _, files in os.walk('src/main/java'):
    for file in files:
        if file.endswith('.java'):
            process_file(os.path.join(root, file))

