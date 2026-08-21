# Changelog

All notable changes to this project will be documented in this file.

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