package gregtech.api.unification.material.properties;

import slimeknights.tconstruct.library.materials.IMaterialStats;
import slimeknights.tconstruct.library.traits.ITrait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TCMaterialProperty implements IMaterialProperty {

    // Properties
    private List<IMaterialStats> stats = new ArrayList<>();
    private Map<String, List<ITrait>> traits = new HashMap<>();

    @Override
    public void verifyProperty(MaterialProperties properties) {}

    public void addStats(IMaterialStats materialStats) {
        this.stats.add(materialStats);
    }

    public void addTrait(ITrait trait) {
        addTrait(trait, "default");
    }

    public void addTrait(ITrait trait, String dependency) {
        if (!this.traits.containsKey(dependency)) {
            this.traits.put(dependency, new ArrayList<>());
        }
        this.traits.get(dependency).add(trait);
    }

    public List<IMaterialStats> getStats() {
        return stats;
    }

    public Map<String, List<ITrait>> getTraits() {
        return traits;
    }

    public void setStats(List<IMaterialStats> stats) {
        this.stats = stats;
    }

    public void setTraits(Map<String, List<ITrait>> traits) {
        this.traits = traits;
    }
}
