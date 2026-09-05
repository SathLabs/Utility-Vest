package dev.satherov.utilityvest;

import dev.satherov.utilityvest.client.input.UVKeybindManager;
import dev.satherov.utilityvest.client.screen.UVFilterScreen;
import dev.satherov.utilityvest.client.screen.UVInventoryScreen;
import dev.satherov.utilityvest.common.capabilities.UVVestCapability;
import dev.satherov.utilityvest.common.item.UVVestItem;
import dev.satherov.utilityvest.config.ConfigLoader;
import dev.satherov.utilityvest.core.UVRegistry;
import dev.satherov.utilityvest.network.UVNetworking;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

import net.minecraft.resources.ResourceLocation;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import top.theillusivec4.curios.api.CuriosCapability;


@Mod(UtilityVest.MOD_ID)
public class UtilityVest {
    
    public static final String MOD_ID = "utilityvest";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final ConfigLoader CONFIG = ConfigLoader.create();
    
    public UtilityVest(IEventBus bus, FMLModContainer container) {
        UtilityVest.CONFIG.discover(container);
        
        bus.addListener(UVNetworking::registerPayload);
        
        UVRegistry.ITEMS.register(bus);
        UVRegistry.DATA_COMPONENT_TYPES.register(bus);
        UVRegistry.RECIPE_SERIALIZERS.register(bus);
        UVRegistry.CREATIVE_MODE_TABS.register(bus);
        UVRegistry.MENU_TYPES.register(bus);
        
        bus.addListener(this::registerCapabilities);
        bus.addListener(this::onConfigLoad);
        bus.addListener(this::onConfigReload);
        
        if (FMLEnvironment.dist.isClient()) {
            Client.init(container);
            bus.addListener(Client::clientSetup);
            bus.addListener(Client::registerMenuScreens);
            bus.addListener(Client::registerKeys);
        }
    }
    
    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(UtilityVest.MOD_ID, path);
    }
    
    public void onConfigLoad(final ModConfigEvent.Loading event) {
        UtilityVest.CONFIG.update(event.getConfig().getSpec());
    }
    
    public void onConfigReload(final ModConfigEvent.Reloading event) {
        UtilityVest.CONFIG.update(event.getConfig().getSpec());
    }
    
    public void registerCapabilities(final RegisterCapabilitiesEvent event) {
        UVRegistry.ITEMS.getEntries().stream().toList().forEach(item -> {
            if (item.get() instanceof UVVestItem vest) {
                event.registerItem(
                        CuriosCapability.ITEM,
                        (stack, ctx) -> () -> stack,
                        vest
                );
                event.registerItem(Capabilities.ItemHandler.ITEM, (stack, context) -> new UVVestCapability(stack, vest), vest);
            }
        });
    }
    
    static class Client {
        
        public static void init(FMLModContainer container) {
            container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
        
        public static void clientSetup(final FMLClientSetupEvent event) {
            NeoForge.EVENT_BUS.register(new UVKeybindManager());
        }
        
        public static void registerMenuScreens(final RegisterMenuScreensEvent event) {
            event.register(UVRegistry.INVENTORY_MENU_ONE.get(), UVInventoryScreen::new);
            event.register(UVRegistry.INVENTORY_MENU_TWO.get(), UVInventoryScreen::new);
            event.register(UVRegistry.INVENTORY_MENU_THREE.get(), UVInventoryScreen::new);
            event.register(UVRegistry.INVENTORY_MENU_FOUR.get(), UVInventoryScreen::new);
            event.register(UVRegistry.INVENTORY_MENU_FIVE.get(), UVInventoryScreen::new);
            event.register(UVRegistry.INVENTORY_MENU_SIX.get(), UVInventoryScreen::new);
            event.register(UVRegistry.INVENTORY_MENU_SEVEN.get(), UVInventoryScreen::new);
            event.register(UVRegistry.INVENTORY_MENU_EIGHT.get(), UVInventoryScreen::new);

            event.register(UVRegistry.FILTER_MENU_ONE.get(), UVFilterScreen::new);
            event.register(UVRegistry.FILTER_MENU_TWO.get(), UVFilterScreen::new);
            event.register(UVRegistry.FILTER_MENU_THREE.get(), UVFilterScreen::new);
            event.register(UVRegistry.FILTER_MENU_FOUR.get(), UVFilterScreen::new);
            event.register(UVRegistry.FILTER_MENU_FIVE.get(), UVFilterScreen::new);
            event.register(UVRegistry.FILTER_MENU_SIX.get(), UVFilterScreen::new);
            event.register(UVRegistry.FILTER_MENU_SEVEN.get(), UVFilterScreen::new);
            event.register(UVRegistry.FILTER_MENU_EIGHT.get(), UVFilterScreen::new);
        }
        
        public static void registerKeys(final RegisterKeyMappingsEvent event) {
            event.register(UVKeybindManager.GUI_KEY);
            event.register(UVKeybindManager.RADIAL_KEY);
            event.register(UVKeybindManager.RESTOCK);
            event.register(UVKeybindManager.SAVE);
            event.register(UVKeybindManager.LOAD);
        }
    }
}
