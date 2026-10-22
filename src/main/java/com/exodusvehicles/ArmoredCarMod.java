package com.exodusvehicles;

import com.exodusvehicles.entity.ArmoredCarEntity;
import com.exodusvehicles.entity.HullSegmentEntity;
import com.exodusvehicles.entity.TruckEntity;
import com.exodusvehicles.item.ArmoredCarItem;
import com.exodusvehicles.network.FireGunPayload;
import com.exodusvehicles.network.ReloadPayload;
import com.exodusvehicles.network.ToggleEnginePayload;
import com.exodusvehicles.network.ToggleBrakePayload;
import com.exodusvehicles.network.TracerPayload;
import com.exodusvehicles.network.SwitchSeatPayload;
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
    public static final String MOD_ID = "exodus_vehicles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static EntityType<ArmoredCarEntity> EISENWOLF;
    public static EntityType<HullSegmentEntity> HULL_SEGMENT;
    public static Item EISENWOLF_ITEM;
    public static EntityType<TruckEntity> DAIMLER_DR;
    public static EntityType<HullSegmentEntity> TRUCK_SEGMENT;
    public static Item DAIMLER_DR_ITEM;
    public static EntityType<TruckEntity> DAIMLER_DR_OPEN;
    public static Item DAIMLER_DR_OPEN_ITEM;
    public static net.minecraft.world.inventory.MenuType<com.exodusvehicles.menu.CargoMenu> CARGO_MENU;
    public static EntityType<com.exodusvehicles.entity.MorserEntity> MORSER;
    public static Item MORSER_ITEM;
    public static Item SHELL_ITEM;
    public static EntityType<com.exodusvehicles.entity.FieldGunEntity> FIELD_GUN;
    public static EntityType<com.exodusvehicles.entity.FieldMgEntity> FIELD_MG, FIELD_MG_SHIELD;
    public static Item FIELD_MG_ITEM, FIELD_MG_SHIELD_ITEM;
    public static Item FIELD_GUN_ITEM;
    public static Item FIELD_SHELL_ITEM;
    public static net.minecraft.world.level.block.Block FIELD_SHELL_PILE;
    public static Item FIELD_SHELL_PILE_ITEM;
    public static SoundEvent FK_CREW, FK_CLOSE, FK_MID, FK_DISTANT, FK_FAR, FK_RELOAD, FK_MOVE;
    public static SoundEvent MORSER_CLOSE;
    public static SoundEvent MORSER_DISTANT;
    public static SoundEvent MORSER_MID;
    public static SoundEvent SHELL_IMPACT_MID;
    public static SoundEvent MORSER_FAR;
    public static SoundEvent MORSER_RELOAD;
    public static SoundEvent SHELL_WHISTLE;
    public static SoundEvent BREECH_OPEN;
    public static SoundEvent BREECH_CLOSE;
    public static SoundEvent GUN_TRAVERSE;
    public static SoundEvent GUN_PUSH;
    public static SoundEvent SHELL_IMPACT_CLOSE;
    public static SoundEvent SHELL_IMPACT_FAR;
    public static SoundEvent SHELL_SHOCK;
    public static SoundEvent SHELL_FLIGHT;
    public static SoundEvent REPAIR;
    public static SoundEvent REFUEL;
    public static Item JERRY_CAN;
    public static Item JERRY_CAN_EMPTY;
    public static Item WRENCH;
    public static Item MG_BELT_ITEM;
    public static net.minecraft.world.level.block.Block SHELL_PILE;
    public static Item SHELL_PILE_ITEM;
    public static net.minecraft.world.level.block.Block SERVICE_STATION;
    public static net.minecraft.world.level.block.entity.BlockEntityType<com.exodusvehicles.block.ServiceStationBlockEntity> SERVICE_STATION_BE;
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
    public static SoundEvent MG_FIRE_INSIDE;
    public static SoundEvent RELOAD_OUT;
    public static SoundEvent RELOAD_IN;
    public static SoundEvent IMPACT_GROUND;
    public static SoundEvent BULLET_FLYBY;

    @Override
    public void onInitialize() {
        ModConfig.get(); // load / create config/exodus_vehicles.json

        // Main hitbox covers the middle 2.75 m of the 8 m hull; two HullSegmentEntity boxes cover nose and tail.
        EISENWOLF = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("eisenwolf"),
                EntityType.Builder.<ArmoredCarEntity>of(ArmoredCarEntity::new, MobCategory.MISC)
                        .sized(2.75F, 3.8F)
                        .fireImmune()
                        .clientTrackingRange(10)
                        .build("eisenwolf"));
        HULL_SEGMENT = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("hull_segment"),
                EntityType.Builder.<HullSegmentEntity>of(HullSegmentEntity::new, MobCategory.MISC)
                        .sized(2.75F, 2.6F)
                        .fireImmune()
                        .noSave()
                        .noSummon()
                        .clientTrackingRange(10)
                        .build("hull_segment"));

        EISENWOLF_ITEM = Registry.register(BuiltInRegistries.ITEM, id("eisenwolf"),
                new ArmoredCarItem(new Item.Properties().stacksTo(1), () -> EISENWOLF));

        // Daimler-Marienfelde DR lorry: 6.6 m long, 2.1 m wide; main box in the middle, one segment at each end
        DAIMLER_DR = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("daimler_dr"),
                EntityType.Builder.<TruckEntity>of(TruckEntity::new, MobCategory.MISC)
                        .sized(2.1F, 2.75F)
                        .clientTrackingRange(10)
                        .build("daimler_dr"));
        DAIMLER_DR_OPEN = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("daimler_dr_open"),
                EntityType.Builder.<TruckEntity>of(TruckEntity::new, MobCategory.MISC)
                        .sized(2.1F, 2.75F)
                        .clientTrackingRange(10)
                        .build("daimler_dr_open"));
        TRUCK_SEGMENT = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("truck_segment"),
                EntityType.Builder.<HullSegmentEntity>of(HullSegmentEntity::new, MobCategory.MISC)
                        .sized(2.1F, 2.75F)
                        .noSave()
                        .noSummon()
                        .clientTrackingRange(10)
                        .build("truck_segment"));
        DAIMLER_DR_ITEM = Registry.register(BuiltInRegistries.ITEM, id("daimler_dr"),
                new ArmoredCarItem(new Item.Properties().stacksTo(1), () -> DAIMLER_DR));
        DAIMLER_DR_OPEN_ITEM = Registry.register(BuiltInRegistries.ITEM, id("daimler_dr_open"),
                new ArmoredCarItem(new Item.Properties().stacksTo(1), () -> DAIMLER_DR_OPEN));

        MORSER = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("morser16"),
                EntityType.Builder.<com.exodusvehicles.entity.MorserEntity>of(com.exodusvehicles.entity.MorserEntity::new, MobCategory.MISC)
                        .sized(2.4F, 2.2F)
                        .fireImmune()
                        .clientTrackingRange(12)
                        .updateInterval(2)
                        .build("morser16"));
        MORSER_ITEM = Registry.register(BuiltInRegistries.ITEM, id("morser16"),
                new ArmoredCarItem(new Item.Properties().stacksTo(1), () -> MORSER));
        SHELL_ITEM = Registry.register(BuiltInRegistries.ITEM, id("shell_21cm"), new Item(new Item.Properties().stacksTo(1)));
        // shells don't stack (one per slot), but a player may carry as many as they have room for
        FIELD_GUN = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("fk96"),
                EntityType.Builder.<com.exodusvehicles.entity.FieldGunEntity>of(com.exodusvehicles.entity.FieldGunEntity::new, MobCategory.MISC)
                        .sized(1.6F, 1.5F)
                        .fireImmune()
                        .clientTrackingRange(12)
                        .updateInterval(2)
                        .build("fk96"));
        FIELD_GUN_ITEM = Registry.register(BuiltInRegistries.ITEM, id("fk96"),
                new ArmoredCarItem(new Item.Properties().stacksTo(1), () -> FIELD_GUN));
        FIELD_MG = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("mg08"),
                EntityType.Builder.<com.exodusvehicles.entity.FieldMgEntity>of(com.exodusvehicles.entity.FieldMgEntity::new, MobCategory.MISC)
                        .sized(0.9F, 0.9F).fireImmune().clientTrackingRange(10).build("mg08"));
        FIELD_MG_SHIELD = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("mg08_shielded"),
                EntityType.Builder.<com.exodusvehicles.entity.FieldMgEntity>of(com.exodusvehicles.entity.FieldMgEntity::new, MobCategory.MISC)
                        .sized(0.9F, 0.9F).fireImmune().clientTrackingRange(10).build("mg08_shielded"));
        FIELD_MG_ITEM = Registry.register(BuiltInRegistries.ITEM, id("mg08"),
                new ArmoredCarItem(new Item.Properties().stacksTo(1), () -> FIELD_MG));
        FIELD_MG_SHIELD_ITEM = Registry.register(BuiltInRegistries.ITEM, id("mg08_shielded"),
                new ArmoredCarItem(new Item.Properties().stacksTo(1), () -> FIELD_MG_SHIELD));
        FIELD_SHELL_ITEM = Registry.register(BuiltInRegistries.ITEM, id("shell_77mm"), new Item(new Item.Properties().stacksTo(1)));
        FK_CREW = registerSound("fk_crew");
        FK_CLOSE = registerSound("fk_close");
        FK_MID = registerSound("fk_mid");
        FK_DISTANT = registerSound("fk_distant");
        FK_FAR = registerSound("fk_far");
        FK_RELOAD = registerSound("fk_reload");
        FK_MOVE = registerSound("fk_move");
        com.exodusvehicles.artillery.ArtilleryRules.register();
        MORSER_CLOSE = registerSound("morser_close");
        MORSER_DISTANT = registerSound("morser_distant");
        MORSER_MID = registerSound("morser_mid");
        SHELL_IMPACT_MID = registerSound("shell_impact_mid");
        MORSER_FAR = registerSound("morser_far");
        MORSER_RELOAD = registerSound("morser_reload");
        SHELL_WHISTLE = registerSound("shell_whistle");
        BREECH_OPEN = registerSound("breech_open");
        BREECH_CLOSE = registerSound("breech_close");
        GUN_TRAVERSE = registerSound("gun_traverse");
        GUN_PUSH = registerSound("gun_push");
        SHELL_IMPACT_CLOSE = registerSound("shell_impact_close");
        SHELL_IMPACT_FAR = registerSound("shell_impact_far");
        SHELL_SHOCK = registerSound("shell_shock");
        SHELL_FLIGHT = registerSound("shell_flight");
        REPAIR = registerSound("repair");
        REFUEL = registerSound("refuel");

        // supplies: fuel, repair, ammunition
        JERRY_CAN = Registry.register(BuiltInRegistries.ITEM, id("jerry_can"),
                new com.exodusvehicles.item.JerryCanItem(new Item.Properties().durability(com.exodusvehicles.item.JerryCanItem.DECILITRES)));
        JERRY_CAN_EMPTY = Registry.register(BuiltInRegistries.ITEM, id("jerry_can_empty"), new Item(new Item.Properties().stacksTo(16)));
        WRENCH = Registry.register(BuiltInRegistries.ITEM, id("wrench"),
                new com.exodusvehicles.item.WrenchItem(new Item.Properties().durability(1200)));
        MG_BELT_ITEM = Registry.register(BuiltInRegistries.ITEM, id("mg_belt"), new Item(new Item.Properties().stacksTo(8)));
        SHELL_PILE = Registry.register(BuiltInRegistries.BLOCK, id("shell_pile"),
                new com.exodusvehicles.block.ShellPileBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                        .strength(1.5F).sound(net.minecraft.world.level.block.SoundType.METAL).noOcclusion()
                        .dynamicShape(), true, () -> SHELL_ITEM));
        SHELL_PILE_ITEM = Registry.register(BuiltInRegistries.ITEM, id("shell_pile"),
                new net.minecraft.world.item.BlockItem(SHELL_PILE, new Item.Properties()));
        FIELD_SHELL_PILE = Registry.register(BuiltInRegistries.BLOCK, id("field_shell_pile"),
                new com.exodusvehicles.block.ShellPileBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                        .strength(1.0F).sound(net.minecraft.world.level.block.SoundType.METAL).noOcclusion(), false, () -> FIELD_SHELL_ITEM));
        FIELD_SHELL_PILE_ITEM = Registry.register(BuiltInRegistries.ITEM, id("field_shell_pile"),
                new net.minecraft.world.item.BlockItem(FIELD_SHELL_PILE, new Item.Properties()));

        SERVICE_STATION = Registry.register(BuiltInRegistries.BLOCK, id("service_station"),
                new com.exodusvehicles.block.ServiceStationBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                        .strength(3.0F, 6.0F).sound(net.minecraft.world.level.block.SoundType.METAL).noOcclusion()));
        Registry.register(BuiltInRegistries.ITEM, id("service_station"), new net.minecraft.world.item.BlockItem(SERVICE_STATION, new Item.Properties()));
        SERVICE_STATION_BE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("service_station"),
                net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(com.exodusvehicles.block.ServiceStationBlockEntity::new,
                        SERVICE_STATION).build(null));

        // everything in one creative tab
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("main"),
                net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder()
                        .icon(() -> new net.minecraft.world.item.ItemStack(EISENWOLF_ITEM))
                        .title(net.minecraft.network.chat.Component.translatable("itemGroup.exodus_vehicles"))
                        .displayItems((params, out) -> {
                            out.accept(EISENWOLF_ITEM);
                            out.accept(DAIMLER_DR_ITEM);
                            out.accept(DAIMLER_DR_OPEN_ITEM);
                            out.accept(MORSER_ITEM);
                            out.accept(SHELL_ITEM);
                            out.accept(SHELL_PILE_ITEM);
                            out.accept(FIELD_MG_ITEM);
                            out.accept(FIELD_MG_SHIELD_ITEM);
                            out.accept(FIELD_GUN_ITEM);
                            out.accept(FIELD_SHELL_ITEM);
                            out.accept(FIELD_SHELL_PILE_ITEM);
                            out.accept(MG_BELT_ITEM);
                            out.accept(JERRY_CAN);
                            out.accept(JERRY_CAN_EMPTY);
                            out.accept(WRENCH);
                            out.accept(SERVICE_STATION);
                        })
                        .build());
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(
                com.exodusvehicles.artillery.ShellManager::tick);
        PayloadTypeRegistry.playS2C().register(com.exodusvehicles.network.ShellFiredPayload.TYPE,
                com.exodusvehicles.network.ShellFiredPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(com.exodusvehicles.network.SuppressPayload.TYPE,
                com.exodusvehicles.network.SuppressPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(com.exodusvehicles.network.HitMarkerPayload.TYPE,
                com.exodusvehicles.network.HitMarkerPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(com.exodusvehicles.network.ShellImpactPayload.TYPE,
                com.exodusvehicles.network.ShellImpactPayload.CODEC);

        CARGO_MENU = Registry.register(BuiltInRegistries.MENU, id("truck_cargo"),
                new net.minecraft.world.inventory.MenuType<>(com.exodusvehicles.menu.CargoMenu::new,
                        net.minecraft.world.flag.FeatureFlags.VANILLA_SET));

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
        MG_FIRE_INSIDE = registerSound("mg_fire_inside");
        RELOAD_OUT = registerSound("reload_out");
        RELOAD_IN = registerSound("reload_in");
        IMPACT_GROUND = registerSound("impact_ground");
        BULLET_FLYBY = registerSound("bullet_flyby");



        PayloadTypeRegistry.playC2S().register(SwitchSeatPayload.TYPE, SwitchSeatPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FireGunPayload.TYPE, FireGunPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ReloadPayload.TYPE, ReloadPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleEnginePayload.TYPE, ToggleEnginePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleBrakePayload.TYPE, ToggleBrakePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleBrakePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            player.getServer().execute(() -> {
                if (player.getVehicle() instanceof ArmoredCarEntity car) {
                    car.toggleParkingBrake(player);
                }
            });
        });
        PayloadTypeRegistry.playS2C().register(TracerPayload.TYPE, TracerPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleEnginePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            player.getServer().execute(() -> {
                if (player.getVehicle() instanceof ArmoredCarEntity car) {
                    car.toggleEngine(player);
                } else if (player.getVehicle() instanceof com.exodusvehicles.entity.MorserEntity gun) {
                    gun.toggleDeploy(player);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(ReloadPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            player.getServer().execute(() -> {
                if (player.getVehicle() instanceof ArmoredCarEntity car) {
                    car.startReload(player);
                } else if (player.getVehicle() instanceof com.exodusvehicles.entity.FieldMgEntity mg) {
                    mg.startReload(player);
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
                } else if (player.getVehicle() instanceof com.exodusvehicles.entity.MorserEntity gun) {
                    gun.fire(player);
                } else if (player.getVehicle() instanceof com.exodusvehicles.entity.FieldMgEntity mg) {
                    mg.fire(player);
                }
            });
        });

        // The crew sit inside the armour: bullets and blows aimed at a crew member hit the hull instead.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            // the shielded MG 08's plate stops small-arms fire from the front
            if (entity.getVehicle() instanceof com.exodusvehicles.entity.FieldMgEntity mg && mg.hasShield()
                    && !source.is(DamageTypeTags.IS_EXPLOSION) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                net.minecraft.world.phys.Vec3 from = source.getEntity() != null ? source.getEntity().getEyePosition() : source.getSourcePosition();
                if (from != null && source.getDirectEntity() != null && source.getDirectEntity() != entity && mg.shieldCovers(from)) {
                    entity.level().playSound(null, entity.getX(), entity.getEyeY(), entity.getZ(), RICOCHET,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.9F, 0.9F + entity.getRandom().nextFloat() * 0.2F);
                    return false;
                }
            }
            if (entity.getVehicle() instanceof ArmoredCarEntity car && car.isArmored() && !car.isWrecked()) {
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

        LOGGER.info("Exodus Vehicles loaded");
    }

    /** 21 cm shells a player has on them (inventory and the item held on the cursor). */
    public static int carriedShells(net.minecraft.world.entity.player.Player p) {
        int n = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            if (p.getInventory().getItem(i).is(SHELL_ITEM)) {
                n += p.getInventory().getItem(i).getCount();
            }
        }
        if (p.containerMenu.getCarried().is(SHELL_ITEM)) {
            n += p.containerMenu.getCarried().getCount();
        }
        return n;
    }

    private static SoundEvent registerSound(String name) {
        ResourceLocation rl = id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, rl, SoundEvent.createVariableRangeEvent(rl));
    }
}
