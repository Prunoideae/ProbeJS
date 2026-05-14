export * as java from "@package/java";
export * as moe from "@package/moe";

interface Foo { }
declare class Bar { }

declare namespace MapTypes {
    type ItemObject = { id: string, count: number };
    type BlockObject = { id: string, itemId: string };
    type Items = "minecraft:apple" | "minecraft:stone";
    type Blocks = "minecraft:stone" | "minecraft:dirt";
    type OutputMap = { "item:": ItemObject, "block:": BlockObject, "fluid:": string, "entity:": Foo, "test:": Bar, "test2:": number, "test3": test,
        "ite1m:": ItemObject, "bloc1k:": BlockObject, "flu1id:": string, "enti1ty:": Foo, "tes1t:": Bar, "tes1t2:": number, "tes1t3": test,
        "i3tem:": ItemObject, "blo3ck:": BlockObject, "flui3d:": string, "enti3ty:": Foo, "t3est:": Bar, "te3st2:": number, "t3est3": test
     };
    type ResolveOutput<T extends keyof OutputMap> = OutputMap[T];
    type InputMap = { "item": Items, "block": Blocks }
    type test = [foo: number, bar: string];
    type ResolveInput<T extends keyof InputMap> = InputMap[T];
}


type ResolveJavaClass<E, N extends string> = N extends `${infer H}.${infer T}` ? H extends keyof E ? ResolveJavaClass<E[H], T> : never : N extends keyof E ? E[N] : never;


declare global {
    class Java {
        static loadClass<N extends string>(name: N): ResolveJavaClass<typeof import("@package"), N>;

        testOutput<T extends keyof MapTypes.OutputMap>(output: T): MapTypes.OutputMap[T];
        static testInput<T extends keyof MapTypes.InputMap>(key: T, value: MapTypes.ResolveInput<T>): void;
    }

}

