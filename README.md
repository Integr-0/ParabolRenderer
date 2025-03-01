# Parabol Renderer
Rendering helper for the Parabol project.
Parabol Renderer uses the [Renderer](https://github.com/0x3C50/Renderer) library by [0x3C50](https://github.com/0x3C50)
for its internal rendering operations.
Parabol Renderer is used as a simplification of the mentioned library, and is not intended to be a full replacement.
I encourage you to check out the Renderer library if you are looking for a more feature-rich rendering library.

## Features
- Font utility to load, cache, and render fonts.
- 2D rendering Context to draw on the UI layer.
- 3D rendering Context to draw in the 3D world.

## Dependencies
- [Renderer](https://github.com/0x3C50/Renderer)
- [Fabric Loader](https://fabricmc.net/)

## Usage
### 2D Context
```kotlin
val ctx = Parabol2dCtx.create(drawCtx.matrices)

ctx.apply {
    useMultisample {
        circle(10.0, 10.0, 10.0, Color(24, 124, 75))
    }

    val theText = Text.literal("The quick brown fox jumps over the lazy dog\n")
        .append(Text.literal("italic\n").styled { it.withItalic(true) }.withColor(Color.GREEN.rgb))
        .append(Text.literal("bold\n").styled { it.withBold(true) })
        .append(Text.literal("bold italic\n").styled { it.withBold(true).withItalic(true) })
        .append(Text.literal("under\n").styled { it.withUnderline(true) })
        .append(Text.literal("strikethrough\nwith nl\n").styled { it.withStrikethrough(true) })
        .append(Text.literal("Special chars: 1234@æđðħſ.ĸ|aa{a}()"))

    useMultisample {
        circle(80.0, 80.0, 20.0, Color(245, 124, 75))
    }

    useBlurMask {
        val width = 50
        val height = 50
        quad(
            drawCtx.scaledWindowWidth/2.0 - width/2.0,
            drawCtx.scaledWindowHeight/2.0 - height/2.0,
            drawCtx.scaledWindowWidth/2.0 + width/2.0,
            drawCtx.scaledWindowHeight/2.0 + height/2.0
        )
    }

    text(theText, 10f, 10f, 1f, 20f)

}
```

## Implementing
This mod is not intended to be used as a standalone mod, but rather as a dependency for the Parabol configuration utility.

TODO: Publish to maven for usage.