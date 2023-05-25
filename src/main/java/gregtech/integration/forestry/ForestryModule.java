package gregtech.integration.forestry;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;
import gregtech.modules.GregTechModules;

@GregTechModule(
        moduleID = GregTechModules.MODULE_FR,
        containerID = GTValues.MODID,
        modDependencies = GTValues.MODID_FR,
        name = "GregTech Forestry Integration",
        descriptionKey = "gregtech.modules.fr_integration.description"
)
public class ForestryModule extends IntegrationSubmodule {
}
