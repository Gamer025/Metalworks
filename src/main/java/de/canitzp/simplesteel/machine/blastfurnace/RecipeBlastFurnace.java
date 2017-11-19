package de.canitzp.simplesteel.machine.blastfurnace;

import de.canitzp.simplesteel.recipe.OreDictStack;
import net.minecraft.item.ItemStack;

/**
 * @author canitzp
 */
public class RecipeBlastFurnace {

    private final String id;

    private OreDictStack[] inputs;
    private ItemStack[] outputs;
    private int secondOutputChance, burnTime;

    public RecipeBlastFurnace(OreDictStack input1, OreDictStack input2, OreDictStack input3, ItemStack output1, ItemStack output2, int out2Chance, int burnTime){
        this.inputs = new OreDictStack[]{input1, input2, input3};
        this.outputs = new ItemStack[]{output1, output2};
        this.secondOutputChance = out2Chance;
        this.burnTime = burnTime;
        this.id = String.format("%s#%s#%s#%s#%s#%d#%d", input1.getName(), input2.getName(), input3.getName(), output1.getUnlocalizedName(), output2.getUnlocalizedName(), out2Chance, burnTime);
    }

    public OreDictStack[] getInputs() {
        return inputs.clone();
    }

    public ItemStack[] getOutputs() {
        return outputs.clone();
    }

    public int getSecondOutputChance() {
        return secondOutputChance;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public boolean isRecipe(ItemStack input1, ItemStack input2, ItemStack input3){
        if(this.inputs[0].isMergeable(input1)){
            if(this.inputs[1].isMergeable(input2)){
                return this.inputs[2].isMergeable(input3);
            } else if(this.inputs[1].isMergeable(input3)){
                return this.inputs[2].isMergeable(input2);
            }
        } else if(this.inputs[0].isMergeable(input2)){
            if(this.inputs[1].isMergeable(input1)){
                return this.inputs[2].isMergeable(input3);
            } else if(this.inputs[1].isMergeable(input3)){
                return this.inputs[2].isMergeable(input1);
            }
        } else if(this.inputs[0].isMergeable(input3)){
            if(this.inputs[1].isMergeable(input1)){
                return this.inputs[2].isMergeable(input2);
            } else if(this.inputs[1].isMergeable(input2)){
                return this.inputs[2].isMergeable(input1);
            }
        }
        return false;
    }

    public boolean isOutputMergeable(ItemStack output1, ItemStack output2){
        return (output1.isEmpty() || (output1.isItemEqual(this.outputs[0].copy()) && output1.copy().getCount() + this.outputs[0].copy().getCount() <= output1.getMaxStackSize())) &&
                output2.isEmpty() || (output2.isItemEqual(this.outputs[1].copy()) && output2.copy().getCount() + this.outputs[1].copy().getCount() <= output2.getMaxStackSize());
    }

    public String getID(){
        return this.id;
    }

    public void shrink(ItemStack stack){
        for(OreDictStack oreDictStack : this.inputs){
            if(oreDictStack.isMergeable(stack)){
                stack.shrink(oreDictStack.getStacksize());
                return;
            }
        }
    }
}
