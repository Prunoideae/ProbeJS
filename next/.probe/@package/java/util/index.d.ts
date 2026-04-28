import { Object, String } from "@package/java/lang";
import { Bar } from "@package/moe/wolfgirl";
import { Foo } from "@package/moe/wolfgirl";
import { MarkerCarrier } from "@package/moe/wolfgirl";

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

    export type ResolveTag<T> = T extends MarkerCarrier<infer M1, any> ? M1 : never;
    export type ResolveObject<T> = T extends MarkerCarrier<any, infer M2> ? M2 : never;

    export type FooMarker = ResolveTag<Foo>; // 1 | 2 | 3
    export type BarMarker = ResolveObject<Bar>;
}