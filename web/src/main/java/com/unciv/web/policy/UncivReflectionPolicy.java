package com.unciv.web.policy;

import com.github.xpenatan.gdx.teavm.backends.shared.config.reflection.GdxReflectionHelper;
import org.teavm.extension.Autoregistered;
import org.teavm.extension.spi.reflection.SimpleReflectionPolicy;

/**
 * Unciv (de)serializes its whole data model - rulesets, saves, settings - through libGDX Json, which uses
 * reflection on fields and no-arg constructors. This grants that for everything under com.unciv.
 * Has to be Java: gdx-teavm only auto-registers Java policies (@Autoregistered).
 */
@Autoregistered
public class UncivReflectionPolicy extends SimpleReflectionPolicy {
    @Override
    protected void setup() {
        GdxReflectionHelper.applyDefaults(this::selectClasses);
        selectClasses(type -> type.name().startsWith("com.unciv."))
                .foundByName()
                .reflectableFields(field -> true)
                .reflectableMethods(method -> method.isConstructor() && withParameterCount(0).test(method));
        for (String name : new String[] {"java.util.HashMap", "java.util.LinkedHashMap", "java.util.ArrayList",
                "java.util.HashSet", "java.util.LinkedHashSet", "java.util.TreeMap"}) {
            selectClass(name).foundByName()
                    .reflectableMethods(method -> method.isConstructor() && withParameterCount(0).test(method));
        }
    }
}
