# Gift System Plugin

A Minecraft 1.21.4 plugin that creates an intuitive GUI for players to create, customize, and send gifts to other players. Features internationalization support, modern Adventure API text components, and fixed button interaction.

## Features

- **Main Menu**: Access gift creation, information, and gift history
- **Gift Creation**: Add items, set name, description, and recipient
- **Recipient Selection**: Choose from online players
- **Internationalization**: Multi-language support with customizable language files
- **Modern Text Components**: Uses Adventure API for rich text formatting

## Installation

1. Place the plugin JAR file in your server's `plugins` folder
2. Restart your server
3. Edit the config.yml and language files as needed

## Configuration

### config.yml

```yaml
# Language settings
language:
  # Available languages: en, uk
  # Put language files in the plugin's lang folder
  default: "en"

# Gift settings
gifts:
  # Maximum number of items allowed in a gift
  max-items: 7

  # Whether to announce gifts publicly when sent
  public-announcements: true

  # How long to keep completed gifts in storage (in days)
  # Set to -1 to keep forever
  storage-time: 30

  # Delivery animation settings
  delivery:
    # Whether to use bee delivery animation
    use-bees: true

    # Number of seconds before gift is delivered
    delay: 10

    # Maximum number of bees to spawn for multiple gifts
    max-bees: 5

    # Particle effects when gift is opened
    particles: true

# GUI settings
gui:
  # Sound when opening gift-related GUIs
  open-sound: true

  # Whether to show item tooltips in gift creation
  show-tooltips: true
```

## Translation

Language files are stored in the `plugins/HappyGifts/lang/` folder. The plugin comes with a default English language file (`en.yml`). To add a new language:

1. Copy the `en.yml` file
2. Rename it to your language code (e.g., `es.yml`, `de.yml`)
3. Translate all strings in the file
4. Change the `language` setting in `config.yml` to use your language

## Commands

- `/gift`: Opens the main gift menu
- Other commands coming soon!

## Usage

### Creating a Gift:

1. Click the "Create Gift" button in the main menu
2. Add items by dragging them from your inventory to the gift slots
3. Set a name, description, and recipient
4. Click "Finalize Gift" to send

### Adding Items:
- **Left-click** with an item in your cursor to add it to a gift slot
- **Right-click** on a gift slot to remove an item

## Dependencies

- Paper/Spigot 1.21.4

## Known Issues

- ~~Gifts history is not yet implemented~~
- Recipient selection does not yet support offline players
- GUI titles still use legacy formatting (§)
- More than 7 items added to the gift will cause the GUI to break
- ~~Gifts are currently not persistent between server restarts~~
- ~~Gifts are not yet delivered to recipients~~
- ~~Gifts are not yet announced publicly~~
- ~~Gifts are not yet opened with particle effects~~
- ~~Gifts are not yet delivered by bees~~

## License

This project is licensed under the MIT License - see the LICENSE file for details.