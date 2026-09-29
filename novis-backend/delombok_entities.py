import re
import os

def generate_entity_code(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
        
    if '@Data' not in content and '@Builder' not in content:
        return
        
    # Remove lombok imports
    lines = content.split('\n')
    lines = [l for l in lines if not l.strip().startswith('import lombok.')]
    content = '\n'.join(lines)
    
    # Remove class annotations
    content = re.sub(r'@Data\s*', '', content)
    content = re.sub(r'@Builder\s*', '', content)
    content = re.sub(r'@NoArgsConstructor\s*', '', content)
    content = re.sub(r'@AllArgsConstructor\s*', '', content)
    
    # Remove @Builder.Default but extract the default values
    # Actually wait, let's just find the fields and their defaults manually
    
    class_name_match = re.search(r'public\s+class\s+([A-Za-z0-9_]+)(?:.*?)\s*\{', content)
    if not class_name_match:
        return
        
    class_name = class_name_match.group(1)
    
    # We will just parse the file carefully.
    
