package fiskfille.tf.common.transformer.base;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.Vec3;
import fiskfille.tf.client.keybinds.TFKeyBinds;
import fiskfille.tf.client.particle.NitroParticleHandler;
import fiskfille.tf.common.entity.EntityMissile;
import fiskfille.tf.common.item.TFItems;
import fiskfille.tf.common.motion.TFMotionManager;
import fiskfille.tf.common.motion.VehicleMotion;
import fiskfille.tf.common.packet.PacketVehicleNitro;
import fiskfille.tf.common.packet.base.TFPacketManager;
import fiskfille.tf.common.playerdata.TFDataManager;
import fiskfille.tf.config.TFConfig;
import fiskfille.tf.helper.TFModelHelper;

public abstract class TransformerCar extends Transformer
{	
	public TransformerCar(String name)
	{
		super(name);
	}

	@Override
	public float fall(EntityPlayer player, float distance)
	{
		return TFDataManager.isInVehicleMode(player) ? distance / 2 : super.fall(player, distance);
	}
	
	@Override
	public void transformationTick(EntityPlayer player, int timer)
	{
		if (timer >= 14 && TFDataManager.isInVehicleMode(player))
		{
			player.motionY += 0.12D;
		}
	}

	@Override
	public boolean hasStealthForce(EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean canJumpAsVehicle(EntityPlayer player)
	{
		return TFDataManager.isInStealthMode(player);
	}

	@Override
	public float getCameraYOffset(EntityPlayer player)
	{
		return -1.1F;
	}

	@Override
	public void updateMovement(EntityPlayer player)
	{
		Minecraft mc = Minecraft.getMinecraft();

		boolean inStealthMode = TFDataManager.isInStealthMode(player);
		boolean moveForward = mc.gameSettings.keyBindForward.getIsKeyPressed();
		boolean moveSide = player.moveStrafing != 0;
		boolean nitroPressed = TFKeyBinds.keyBindingNitro.getIsKeyPressed() || mc.gameSettings.keyBindSprint.getIsKeyPressed();
		boolean driftPressed = TFKeyBinds.keyBindingBrake.getIsKeyPressed();
		int nitro = 0;
		double forwardVelocity = 0;
		double horizontalVelocity = 0;

		player.stepHeight = 1.0F;

		VehicleMotion transformedPlayer = TFMotionManager.getTransformerPlayer(player);

		if (transformedPlayer != null)
		{
			nitro = transformedPlayer.getNitro();
			forwardVelocity = transformedPlayer.getForwardVelocity();
			horizontalVelocity = transformedPlayer.getHorizontalVelocity();
			double increment;

			if (inStealthMode)
			{
				increment = (0.328D - forwardVelocity) / 10 + 0.001D;
			}
			else
			{
				increment = ((nitroPressed && nitro > 0 ? 5.5D : 0.74D) - forwardVelocity) / 10 + 0.001D;
			}

			if (moveForward && forwardVelocity <= 1.0D)
			{
				forwardVelocity += increment * 0.5F;
			}
			else if (forwardVelocity > 0.02D)
			{
				forwardVelocity -= 0.02D;
			}
			else if (forwardVelocity <= 0.02D)
			{
				forwardVelocity = 0;
			}

			if (moveSide && horizontalVelocity <= 1.0D && inStealthMode)
			{
				horizontalVelocity += increment * 0.5F;
			}
			else if (horizontalVelocity > 0.02D)
			{
				horizontalVelocity-= 0.02D;
			}
			else if (horizontalVelocity <= 0.02D)
			{
				horizontalVelocity = 0;
			}

			if (driftPressed && player.onGround && forwardVelocity > 0)
			{
				ModelBiped modelBipedMain = TFModelHelper.modelBipedMain;
				
				float f = modelBipedMain.bipedHead.rotateAngleY - (modelBipedMain.bipedBody.rotateAngleY - modelBipedMain.bipedHead.rotateAngleY) / 3;
				forwardVelocity -= 0.03D;

				Vec3 vec3 = TFMotionManager.getSideCoords(player, forwardVelocity, -(int)(f * 45)/*f > -0.25D && f < 0.25D ? 0 : (f > 0 ? -45 : 30)*/);
				player.motionX = (vec3.xCoord - player.posX);
				player.motionZ = (vec3.zCoord - player.posZ);

				if (forwardVelocity > 0.1D)
				{
					for (int i = 0; i < 10; ++i)
					{
						Vec3 side = TFMotionManager.getBackSideCoords(player, 0.15F, i < 2, -0.5F, false);
						player.worldObj.spawnParticle("reddust", side.xCoord, player.posY - 1.5F, side.zCoord, -1, 0, 0);
					}
				}

				float decrement = (float) (1.0F - (increment * 0.5F));

				if (f > 0.5F && forwardVelocity > 0.0F)
				{
					player.rotationYaw += decrement;
				}
				if (f < -0.5F && forwardVelocity > 0.0F)
				{
					player.rotationYaw -= decrement;
				}
			}
			else
			{
				Vec3 forwardVec = TFMotionManager.getFrontCoords(player, 0, forwardVelocity);
				player.motionX = (forwardVec.xCoord - player.posX);
				player.motionZ = (forwardVec.zCoord - player.posZ);
			}

			if (forwardVelocity <= 0) {forwardVelocity = 0;}
			if (forwardVelocity > 1) {forwardVelocity = 1;}

			boolean prevNitro = TFMotionManager.prevNitro;

			if (nitro > 0 && nitroPressed && moveForward && player == mc.thePlayer && !inStealthMode)
			{
				if (!player.capabilities.isCreativeMode) --nitro;

				if (!prevNitro)
				{
					TFPacketManager.networkWrapper.sendToServer(new PacketVehicleNitro(player, true));
					TFMotionManager.prevNitro = true;
				}

				for (int i = 0; i < 4; ++i)
				{
					Vec3 side = TFMotionManager.getBackSideCoords(player, 0.15F, i < 2, -0.9, false);
					Random rand = new Random();
					player.worldObj.spawnParticle("smoke", side.xCoord, player.posY - 1.6F, side.zCoord, rand.nextFloat() / 20, rand.nextFloat() / 20, rand.nextFloat() / 20);
				}

				for (int i = 0; i < 10; ++i)
				{
					Vec3 side = TFMotionManager.getBackSideCoords(player, 0.15F, i < 2, -0.9, false);
					Random rand = new Random();
					player.worldObj.spawnParticle("smoke", side.xCoord, player.posY - 1.6F, side.zCoord, rand.nextFloat() / 10, rand.nextFloat() / 10 + 0.05F, rand.nextFloat() / 10);
				}
			}
			else
			{
				if (prevNitro)
				{
					TFPacketManager.networkWrapper.sendToServer(new PacketVehicleNitro(player, false));
					TFMotionManager.prevNitro = false;
				}
			}

			transformedPlayer.setNitro(nitro);
			transformedPlayer.setForwardVelocity(forwardVelocity);
			transformedPlayer.setHorizontalVelocity(horizontalVelocity);

			if (player.isInWater())
			{
				player.motionY = -0.1F;
			}
		}
	}

	@Override
	public boolean canShoot(EntityPlayer player)
	{
		return TFDataManager.getStealthModeTimer(player) < 5;
	}

	@Override
	public Item getShootItem()
	{
		return TFItems.missile;
	}

	@Override
	public Entity getShootEntity(EntityPlayer player)
	{
		EntityMissile entityMissile = new EntityMissile(player.worldObj, player, 3, TFConfig.allowMissileExplosions, TFDataManager.isInStealthMode(player));
		entityMissile.posY--;

		return entityMissile;
	}

	@Override
	public int getShots()
	{
		return 8;
	}

	@Override
	public void doNitroParticles(EntityPlayer player)
	{
		for (int i = 0; i < 4; ++i)
		{
			Vec3 side = NitroParticleHandler.getBackSideCoords(player, 0.15F, i < 2, -1.3, false);
			Random rand = new Random();
			player.worldObj.spawnParticle("smoke", side.xCoord, player.posY - 0.6F, side.zCoord, rand.nextFloat() / 20, rand.nextFloat() / 20, rand.nextFloat() / 20);
		}

		for (int i = 0; i < 10; ++i)
		{
			Vec3 side = NitroParticleHandler.getBackSideCoords(player, 0.15F, i < 2, -1.3, false);
			Random rand = new Random();
			player.worldObj.spawnParticle("smoke", side.xCoord, player.posY - 0.6F, side.zCoord, rand.nextFloat() / 10, rand.nextFloat() / 10 + 0.05F, rand.nextFloat() / 10);
		}
	}
}
