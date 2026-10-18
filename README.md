![Banner for Leafitbe](/assets/images/banner.png)

**Leaf decay that doesn't keep you waiting.** In vanilla, a chopped tree leaves its canopy hanging in the air for a minute
or more while leaves drop one random tick at a time. Leafitbe makes the whole canopy break up in a few seconds.

When a log or leaf is broken, burned or blown up, nearby leaves are checked straight away, and every leaf that decays
checks its own neighbours, so the decay spreads through the tree.

---

![bStats](https://bstats.org/signatures/bukkit/leafitbe.svg)

---

## Features

### Decay

- **Fast decay** - leaves near a broken, burned or exploded log or leaf are checked right away instead of waiting for random ticks
- **Natural spread** - each leaf waits a random delay (`min-delay` to `max-delay`), so the canopy breaks up gradually instead of vanishing at once
- **Off switch** - set `decay.enabled: false` and leaves never decay

### Rules

- **Configurable reach** - how far a leaf can be from a log before it decays, from 1 to 16 blocks (vanilla is 6)
- **Diagonal connections** - leaves that only touch at an edge or corner can count as connected, so oddly shaped trees don't leave floating leaves behind
- Uses the same blocks as vanilla: anything in the `prevents_nearby_leaf_decay` tag counts as a log
- Leaves placed by players never decay

### Vanilla feel

- Saplings, sticks and apples **drop** like vanilla (`decay.drops`)
- Break **particles and sound** play (`decay.effects`)
- Fires the normal `LeavesDecayEvent`, so claim and protection plugins can still stop leaves from decaying

---

## Commands & permissions

| Command             | Description                  | Permission                |
|---------------------|------------------------------|---------------------------|
| `/leafitbe reload`  | Reload `config.yml` from disk | `leafitbe.command.reload` |
| `/leafitbe version` | Show plugin version          | -                         |

`/leaf` is an alias for `/leafitbe`.

---

## Configuration

Server-wide options live in `plugins/Leafitbe/config.yml`, copied on first run. Edit, then `/leafitbe reload`.

```yaml
# How far a leaf can be from a log before it decays, in leaf steps. Vanilla is 6, the most is 16.
max-distance: 6

# Leaves that only touch at an edge or corner count as connected. Vanilla needs a shared face.
diagonal: true

decay:
  enabled: true   # false = leaves never decay
  drops: true     # drop saplings, sticks and apples
  effects: true   # break particles and sound

  fast:
    enabled: true # check nearby leaves when a log or leaf is broken, burned or blown up
    min-delay: 4  # random delay per leaf, in ticks (20 ticks = 1 second)
    max-delay: 12
```

Built for Paper 26.2.
