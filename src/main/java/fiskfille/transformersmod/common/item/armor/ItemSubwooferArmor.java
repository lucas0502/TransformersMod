package fiskfille.transformersmod.common.item.armor;

import fiskfille.transformersmod.TransformerManager;
import fiskfille.transformersmod.TransformersMod;
import fiskfille.transformersmod.common.item.TFItems;
import fiskfille.transformersmod.common.transformer.base.Transformer;

public class ItemSubwooferArmor extends ItemTransformerArmor
{
	public ItemSubwooferArmor(int armorPiece)
	{
		super(TFItems.TRANSFORMERMATERIAL, 4, armorPiece);
		this.setCreativeTab(TransformersMod.transformersTab);
	}

	@Override
	public Transformer getTransformer() 
	{
		return TransformerManager.transformerSubwoofer;
	}
}