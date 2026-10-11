# TNT Event (Fabric, Minecraft 26.2)

Server-side Fabric mod. All commands can be used by **every** player, no OP needed.

### `/tntexplode`
- Every online player gets **30 seconds of invincibility** (Resistance V, Fire Resistance, Regeneration V).
- **100 lit TNT** spawn on top of every online player (fuses of 3–5 seconds, so they all go off while players are protected).

### `/tntrain`
- For **60 seconds**, lit TNT falls from the sky around every online player like rain: 100 TNT per second per player, landing anywhere within 240 blocks, 15–25 blocks above the ground.
- TNT only spawns in chunks inside the server's simulation distance (default 10 chunks = 160 blocks), so the real radius is capped by that. Raise `simulation-distance` in `server.properties` to use the full 240.
- Everyone is invincible for the whole rain plus a few seconds after, so the last TNT can't hurt anyone. Players who join mid-rain are protected too.
- Running it again while it's raining restarts the 60 seconds.

### `/drain`
- Removes **all water** in every chunk players currently have loaded (the server view distance around each player): water source and flowing blocks, waterlogging on stairs/slabs/etc., and kelp/seagrass/bubble columns.
- Spread over several ticks so big oceans don't freeze the server; chat says how many blocks were removed when it's done.
- It doesn't load new chunks, and water in chunks outside that area stays. Water next to the drained edge can flow back in when something updates it.

## Get the jar
Push to GitHub and open **Actions → Build TNT Event mod**, then download the `tnt-event-mod` artifact.
Use `tnt-event-1.0.0.jar` (not the `-sources` jar).

Or build locally (needs Java 25 and Gradle 9.4+): `cd tnt-event-mod && gradle build` → `build/libs/`.

## Install
Put the jar **and Fabric API** in the server's `mods/` folder. Players do not need the mod on their client.

## Notes
- Invincibility does not stop the void or `/kill`, and TNT knockback still launches people — hold the event over solid ground.
- Both commands spawn a lot of TNT (the rain is up to 6,000 TNT per player), which will lag a weak server and destroy terrain (turn off the TNT-explodes gamerule if you want to keep blocks safe).
- Tweak `TNT_PER_PLAYER`, `INVINCIBLE_TICKS`, `SPREAD` and the `RAIN_*` settings (and `BLOCKS_PER_TICK` in `WaterDrainer.java`) at the top of `TntEventMod.java`.
