# Changelog

All notable changes to this project will be documented in this file.

## [v2.1.0] - 2026-10-20

### Additions
- Add support for 26.1.2, 26.2, and 26.3
- Add mod description to fabric.mod.json and neoforge.mods.toml/mods.toml

### Fixes
- Fix resource packs not syncing properly
- Fix minor issues in ResourcePackUtils
- Fix hanging on closing in 26.2+
- Fix title being transparent in config menu

### Changes
- Remove support for select minor versions with later hotfixes (1.21.6, 1.21.7, 1.21.9)
- Update required versions for dependencies
- Update README with ModDex badge
- Update version to 2.1.0

## [v2.0.0] - 2026-08-21

### Additions
- Add full support for syncing resource packs
- New in-game configuration menu, allowing for disabling toasts and resource pack syncing

### Fixes
- Fix crashes on 1.20.4
- Fix toast not functioning on older versions

### Changes
- Switch from raw .txt global config format to .json
- Switch to the Stonecutter framework instead of using reflection
- Switch to .lang files to allow for translation support
- Adjust existing import/overwrite menu to make the options more clear