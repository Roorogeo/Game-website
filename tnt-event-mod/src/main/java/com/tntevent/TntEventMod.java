package com.tntevent;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.PrimedTnt;

import java.util.List;

public class TntEventMod implements ModInitializer {
	// /tntexplode
	private static final int TNT_PER_PLAYER = 100;
	private static final int INVINCIBLE_TICKS = 30 * 20; // 30 seconds
	private static final double SPREAD = 3.0;

	// /tntrain
	private static final int RAIN_TICKS = 60 * 20; // 60 seconds
	private static final int RAIN_TNT_PER_PLAYER_PER_TICK = 1; // 20 TNT per second per player
	private static final double RAIN_RADIUS = 12.0;
	private static final double RAIN_MIN_HEIGHT = 15.0;
	private static final double RAIN_MAX_HEIGHT = 25.0;
	// Keep players protected until the last TNT that fell has gone off
	private static final int RAIN_PROTECTION_BUFFER_TICKS = 6 * 20;

	private static int rainTicksLeft = 0;

	@Override
	public void onInitialize() {
		// No .requires(...) on purpose: every player can run these commands.
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("tntexplode").executes(ctx -> explode(ctx.getSource())));
			dispatcher.register(Commands.literal("tntrain").executes(ctx -> startRain(ctx.getSource())));
		});

		ServerTickEvents.END_SERVER_TICK.register(TntEventMod::tickRain);
	}

	private static int explode(CommandSourceStack source) {
		List<ServerPlayer> players = List.copyOf(source.getServer().getPlayerList().getPlayers());

		for (ServerPlayer player : players) {
			makeInvincible(player, INVINCIBLE_TICKS);
			spawnTntOn(player);
		}

		source.sendSuccess(() -> Component.literal("TNT EVENT! " + TNT_PER_PLAYER
				+ " lit TNT dropped on " + players.size() + " player(s). Everyone is invincible for 30 seconds!"), true);
		return players.size();
	}

	private static int startRain(CommandSourceStack source) {
		// Running it again while it is already raining restarts the 60 second timer
		rainTicksLeft = RAIN_TICKS;
		source.sendSuccess(() -> Component.literal("It's raining TNT for 60 seconds! Everyone is invincible until it stops."), true);
		return 1;
	}

	private static void tickRain(MinecraftServer server) {
		if (rainTicksLeft <= 0) {
			return;
		}

		boolean refreshProtection = rainTicksLeft % 20 == 0 || rainTicksLeft == RAIN_TICKS;
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			// Refresh every second so players who join mid-rain are covered too
			if (refreshProtection) {
				makeInvincible(player, rainTicksLeft + RAIN_PROTECTION_BUFFER_TICKS);
			}
			for (int i = 0; i < RAIN_TNT_PER_PLAYER_PER_TICK; i++) {
				spawnRainTnt(player);
			}
		}

		rainTicksLeft--;
		if (rainTicksLeft == 0) {
			server.getPlayerList().broadcastSystemMessage(Component.literal("The TNT rain has stopped."), false);
		}
	}

	private static void makeInvincible(ServerPlayer player, int ticks) {
		// Resistance V (amplifier 4) blocks all damage that resistance applies to
		player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, ticks, 4, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, ticks, 0, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ticks, 4, false, true));
	}

	private static void spawnTntOn(ServerPlayer player) {
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

	private static void spawnRainTnt(ServerPlayer player) {
		ServerLevel level = (ServerLevel) player.level();
		RandomSource random = level.getRandom();

		// Random point in a circle around the player, high up in the sky
		double angle = random.nextDouble() * Math.PI * 2;
		double distance = Math.sqrt(random.nextDouble()) * RAIN_RADIUS;
		double x = player.getX() + Math.cos(angle) * distance;
		double z = player.getZ() + Math.sin(angle) * distance;
		double y = player.getY() + RAIN_MIN_HEIGHT + random.nextDouble() * (RAIN_MAX_HEIGHT - RAIN_MIN_HEIGHT);

		PrimedTnt tnt = new PrimedTnt(level, x, y, z, player);
		// Long enough to land before exploding
		tnt.setFuse(70 + random.nextInt(30));
		level.addFreshEntity(tnt);
	}
}
