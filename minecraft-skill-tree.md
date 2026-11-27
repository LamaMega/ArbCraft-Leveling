# Minecraft Mod — Skill Tree & XP Progression

## Main idea

Use **one central “XP Core”** at the bottom of the GUI.

- Players open the tree with a keybind, such as `K`.
- They spend **XP levels** to unlock or upgrade nodes.
- The first branch they should unlock is **XP Gain**, because it helps them earn XP faster and progress through every other branch.
- Each node can have 1–5 levels.
- A node must be unlocked before its connected next node becomes available.

## XP Core

| Node | Levels | Cost | Effect |
|---|---:|---:|---|
| XP Efficiency | 5 | 1, 2, 3, 4, 5 XP levels | +5% XP gained per level |
| XP Mastery | 5 | 3, 5, 8, 12, 18 XP levels | +10% XP gained per level |
| Lucky XP | 3 | 5, 10, 15 XP levels | 10% chance for double XP per level |
| XP Magnet | 3 | 2, 4, 6 XP levels | XP orbs are collected from farther away |
| XP Storage | 3 | 4, 8, 12 XP levels | Keep 10%, 20%, or 30% XP after death |

The XP multiplier should be shown permanently in the GUI:

```text
XP MULTIPLIER: +75%
```

Example: with **XP Efficiency V** and **XP Mastery V**, the player gets:

```text
1 + 0.25 + 0.50 = 1.75
```

So they earn **75% more XP** from normal sources.

## Enchantment Upgrades

This branch lets players combine vanilla enchantments into stronger custom versions.

| Node | Requirement | Effect |
|---|---|---|
| Fortune IV | Combine 2 Fortune III items/books | Mining diamonds, emeralds, coal, etc. gives more drops |
| Fortune V | Combine 2 Fortune IV items/books | Even higher ore drops |
| Efficiency VI | Combine 2 Efficiency V tools | Mine faster |
| Unbreaking IV | Combine 2 Unbreaking III items | Tools and armor last longer |
| Sharpness VI | Combine 2 Sharpness V swords | More melee damage |
| Protection V | Combine 2 Protection IV armor pieces | Better damage reduction |
| Power VI | Combine 2 Power V bows | More arrow damage |
| Looting IV | Combine 2 Looting III swords | More mob drops |
| Silk Touch II | Combine 2 Silk Touch I items | Special block-drop upgrades |
| Mending II | Combine 2 Mending I items | Repairs more durability per XP orb |

**Important rule:** the player must first unlock the corresponding node in the Skill Tree with XP. Then they can combine the items in an anvil, crafting table, or custom “Enchant Fusion” block.

Example:

```text
Fortune III Pickaxe + Fortune III Pickaxe
        +
XP Unlock: Fortune IV
        =
Fortune IV Pickaxe
```

## Mining Branch

| Node | Levels | Effect |
|---|---:|---|
| Mining XP | 5 | +10% XP from mining ores per level |
| Deep Miner | 3 | Mine faster below Y level 0 |
| Ore Sense | 3 | Nearby ores briefly glow or show particles |
| Auto Smelt | 1 | Small chance for ores to drop smelted versions |
| Fortune Fusion | 1 | Unlocks Fortune IV combining |
| Fortune Mastery | 1 | Unlocks Fortune V combining |

## Combat Branch

| Node | Levels | Effect |
|---|---:|---|
| Combat XP | 5 | +10% XP from killing mobs per level |
| Sharp Focus | 3 | +5% melee damage per level |
| Arrow Mastery | 3 | +10% bow/crossbow damage per level |
| Looting Fusion | 1 | Unlocks Looting IV combining |
| Sharpness Fusion | 1 | Unlocks Sharpness VI combining |
| Power Fusion | 1 | Unlocks Power VI combining |

## Farming Branch

| Node | Levels | Effect |
|---|---:|---|
| Farming XP | 5 | +10% XP from crops, breeding, and fishing |
| Green Thumb | 3 | Crops grow slightly faster near the player |
| Animal Friend | 3 | Animals give more XP/items when bred |
| Harvest Boost | 3 | Small chance for extra crop drops |
| Fisher’s Luck | 3 | Better fishing loot chance |

## Exploration Branch

| Node | Levels | Effect |
|---|---:|---|
| Explorer XP | 5 | +10% XP from exploring new chunks and structures |
| Swift Feet | 3 | +5% movement speed per level |
| Food Vision | 3 | After eating, gain short-range night vision |
| Trailblazer | 3 | Slightly faster sprinting on paths and natural blocks |
| Cartographer | 3 | More XP from finding villages, structures, and new biomes |

## Food Vision effect

Instead of full Night Vision, make it a **limited-radius night vision** that activates only after eating.

| Node | Levels | Effect after eating |
|---|---:|---|
| Food Vision I | 1 | 10-block radius night vision for 30 seconds |
| Food Vision II | 2 | 15-block radius night vision for 45 seconds |
| Food Vision III | 3 | 20-block radius night vision for 60 seconds |

It should not make caves fully visible. It should work like a soft light circle around the player:

```text
Before eating:
[ dark cave ]

After eating:
        bright area
      +-----------+
      | 10 blocks |
      |  radius   |
      +-----------+
```

## GUI layout

```text
+------------------------------------------------------------------+
|                            SKILL TREE                            |
|                                                                  |
|  +------------------+                       +------------------+ |
|  |      MINING      |                       |      COMBAT      | |
|  |                  |                       |                  | |
|  |  [Mining XP]     |                       |  [Combat XP]     | |
|  |      |           |                       |      |           | |
|  |  [Fortune IV]    |                       |  [Sharpness VI]  | |
|  |      |           |                       |      |           | |
|  |  [Fortune V]     |                       |  [Looting IV]    | |
|  +--------+---------+                       +--------+---------+ |
|           |                                          |           |
|           +-------------------+----------------------+           |
|                               |                                  |
|                     +---------+---------+                        |
|                     |      XP CORE      |                        |
|                     |                   |                        |
|                     |  XP MULTIPLIER    |                        |
|                     |       +75%        |                        |
|                     |                   |                        |
|                     |  [Upgrade XP]     |                        |
|                     +---------+---------+                        |
|           +-------------------+----------------------+           |
|           |                                      |               |
|  +--------+---------+                   +--------+---------+     |
|  |      FARMING     |                   |   EXPLORATION    |     |
|  |                  |                   |                  |     |
|  |  [Farming XP]    |                   |  [Explorer XP]   |     |
|  |      |           |                   |      |           |     |
|  |  [Green Thumb]   |                   |  [Swift Feet]    |     |
|  |      |           |                   |      |           |     |
|  |  [Food Vision]   |                   |  [Food Vision]   |     |
|  +------------------+                   +------------------+     |
|                                                                  |
|  XP Levels: 24            Selected: Fortune IV                   |
|  Cost: 10 XP levels      Requires: 2x Fortune III                |
|                                                                  |
|  [ Upgrade Skill ]   [ Combine Enchantment ]       [ Close ]     |
+------------------------------------------------------------------+
```

## Enchantment Fusion GUI

When the player clicks **Combine Enchantment**, open a small second screen:

```text
+------------------------------------+
|         ENCHANTMENT FUSION         |
|                                    |
|  +--------------+  +--------------+ |
|  | Fortune III  |  | Fortune III  | |
|  |   Pickaxe    |  |   Pickaxe    | |
|  +--------------+  +--------------+ |
|                                    |
|              v                     |
|                                    |
|         +--------------+           |
|         |  Fortune IV  |           |
|         |   Pickaxe    |           |
|         +--------------+           |
|                                    |
|  XP Cost: 10 XP levels             |
|  Skill Required: Fortune IV        |
|                                    |
|  [ Combine ]        [ Cancel ]     |
+------------------------------------+
```

## Node states

| State | Look | Meaning |
|---|---|---|
| **Unlocked** | Bright icon, colored border | Skill is active |
| **Available** | Normal icon, glowing outline | Can be bought with XP |
| **Locked** | Dark icon, gray lock symbol | Requires a previous node |

Example:

```text
[ XP Efficiency I ] --- [ XP Efficiency II ] --- [ XP Efficiency III ]
       UNLOCKED                  AVAILABLE                    LOCKED
```

## Balance rules

Do **not** let players combine enchantments freely. They must:

1. Unlock the upgrade in the Skill Tree using XP.
2. Have two valid items or books with the required enchantment level.
3. Pay an XP cost.
4. Possibly use a custom block, such as an **Enchant Fusion Altar**.

## First version

For the first playable version, include only:

- Open GUI with `K`
- XP Core with 5 levels
- XP multiplier shown in the GUI
- Mining, Combat, Farming, and Exploration branches with 3 nodes each
- XP levels as the currency
- Simple click-to-upgrade button
- Enchantment Fusion for Fortune IV as the first special upgrade
- Save unlocked skills when the player leaves and rejoins
