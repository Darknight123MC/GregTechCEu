package gregtech.integration.forestry;

import forestry.modules.ModuleHelper;

public class ForestryUtil {

    public static boolean apicultureEnabled() {
        return ModuleHelper.isEnabled("apiculture");
    }
}
