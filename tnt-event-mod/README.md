# TNT Event (Fabric, Minecraft 26.2)

Server-side Fabric mod. Both commands can be used by **every** player, no OP needed.

### `/tntexplode`
- Every online player gets **30 seconds of invincibility** (Resistance V, Fire Resistance, Regeneration V).
- **100 lit TNT** spawn on top of every online player (fuses of 3–5 seconds, so they all go off while players are protected).

### `/tntrain`
- For **60 seconds**, lit TNT falls from the sky around every online player like rain: 20 TNT per second per player, landing within 12 blocks.
- Everyone is invincible for the whole rain plus a few seconds after, so the last TNT can't hurt anyone. Players who join mid-rain are protected too.
- Running it again while it's raining restarts the 60 seconds.

## Get the jar
Push to GitHub and open **Actions → Build TNT Event mod**, then download the `tnt-event-mod` artifact.
Use `tnt-event-1.0.0.jar` (not the `-sources` jar).

Or build locally (needs Java 25 and Gradle 9.4+): `cd tnt-event-mod && gradle build` → `build/libs/`.

## Install
Put the jar **and Fabric API** in the server's `mods/` folder. Players do not need the mod on their client.

## Notes
- Invincibility does not stop the void or `/kill`, and TNT knockback still launches people — hold the event over solid ground.
- Both commands spawn a lot of TNT (the rain is about 1,200 TNT per player), which will lag a weak server and destroy terrain (turn off the TNT-explodes gamerule if you want to keep blocks safe).
- Tweak `TNT_PER_PLAYER`, `INVINCIBLE_TICKS`, `SPREAD` and the `RAIN_*` settings at the top of `TntEventMod.java`.
