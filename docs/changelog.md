## v7.0.0

- Final release for Minecraft 1.20.1
- Dropped Fabric support
- Completely rewritten the codebase to allow better codegen

### TypeScript Generation

- Declaration files are now generated in a per-script type manner.
- You can only have type hinting for bindings you have in corresponding script types.
- Typing generation folder is moved to `.minecraft/.probe` instead of `.minecraft/kubejs/probe`.
- Each script folder will now have its own `jsconfig.json` and `src` folder.

### Java Class Dumping

- Largely sanitized the generated Java declarations, it now works in a nearly TS-compatible manner.
- Java classes are now arraged in their corresponding package as modules, instead of being prefixed with `Internal.`.
- Reacheable classes are now all visible with the VSCode JS/TS extension.

### `require` Statement

- As the module-based TypeScript generation is now implemented, CommonJS `require` statements are now supported.
- `require` is an alias of `Java.tryLoadClass`, and can be implemented without the installation of ProbeJS.
- `require` statements enable VSCode to auto-import classes you needed in your script.
- `Java.loadClass` dumping support is removed in favor of `require` statements.

### `export` Statements

- To prevent namespace pollution, ProbeJS will isolate the identifiers for each script by default.
- You can declare `const ident = ...` for each script without the need to worry about the global scope now with isolation.
- To make a variable or function available to other scripts, you can use the `export` statement: `export const/var/let/function ident ..`.
- Can be disabled by `/probejs scope_isolation`.
- `export` statements are stripped by mixin, so you can't use them without ProbeJS.

### Snippets

- Added a few snippets: `#uuid`, `#ignored`, `#itemstack`, `#packmode`, `#priority`, `#recipes`, `#requires`.
- `@-` snippets now include `@mod`, `@lang_key`, `@recipe_id`, and `@texture`.
- Added `ProbeEvents.snippets` to allow you to add your own snippets.

### Document Generation

- Documents to Java classes is now accessible from Java and JS side.
- Removed the need of downloading document files from the remote server.
- Added `ProbeEvents.assignType` and `ProbeEvents.modifyClass` to allow you to modify the generated Java classes.

### Decompilation

- Now includes a runtime Java decompiler (Vineflower) to allow you to decompile classes in-game.
- Obfuscated classes will be remapped to their deobfuscated names.
- Decompilation will provide class info to ProbeJS, allowing you to use `require` for almost any Java class.
- Set `probejs.enableDecompiler` to `true` and `probejs.modHash` to `-1` to trigger decompilation at next dump.

### Linting

- Added `/probejs lint` to perform linting on your scripts.
- Linting will check for non-JS problems in your scripts. Like the redeclaration of exported variables, `import` conflict due to priorities, etc.
- Auto-linting by interop with VSCode will be implemented in 1.21.

### VSCode Extension

- Does not require the installation of ProbeJS extension for any features now.
- Extension will be rewritten to support a different set of features.

### Forge Events

- Literal support for Forge events is removed in favor of `require` event classes directly.
