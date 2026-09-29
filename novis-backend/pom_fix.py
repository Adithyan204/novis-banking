with open('pom.xml', 'r') as f:
    content = f.read()

import re
content = re.sub(r'<lombok\.version>.*?</lombok\.version>\s*\n', '', content)

lombok_dep = r"""        <dependency>
            <groupId>org\.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>\$\{lombok\.version\}</version>
            <optional>true</optional>
        </dependency>\s*\n"""
content = re.sub(lombok_dep, '', content)

compiler_plugin_regex = r"<plugin>\s*<groupId>org\.apache\.maven\.plugins</groupId>\s*<artifactId>maven-compiler-plugin</artifactId>.*?</plugin>"
new_compiler_plugin = """<plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <compilerArgs>
                        <arg>-parameters</arg>
                    </compilerArgs>
                </configuration>
            </plugin>"""
content = re.sub(compiler_plugin_regex, new_compiler_plugin, content, flags=re.DOTALL)

spring_boot_plugin = r"""<configuration>\s*<excludes>\s*<exclude>\s*<groupId>org\.projectlombok</groupId>\s*<artifactId>lombok</artifactId>\s*</exclude>\s*</excludes>\s*</configuration>"""
content = re.sub(spring_boot_plugin, '', content)

with open('pom.xml', 'w') as f:
    f.write(content)
