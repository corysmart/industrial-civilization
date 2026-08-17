package com.industrialcivilization.core.bootstrap;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

/** Loads the recipe ingredient registry safety transformer before Minecraft classes. */
@IFMLLoadingPlugin.Name("Industrial Civilization Ingredient Registry Guard")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions("com.industrialcivilization.core.bootstrap")
public final class IngredientRegistryCorePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] { IngredientRegistryTransformer.class.getName() };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // No runtime data is required.
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
