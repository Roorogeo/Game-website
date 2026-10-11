package com.tntevent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Removes all water in the chunks players currently have loaded (within the server view distance).
 * The work is spread over several ticks so the server doesn't freeze on big oceans.
 */
public final class WaterDrainer {
	// Rough cap on blocks changed per tick; a full ocean chunk is ~15k water blocks
	private static final int BLOCKS_PER_TICK = 40_000;
	// Only touch the blocks themselves and tell clients; no neighbour updates, so water doesn't flow back in
	private static final int SET_FLAGS = Block.UPDATE_CLIENTS;

	private record Job(ServerLevel level, int chunkX, int chunkZ) {
	}

	private static final Deque<Job> queue = new ArrayDeque<>();
	private static int drainedBlocks = 0;

	private WaterDrainer() {
	}

	/** Queues every loaded chunk around every player. Returns the number of chunks queued. */
	public static int start(MinecraftServer server) {
		int viewDistance = server.getPlayerList().getViewDistance();
		Set<Job> seen = new HashSet<>(queue);

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			ServerLevel level = (ServerLevel) player.level();
			int centerX = SectionPos.blockToSectionCoord(player.getBlockX());
			int centerZ = SectionPos.blockToSectionCoord(player.getBlockZ());
			for (int dx = -viewDistance; dx <= viewDistance; dx++) {
				for (int dz = -viewDistance; dz <= viewDistance; dz++) {
					Job job = new Job(level, centerX + dx, centerZ + dz);
					if (seen.add(job)) {
						queue.add(job);
					}
				}
			}
		}
		return queue.size();
	}

	public static void tick(MinecraftServer server) {
		if (queue.isEmpty()) {
			return;
		}

		int changedThisTick = 0;
		while (!queue.isEmpty() && changedThisTick < BLOCKS_PER_TICK) {
			Job job = queue.poll();
			changedThisTick += drainChunk(job.level(), job.chunkX(), job.chunkZ());
		}
		drainedBlocks += changedThisTick;

		if (queue.isEmpty()) {
			int total = drainedBlocks;
			drainedBlocks = 0;
			server.getPlayerList().broadcastSystemMessage(
					Component.literal("Drain finished: removed " + total + " water blocks."), false);
		}
	}

	private static int drainChunk(ServerLevel level, int chunkX, int chunkZ) {
		// Never load or generate chunks, only drain ones that are already loaded
		LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
		if (chunk == null) {
			return 0;
		}

		int minX = SectionPos.sectionToBlockCoord(chunkX);
		int minZ = SectionPos.sectionToBlockCoord(chunkZ);
		int changed = 0;
		LevelChunkSection[] sections = chunk.getSections();
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		for (int i = 0; i < sections.length; i++) {
			LevelChunkSection section = sections[i];
			if (section.hasOnlyAir() || !section.getStates().maybeHas(state -> !state.getFluidState().isEmpty())) {
				continue;
			}

			int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					for (int x = 0; x < 16; x++) {
						BlockState state = section.getBlockState(x, y, z);
						BlockState drained = drained(state);
						if (drained != null) {
							mutable.set(minX + x, baseY + y, minZ + z);
							level.setBlock(mutable, drained, SET_FLAGS);
							changed++;
						}
					}
				}
			}
		}
		return changed;
	}

	/** What a block should become with its water removed, or null if it holds no water. */
	private static BlockState drained(BlockState state) {
		if (!state.getFluidState().is(FluidTags.WATER)) {
			return null;
		}
		// Waterlogged stairs, slabs, fences, etc. keep the block and just lose the water
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			return state.setValue(BlockStateProperties.WATERLOGGED, false);
		}
		// Water itself, plus blocks that only exist in water (kelp, seagrass, bubble columns)
		return Blocks.AIR.defaultBlockState();
	}
}
