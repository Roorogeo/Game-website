# TNT Event (Fabric, Minecraft 26.2)

Server-side Fabric mod. All commands can be used by **every** player, no OP needed.

### `/tntexplode`
- Every online player gets **30 seconds of invincibility** (Resistance V, Fire Resistance, Regeneration V).
- **1,000 lit TNT** spawn on top of every online player (fuses of 3–5 seconds, so they all go off while players are protected).

### `/tntrain`
- For **60 seconds**, once every second, **every other block** (checkerboard) within 32 blocks of each online player gets a lit TNT dropped 15–25 blocks above the ground. That's about 1,600 TNT per second per player.
- Players standing close together share the same grid, so TNT isn't doubled up where their areas overlap.
- Everyone is invincible for the whole rain plus a few seconds after, so the last TNT can't hurt anyone. Players who join mid-rain are protected too.
- Running it again while it's raining restarts the 60 seconds.

### `/drain`
- Removes **all water** in every chunk players currently have loaded (the server view distance around each player): water source and flowing blocks, waterlogging on stairs/slabs/etc., and kelp/seagrass/bubble columns.
- Spread over several ticks so big oceans don't freeze the server; chat says how many blocks were removed when it's done.
- It doesn't load new chunks, and water in chunks outside that area stays. Water next to the drained edge can flow back in when something updates it.

### `/spawnall`
- Spawns **100 of every entity** that `/summon` allows (every mob, boss, boat, minecart, projectile, etc.) within 16 blocks of whoever runs it. That's 100 Ender Dragons and 100 Withers too.
- Spawning is spread over a few seconds (200 per tick) so the server doesn't freeze at once; chat says when it's done.
- The result is well over 10,000 entities with AI. Expect heavy lag and a destroyed area. `/kill @e[type=!player]` clears it.

## Get the jar
Push to GitHub and open **Actions → Build TNT Event mod**, then download the `tnt-event-mod` artifact.
Use `tnt-event-1.0.0.jar` (not the `-sources` jar).

Or build locally (needs Java 25 and Gradle 9.4+): `cd tnt-event-mod && gradle build` → `build/libs/`.

## Install
Put the jar **and Fabric API** in the server's `mods/` folder. Players do not need the mod on their client.

## Notes
- Invincibility does not stop the void or `/kill`, and TNT knockback still launches people — hold the event over solid ground.
- The TNT commands spawn a lot of TNT (the rain is about 96,000 TNT per player over the minute, and `/tntexplode` drops 1,000 per player at once), which will lag a weak server and destroy terrain (turn off the TNT-explodes gamerule if you want to keep blocks safe).
- Tweak `TNT_PER_PLAYER`, `INVINCIBLE_TICKS`, `SPREAD` and the `RAIN_*` settings (plus `BLOCKS_PER_TICK` in `WaterDrainer.java` and `PER_TYPE`/`SPREAD` in `EntitySpawner.java`) at the top of `TntEventMod.java`.
