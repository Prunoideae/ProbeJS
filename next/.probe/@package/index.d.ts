export * as java from "@package/java";
export * as moe from "@package/moe";

interface Foo { }
declare class Bar { }

declare namespace MapTypes {
    type ItemObject = { id: string, count: number };
    type BlockObject = { id: string, itemId: string };
    type Items = "minecraft:apple" | "minecraft:stone";
    type Blocks = "minecraft:stone" | "minecraft:dirt";
    type OutputMap = {
        "item:": ItemObject, "block:": BlockObject, "fluid:": string, "entity:": Foo, "test:": Bar, "test2:": number, "test3": test,
        "ite1m:": ItemObject, "bloc1k:": BlockObject, "flu1id:": string, "enti1ty:": Foo, "tes1t:": Bar, "tes1t2:": number, "tes1t3": test,
        "i3tem:": ItemObject, "blo3ck:": BlockObject, "flui3d:": string, "enti3ty:": Foo, "t3est:": Bar, "te3st2:": number, "t3est3": test
    };
    type ResolveOutput<T extends keyof OutputMap> = OutputMap[T];
    type InputMap = { "minecraft:lodestone_tracker": $LodestoneTracker_, "minecraft:food": $FoodProperties_, "minecraft:charged_projectiles": $ChargedProjectiles, "minecraft:recipes": $List_<$ResourceLocation_>, "minecraft:container": $ItemContainerContents, "minecraft:fire_resistant": $Unit_, "minecraft:damage": number, "minecraft:entity_data": $CustomData, "minecraft:map_color": $MapItemColor_, "minecraft:stored_enchantments": $ItemEnchantments_, "minecraft:max_damage": number, "minecraft:enchantment_glint_override": boolean, "minecraft:intangible_projectile": $Unit_, "minecraft:bucket_entity_data": $CustomData, "minecraft:custom_model_data": $CustomModelData_, "minecraft:map_decorations": $MapDecorations_, "minecraft:lore": $ItemLore_, "minecraft:firework_explosion": $FireworkExplosion_, "minecraft:trim": $ArmorTrim, "minecraft:container_loot": $SeededContainerLoot_, "minecraft:banner_patterns": $BannerPatternLayers_, "minecraft:block_entity_data": $CustomData, "minecraft:enchantments": $ItemEnchantments_, "minecraft:can_break": $AdventureModePredicate, "minecraft:hide_tooltip": $Unit_, "minecraft:bees": $List_<$BeehiveBlockEntity$Occupant_>, "minecraft:writable_book_content": $WritableBookContent_, "minecraft:map_post_processing": $MapPostProcessing_, "minecraft:attribute_modifiers": $ItemAttributeModifiers_, "minecraft:debug_stick_state": $DebugStickState_, "minecraft:potion_contents": $PotionContents_, "minecraft:pot_decorations": $PotDecorations_, "minecraft:suspicious_stew_effects": $SuspiciousStewEffects_, "minecraft:hide_additional_tooltip": $Unit_, "minecraft:block_state": $BlockItemStateProperties_, "minecraft:max_stack_size": number, "minecraft:dyed_color": $DyedItemColor_, "minecraft:ominous_bottle_amplifier": number, "minecraft:base_color": $DyeColor_, "minecraft:can_place_on": $AdventureModePredicate, "minecraft:repair_cost": number, "minecraft:rarity": $Rarity_, "minecraft:bundle_contents": $BundleContents, "minecraft:creative_slot_lock": $Unit_, "minecraft:unbreakable": $Unbreakable_, "minecraft:lock": $LockCode_, "minecraft:instrument": $Holder_<$Instrument>, "minecraft:custom_data": $CustomData, "minecraft:tool": $Tool_, "minecraft:item_name": $Component_, "minecraft:fireworks": $Fireworks_, "minecraft:written_book_content": $WrittenBookContent_, "minecraft:map_id": $MapId_, "minecraft:note_block_sound": $ResourceLocation_, "minecraft:jukebox_playable": $JukeboxPlayable_, "minecraft:profile": $ResolvableProfile_, "minecraft:custom_name": $Component_, }; //{ "item": Items, "block": Blocks }
    type test = [foo: number, bar: string];
    type ResolveInput<T extends keyof InputMap> = InputMap[T];
}


type ResolveJavaClass<E, N extends string> = N extends `${infer H}.${infer T}` ? H extends keyof E ? ResolveJavaClass<E[H], T> : never : N extends keyof E ? E[N] : never;


declare global {
    class Java {
        static loadClass<N extends string>(name: N): ResolveJavaClass<typeof import("@package"), N>;

        testOutput<T extends keyof MapTypes.OutputMap>(output: T): MapTypes.OutputMap[T];
        testInput(components: Partial<MapTypes.InputMap>): this;
        testInput<T extends keyof MapTypes.InputMap>(key: T, value: MapTypes.ResolveInput<T>): this;
        

    }

}

