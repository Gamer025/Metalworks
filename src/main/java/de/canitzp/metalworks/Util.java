package de.canitzp.metalworks;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author canitzp
 */
public class Util {

    public static final String ENERY_UNIT = "CF";

    public static boolean isDayTime(World world){
        return world.isDaytime();
    }

    public static boolean canBlockSeeSky(World world, BlockPos pos){
        for(int i = pos.getY() + 1; i <= world.getHeight(); i++){
            pos = pos.offset(EnumFacing.UP);
            if(!isBlockTransparent(world, pos, world.getBlockState(pos))){
                return false;
            }
        }
        return true;
    }

    public static boolean isBlockTransparent(World world, BlockPos pos, IBlockState state){
        return state.getBlock() instanceof BlockAir || !state.getMaterial().isOpaque() || !state.isBlockNormalCube() || !state.isFullBlock() || !state.isOpaqueCube();
    }

    public static String formatEnergy(IEnergyStorage energy){
        return String.format("%s / %s", formatEnergy(energy.getEnergyStored()), formatEnergy(energy.getMaxEnergyStored()));
    }

    public static String formatEnergy(int energy) {
        if (energy < 1000) {
            return energy + " " + ENERY_UNIT;
        }
        final int exp = (int) (Math.log(energy) / Math.log(1000));
        final char unitType = "kmg".charAt(exp - 1);
        return String.format("%.1f %s%s", energy / Math.pow(1000, exp), unitType, ENERY_UNIT);
    }

    public static int pushEnergy(World world, BlockPos pos, IEnergyStorage energy){
        return pushEnergy(world, pos, energy, EnumFacing.values());
    }

    public static int pushEnergy(World world, BlockPos pos, IEnergyStorage energy, EnumFacing... sides){
        if(!world.isRemote && energy.canExtract()){
            for(EnumFacing side : sides){
                TileEntity tile = world.getTileEntity(pos.offset(side));
                if(tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, side.getOpposite())){
                    IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite());
                    if(storage != null && storage.canReceive()){
                        return energy.extractEnergy(storage.receiveEnergy(energy.extractEnergy(Integer.MAX_VALUE, true), false), false);
                    }
                }
            }
        }
        return 0;
    }

    /**
     * This function is presented to you by you neat helper in every situation, Actually Additions
     * @param world The world the player does live in
     * @return The coloration of something
     */
    @SideOnly(Side.CLIENT)
    public static float[] getWheelColor(World world){
        long time = world.getTotalWorldTime() % 256;
        if(time < 85){
            return new float[]{time * 3.0F, 255.0f - time * 3.0f, 0.0f};
        }
        if(time < 170){
            return new float[]{255.0f - (time -= 85) * 3.0f, 0.0f, time * 3.0f};
        }
        return new float[]{0.0f, (time -= 170) * 3.0f, 255.0f - time * 3.0f};
    }

    @SideOnly(Side.CLIENT)
    public static void applyWheelColor(World world, float alpha){
        float[] col = getWheelColor(world);
        GlStateManager.color(col[0] / 255.0F, col[1] / 255.0F, col[2] / 255.0F, alpha);
    }

    public static boolean canItemStacksStack(@Nonnull ItemStack a, @Nonnull ItemStack b) {
        return canItemStacksStackIgnoreStacksize(a, b) && a.getCount() + b.getCount() <= a.getMaxStackSize();
    }

    public static boolean canItemStacksStackIgnoreStacksize(@Nonnull ItemStack a, @Nonnull ItemStack b){
        return !a.isEmpty() && isItemEqualIgnoreDamageIfWildcard(a, b) && a.hasTagCompound() == b.hasTagCompound() && (!a.hasTagCompound() || a.getTagCompound().equals(b.getTagCompound())) && a.areCapsCompatible(b);
    }

    public static boolean isItemEqualIgnoreDamageIfWildcard(ItemStack first, ItemStack second){
        return !first.isEmpty() && second.getItem() == first.getItem() && (second.getItemDamage() == first.getItemDamage() || second.getItemDamage() == OreDictionary.WILDCARD_VALUE || first.getItemDamage() == OreDictionary.WILDCARD_VALUE);
    }

    public static void drawProgressText(GuiScreen gui, int mouseX, int mouseY, int progress, int maxProgress, boolean reverse){
        if(maxProgress > 0){
            int i = (int) ((progress / (maxProgress * 1.0F)) * 100.0);
            if(reverse){
                i = 100 - i;
            }
            gui.drawHoveringText(i + "%", mouseX, mouseY);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void renderItemInWorld(ItemStack stack){
        if(!stack.isEmpty()){
            Minecraft mc = Minecraft.getMinecraft();
            RenderItem renderer = mc.getRenderItem();
            TextureManager manager = mc.getTextureManager();

            IBakedModel model = renderer.getItemModelWithOverrides(stack, null, null);

            manager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            manager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.pushMatrix();
            model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.FIXED, false);
            renderer.renderItem(stack, model);
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            manager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            manager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        }
    }

}
