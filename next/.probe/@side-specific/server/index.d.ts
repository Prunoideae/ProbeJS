
export { }

declare global {
    export namespace ServerEvent {
        export function init(callback: () => void): void;
    }
}