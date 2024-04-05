package eiteam.esteemedinnovation.api.exosuit;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public abstract class ModelExosuitUpgrade extends Model {
    public final CompoundTag nbtTagCompound = new CompoundTag();

    public ModelExosuitUpgrade(Function<ResourceLocation, RenderType> renderType) {
        super(renderType);
    }

    /**
     * Called in ModelSteamExosuit#render. Handle the rendering of your model here. Call the super method to handle
     * rotation angle copying and sneak translation. You probably want to call the super method at the start of
     * renderModel rather than elsewhere.
     * @param parentModel The exosuit model
     * @param entity The player wearing the suit
     */
    public void renderModel(HumanoidArmorModel parentModel, LivingEntity entity) {
        copyRotationAngles(parentModel, entity);
        if (entity.isCrouching()) {
            // Taken from ModelBiped#render.
//            GlStateManager.translate(0, 0.2F, 0);
        }
    }

    /**
     * Called in {@link ModelExosuitUpgrade#renderModel(HumanoidArmorModel, LivingEntity)}. Override this method to handle
     * copying the parent model's rotation angles into your model renderers.
     */
    public abstract void copyRotationAngles(HumanoidArmorModel parentModel, LivingEntity entity);

    /**
     * Copies the rotate angles from one ModelRenderer to another.
     * @param child The model to copy to
     * @param parent The model to copy from
     */
//    protected static void copyRotateAngles(ModelRenderer child, ModelRenderer parent) {
//        child.rotateAngleX = parent.rotateAngleX;
//        child.rotateAngleY = parent.rotateAngleY;
//        child.rotateAngleZ = parent.rotateAngleZ;
//    }
}
