package fiskfille.tf.common.proxy;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import fiskfille.tf.client.keybinds.TFKeyBinds;
import fiskfille.tf.client.model.transformer.TFModelRegistry;
import fiskfille.tf.client.render.entity.RenderCustomPlayer;
import fiskfille.tf.client.render.entity.RenderMiniMissile;
import fiskfille.tf.client.render.entity.RenderMissile;
import fiskfille.tf.client.render.entity.RenderTankShell;
import fiskfille.tf.client.render.item.RenderItemDisplayVehicle;
import fiskfille.tf.client.render.item.RenderItemFlamethrower;
import fiskfille.tf.client.render.item.RenderItemPurgesKatana;
import fiskfille.tf.client.render.item.RenderItemSkystrikesCrossbow;
import fiskfille.tf.client.render.item.RenderItemVurpsSniper;
import fiskfille.tf.client.render.tileentity.RenderCrystal;
import fiskfille.tf.client.render.tileentity.RenderDisplayPillar;
import fiskfille.tf.client.tick.ClientTickHandler;
import fiskfille.tf.common.entity.EntityMiniMissile;
import fiskfille.tf.common.entity.EntityMissile;
import fiskfille.tf.common.entity.EntityTankShell;
import fiskfille.tf.common.item.TFItems;
import fiskfille.tf.common.tileentity.TileEntityCrystal;
import fiskfille.tf.common.tileentity.TileEntityDisplayPillar;

public class ClientProxy extends CommonProxy
{
	public static Field camRollField;
	
	@Override
	public World getWorld()
	{
		return Minecraft.getMinecraft().theWorld;
	}
	
	@Override
	public void registerRenderInformation()
	{
		RenderCustomPlayer renderCustomPlayer = new RenderCustomPlayer();
		renderCustomPlayer.setRenderManager(RenderManager.instance);
		RenderManager.instance.entityRenderMap.put(EntityPlayer.class, renderCustomPlayer);
		
		int i = 0;
		for (Field curField : EntityRenderer.class.getDeclaredFields())
		{
			if (curField.getType() == float.class)
			{
				if (++i == 15)
				{
					camRollField = curField;
					curField.setAccessible(true);
				}
			}
		}
		
		RenderingRegistry.registerEntityRenderingHandler(EntityTankShell.class, new RenderTankShell());
		RenderingRegistry.registerEntityRenderingHandler(EntityMissile.class, new RenderMissile());
		RenderingRegistry.registerEntityRenderingHandler(EntityMiniMissile.class, new RenderMiniMissile());
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDisplayPillar.class, new RenderDisplayPillar());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrystal.class, new RenderCrystal());
		
		MinecraftForgeClient.registerItemRenderer(TFItems.purgesKatana, new RenderItemPurgesKatana());
		MinecraftForgeClient.registerItemRenderer(TFItems.skystrikesCrossbow, new RenderItemSkystrikesCrossbow());
		
		MinecraftForgeClient.registerItemRenderer(TFItems.displayVehicle, new RenderItemDisplayVehicle());
		MinecraftForgeClient.registerItemRenderer(TFItems.flamethrower, new RenderItemFlamethrower());
		MinecraftForgeClient.registerItemRenderer(TFItems.vurpsSniper, new RenderItemVurpsSniper());
		
		TFModelRegistry.registerModels();
	}
	
	@Override
	public void registerTickHandlers()
	{
		tickHandler = new ClientTickHandler();
	}
	
	@Override
	public EntityPlayer getPlayer()
	{
		return Minecraft.getMinecraft().thePlayer;
	}
	
	@Override
	public void registerKeyBinds()
	{
		TFKeyBinds.register();
	}
}