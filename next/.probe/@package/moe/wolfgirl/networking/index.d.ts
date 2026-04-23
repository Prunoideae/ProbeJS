import { Woofable } from "@package/moe/wolfgirl";

export { }

declare module "@package/moe/wolfgirl/networking" {
    export class NetworkManager implements Woofable {
        constructor();
        connect(host: string, port: number): void;
        woof(): void;
    }
}