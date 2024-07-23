package gregtech.integration.tinkers;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;
import gregtech.modules.GregTechModules;

@GregTechModule(
        moduleID = GregTechModules.MODULE_TC,
        containerID = GTValues.MODID,
        modDependencies = Mods.Names.TINKERS_CONSTRUCT,
        name = "GregTech Tinkers Integration",
        description = "Tinker's Construct Integration Module"
)
public class TCModule extends IntegrationSubmodule {
}
