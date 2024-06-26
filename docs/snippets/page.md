*Note: this only works with VSCode*

Snippets are small pieces of text that can be inserted into a document. They are often used to provide examples of code, commands, or configuration options.

To facilitate your writing, ProbeJS has a set of snippets that you can use to quickly insert useful strings like item or block names, tags, and other things like a stub for recipe events.

The usage of snippets does not require VSCode to have type support for your current file. So it can be especially useful when modifying config files or writing documentation. Since the ProbeJS typing only covers JavaScript files.

Snippets can have tabstops, which are positions in the snippet where the cursor will be placed after the snippet is inserted. You can navigate between tabstops with the `Tab` key.

## `@` Snippets

Snippets that start with `@` usually represent a literal for a registry object. For example, `@item` will insert a string that is a valid item name, like `"minecraft:stone"`. Each registry type also has a snippet that will insert corresponding tags, they are suffixed with `_tag`, so `@item_tag` will insert `"#forge:rods"`.

## `#` Snippets

`#` snippets are usually predefined and serve for inserting useful strings.

### `#recipes`

This snippet will insert a stub for a recipe event.

```js
ServerEvents.recipes(event=>{
    let {/** First tabstop */} = event;
    /** Second tabstop */
})
```

### `#uuid`

This snippet will insert a random UUID as a string.

```js
"00000000-0000-0000-0000-000000000000"
```

### `#priority`

This snippet will insert the comment to configure script priority.

```js
// priority: /** Tabstop, default to 0 */
```

### `#requires`

This snippet will insert the comment to configure script requirements of mods.

```js
// requires: /** Tabstop, can choose one of the existing mods */
```

### `#packmode`

This snippet will insert the comment to configure the pack mode.

```js
// packmode: /** Tabstop */
```

### `#itemstack`

This snippet will insert a stub for an item stack.

```js
"/** Tabstop */x /** Tabstop of item names*/"
```
