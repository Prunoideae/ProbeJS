# ProbeJS

A data dumper and typing generator for the KubeJS functions, constants and classes.

Great thanks to @DAmNRelentless, @LatvianModder and @yesterday17 for invaluable suggestions during the development!

For the detailed information about documents, please refer to [the doc page](docs). (Currently revamping)

## 0. ProbeJS v8.0

Welcome to the future! As TypeScript had experienced a major update that deprecated the typing structure of ProbeJS in 
v7.7.2, we rewrote the mod and the extension to be compatible with the new TypeScript version (and hopefully backward-compatible
with the previous TypeScript versions.)

However, there are some features missing in the mod due to the `TypeScript Native` (`tsgo`) is not fully implemented yet,
the mod will receive continuous updates to add the missing features when TypeScript Native is ready.

What's new in v8.0:
- `tsgo` compatible type declarations. TypeScript Native is a new TypeScript compiler written in Go, which is 10x faster
  and use much less memory than the original TypeScript. We had tested `tsgo` and it showed less than 500ms item literal
  completion time and instant class/event completions on ATM10.
- A GUI that allows you to configure the mod easily in-game and show the dumping progress. Also, it allows you to convert
  `require()` to `Java.loadClass()` due to `tsgo` is not supporting language plugins yet.
- (Hopefully) more correct typing generation. Most problems in the previous versions are now fixed as the type resolution
  is rewritten.
- Intensive support of agents and LLMs. You can make AIs like DeepSeek or GPT generate 100% correct KubeJS code now!

## 1. Installation

1. Get VSCode. And install `Typescript (Native Preview)` extension by Microsoft.
2. Install the mod, **and** install the ProbeJS VSCode Extension.
3. In game, use `/probejs` and click the dump button.
4. Open the `.minecraft` folder in VSCode, you should see snippets and typing functioning.
5. Re-dump in case of you want to refresh the generated typing. If VSCode is not responding to file changes,
   restart VSCode to force a refresh in extension and the language server.

## 3. Showcase

(To be added)