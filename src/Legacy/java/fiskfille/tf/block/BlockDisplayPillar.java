package fiskfille.tf.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import fiskfille.tf.item.IDisplayPillarItem;
import fiskfille.tf.main.MainClass;
import fiskfille.tf.main.TFItems;
import fiskfille.tf.render.tileentity.RenderDisplayPillar;
import fiskfille.tf.tileentity.TileEntityDisplayPillar;

public class BlockDisplayPillar extends BlockBasic implements ITileEntityProvider
{
	private Random rand = new Random();
	
	public BlockDisplayPillar()
	{
		super(Material.rock);
		this.setCreativeTab(MainClass.tabTransformers);
	}
	
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
    {
		ItemStack itemstack = metadata != 0 ? new ItemStack(TFItems.displayVehicle, 1, metadata - 1) : null;
        
        if (itemstack != null)
        {
            float f = this.rand.nextFloat() * 0.8F + 0.1F;
            float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
            float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

            while (itemstack.stackSize > 0)
            {
                int j1 = this.rand.nextInt(21) + 10;

                if (j1 > itemstack.stackSize)
                {
                    j1 = itemstack.stackSize;
                }

                itemstack.stackSize -= j1;
                EntityItem entityitem = new EntityItem(world, (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));

                if (itemstack.hasTagCompound())
                {
                    entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                }

                float f3 = 0.05F;
                entityitem.motionX = (double)((float)this.rand.nextGaussian() * f3);
                entityitem.motionY = (double)((float)this.rand.nextGaussian() * f3 + 0.2F);
                entityitem.motionZ = (double)((float)this.rand.nextGaussian() * f3);
                world.spawnEntityInWorld(entityitem);
            }
        }

        super.breakBlock(world, x, y, z, block, metadata);
    }
	
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_)
    {
        if (world.isRemote)
        {
            return false;
        }
        else
        {
            TileEntityDisplayPillar tileentitydisplaypillar = (TileEntityDisplayPillar)world.getTileEntity(x, y, z);

            if (tileentitydisplaypillar != null)
            {
            	int meta = world.getBlockMetadata(x, y, z);
            	
            	for (int i = 0; i < 5; ++i)
            	{
                	if (meta == i + 1 && player.getHeldItem() == null)
                	{
                    	world.setBlockMetadataWithNotify(x, y, z, 0, 2);
                    	player.setCurrentItemOrArmor(0, new ItemStack(TFItems.displayVehicle, 1, i));
                	}
                	else if (meta == 0 && player.getHeldItem() != null && player.getHeldItem().getItem() == TFItems.displayVehicle && player.getHeldItem().getItemDamage() == i)
                	{
                		world.setBlockMetadataWithNotify(x, y, z, i + 1, 2);
                    	player.setCurrentItemOrArmor(0, null);
                	}
            	}
            }

            return false;
        }
    }
	
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 p_149731_5_, Vec3 p_149731_6_)
    {
        int l = world.getBlockMetadata(x, y, z);
        float f = 0.21F;

        if (l == 0)
        {
        	this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.55F, 1.0F);
        }
        else if (l == 1 || l == 2)
        {
        	this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
        else if (l == 3 || l == 4)
        {
        	this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
        }
        
        return super.collisionRayTrace(world, x, y, z, p_149731_5_, p_149731_6_);
    }
	
	public boolean renderAsNormalBlock()
    {
        return false;
    }
	
	public int getRenderType()
	{
		return -1;
	}
	
	public boolean isOpaqueCube()
	{
		return false;
	}
	
    public boolean hasTileEntity()
    {
        return true;
    }
	
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new TileEntityDisplayPillar();
	}
	
    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
    	blockIcon = par1IconRegister.registerIcon(MainClass.modid + ":" + getUnlocalizedName().substring(5));
    }
}