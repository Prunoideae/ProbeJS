import { NetworkManager } from "@package/moe/wolfgirl/networking";

export { }

declare global {
    export namespace StartupEvent {
        export function networkReady(callback: (manager: NetworkManager) => void): void;
    }
}