package gregtech.integration.tinkers;

import gregtech.api.GTValues;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.modules.GregTechModule;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;
import gregtech.modules.GregTechModules;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.BowMaterialStats;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.ProjectileMaterialStats;
import slimeknights.tconstruct.tools.TinkerTraits;

import static slimeknights.tconstruct.library.materials.MaterialTypes.HEAD;

@GregTechModule(
                moduleID = GregTechModules.MODULE_TC,
                containerID = GTValues.MODID,
                modDependencies = Mods.Names.TINKERS_CONSTRUCT,
                name = "GregTech Tinkers Integration",
                description = "Tinker's Construct Integration Module")
public class TCModule extends IntegrationSubmodule {

    public void preInit(FMLPreInitializationEvent event) {
        if (Mods.TinkersConstruct.isModLoaded()) {
            var test = new Material("aluminium", 0xFFFFFF);
            test.setFluid(Materials.Water.getFluid(FluidStorageKeys.LIQUID));
            test.setCastable(true);
            test.setCraftable(false);
            test.setRepresentativeItem("ingotAluminium");
            test.addTrait(TinkerTraits.dense, HEAD);
            test.addTrait(TinkerTraits.heavy);
            test.addStats(new HeadMaterialStats(1000, 5.5f, 4f, 3));
            test.addStats(new HandleMaterialStats(1f, 125));
            test.addStats(new ExtraMaterialStats(40));
            test.addStats(new ProjectileMaterialStats());
            test.addStats(new BowMaterialStats(0.6f, 1.25f, 4f));

            TinkerRegistry.addMaterial(test);
        }
    }
}
