import os
import re

def process(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
        
    class_name = re.search(r'public\s+class\s+([A-Za-z0-9_]+)', content).group(1)
    
    # Remove everything from the first standard getter or the no-args constructor
    # We will search for exactly our generated block
    
    # We know the first getter generated is usually for 'id' or the first field.
    # Let's find "    public [Type] get" or "    public [Type] is" that matches a field.
    
    # Instead of deleting blindly, let's find the start of our generated code.
    # We generated `public {class_name}() {`
    
    idx = content.find(f'    public Long getId() {{')
    if idx == -1:
        idx = content.find(f'    public String getId() {{')
    
    if idx != -1:
        clean_content = content[:idx]
    else:
        clean_content = content[:-2] # fallback
        
    if not clean_content.endswith('}\n'):
        clean_content = clean_content.rstrip() + '\n}\n'
        
    # Now parse fields from clean_content
    # Field pattern: private Type name [= value];
    fields = []
    for line in clean_content.split('\n'):
        match = re.search(r'private\s+([A-Za-z0-9_<>\[\]]+)\s+([A-Za-z0-9_]+)(?:\s*=\s*(.+?))?;', line)
        if match and 'static' not in line:
            fields.append({
                'type': match.group(1),
                'name': match.group(2),
                'default': match.group(3)
            })
            
    # Generate stuff
    out = "\n"
    for f in fields:
        cap_name = f['name'][0].upper() + f['name'][1:]
        prefix = "is" if f['type'] == 'boolean' else "get"
        out += f"    public {f['type']} {prefix}{cap_name}() {{\n        return {f['name']};\n    }}\n\n"
        out += f"    public void set{cap_name}({f['type']} {f['name']}) {{\n        this.{f['name']} = {f['name']};\n    }}\n\n"
        
    # constructors
    out += f"    public {class_name}() {{\n"
    for f in fields:
        if f['default']:
            out += f"        this.{f['name']} = {f['default']};\n"
    out += "    }\n\n"
    
    args = ", ".join([f"{f['type']} {f['name']}" for f in fields])
    out += f"    public {class_name}({args}) {{\n"
    for f in fields:
        out += f"        this.{f['name']} = {f['name']};\n"
    out += "    }\n\n"
    
    # builder
    out += f"    public static Builder builder() {{\n        return new Builder();\n    }}\n\n"
    out += f"    public static class Builder {{\n"
    for f in fields:
        if f['default']:
            out += f"        private {f['type']} {f['name']} = {f['default']};\n"
        else:
            out += f"        private {f['type']} {f['name']};\n"
            
    for f in fields:
        out += f"        public Builder {f['name']}({f['type']} {f['name']}) {{\n            this.{f['name']} = {f['name']};\n            return this;\n        }}\n\n"
        
    out += f"        public {class_name} build() {{\n            {class_name} obj = new {class_name}();\n"
    for f in fields:
        out += f"            obj.{f['name']} = this.{f['name']};\n"
    out += "            return obj;\n        }\n    }\n"
    
    final_content = clean_content.rstrip()[:-1] + out + "}\n"
    
    with open(filepath, 'w') as f:
        f.write(final_content)

for root, _, files in os.walk('src/main/java/com/novis'):
    for file in files:
        if file.endswith('.java') and 'entity' in root:
            try:
                process(os.path.join(root, file))
            except Exception as e:
                print(f"Error processing {file}: {e}")

