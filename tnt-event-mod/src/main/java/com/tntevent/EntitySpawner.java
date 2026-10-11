package com.tntevent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Spawns a fixed number of every summonable entity type around a point.
 * Spawning is spread over several ticks so the server doesn't freeze.
 */
public final class EntitySpawner {
	private static final int PER_TYPE = 100;
	private static final int SPAWNS_PER_TICK = 200;
	private static final int SPREAD = 16;

	private static final class Job {
		final ServerLevel level;
		final BlockPos center;
		final EntityType<?> type;
		int remaining = PER_TYPE;

		Job(ServerLevel level, BlockPos center, EntityType<?> type) {
			this.level = level;
			this.center = center;
			this.type = type;
		}
	}

	private static final Deque<Job> queue = new ArrayDeque<>();

	private EntitySpawner() {
	}

	/** Queues every summonable entity type at the given spot. Returns the number of types queued. */
	public static int start(ServerLevel level, BlockPos center) {
		int types = 0;
		for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
			// Skips players, fishing bobbers and anything else /summon refuses, plus disabled experimental mobs
			if (type == EntityType.PLAYER || !type.canSummon() || !type.isEnabled(level.enabledFeatures())) {
				continue;
			}
			queue.add(new Job(level, center, type));
			types++;
		}
		return types;
	}

	public static void tick(MinecraftServer server) {
		int budget = SPAWNS_PER_TICK;
		while (budget > 0 && !queue.isEmpty()) {
			Job job = queue.peek();
			RandomSource random = job.level.getRandom();
			while (budget > 0 && job.remaining > 0) {
				BlockPos pos = job.center.offset(random.nextInt(SPREAD * 2 + 1) - SPREAD, 0,
						random.nextInt(SPREAD * 2 + 1) - SPREAD);
				job.type.spawn(job.level, pos, EntitySpawnReason.COMMAND);
				job.remaining--;
				budget--;
			}
			if (job.remaining == 0) {
				queue.poll();
				if (queue.isEmpty()) {
					server.getPlayerList().broadcastSystemMessage(
							Component.literal("Finished spawning " + PER_TYPE + " of every entity."), false);
				}
			}
		}
	}
}
