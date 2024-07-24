package gregtech.integration.tinkers;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;

import slimeknights.tconstruct.library.traits.ITrait;

public class TCMaterials {

    public static void init() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(PropertyKey.TC_MATERIAL)) {
                var materialTC = new slimeknights.tconstruct.library.materials.Material(material.getName(),
                        material.getMaterialRGB());

                // Ingot type
                if (material.hasProperty(PropertyKey.INGOT)) {
                    if (material.hasFluid() && material.getProperty(PropertyKey.FLUID).solidifiesFrom() != null) {
                        // materialTC.setCastable(true);
                        // materialTC.setCraftable(false);
                        // materialTC.setFluid(material.getFluid(FluidStorageKeys.LIQUID));
                        materialTC.setRepresentativeItem(new UnificationEntry(OrePrefix.ingot, material).toString());
                    }
                }

                // Gem type
                if (material.hasProperty(PropertyKey.GEM)) {
                    // materialTC.setCastable(false);
                    // materialTC.setCraftable(true);
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

                // TinkerRegistry.addMaterial(materialTC);
            }
        }
    }
}
