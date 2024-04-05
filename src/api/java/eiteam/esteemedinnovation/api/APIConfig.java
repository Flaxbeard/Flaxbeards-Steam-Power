package eiteam.esteemedinnovation.api;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class APIConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<Boolean> SAFE_MODE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("Steam Network");
        SAFE_MODE = builder.comment("Enable safe mode (no explosions)").define("safe_mode", false);
        builder.pop();

        SPEC = builder.build();
    }
}
