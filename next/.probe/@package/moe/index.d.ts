export * as wolfgirl from "@package/moe/wolfgirl";

interface Iterable<T> {
    [Symbol.iterator](): Iterator<T>;
}

export class Wolfgirl implements Iterable<string> {
    [Symbol.iterator](): Iterator<string>;
}
