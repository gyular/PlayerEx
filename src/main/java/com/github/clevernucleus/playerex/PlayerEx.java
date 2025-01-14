package com.github.clevernucleus.playerex;

import com.github.clevernucleus.dataattributes.api.event.EntityAttributeModifiedEvents;
import com.github.clevernucleus.playerex.api.ExAPI;
import com.github.clevernucleus.playerex.api.event.LivingEntityEvents;
import com.github.clevernucleus.playerex.api.event.PlayerEntityEvents;
import com.github.clevernucleus.playerex.config.ConfigImpl;
import com.github.clevernucleus.playerex.factory.DamageFactory;
import com.github.clevernucleus.playerex.factory.EventFactory;
import com.github.clevernucleus.playerex.factory.ExScreenFactory;
import com.github.clevernucleus.playerex.factory.NetworkFactory;
import com.github.clevernucleus.playerex.factory.PlaceholderFactory;
import com.github.clevernucleus.playerex.factory.RefundFactory;
import com.github.clevernucleus.playerex.impl.CommandsImpl;

import eu.pb4.placeholders.PlaceholderAPI;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PlayerEx implements ModInitializer {
	public static final ScreenHandlerType<ExScreenFactory.Handler> EX_SCREEN = Registry.register(Registry.SCREEN_HANDLER, new Identifier(ExAPI.MODID, "ex_screen"), ExScreenFactory.type());
	public static final SoundEvent LEVEL_UP_SOUND = new SoundEvent(new Identifier(ExAPI.MODID, "level_up"));
	public static final SoundEvent SP_SPEND_SOUND = new SoundEvent(new Identifier(ExAPI.MODID, "sp_spend"));
	
	@Override
	public void onInitialize() {
		AutoConfig.register(ConfigImpl.class, GsonConfigSerializer::new);
		
		ServerLoginNetworking.registerGlobalReceiver(NetworkFactory.CONFIG, NetworkFactory::loginQueryResponse);
		ServerPlayNetworking.registerGlobalReceiver(NetworkFactory.SCREEN, NetworkFactory::switchScreen);
		ServerPlayNetworking.registerGlobalReceiver(NetworkFactory.MODIFY, NetworkFactory::modifyAttributes);
		
		CommandRegistrationCallback.EVENT.register(CommandsImpl::register);
		ServerLoginConnectionEvents.QUERY_START.register(NetworkFactory::loginQueryStart);
		ServerLifecycleEvents.SERVER_STARTING.register(EventFactory::serverStarting);
		ServerPlayerEvents.COPY_FROM.register(EventFactory::reset);
		LivingEntityEvents.ON_HEAL.register(EventFactory::healed);
		LivingEntityEvents.EVERY_SECOND.register(EventFactory::healthRegeneration);
		LivingEntityEvents.ON_DAMAGE.register(EventFactory::onDamage);
		LivingEntityEvents.SHOULD_DAMAGE.register(EventFactory::shouldDamage);
		PlayerEntityEvents.ON_CRIT.register(EventFactory::onCritAttack);
		PlayerEntityEvents.SHOULD_CRIT.register(EventFactory::attackIsCrit);
		EntityAttributeModifiedEvents.CLAMPED.register(EventFactory::clamped);
		
		DamageFactory.STORE.forEach(ExAPI::registerDamageModification);
		RefundFactory.STORE.forEach(ExAPI::registerRefundCondition);
		PlaceholderFactory.STORE.forEach(PlaceholderAPI::register);
		
		Registry.register(Registry.SOUND_EVENT, LEVEL_UP_SOUND.getId(), LEVEL_UP_SOUND);
		Registry.register(Registry.SOUND_EVENT, SP_SPEND_SOUND.getId(), SP_SPEND_SOUND);
	}
}
