# Sky Shaders

Animated Minecraft skies with **9 effects**, **31 color themes**, and settings accessible through **Mod Menu**.

**Created by x1eqn.** Client-side Fabric mod.

## Features

- Aurora, Galaxy, Black Hole, Thunderstorm, Nebula, Synthwave, Blood Moon, Dreamscape, and Prism.
- 31 color themes and a Custom palette with two accent colors.
- Animation speed, brightness, intensity, scale, rotation, and star controls.
- Horizon blending and fog color matching.
- Options to hide clouds, keep the sun and moon, and enable effects in the End.
- Full, 75%, and 50% effect resolution.
- English and Turkish configuration screens.

## Downloads and installation

Download the JAR for your Minecraft version from [Releases](https://github.com/myhticc/Sky-Shaders/releases). Install Fabric Loader, Fabric API, and Mod Menu, then place the Sky Shaders JAR in your profile's `mods` folder.

Use **Mods → Sky Shaders → Configure** to open the settings. Install only one Sky Shaders JAR at a time. The mod does not need to be installed on the server.

| Minecraft | Mod version | Java | Fabric API | Mod Menu | Source |
| --- | --- | --- | --- | --- | --- |
| 1.20.1 | 1.1.2 | 17+ | 0.92.6+1.20.1 | 7.2.2 | [Project](versions/1.20.1) |
| 1.20.4 | 1.1.0 | 17+ | 0.97.3+1.20.4 | 9.2.0 | [Project](versions/1.20.4) |
| 1.21 | 1.1.0 | 21+ | 0.102.0+1.21 | 11.0.5 | [Project](versions/1.21) |
| 1.21.1 | 1.1.0 | 21+ | 0.116.17+1.21.1 | 11.0.5 | [Project](versions/1.21.1) |
| 1.21.4 | 1.1.0 | 21+ | 0.119.4+1.21.4 | 13.0.4 | [Project](versions/1.21.4) |
| 1.21.8 | 1.1.0 | 21+ | 0.136.1+1.21.8 | 15.0.2 | [Project](versions/1.21.8) |
| 1.21.11 | 1.1.0 | 21+ | 0.141.6+1.21.11 | 17.0.1 | [Project](versions/1.21.11) |
| 26.1.2 | 1.1.0 | 25+ | 0.155.3+26.1.2 | 18.0.2 | [Root project](.) |
| 26.2 | 1.1.0 | 25+ | 0.161.0+26.2 | 20.0.3 | [Project](versions/26.2) |
| 26.3 | 1.1.0 | 25+ | 0.164.0+26.3 | 21.0.0 | [Project](versions/26.3) |

The 1.20.1 build requires Fabric Loader **0.19.3+**. Other builds require **0.19.5+**. The table lists the dependency versions used for these builds.

**Minecraft 26.2 and 26.3:** select OpenGL in the game's graphics backend settings and restart. The effects use the original OpenGL/GLSL engine; Vulkan uses the vanilla sky and clouds.

## Configuration

Settings are stored in `config/sky-shaders.json`.

- **Natural colors** uses the effect's original palette. Prism follows the selected theme.
- To apply a theme to another effect, disable Natural colors and enable **Use theme colors**.
- For manual colors, disable both options. Hex colors and `theme` / `theme2` references are supported.
- Choose **Custom** to set your own accent colors.
- **Save** applies changes; **Cancel** or Esc discards the draft.

## Building

The root project targets Minecraft 26.1.2. Each directory under `versions/` is an independent Gradle project with its own dependencies and Java target.

From the desired project directory, use the required JDK and run:

```powershell
.\gradlew.bat build
```

The checked-in Gradle configurations are provided for normal development. The distributed JARs were built with the local helper scripts, not a verified Gradle build:

```powershell
.\scripts\build-local.ps1 -JavaHome 'C:\path\to\jdk' -ValidateGpu
```

Read the individual project's README for helper requirements. Older Minecraft projects use JDK 21 for the helpers and emit Java 17 or Java 21 classes as appropriate. Builds before 26.x are remapped to Fabric intermediary names; `-named.jar` files are development artifacts and must not be installed.

## Validation and compatibility

All builds passed shader compilation, linking, GPU drawing/readback, theme, and configuration checks. Minecraft startup, 27 effect/resolution draws, and four configuration pages were tested for the versioned projects. The root 26.1.2 project has no separate Minecraft smoke session recorded here.

The **1.20.1 / 1.1.2** build also passed startup and sky-hook coexistence checks with **Sodium 0.5.13** and **Sodium Extra 0.5.9**, using Loader 0.19.3 and Java 17. It fixes the sky redirect conflict in 1.1.1.

Manual in-world visual comparisons, Iris compatibility, and full third-party modpacks have not been tested.

## License

**All rights reserved.** This project is not MIT-licensed. See [LICENSE](LICENSE) for the terms.
