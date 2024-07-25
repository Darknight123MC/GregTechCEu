package gregtech.integration.tinkers;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.modules.GregTechModule;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;
import gregtech.modules.GregTechModules;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.Collections;
import java.util.List;

@GregTechModule(
                moduleID = GregTechModules.MODULE_TC,
                containerID = GTValues.MODID,
                modDependencies = Mods.Names.TINKERS_CONSTRUCT,
                name = "GregTech Tinkers Integration",
                description = "Tinker's Construct Integration Module")
public class TCModule extends IntegrationSubmodule {

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(TCModule.class);
    }

    public void preInit(FMLPreInitializationEvent event) {
        if (Mods.TinkersConstruct.isModLoaded()) {
            for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
                if (material.hasProperty(PropertyKey.TC_MATERIAL)) {
                    var materialTC = new TCMaterial(material, material.getResourceLocation());

                    // Ingot type
                    if (material.hasProperty(PropertyKey.INGOT) && material.hasFluid()) {
                        materialTC.setCastable(true);
                        materialTC.setCraftable(false);
                        materialTC.setFluid(material.getFluid(FluidStorageKeys.LIQUID));
                        materialTC.setRepresentativeItem(new UnificationEntry(OrePrefix.ingot, material).toString());
                    }

                    // Gem type
                    if (material.hasProperty(PropertyKey.GEM)) {
                        materialTC.setCastable(false);
                        materialTC.setCraftable(true);
                        materialTC.setRepresentativeItem(new UnificationEntry(OrePrefix.gem, material).toString());
                    }

                    // Add traits & stats
                    var tcProp = material.getProperty(PropertyKey.TC_MATERIAL);
                    tcProp.getStats().forEach(materialTC::addStats);
                    tcProp.getTraits().forEach((dependency, trait) -> {
                        for (ITrait iTrait : trait) {
                            if (dependency.equals("default")) {
                                materialTC.addTrait(iTrait);
                            } else {
                                materialTC.addTrait(iTrait, dependency);
                            }
                        }
                    });

                    TinkerRegistry.addMaterial(materialTC);
                    TinkerRegistry.integrate(materialTC, material.getFluid(FluidStorageKeys.LIQUID),
                            material.toCamelCaseString());
                } else {
                    if (false)
                        TinkerSmeltery.registerOredictMeltingCasting(material.getFluid(FluidStorageKeys.LIQUID),
                                material.toCamelCaseString());
                }
            }
        }
    }
}
