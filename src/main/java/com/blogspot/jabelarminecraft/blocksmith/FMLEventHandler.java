/**
    Copyright (C) 2014 by jabelar

    This file is part of jabelar's Minecraft Forge modding examples; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    For a copy of the GNU General Public License see <http://www.gnu.org/licenses/>.

	If you're interested in licensing the code under different terms you can
	contact the author at julian_abelar@hotmail.com 
*/

package com.blogspot.jabelarminecraft.blocksmith;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import com.blogspot.jabelarminecraft.blocksmith.items.IExtendedReach;


public class FMLEventHandler 
{
	
	/*
	 * Common events
	 */

	// events in the cpw.mods.fml.common.event package are actually handled with
	// @EventHandler annotation in the main mod class or the proxies.
	
	/*
	 * Game input events
	 */

//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(InputEvent event)
//	{
//		
//	}
//
//	@SideOnly(Side.CLIENT)
//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(KeyInputEvent event)
//	{
//
//	}
//
//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(MouseInputEvent event)
//	{
//
//	}
//	
//	/*
//	 * Player events
//	 */
//
//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(PlayerEvent event)
//	{
//		
//	}
//
//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(ItemCraftedEvent event)
//	{
//		
//	}
//
//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(ItemPickupEvent event)
//	{
//		
//	}
//
//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(ItemSmeltedEvent event)
//	{
//		
//	}
//
//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(PlayerChangedDimensionEvent event)
//	{
//		
//	}

	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(PlayerLoggedInEvent event)
	{
		if (event.player.getDisplayName().equals("MistMaestro"))
		{
			// DEBUG
			System.out.println("Welcome Master!");
		}
		
		// DEBUG
//		System.out.println("WorldData hasCastleSpawned ="+WorldData.get(((EntityPlayerSP)thePlayer).movementInput.worldObj).getHasCastleSpwaned()+
//				", familyCowHasGivenLead ="+WorldData.get(((EntityPlayerSP)thePlayer).movementInput.worldObj).getFamilyCowHasGivenLead());
	}

	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(PlayerLoggedOutEvent event)
	{
		// DEBUG
		System.out.println("Player logged out");
		
	}

//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(PlayerRespawnEvent event)
//	{
//		// DEBUG
//		System.out.println("The memories of past existences are but glints of light.");
//		
//	}
//
//	/*
//	 * Tick events
//	 */
//
//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(ClientTickEvent event) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
//	{
//		if (event.phase == TickEvent.Phase.END) // only proceed if START phase otherwise, will execute twice per tick
//		{
//			return;
//		}	
//
//	}

	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(PlayerTickEvent event)
	{		
		if (event.phase == TickEvent.Phase.END) // only proceed if START phase otherwise, will execute twice per tick
		{
			if (event.player != null && event.player.swingProgressInt == 1) // Just swung
			{
				ItemStack itemstack = event.player.getCurrentEquippedItem();
				IExtendedReach ieri;
				if (itemstack != null)
				{
					if (itemstack.getItem() instanceof IExtendedReach)
					{
						ieri = (IExtendedReach) itemstack.getItem();
					} else if (itemstack.getItem() instanceof IExtendedReach)
					{
						ieri = (IExtendedReach) itemstack.getItem();
					} else
					{
						ieri = null;
					}

					if (ieri != null)
					{
						float reach = ieri.getReach();
						MovingObjectPosition mov = getMouseOver(0, reach);

						if (mov != null && mov.entityHit != null && mov.entityHit != event.player && mov.entityHit.hurtResistantTime == 0)
						{
							FMLClientHandler.instance().getClient().playerController.attackEntity(event.player, mov.entityHit);
						}
					}
				}
			}
		}
		
		EntityPlayer thePlayer = event.player;
		World world = thePlayer.worldObj;
				
		if (!BlockSmith.haveWarnedVersionOutOfDate && world.isRemote && !BlockSmith.versionChecker.isLatestVersion())
		{
			ClickEvent versionCheckChatClickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, "http://jabelarminecraft.blogspot.com");
			ChatStyle clickableChatStyle = new ChatStyle().setChatClickEvent(versionCheckChatClickEvent);
			ChatComponentText versionWarningChatComponent = new ChatComponentText("Your Magic Beans Mod is not latest version!  Click here to update.");
			versionWarningChatComponent.setChatStyle(clickableChatStyle);
			thePlayer.addChatMessage(versionWarningChatComponent);
			BlockSmith.haveWarnedVersionOutOfDate = true;
		}
	}
	
	public static MovingObjectPosition getMouseOver(float frame, float dist)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();
		Entity theRenderViewEntity = mc.getRenderViewEntity();
		AxisAlignedBB theBoundingBox = new AxisAlignedBB(
				theRenderViewEntity.posX-0.5D,
				theRenderViewEntity.posY-0.5D,
				theRenderViewEntity.posZ-0.5D,
				theRenderViewEntity.posX+0.5D,
				theRenderViewEntity.posY+0.5D,
				theRenderViewEntity.posZ+0.5D
				);
		MovingObjectPosition mop = null;
		if (mc.getRenderViewEntity() != null)
		{
			if (mc.theWorld != null)
			{
				double var2 = dist;
				mop = theRenderViewEntity.rayTrace(var2, frame);
				double calcdist = var2;
				Vec3 pos = theRenderViewEntity.getPositionEyes(frame);
				var2 = calcdist;
				if (mop != null)
				{
					calcdist = mop.hitVec.distanceTo(pos);
				}
				
				Vec3 lookvec = theRenderViewEntity.getLook(frame);
				Vec3 var8 = pos.addVector(lookvec.xCoord * var2, lookvec.yCoord * var2, lookvec.zCoord * var2);
				Entity pointedEntity = null;
				float var9 = 1.0F;
				@SuppressWarnings("unchecked")
				List<Entity> list = mc.theWorld.getEntitiesWithinAABBExcludingEntity(theRenderViewEntity, theBoundingBox.addCoord(lookvec.xCoord * var2, lookvec.yCoord * var2, lookvec.zCoord * var2).expand(var9, var9, var9));
				double d = calcdist;
				
				for (Entity entity : list)
				{
					if (entity.canBeCollidedWith() && entity.getBoundingBox() != null)
					{
						float bordersize = entity.getCollisionBorderSize();
						AxisAlignedBB aabb = entity.getBoundingBox().expand(bordersize, bordersize, bordersize);
						MovingObjectPosition mop0 = aabb.calculateIntercept(pos, var8);
						
						if (aabb.isVecInside(pos))
						{
							if (0.0D < d || d == 0.0D)
							{
								pointedEntity = entity;
								d = 0.0D;
							}
						} else if (mop0 != null)
						{
							double d1 = pos.distanceTo(mop0.hitVec);
							
							if (d1 < d || d == 0.0D)
							{
								pointedEntity = entity;
								d = d1;
							}
						}
					}
				}
				
				if (pointedEntity != null && (d < calcdist || mop == null))
				{
					mop = new MovingObjectPosition(pointedEntity);
				}
			}
		}
		return mop;
	}


//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(RenderTickEvent event)
//	{
//		if (event.phase == TickEvent.Phase.END) // only proceed if START phase otherwise, will execute twice per tick
//		{
//			return;
//		}
//		
//	}
//
//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(ServerTickEvent event)
//	{
//	    if (event.phase == TickEvent.Phase.END) // only proceed if START phase otherwise, will execute twice per tick
//	    {
//		    return;
//	    }	
//		
//	}

	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(OnConfigChangedEvent eventArgs) 
	{
		// DEBUG
		System.out.println("OnConfigChangedEvent");
		if(eventArgs.modID.equals(BlockSmith.MODID))
		{
			System.out.println("Syncing config for mod ="+eventArgs.modID);
			BlockSmith.config.save();
			BlockSmith.proxy.syncConfig();
	    }
	}

//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(PostConfigChangedEvent eventArgs) 
//	{
//		// useful for doing something if another mod's config has changed
//		// if(eventArgs.modID.equals(MagicBeans.MODID))
//		// {
//		//		// do whatever here
//		// }
//	}
}
