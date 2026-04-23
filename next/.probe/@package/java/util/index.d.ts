export { };
import { Object, String } from "@package/java/lang";

declare module "@package/java/util" {
    export interface Iterator<T> {
        hasNext(): boolean;
        next(): T;
    }

    export interface Collection<T> {
        add(value: T): boolean;
        clear(): void;
        size(): number;
        iterator(): Iterator<T>;
    }

    export interface List<T> extends Collection<T> {
        get(index: number): T;
    }

    export interface Map<K, V> {
        get(key: K): V | null;
        put(key: K, value: V): V | null;
        size(): number;
    }

    export class ArrayList<T = Object> implements List<T> {
        constructor(initialCapacity?: number);
        add(value: T): boolean;
        clear(): void;
        size(): number;
        iterator(): Iterator<T>;
        get(index: number): T;
        toString(): String;
    }
}