package com.tntevent;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.PrimedTnt;

import java.util.List;

public class TntEventMod implements ModInitializer {
	private static final int TNT_PER_PLAYER = 100;
	private static final int INVINCIBLE_TICKS = 30 * 20; // 30 seconds
	private static final double SPREAD = 3.0;

	@Override
	public void onInitialize() {
		// No .requires(...) on purpose: every player can run this command.
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("tntexplode").executes(ctx -> run(ctx.getSource()))));
	}

	private static int run(CommandSourceStack source) {
		List<ServerPlayer> players = List.copyOf(source.getServer().getPlayerList().getPlayers());

		for (ServerPlayer player : players) {
			makeInvincible(player);
			spawnTnt(player);
		}

		source.sendSuccess(() -> Component.literal("TNT EVENT! " + TNT_PER_PLAYER
				+ " lit TNT dropped on " + players.size() + " player(s). Everyone is invincible for 30 seconds!"), true);
		return players.size();
	}

	private static void makeInvincible(ServerPlayer player) {
		// Resistance V (amplifier 4) blocks all damage that resistance applies to
		player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, INVINCIBLE_TICKS, 4, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, INVINCIBLE_TICKS, 0, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, INVINCIBLE_TICKS, 4, false, true));
	}

	private static void spawnTnt(ServerPlayer player) {
		ServerLevel level = (ServerLevel) player.level();
		RandomSource random = level.getRandom();

		for (int i = 0; i < TNT_PER_PLAYER; i++) {
			double x = player.getX() + (random.nextDouble() - 0.5) * SPREAD;
			double y = player.getY() + 1.0 + random.nextDouble() * 2.0;
			double z = player.getZ() + (random.nextDouble() - 0.5) * SPREAD;

			PrimedTnt tnt = new PrimedTnt(level, x, y, z, player);
			// Default fuse is 80 ticks; stagger them a little for a rolling boom
			tnt.setFuse(60 + random.nextInt(40));
			level.addFreshEntity(tnt);
		}
	}
}
