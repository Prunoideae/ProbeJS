export { }

// These are exported to the global scope
// Only namespaces, classes, consts, functions can be exported
declare global {
    namespace Special {
        type Item = "apple" | "diamond";
    }

    const HOUR = 1000 * 60 * 60;
}