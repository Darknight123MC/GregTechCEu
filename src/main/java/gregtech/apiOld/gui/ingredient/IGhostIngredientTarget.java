package gregtech.apiOld.gui.ingredient;

import mezz.jei.api.gui.IGhostIngredientHandler.Target;

import java.util.List;

public interface IGhostIngredientTarget {

    List<Target<?>> getPhantomTargets(Object ingredient);

}
