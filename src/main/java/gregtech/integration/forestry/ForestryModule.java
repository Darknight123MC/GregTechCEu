package gregtech.integration.forestry;

import forestry.api.core.ForestryAPI;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.modules.GregTechModule;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.forestry.frames.FrameRecipes;
import gregtech.integration.forestry.frames.GTFrameType;
import gregtech.integration.forestry.frames.GTItemFrame;
import gregtech.modules.GregTechModules;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@GregTechModule(
        moduleID = GregTechModules.MODULE_FR,
        containerID = GTValues.MODID,
        modDependencies = GTValues.MODID_FR,
        name = "GregTech Forestry Integration",
        descriptionKey = "gregtech.modules.fr_integration.description"
)
public class ForestryModule extends IntegrationSubmodule {

    public static GTItemFrame frameAccelerated;
    public static GTItemFrame frameMutagenic;
    public static GTItemFrame frameWorking;
    public static GTItemFrame frameDecaying;
    public static GTItemFrame frameSlowing;
    public static GTItemFrame frameStabilizing;
    public static GTItemFrame frameArborist;

    @Nonnull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(ForestryModule.class);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        // GT Frames
        if (ForestryConfig.enableGTFrames) {
            if (ForestryUtil.apicultureEnabled()) {
                frameAccelerated = new GTItemFrame(GTFrameType.ACCELERATED);
                frameMutagenic = new GTItemFrame(GTFrameType.MUTAGENIC);
                frameWorking = new GTItemFrame(GTFrameType.WORKING);
                frameDecaying = new GTItemFrame(GTFrameType.DECAYING);
                frameSlowing = new GTItemFrame(GTFrameType.SLOWING);
                frameStabilizing = new GTItemFrame(GTFrameType.STABILIZING);
                frameArborist = new GTItemFrame(GTFrameType.ARBORIST);
            } else {
                getLogger().warn("GregTech Frames are enabled, but Forestry Apiculture module is disabled. Skipping...");
            }
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        if (ForestryUtil.apicultureEnabled()) {
            if (ForestryConfig.enableGTFrames) {
                registry.register(frameAccelerated);
                registry.register(frameMutagenic);
                registry.register(frameWorking);
                registry.register(frameDecaying);
                registry.register(frameSlowing);
                registry.register(frameStabilizing);
                registry.register(frameArborist);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        if (ForestryUtil.apicultureEnabled()) {
            if (ForestryConfig.enableGTFrames) {
                frameAccelerated.registerModel(frameAccelerated, ForestryAPI.modelManager);
                frameMutagenic.registerModel(frameMutagenic, ForestryAPI.modelManager);
                frameWorking.registerModel(frameWorking, ForestryAPI.modelManager);
                frameDecaying.registerModel(frameDecaying, ForestryAPI.modelManager);
                frameSlowing.registerModel(frameSlowing, ForestryAPI.modelManager);
                frameStabilizing.registerModel(frameStabilizing, ForestryAPI.modelManager);
                frameArborist.registerModel(frameArborist, ForestryAPI.modelManager);
            }
        }
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        if (ForestryUtil.apicultureEnabled()) {
            if (ForestryConfig.enableGTFrames) {
                FrameRecipes.init();
            }
        }
    }

    @SubscribeEvent
    public static void registerMaterials(GregTechAPI.MaterialEvent event) {
        if (ForestryUtil.apicultureEnabled()) {
            if (ForestryConfig.enableGTFrames) {
                Materials.TreatedWood.addFlags(MaterialFlags.GENERATE_LONG_ROD);
                Materials.Uranium235.addFlags(MaterialFlags.GENERATE_LONG_ROD);
                Materials.Plutonium241.addFlags(MaterialFlags.GENERATE_LONG_ROD, MaterialFlags.GENERATE_FOIL);
                Materials.BlueSteel.addFlags(MaterialFlags.GENERATE_LONG_ROD);
            }
        }
    }
}
