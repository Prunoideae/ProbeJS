export * as java from "@package/java";
export * as moe from "@package/moe";

type ResolveJavaClass<E, N extends string> = N extends `${infer H}.${infer T}` ? H extends keyof E ? ResolveJavaClass<E[H], T> : never : N extends keyof E ? E[N] : never;
declare global {
    class Java {
        static loadClass<N extends string>(name: N): ResolveJavaClass<typeof import("@package"), N>;
    }
}