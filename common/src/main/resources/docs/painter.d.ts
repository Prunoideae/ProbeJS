
type Root = { visible?: boolean }
type ScreenObject = {
    x?: number | string,
    y?: number | string,
    z?: number | string,
    w?: number | string,
    h?: number | string,
    moveX?: number | string,
    moveY?: number | string,
    expandW?: number | string,
    expandH?: number | string,
    alignx?: "left" | "center" | "right",
    alignY?: "top" | "center" | "bottom",
    draw?: "ingame" | "gui" | "always"
} & Root
type RectangleObject = {
    type: "rectangle",
    color?: Internal.Color,
    texture?: ResourceLocation_,
    u0?: number | string,
    v0?: number | string,
    u1?: number | string,
    v1?: number | string
} & ScreenObject
type GradientObject = {
    type: "gradient",
    color?: Internal.Color,
    texture?: ResourceLocation_,
    u0?: number | string,
    v0?: number | string,
    u1?: number | string,
    v1?: number | string,
    colorT?: Internal.Color,
    colorB?: Internal.Color,
    colorL?: Internal.Color,
    colorTL?: Internal.Color,
    colorTR?: Internal.Color,
    colorBL?: Internal.Color,
    colorBR?: Internal.Color,
} & ScreenObject
type TextObject = {
    type: "text",
    text?: Text_,
    shadow?: boolean,
    scale?: number,
    color?: Internal.Color,
    centered?: boolean
} & ScreenObject

type Paintable = TextObject | GradientObject | RectangleObject
