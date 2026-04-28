export * as networking from "@package/moe/wolfgirl/networking";

declare module "@package/moe/wolfgirl" {
    // Keep marker metadata on a module-private symbol so it doesn't become a visible named field.
    const tagMarker: unique symbol;
    const objectMarker: unique symbol;

    export interface MarkerCarrier<M1, M2> {
        readonly [tagMarker]: M1;
        readonly [objectMarker]: M2;
    }

    // We need to declare a same class because Java interfaces are classes,
    // Having a class allows it to be used with Java.loadClass
    export class Woofable {
        woof(): void;
    }

    // And keep an interface here because TypeScript classes can't implement classes
    export interface Woofable {
        woof(): void;
    }

    export class Foo { }
    export interface Foo extends MarkerCarrier<1 | 2 | 3, string> { }

    export class Bar { }
    export interface Bar extends MarkerCarrier<4 | 5 | 6, number> { }
}
