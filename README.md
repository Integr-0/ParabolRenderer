# Parabol Renderer
Rendering helper for the Parabol project.
Parabol Renderer uses the [Renderer](https://github.com/0x3C50/Renderer) library by [0x3C50](https://github.com/0x3C50)
for its internal rendering operations.
Parabol Renderer is used as a simplification of the mentioned library, and is not intended to be a full replacement.
I encourage you to check out the Renderer library if you are looking for a more feature-rich rendering library.

This library bundles the Roboto Regular font by default, which is used as the default font for the Parabol project.
I encourage you to check out the [Roboto Font](https://fonts.google.com/specimen/Roboto) for more info.

Parabol Renderer also includes a custom-made font renderer with the ability to load custom fonts from files,
use system fonts, and render text with different styles and colors. All being similar to the vanilla Minecraft text rendering.

## Features
- Font utility to load, cache, and render fonts.
- 2D rendering Context to draw on the UI layer.
- 3D rendering Context to draw in the 3D world.

## Usage
### Font Manager
```kotlin
// Getting a system font as a usable FontRenderer
val fontRenderer = ParabolFontManager.getOrLoadFontRenderer("Arial", 20f) // Font name, font size

// Getting a custom font as a usable FontRenderer
ParabolFontManager.registerCustomFont("path/to/your/font.ttf", "MyFont")
val fontRenderer = ParabolFontManager.getOrLoadFontRenderer("MyFont", 16f) // Font name, font size

// Getting default font as a usable FontRenderer
ParabolFontManager.getDefaultFontRenderer(16f) // Font size

// Setting the default font
ParabolFontManager.setDefaultFont("MyFont") // Font name
```
### 2D Context
```kotlin
val ctx = Render2dContext.create(matrixStack)

val svgIcon = SvgLoader.loadIcon("path/to/your/icon.svg", 512, 512) // Path, width, height

ctx.apply {
    useMultisample {
        circle(10.0, 10.0, 10.0, Color(24, 124, 75))
    }

    val text = ParabolText.literal("The quick brown fox jumps over the ")
        .append("lazy ").styled(ParabolText.Style.BOLD).colored(Color.RED)
        .append("dog!")

    useMultisample {
        circle(80.0, 80.0, 20.0, Color(245, 124, 75))
    }
    
    useBlurMask {
        val width = 50
        val height = 50
        quad(
            screenCenterX() - width/2.0,
            screenCenterY() - height/2.0,
            screenCenterX() + width/2.0,
            screenCenterY() + height/2.0
        )
    }

    text(text, 10f, 10f, 1f, 20f)
    text(text, 10f, 40f, 1f, "MyFont", 20f)
    svg(svgIcon, 10.0, 10.0, 100.0, 100.0, Color(255, 255, 255))
}
```

### 3D Context
```kotlin
val ctx = Render3dContext.create(matrixStack)
ctx.useThroughWalls {
    useMultisample {
        edged(Color(245, 124, 75, 100), Color(245, 124, 75), Vec3d(0.0, 0.0, 0.0), Vec3d(1.0, 1.0, 1.0))
    }
}
```

## Implementing
This mod is not intended to be used as a standalone mod, but rather as a dependency for the Parabol configuration utility.

You can include this mod as a dependency in your `build.gradle` file:
```groovy
include modImplementation("io.github.integr-0:parabol-renderer:$version") // Using fabrics Jar-in-Jar
```

or in your `build.gradle.kts` file:
```kotlin
include(modImplementation("io.github.integr-0:parabol-renderer:$version")!!)  // Using fabrics Jar-in-Jar
```

It is also possible to use maven:
```xml
<dependency>
    <groupId>io.github.integr-0</groupId>
    <artifactId>parabol-renderer</artifactId>
    <version>${version}</version>
</dependency>
```

## Dependencies
- [Renderer](https://github.com/0x3C50/Renderer)
- [Fabric Loader](https://fabricmc.net/)
- [Roboto Font](https://fonts.google.com/specimen/Roboto)