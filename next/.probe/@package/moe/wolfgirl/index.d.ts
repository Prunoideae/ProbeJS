export * as networking from "@package/moe/wolfgirl/networking";

declare module "@package/moe/wolfgirl" {
    // We need to declare a same class because Java interfaces are classes,
    // Having a class allows it to be used with Java.loadClass
    export class Woofable {
        woof(): void;
    }

    // And keep an interface here because TypeScript classes can't implement classes
    export interface Woofable {
        woof(): void;
    }
}