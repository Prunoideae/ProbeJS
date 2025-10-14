## v7.7.2

### JavaScript Project

- VSCode config writing should be more reasonable now.

### Typing

- Triggers auto-dump in a more reasonable way.
- Added back partial dump to make loading faster.
- Added config to toggle bean generation due to allergy.
- Static fields can also get beaned now.
- Switched to KubeJS's type resolution system.
- Class discovery is now on render thread to avoid problems.
- Redundant imports are stripped to reduce generated file size.
- All `string` now resolves to `type StringJS = {}` to avoid weird typing problem like `string | Special.Item`.
- Added support for ComponentHolder's `set` and `get` functions. E.g. setting the max durability of an ItemStack.
- Removed TS-compatible typing for `Java.loadClass` and `Special.XXX` due to performance issues. The support is now added using the tsserver plugin provided in the extension.
- Removed dumping for `CUSTOM_STAT` due to crazy typings in it.

### Java Class Discovery

- Now does not initialize the class when checking class using `Class.forName`.
- Now skips loading of mixin-like classes to avoid problem, mixin classes is defined by having `mixin` or `mixins` in package path.

### Extension

- Added a lot of endpoints for the VSCode extension.

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
