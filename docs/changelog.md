## v7.0.0 (1.21)

### JavaScript Project

- A separate `jsconfig.json` is now generated for each script type.
- Each script type will only be able to access their own events and bindings.
- Typing for each script type is now generated at `.minecraft/.probe`, instead of `.minecraft/kubejs/probe`.
- Does not require you to place scripts under `src/` folder anymore. However, it is still advised to do so for a better project structure.
- Also generates a `test/` folder for each script type, scripts in this folder will be able to invoke some functions for debugging.

### Typing

- Generation of class types now adapt to a more module-like manner to allow the auto-import to work.
- Improved completion performance by over 10x via separating complex object types to individual type declarations.

### Java Class Discovery

- Decompiler now uses runtime class bytecode to generate decompiled code, meaning that most of the results is deobfuscated and well-defined.
- Implemented a class scanner to discover mod, NeoForge and Minecraft classes in order to eliminate the need of checking registry classes or a lot of other work. Basically those are all classes you can load in the game.

### `require` and `export`

- `require` is largely fixed by using Rhino's parser to patch the script code.
- ESM `import` is not supported due to fundamental conflicts to Rhino.
- Scope isolation is now off by default.

### VSCode Extension

- Rewritten the extension for better VSCode-Minecraft interaction.
