package gregtech.integration.tinkers;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.Material;

public class TCMaterial extends Material {

    private gregtech.api.unification.material.Material material;
    private final ResourceLocation resourceLocation;

    public TCMaterial(gregtech.api.unification.material.Material material, @NotNull ResourceLocation resourceLocation) {
        super(material.getName(), material.getMaterialRGB());
        this.resourceLocation = resourceLocation;
    }

    @Override
    public String getLocalizedName() {
        return Util.translate(resourceLocation.getNamespace() + ".material." + resourceLocation.getPath());
    }
}
