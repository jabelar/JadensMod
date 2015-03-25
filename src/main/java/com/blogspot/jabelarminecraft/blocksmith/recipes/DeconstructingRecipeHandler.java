package com.blogspot.jabelarminecraft.blocksmith.recipes;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.blogspot.jabelarminecraft.blocksmith.BlockSmith;

public final class DeconstructingRecipeHandler
{
	public static int divideByTwoCounter = 1;
	public static int divideByThreeCounter = 2;
	public static int divideByFourCounter = 3;
	
	public static ItemStack[] getDeconstructResults(ItemStack parItemStack)
	{
		// DEBUG
		System.out.println("Looking for deconstructing a recipe for "+parItemStack.getUnlocalizedName());
		
		// Allow recipes for some vanilla items that normally don't have recipes
		Item theItem = parItemStack.getItem();
		if (
				theItem == Items.enchanted_book
				)
		{
			return getCraftingGrid(theItem);
		}

		// check all recipes for recipe for Itemstack
		List<?> listAllRecipes = CraftingManager.getInstance().getRecipeList();
//		// DEBUG
//		System.out.println("DeconstructingManager getDeconstructResults() recipe list size = "+listAllRecipes.size());
				
		for(int i = 0;i<listAllRecipes.size();i++)
		{
//			// DEBUG
//			System.out.println("Checking recipe number = "+i);
			
			IRecipe recipe = (IRecipe) listAllRecipes.get(i);
			if(recipe != null)
			{
				ItemStack recipeKeyItemStack = recipe.getRecipeOutput();
				if(recipeKeyItemStack!=null)
				{
//					// DEBUG
//					System.out.println("Comparing with a recipe for "+recipeKeyItemStack.getUnlocalizedName()+" with input item = "+parItemStack.getUnlocalizedName());
					if (recipeKeyItemStack.getUnlocalizedName().equals(parItemStack.getUnlocalizedName()))
					{
						// DEBUG
						System.out.println("Recipe matches item type = "+parItemStack.getUnlocalizedName()+" with item has damage value = "+parItemStack.getItemDamage());
						System.out.println("Recipe class is "+recipe.getClass().toString()+", adding crafting grid to list");
						return getCraftingGrid(recipe);
					}
					else
					{
//						// DEBUG
//						System.out.println("Recipe doesn't match item type");
					}
				}
//				else
//				{
//					// DEBUG
//					System.out.println("DeconstructingManager getDeconstructingResults() no recipe found for input item");
//				}
			}
		}
		return null;
	}	
	
	public static ItemStack[] getCraftingGrid(Item parItem)
	{
		// Initialize the result array
		ItemStack[] resultItemStackArray = new ItemStack[9];
		for(int j = 0;j<resultItemStackArray.length;j++)
		{
			resultItemStackArray[j] = null;
		}
		
		if (parItem == Items.enchanted_book)
		{
			resultItemStackArray = new ItemStack[] {
					null,
					new ItemStack(Items.paper, 1, 0),
					null,
					null,
					new ItemStack(Items.paper, 1, 0),
					null,
					null,
					new ItemStack(Items.paper, 1, 0),
					null
			};
		}
		return resultItemStackArray;
	}
	
	public static ItemStack[] getCraftingGrid(IRecipe parRecipe)
	{
		// Initialize the result array
		ItemStack[] resultItemStackArray = new ItemStack[9];
		for(int j = 0;j<resultItemStackArray.length;j++)
		{
			resultItemStackArray[j] = null;
		}
		
		if (parRecipe instanceof ShapedRecipes)
		{
			// DEBUG
			System.out.println("getCraftingGrid for shaped recipe");
			ShapedRecipes shaped = (ShapedRecipes)parRecipe;
			for(int j = 0;j<shaped.recipeItems.length;j++)
			{
				resultItemStackArray[j] = shaped.recipeItems[j];
			}
		}
		
		if (parRecipe instanceof ShapelessRecipes)
		{
			// DEBUG
			System.out.println("getCraftingGrid for shapeless recipe");
			ShapelessRecipes shapeless = (ShapelessRecipes)parRecipe;
			for(int j = 0;j<shapeless.recipeItems.size();j++)
			{
				resultItemStackArray[j] = (ItemStack) shapeless.recipeItems.get(j);
			}
		}
		
		if (parRecipe instanceof ShapedOreRecipe)
		{
			// DEBUG
			System.out.println("getCraftingGrid for shaped ore recipe");
			ShapedOreRecipe shaped = (ShapedOreRecipe)parRecipe;
			for(int j = 0;j<shaped.getInput().length;j++)
			{
				if(shaped.getInput()[j] instanceof ItemStack)
				{
					resultItemStackArray[j] = (ItemStack) shaped.getInput()[j];
				}
				else if(shaped.getInput()[j] instanceof List)
				{
					Object o = ((List) shaped.getInput()[j]).get(0);
					if(o instanceof ItemStack)
					{
						resultItemStackArray[j] = (ItemStack)o;
					}
				}
			}
		}
		
		if (parRecipe instanceof ShapelessOreRecipe)
		{
			ArrayList shapelessArray = ((ShapelessOreRecipe)parRecipe).getInput();
			// DEBUG
			System.out.println("getCraftingGrid for shapeless ore recipe with input array size = "+shapelessArray.size());
			for(int j = 0; j<shapelessArray.size(); j++)
			{
				if(shapelessArray.get(j) instanceof ItemStack)
				{
//					// DEBUG
//					System.out.println("Ingredient in shapeless array slot "+j+" is an ItemStack");
					resultItemStackArray[j] = (ItemStack) shapelessArray.get(j);
				}
				else if(shapelessArray.get(j) instanceof List)
				{
//					// DEBUG
//					System.out.println("Ingredient in shapeless array slot "+j+" is a List");
					Object o = ((List)shapelessArray.get(j)).get(0);
					if(o instanceof ItemStack)
					{
						resultItemStackArray[j] = (ItemStack)o;
					}
					else
					{
						// DEBUG
						System.out.println("But list element is not an ItemStack");
					}
				}
			}
		}

		return adjustOutputQuantities(resultItemStackArray, parRecipe.getRecipeOutput().getItem());
	}

	/**
	 * Adjust those cases where the recipe can be divided down (e.g. one door gives back two blocks)
	 * @param parItemStackArray should hold the regular recipe output item stack array
	 * @param parItem 
	 */
	private static ItemStack[] adjustOutputQuantities(ItemStack[] parItemStackArray, Item parItem) 
	{
		if (parItem == Items.oak_door)
		{
			return new ItemStack[] {
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1),
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1),
					null, null, null, null, null, null, null
			};
		}
		if (parItem == Items.spruce_door)
		{
			return new ItemStack[] {
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 1),
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 1),
					null, null, null, null, null, null, null
			};
		}
		if (parItem == Items.birch_door)
		{
			return new ItemStack[] {
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 2),
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 2),
					null, null, null, null, null, null, null
			};
		}
		if (parItem == Items.jungle_door)
		{
			return new ItemStack[] {
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 3),
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 3),
					null, null, null, null, null, null, null
			};
		}
		if (parItem == Items.acacia_door)
		{
			return new ItemStack[] {
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 4),
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 4),
					null, null, null, null, null, null, null
			};
		}
		if (parItem == Items.dark_oak_door)
		{
			return new ItemStack[] {
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 5),
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 5),
					null, null, null, null, null, null, null
			};
		}
		if (parItem == Items.iron_door)
		{
			return new ItemStack[] {
					new ItemStack(Items.iron_ingot, 1, 0),
					new ItemStack(Items.iron_ingot, 1, 0),
					null, null, null, null, null, null, null
			};
		}
		if (parItem == Items.paper)
		{
			return new ItemStack[] {
					new ItemStack(Items.reeds, 1, 0),
					null, null, null, null, null, null, null, null
			};
		}
		if (parItem == Items.stick)
		{
			return new ItemStack[] {
					new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 0),
					null, null, null, null, null, null, null, null
			};
		}
		if (parItem == Item.getItemFromBlock(Blocks.ladder))
		{
			if (divideByThreeCounter <= 0)
			{
				decrementDivideByThreeCounter();
				return new ItemStack[] {
						null, null, null,
						new ItemStack(Items.stick, 1, 0),
						new ItemStack(Items.stick, 1, 0), 
						new ItemStack(Items.stick, 1, 0), 
						null, null, null
				};
			}
			else if (divideByThreeCounter == 1)
			{
				decrementDivideByThreeCounter();
				return new ItemStack[] {
						new ItemStack(Items.stick, 1, 0),
						null,
						new ItemStack(Items.stick, 1, 0), 
						null, null, null, null, null, null
				};
			}
			else if (divideByThreeCounter == 2)
			{
				decrementDivideByThreeCounter();
				return new ItemStack[] {
						null, null, null, null, null, null,
						new ItemStack(Items.stick, 1, 0),
						null,
						new ItemStack(Items.stick, 1, 0), 
				};
			}
		}
		if (parItem == Item.getItemFromBlock(Blocks.oak_fence))
		{
			if (divideByThreeCounter <= 0)
			{
				decrementDivideByThreeCounter();
				return new ItemStack[] {
						new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 0),
						new ItemStack(Items.stick, 1, 0), 
						null, null, null, null, null, null, null
				};
			}
			else if (divideByThreeCounter == 1)
			{
				decrementDivideByThreeCounter();
				return new ItemStack[] {
						null, null,
						new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 0),
						new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 0),
						null, null, null, null, null
				};
			}
			else if (divideByThreeCounter == 2)
			{
				decrementDivideByThreeCounter();
				return new ItemStack[] {
						null, null, null, null,
						new ItemStack(Items.stick, 1, 0), 
						new ItemStack(Item.getItemFromBlock(Blocks.planks), 1, 0),
						null, null, null
				};
			}
		}
		if (parItem == Items.enchanted_book)
		{
			return new ItemStack[] {
					null, 
					new ItemStack(Items.reeds, 1, 0),
					null,
					null, 
					new ItemStack(Items.reeds, 1, 0),
					null,
					null, 
					new ItemStack(Items.reeds, 1, 0),
					null
			};
		}

		// else no adjustments needed
		return parItemStackArray ;
	}
	
	public static void decrementDivideByThreeCounter()
	{
		divideByThreeCounter--;
		if (divideByThreeCounter<0)
		{
			divideByThreeCounter=2;
		}				
	}
	
	public static int getStackSizeNeeded(ItemStack parItemStack)
	{
		Item theItem = parItemStack.getItem();
		// Create recipes for some things that don't normally have them
		if (
				theItem == Items.enchanted_book
				)
		{
			return 1;
		}
		List<?> crafts = CraftingManager.getInstance().getRecipeList();
		for(int i = 0;i<crafts.size();i++)
		{
			IRecipe recipe = (IRecipe) crafts.get(i);
			if(recipe != null)
			{
				ItemStack outputItemStack = recipe.getRecipeOutput();
				// if found matching recipe
				if (outputItemStack != null)
				{					
//					// DEBUG
//					System.out.println("Checking if recipes match: "+outputItemStack.getUnlocalizedName()+" with "+parItemStack.getUnlocalizedName());
					if (outputItemStack.getUnlocalizedName().equals(parItemStack.getUnlocalizedName()))
					{
						// DEBUG
						System.out.println("getStackSizeNeeded() found matching recipe");
						// prevent some deconstructions that aren't realistic (like paper into reeds)
						if (!BlockSmith.allowDeconstructAllCraftable)
						{
							if (theItem == Items.paper
							|| theItem == Items.melon_seeds
							|| theItem == Items.bread
							|| theItem == Items.cake
							)
							{
								return 0;
							}
						}
						if (       theItem == Items.oak_door
								|| theItem == Items.spruce_door
								|| theItem == Items.birch_door
								|| theItem == Items.jungle_door
								|| theItem == Items.acacia_door
								|| theItem == Items.dark_oak_door
								|| theItem == Items.iron_door
								|| theItem == Items.paper
								|| theItem == Items.stick
								|| theItem == Item.getItemFromBlock(Blocks.ladder)
								|| theItem == Items.enchanted_book					
								|| theItem == Item.getItemFromBlock(Blocks.oak_fence)
								)
						{
							return 1;
						}
						// DEBUG
						System.out.println("No adjustment needed to recipe amount");
						return outputItemStack.stackSize;
					}
				}
//				else
//				{
//					// DEBUG
//					System.out.println("Recipe output stack is null!");
//				}
			}
		}
		// DEBUG
		System.out.println("No matching recipe found!");
		return 0; // no recipe found
	}
	
}
