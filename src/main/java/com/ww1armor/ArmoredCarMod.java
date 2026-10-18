package com.ww1armor;

import com.ww1armor.entity.ArmoredCarEntity;
import com.ww1armor.entity.HullSegmentEntity;
import com.ww1armor.item.ArmoredCarItem;
import com.ww1armor.network.FireGunPayload;
import com.ww1armor.network.ReloadPayload;
import com.ww1armor.network.ToggleEnginePayload;
import com.ww1armor.network.TracerPayload;
import com.ww1armor.network.SwitchSeatPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArmoredCarMod implements ModInitializer {
    public static final String MOD_ID = "ww1armor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static EntityType<ArmoredCarEntity> BUSSING_A5P;
    public static EntityType<HullSegmentEntity> HULL_SEGMENT;
    public static Item BUSSING_A5P_ITEM;
    public static SoundEvent MG_FIRE;
    public static SoundEvent RICOCHET;
    public static SoundEvent ENGINE;
    public static SoundEvent ARMOR_HIT;
    public static SoundEvent MG_FIRE_FAR;
    public static SoundEvent MG_TAIL;
    public static SoundEvent MG_TAIL_FAR;
    public static SoundEvent ENGINE_START;
    public static SoundEvent ENGINE_STOP;
    public static SoundEvent ENGINE_RAMP;

    @Override
    public void onInitialize() {
        ModConfig.get(); // load / create config/ww1armor.json

        // Main hitbox covers the middle 2.75 m of the 8 m hull; two HullSegmentEntity boxes cover nose and tail.
        BUSSING_A5P = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("bussing_a5p"),
                EntityType.Builder.<ArmoredCarEntity>of(ArmoredCarEntity::new, MobCategory.MISC)
                        .sized(2.75F, 3.8F)
                        .fireImmune()
                        .clientTrackingRange(10)
                        .build("bussing_a5p"));
        HULL_SEGMENT = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("hull_segment"),
                EntityType.Builder.<HullSegmentEntity>of(HullSegmentEntity::new, MobCategory.MISC)
                        .sized(2.75F, 2.6F)
                        .fireImmune()
                        .noSave()
                        .noSummon()
                        .clientTrackingRange(10)
                        .build("hull_segment"));

        BUSSING_A5P_ITEM = Registry.register(BuiltInRegistries.ITEM, id("bussing_a5p"),
                new ArmoredCarItem(new Item.Properties().stacksTo(1)));

        MG_FIRE = registerSound("mg_fire");
        RICOCHET = registerSound("ricochet");
        ENGINE = registerSound("engine");
        ARMOR_HIT = registerSound("armor_hit");
        MG_FIRE_FAR = registerSound("mg_fire_far");
        MG_TAIL = registerSound("mg_tail");
        MG_TAIL_FAR = registerSound("mg_tail_far");
        ENGINE_START = registerSound("engine_start");
        ENGINE_STOP = registerSound("engine_stop");
        ENGINE_RAMP = registerSound("engine_ramp");

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> entries.accept(BUSSING_A5P_ITEM));

        PayloadTypeRegistry.playC2S().register(SwitchSeatPayload.TYPE, SwitchSeatPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FireGunPayload.TYPE, FireGunPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ReloadPayload.TYPE, ReloadPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleEnginePayload.TYPE, ToggleEnginePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TracerPayload.TYPE, TracerPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleEnginePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            player.getServer().execute(() -> {
                if (player.getVehicle() instanceof ArmoredCarEntity car) {
                    car.toggleEngine(player);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(ReloadPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            player.getServer().execute(() -> {
                if (player.getVehicle() instanceof ArmoredCarEntity car) {
                    car.startReload(player);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SwitchSeatPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            player.getServer().execute(() -> {
                if (player.getVehicle() instanceof ArmoredCarEntity car) {
                    car.switchSeat(player);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(FireGunPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            player.getServer().execute(() -> {
                if (player.getVehicle() instanceof ArmoredCarEntity car) {
                    car.fireGun(player);
                }
            });
        });

        // The crew sit inside the armour: bullets and blows aimed at a crew member hit the hull instead.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getVehicle() instanceof ArmoredCarEntity car && !car.isWrecked()) {
                if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                    return true;
                }
                Entity direct = source.getDirectEntity();
                if (direct != null && direct != entity && direct != car && !car.hasPassenger(direct)) {
                    car.hurt(source, amount);
                    return false;
                }
            }
            return true;
        });

        LOGGER.info("WW1 Armored Cars (Büssing A5P) loaded");
    }

    private static SoundEvent registerSound(String name) {
        ResourceLocation rl = id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, rl, SoundEvent.createVariableRangeEvent(rl));
    }
}
