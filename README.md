# LuisCoins

A lightweight economy plugin for Spigot 1.8.8. Players can earn, check, and transfer coins. No Vault required.

## Features
- Player balances with YAML or JSON storage
- Commands for balance, pay, earn, top list
- Admin commands to add/remove/set/reset/reload
- Rank-based earn multipliers via permissions
- Autosave and join/quit persistence

## Build
- Requires Java 8 and Gradle (or use Gradle Wrapper if you add it)
- Build:
```
gradle clean build
```
- Output JAR:
`build/libs/LuisCoins.jar`

## Installation
1. Place the JAR in your server's `plugins/` directory.
2. Start the server to generate `config.yml` and data files.
3. Configure `config.yml` in `plugins/LuisCoins/`.

## Commands
- /balance [player]
- /luisbal [player|top]
- /pay <player> <amount>
- /earn <amount>
- /luis <add|remove|reset|set|reload>

## Permissions
- luiscoins.balance.others – view others' balances
- luiscoins.pay – use /pay
- luiscoins.earn – use /earn (if require-permission is true)
- luiscoins.top – view top list
- luiscoins.admin – admin bundle (add/remove/reset/set/reload)

## Config (`src/main/resources/config.yml`)
- storage: yaml or json
- starting-balance: default balance
- autosave-minutes: periodic save
- earn.enabled / require-permission / cooldown-minutes / max-per-use
- rank-multipliers: map of permission node -> multiplier
- messages: customizable messages

## Notes
- Mojang/Spigot 1.8.8 API (no api-version tag)
- Optional integration with LuckPerms via permission checks only
