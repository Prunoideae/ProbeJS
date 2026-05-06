import os
import glob

base_dir = r"src\main\java\moe\wolfgirl\probejs"
pattern = os.path.join(base_dir, "**", "*.java")

files_changed = 0
lines_changed = 0

for filepath in glob.glob(pattern, recursive=True):
    with open(filepath, "r", encoding="utf-8") as f:
        lines = f.readlines()

    new_lines = []
    file_modified = False
    for line in lines:
        stripped = line.lstrip()
        if stripped.startswith("package ") or stripped.startswith("import "):
            if ".next." in line:
                new_line = line.replace(".next.", ".")
                new_lines.append(new_line)
                file_modified = True
                lines_changed += 1
                print(f"  {filepath}: {line.strip()} -> {new_line.strip()}")
            else:
                new_lines.append(line)
        else:
            new_lines.append(line)

    if file_modified:
        with open(filepath, "w", encoding="utf-8") as f:
            f.writelines(new_lines)
        files_changed += 1

print(f"\nDone! {files_changed} files modified, {lines_changed} lines changed.")
