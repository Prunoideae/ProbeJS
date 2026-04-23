export * as java from "@package/java";
export * as moe from "@package/moe";

type ResolveJavaClass<Tree, Name extends string> = Name extends `${infer Head}.${infer Tail}`
    ? Head extends keyof Tree
    ? ResolveJavaClass<Tree[Head], Tail>
    : never
    : Name extends keyof Tree
    ? Tree[Name]
    : never;


type JavaClassForName<Name extends string> = ResolveJavaClass<typeof import("@package"), Name>;

declare global {
    class Java {
        static loadClass<Name extends string>(name: Name): JavaClassForName<Name>;
    }
}