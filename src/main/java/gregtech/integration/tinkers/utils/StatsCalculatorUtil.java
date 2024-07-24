package gregtech.integration.tinkers.utils;

import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.TCMaterialProperty;

import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;

public class StatsCalculatorUtil {

    public static void processStats(TCMaterialProperty property, MaterialProperties properties) {
        var toolProp = properties.getProperty(PropertyKey.TOOL);
        property.addStats(new HeadMaterialStats(toolProp.getToolDurability(), toolProp.getToolSpeed(),
                toolProp.getToolAttackDamage(), toolProp.getToolHarvestLevel()));
        property.addStats(new HandleMaterialStats(1.0f, toolProp.getToolDurability()));
        property.addStats(new ExtraMaterialStats(toolProp.getToolDurability() / 10));
    }
}
