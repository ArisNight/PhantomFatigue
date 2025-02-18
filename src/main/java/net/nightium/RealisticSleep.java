package net.nightium;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RealisticSleep implements DedicatedServerModInitializer {

	public static final String MOD_ID = "nosleepmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final int DAYS_UNTIL_EFFECTS = 3;
	private static final int TICKS_IN_DAY = 24000;

	private final Map<ServerPlayerEntity, Long> lastSleepTime = new HashMap<>();

	@Override
	public void onInitializeServer() {
		LOGGER.info("Initializing NoSleepMod for Dedicated Server...");

		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

		LOGGER.info("NoSleepMod initialized successfully!");
	}

	private void onServerTick(MinecraftServer server) {
		if (server != null) {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (player == null) continue; // Добавлена проверка на null

				if (player.isSleeping()) {
					lastSleepTime.put(player, (long) server.getTicks());
					LOGGER.debug("Player {} is sleeping. Resetting sleep timer.", player.getName());
				} else {
					// Если игрок не спал DAYS_UNTIL_EFFECTS дней (или если это новый игрок)
					Long lastSleep = lastSleepTime.get(player);
					if (lastSleep == null) {
						lastSleepTime.put(player, (long) server.getTicks());
						LOGGER.debug("New player {} detected. Setting initial sleep timer.", player.getName());
					} else if ((server.getTicks() - lastSleep) > (DAYS_UNTIL_EFFECTS * TICKS_IN_DAY)) {
						applyEffects(player);
					}
				}
			}
		}
	}

	private void applyEffects(ServerPlayerEntity player) {
		StatusEffectInstance slowness = new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 1, false, false, false); // 10 секунд, уровень 2
		StatusEffectInstance weakness = new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 1, false, false, false); // 10 секунд, уровень 2

		player.addStatusEffect(slowness);
		player.addStatusEffect(weakness);
	}
}
