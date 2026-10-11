# TNT Event (Fabric, Minecraft 26.2)

Server-side Fabric mod. `/tntexplode` — usable by **every** player, no OP needed:

- Every online player gets **30 seconds of invincibility** (Resistance V, Fire Resistance, Regeneration V).
- **100 lit TNT** spawn on top of every online player (fuses of 3–5 seconds, so they all go off while players are protected).

## Get the jar
Push to GitHub and open **Actions → Build TNT Event mod**, then download the `tnt-event-mod` artifact.
Use `tnt-event-1.0.0.jar` (not the `-sources` jar).

Or build locally (needs Java 25 and Gradle 9): `cd tnt-event-mod && gradle build` → `build/libs/`.

## Install
Put the jar **and Fabric API** in the server's `mods/` folder. Players do not need the mod on their client.

## Notes
- Invincibility does not stop the void or `/kill`, and TNT knockback still launches people — hold the event over solid ground.
- 100 TNT × many players is a lot of entities and will lag a weak server, and it will destroy terrain (turn off the TNT-explodes gamerule if you want to keep blocks safe).
- Tweak `TNT_PER_PLAYER`, `INVINCIBLE_TICKS` and `SPREAD` at the top of `TntEventMod.java`.
