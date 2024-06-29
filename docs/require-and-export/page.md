`require` is a function implemented by ProbeJS that allows you to load Java classes directly in the script. You can consider it has a better alternative to `Java.loadClass` as it can be auto-imported by VSCode and has better type support.

## Using `require` to load classes

```js
// This loads the class net.minecraft.core.component.DataComponents
const { $DataComponents } = require("packages/net/minecraft/core/component/$DataComponents")
```

For most of the time, typing the name of the class will make VSCode suggest the java class and auto-import for you. However, you might also want to know some naming conventions for the class path:

- The class path will always start with `packages/` to indicate it's a Java class.
- The class path will use `/` instead of `.` to conform with the JavaScript module system.
- The class name will be prefixed with `$` to avoid naming conflicts with existing names in KubeJS, like `Item` is a exported utility class in KubeJS.

## Using `require` to discover members in other scripts

As ProbeJS now implements a module-like typing system, functions or variables are now not visible to other scripts unless you `export` them:

```js
export function myFunction() {
    // Do something
}

export const myVariable = 5
```

Note that `export` can only be used on individual functions or variables, and you must ensure that `export` is at the same line as the declaration.

By then, you can use the `require` function to get type hinting for the exported members:

```js
const { myFunction, myVariable } = require("./myScript")
```

Note that this is type-hinting only, and the `export` will be stripped out during the loading process. `require`ing a script will do nothing. You should be careful that all the variables are actually visible to each other, unless with the `scope isolation` on.
