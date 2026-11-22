# Bulwark Spec Range

See the 11 × 11 area of Dinh's bulwark's Shield Bash, centered on your true tile. The overlay works with both Dinh's bulwark and Dinh's blazing bulwark, and requires 50% special attack energy by default.

## Settings

- **Require 50% special energy** hides the area below 50% special energy when enabled.
- **Show outline** draws the perimeter. The fill stays visible when the outline is disabled.
- **Outline color** and **Fill color** control color and opacity.
- **Outline width** controls the perimeter's thickness.
- **Highlight corners** makes the four corners easier to spot.
- **Show center tile** draws a light blue outline on your true tile and is off by default.

The overlay reads your equipment, special energy and position from RuneLite. It draws the range of the area effect. The game determines which targets Shield Bash hits.

## Development

Use Java 21 to run the build. Run `./gradlew check` to verify the overlay and settings, and `./gradlew run` to open an isolated RuneLite development client. On Windows, use `gradlew.bat`.

For account setup in the development client, see [RuneLite's Jagex account guide](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).
