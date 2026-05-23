export { };
declare global {
    export namespace Internal {
        type Object = import("@package/java/lang").Object;
    }
}
declare module "@package/java/lang" {

    export class Object {
        equals(other: unknown): boolean;
        hashCode(): number;
        toString(): string;
    }
    type Object_ = Object | { [key: string]: unknown };


    export class String extends Object {
        constructor(value?: string | number | boolean);
        length(): number;
        substring(start: number, end?: number): String;
        toString(): string;
        valueOf(): string;
    }

    export class Integer extends Object {
        constructor(value: number | string);
        static parseInt(value: string, radix?: number): number;
        static valueOf(value: number | string): Integer;
        intValue(): number;
        compareTo(other: Integer): number;
        toString(): string;
    }

    export class Boolean extends Object {
        constructor(value: boolean | string);
        static valueOf(value: boolean | string): Boolean;
        booleanValue(): boolean;
    }
}