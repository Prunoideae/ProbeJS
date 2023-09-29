import { Minecraft } from "@java/net/minecraft/client/Minecraft";

export class StartupEvent {
    static create(extra: string, event: (mc: Minecraft) => void);
};