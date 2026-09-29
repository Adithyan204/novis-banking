import os
for root, _, files in os.walk('src/main/java'):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            if '.Default' in content:
                content = content.replace('.Default\n', '').replace('    .Default', '')
                with open(filepath, 'w') as f:
                    f.write(content)
