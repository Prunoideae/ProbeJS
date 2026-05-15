// @ts-check

const { Wolfgirl } = require("@package/moe");

const Integer = Java.loadClass("java.lang.Integer");
const ArrayList = Java.loadClass("java.util.ArrayList");

const answer = Integer.valueOf("42");
const names = new ArrayList();

const Woofable = Java.loadClass("moe.wolfgirl.Woofable");
const NetworkManager = Java.loadClass("moe.wolfgirl.networking.NetworkManager");

let manager = new NetworkManager();

let other = JSON.parse("{}");
if (other instanceof Woofable) {
    other.woof();
}

StartupEvent.networkReady((manager) => {
    manager.woof();
});

for (let i of new Wolfgirl()) {

}

//@ts-expect-error
ServerEvent.init(() => {
    console.log("Server is initializing...");
});

let a = new Java().testOutput('entity:');
new Java().testInput('minecraft:block_state', 'minecraft:dirt').testInput({
    'minecraft:item_name': 'minecraft:apple',
    
})