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

package com.blogspot.jabelarminecraft.blocksmith.proxy;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBasic;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import com.blogspot.jabelarminecraft.blocksmith.BlockSmith;
import com.blogspot.jabelarminecraft.blocksmith.EventHandler;
import com.blogspot.jabelarminecraft.blocksmith.FMLEventHandler;
import com.blogspot.jabelarminecraft.blocksmith.OreGenEventHandler;
import com.blogspot.jabelarminecraft.blocksmith.TerrainGenEventHandler;
import com.blogspot.jabelarminecraft.blocksmith.commands.CommandStructureCapture;
import com.blogspot.jabelarminecraft.blocksmith.entities.EntityPigTest;
import com.blogspot.jabelarminecraft.blocksmith.gui.GuiHandler;
import com.blogspot.jabelarminecraft.blocksmith.items.SpawnEgg;
import com.blogspot.jabelarminecraft.blocksmith.networking.MessageExtendedReachAttack;
import com.blogspot.jabelarminecraft.blocksmith.networking.MessageRequestItemStackRegistryFromClient;
import com.blogspot.jabelarminecraft.blocksmith.networking.MessageSendItemStackRegistryToServer;
import com.blogspot.jabelarminecraft.blocksmith.networking.MessageSyncEntityToClient;
import com.blogspot.jabelarminecraft.blocksmith.networking.MessageToClient;
import com.blogspot.jabelarminecraft.blocksmith.networking.MessageToServer;
import com.blogspot.jabelarminecraft.blocksmith.tileentities.TileEntityCompactor;
import com.blogspot.jabelarminecraft.blocksmith.tileentities.TileEntityForge;
import com.blogspot.jabelarminecraft.blocksmith.tileentities.TileEntityGrinder;
import com.blogspot.jabelarminecraft.blocksmith.tileentities.TileEntityMovingLightSource;
import com.blogspot.jabelarminecraft.blocksmith.tileentities.TileEntityTanningRack;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

public class CommonProxy 
{
    
    protected int modEntityID = 0;
    
    /*
     * Sometimes useful to have list of all item types, including subtypes
     */
    protected List itemStackRegistry = new ArrayList();
     
    public void fmlLifeCycleEvent(FMLPreInitializationEvent event)
    { 
        // load configuration before doing anything else
        // got config tutorial from http://www.minecraftforge.net/wiki/How_to_make_an_advanced_configuration_file
        initConfig(event);

        // register stuff
        registerBlocks();
        registerItems();
        registerTileEntities();
        registerModEntities();
        registerEntitySpawns();
        registerFuelHandlers();
        registerSimpleNetworking();
//        VillagerRegistry.instance().registerVillagerId(10);
//      VillagerRegistry.instance().registerVillageTradeHandler(10, new VillageTradeHandlerMagicBeans());
//      VillagerRegistry.getRegisteredVillagers();
    }

    public void fmlLifeCycleEvent(FMLInitializationEvent event)
    {
        // register custom event listeners
        registerEventListeners();
         
        // register recipes here to allow use of items from other mods
        registerRecipes();
        
        // register achievements here to allow use of items from other mods
        registerAchievements();
        
        // register gui handlers
        registerGuiHandlers();
        
//        registerDeconstructingInit(event);
    }
    
    public void registerGuiHandlers() 
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(BlockSmith.instance, new GuiHandler());     
    }

    public void fmlLifeCycleEvent(FMLPostInitializationEvent event)
    {
        // can do some inter-mod stuff here
        initItemStackRegistry();    
    }

    public void fmlLifeCycleEvent(FMLServerAboutToStartEvent event) 
    {
        // TODO Auto-generated method stub
        
    }

    public void fmlLifeCycleEvent(FMLServerStartedEvent event) 
    {
        // TODO Auto-generated method stub

    }

    public void fmlLifeCycleEvent(FMLServerStoppingEvent event) 
    {
        // TODO Auto-generated method stub
        
    }

    public void fmlLifeCycleEvent(FMLServerStoppedEvent event) 
    {
        // TODO Auto-generated method stub
        
    }

    public void fmlLifeCycleEvent(FMLServerStartingEvent event) 
    {
        // // register server commands
        event.registerServerCommand(new CommandStructureCapture());
    }
        
    /*
     * Thanks to diesieben07 tutorial for this code
     */
    /**
     * Registers the simple networking channel and messages for both sides
     */
    protected void registerSimpleNetworking() 
    {
        // DEBUG
        System.out.println("registering simple networking");
        BlockSmith.network = NetworkRegistry.INSTANCE.newSimpleChannel(BlockSmith.NETWORK_CHANNEL_NAME);

        int packetId = 0;
        // register messages from client to server
        BlockSmith.network.registerMessage(MessageToServer.Handler.class, MessageToServer.class, packetId++, Side.SERVER);
        // register messages from server to client
        BlockSmith.network.registerMessage(MessageToClient.Handler.class, MessageToClient.class, packetId++, Side.CLIENT);
        BlockSmith.network.registerMessage(MessageSyncEntityToClient.Handler.class, MessageSyncEntityToClient.class, packetId++, Side.CLIENT);
        BlockSmith.network.registerMessage(MessageExtendedReachAttack.Handler.class, MessageExtendedReachAttack.class, packetId++, Side.SERVER);
        BlockSmith.network.registerMessage(MessageSendItemStackRegistryToServer.Handler.class, MessageSendItemStackRegistryToServer.class, packetId++, Side.SERVER);
        BlockSmith.network.registerMessage(MessageRequestItemStackRegistryFromClient.Handler.class, MessageRequestItemStackRegistryFromClient.class, packetId++, Side.CLIENT);
    }
    
    /*   
     * Thanks to CoolAlias for this tip!
     */
    /**
     * Returns a side-appropriate EntityPlayer for use during message handling
     */
    public EntityPlayer getPlayerEntityFromContext(MessageContext ctx) 
    {
        return ctx.getServerHandler().playerEntity;
    }
    
    /**
     * Process the configuration
     * @param event
     */
    protected void initConfig(FMLPreInitializationEvent event)
    {
        // might need to use suggestedConfigFile (event.getSuggestedConfigFile) location to publish
        BlockSmith.configFile = event.getSuggestedConfigurationFile();
        // DEBUG
        System.out.println(BlockSmith.MODNAME+" config path = "+BlockSmith.configFile.getAbsolutePath());
        System.out.println("Config file exists = "+BlockSmith.configFile.canRead());
        
        BlockSmith.config = new Configuration(BlockSmith.configFile);

        syncConfig();
    }
    
    /*
     * sync the configuration
     * want it public so you can handle case of changes made in-game
     */
    public void syncConfig()
    {
        BlockSmith.config.load();
        BlockSmith.allowDeconstructUnrealistic = BlockSmith.config.get(Configuration.CATEGORY_GENERAL, "All Craftables Can Deconstruct", false, "Allow unrealistic deconstruction like pumpkins back from pumpkin seeds").getBoolean(false);
        // DEBUG
        System.out.println("Allow unrealistic deconstruction = "+BlockSmith.allowDeconstructUnrealistic);
        BlockSmith.allowHorseArmorCrafting = BlockSmith.config.get(Configuration.CATEGORY_GENERAL, "Can Craft Horse Armor", true, "Allow crafting of horse armor and saddles").getBoolean(true);
        // DEBUG
        System.out.println("Allow horse armor crafting = "+BlockSmith.allowHorseArmorCrafting);
        BlockSmith.allowDeconstructEnchantedBooks  = BlockSmith.config.get(Configuration.CATEGORY_GENERAL, "Can Deconstruct Enchanted Books", true, "Allow enchanted books to deconstruct like a regular book").getBoolean(true);
        // DEBUG
        System.out.println("Allow enchanted book deconstruction = "+BlockSmith.allowDeconstructEnchantedBooks);
        BlockSmith.allowPartialDeconstructing = BlockSmith.config.get(Configuration.CATEGORY_GENERAL, "Allow Partial Deconstruction", true, "Allow deconstruction of stacks that are less than crafting output").getBoolean(true);
        // DEBUG
        System.out.println("Allow partial deconstruction = "+BlockSmith.allowPartialDeconstructing);

        
        // save is useful for the first run where config might not exist, and doesn't hurt
        BlockSmith.config.save();
    }

    /**
     * Registers blocks
     */
    public void registerBlocks()
    {
        //example: GameRegistry.registerBlock(blockTomato, "tomatoes");
        GameRegistry.registerBlock(BlockSmith.blockTanningRack, BlockSmith.blockTanningRack.getUnlocalizedName().substring(5));
        GameRegistry.registerBlock(BlockSmith.blockGrinder, BlockSmith.blockGrinder.getUnlocalizedName().substring(5));
        GameRegistry.registerBlock(BlockSmith.blockCompactor, BlockSmith.blockCompactor.getUnlocalizedName().substring(5));
        GameRegistry.registerBlock(BlockSmith.blockDeconstructor, BlockSmith.blockDeconstructor.getUnlocalizedName().substring(5));
        GameRegistry.registerBlock(BlockSmith.blockForge, BlockSmith.blockForge.getUnlocalizedName().substring(5));
//        GameRegistry.registerBlock(BlockSmith.blockForgeLit, BlockSmith.blockForgeLit.getUnlocalizedName().substring(5));
        GameRegistry.registerBlock(BlockSmith.blockMovingLightSource, BlockSmith.blockMovingLightSource.getUnlocalizedName().substring(5));
        
        // each instance of your block should have a name that is unique within your mod.  use lower case.
        // you don't need to register an item corresponding to the block, GameRegistry.registerBlock does this automatically.
        
    }

    /** 
     * Registers fluids
     */
    public void registerFluids()
    {
        // see tutorial at http://www.minecraftforge.net/wiki/Create_a_Fluid
        // Fluid testFluid = new Fluid("testfluid");
        // FluidRegistry.registerFluid(testFluid);
        // testFluid.setLuminosity(0).setDensity(1000).setViscosity(1000).setGaseous(false) ;
     }
    
    /**
     * Registers items
     */
    private void registerItems()
    {
        // DEBUG
        System.out.println("Registering items");

        GameRegistry.registerItem(BlockSmith.cowHide, BlockSmith.cowHide.getUnlocalizedName().substring(5));
        GameRegistry.registerItem(BlockSmith.sheepSkin, BlockSmith.sheepSkin.getUnlocalizedName().substring(5));
        GameRegistry.registerItem(BlockSmith.pigSkin, BlockSmith.pigSkin.getUnlocalizedName().substring(5));
        GameRegistry.registerItem(BlockSmith.horseHide, BlockSmith.horseHide.getUnlocalizedName().substring(5));
        GameRegistry.registerItem(BlockSmith.swordExtended, BlockSmith.swordExtended.getUnlocalizedName().substring(5));
    }
    
    /**
     * Registers tile entities
     */
    public void registerTileEntities()
    {
        // DEBUG
        System.out.println("Registering tile entities");
        GameRegistry.registerTileEntity(TileEntityGrinder.class, "tileEntityGrinder");               
        GameRegistry.registerTileEntity(TileEntityCompactor.class, "tileEntityCompactor");               
        GameRegistry.registerTileEntity(TileEntityTanningRack.class, "tileEntityTanningRack");               
        GameRegistry.registerTileEntity(TileEntityForge.class, "tileEntityForge");               
        GameRegistry.registerTileEntity(TileEntityMovingLightSource.class, "tileEntityMovingLightSource");               
        // example: GameRegistry.registerTileEntity(TileEntityMagicBeanStalk.class, "tileEntityMagicBeanStalk");
    }

    /**
     * Registers recipes
     */
    public void registerRecipes()
    {
        // DEBUG
        System.out.println("Registering recipes");
                       
        // examples:
        //        GameRegistry.addRecipe(recipe);
        //        GameRegistry.addShapedRecipe(output, params);
        //        GameRegistry.addShapelessRecipe(output, params);
        //        GameRegistry.addSmelting(input, output, xp);
        GameRegistry.addShapedRecipe(new ItemStack(Item.getItemFromBlock(BlockSmith.blockGrinder), 1), 
                new Object[]
                {
                    "ABA",
                    "A A",
                    "CCC",
                    'A', Items.stick, 'B', Blocks.stone, 'C', Blocks.cobblestone
                });
        GameRegistry.addShapedRecipe(new ItemStack(BlockSmith.blockDeconstructor), 
                new Object[]
                {
                    "SSS", 
                    "SXS", 
                    "SSS", 
                    'X', Blocks.crafting_table, 'S', Blocks.cobblestone
                });
        GameRegistry.addShapedRecipe(new ItemStack(Items.iron_horse_armor), 
                new Object[]
                {
                    "  S", 
                    "SXS", 
                    "SSS", 
                    'X', Blocks.wool, 'S', Items.iron_ingot
                });
        GameRegistry.addShapedRecipe(new ItemStack(Items.golden_horse_armor), 
                new Object[]
                {
                    "  S", 
                    "SXS", 
                    "SSS", 
                    'X', Blocks.wool, 'S', Items.gold_ingot
                });
        GameRegistry.addShapedRecipe(new ItemStack(Items.diamond_horse_armor), 
                new Object[]
                {
                    "  S", 
                    "SXS", 
                    "SSS", 
                    'X', Blocks.wool, 'S', Items.diamond
                });
        GameRegistry.addShapedRecipe(new ItemStack(Items.saddle), 
                new Object[]
                {
                    "SSS", 
                    "SXS", 
                    "X X", 
                    'X', Items.iron_ingot, 'S', Items.leather
                });

    }

    /**
     * Registers entities as mod entities
     */
    protected void registerModEntities()
    {    
         // DEBUG
        System.out.println("Registering entities");
        // if you want it with a spawn egg use
        // registerModEntityWithEgg(EntityManEatingTiger.class, "Tiger", 0xE18519, 0x000000);
        // or without spawn egg use

        // example: registerModEntity(EntityGoldenGoose.class, "golden_goose");
        registerModEntityWithEgg(EntityPigTest.class, "Test Pig", 0xE18519, 0x000000);
    }
 
    /**
     * Registers an entity as a mod entity with no tracking
     * @param parEntityClass
     * @param parEntityName
     */
     protected void registerModEntity(Class parEntityClass, String parEntityName)
     {
            EntityRegistry.registerModEntity(parEntityClass, parEntityName, ++modEntityID, BlockSmith.instance, 80, 3, false);
     }

     /**
      * Registers an entity as a mod entity with fast tracking.  Good for fast moving objects like throwables
      * @param parEntityClass
      * @param parEntityName
      */
     protected void registerModEntityFastTracking(Class parEntityClass, String parEntityName)
     {
            EntityRegistry.registerModEntity(parEntityClass, parEntityName, ++modEntityID, BlockSmith.instance, 80, 10, true);
     }

     public void registerModEntityWithEgg(Class parEntityClass, String parEntityName, 
              int parEggColor, int parEggSpotsColor)
    {
        registerModEntity(parEntityClass, parEntityName);
        registerSpawnEgg(parEntityName, parEggColor, parEggSpotsColor);
    }

     // can't use vanilla spawn eggs with entities registered with modEntityID, so use custom eggs.
     // name passed must match entity name string
     public void registerSpawnEgg(String parSpawnName, int parEggColor, int parEggSpotsColor)
     {
         Item itemSpawnEgg = new SpawnEgg(parSpawnName, parEggColor, parEggSpotsColor);
         GameRegistry.registerItem(itemSpawnEgg, itemSpawnEgg.getUnlocalizedName().substring(5));
     }

     /**
      * Registers entity natural spawns
      */
     protected void registerEntitySpawns()
     {
        /*
         *  register natural spawns for entities
         * EntityRegistry.addSpawn(MyEntity.class, spawnProbability, minSpawn, maxSpawn, enumCreatureType, [spawnBiome]);
         * See the constructor in BiomeGenBase.java to see the rarity of vanilla mobs; Sheep are probability 10 while Endermen are probability 1
         * minSpawn and maxSpawn are about how groups of the entity spawn
         * enumCreatureType represents the "rules" Minecraft uses to determine spawning, based on creature type. By default, you have three choices:
         *    EnumCreatureType.creature uses rules for animals: spawn everywhere it is light out.
         *    EnumCreatureType.monster uses rules for monsters: spawn everywhere it is dark out.
         *    EnumCreatureType.waterCreature uses rules for water creatures: spawn only in water.
         * [spawnBiome] is an optional parameter of type BiomeGenBase that limits the creature spawn to a single biome type. Without this parameter, it will spawn everywhere. 
         */

         // DEBUG
        System.out.println("Registering natural spawns");

        // // savanna
        // EntityRegistry.addSpawn(EntityLion.class, 6, 1, 5, EnumCreatureType.creature, BiomeGenBase.savanna); //change the values to vary the spawn rarity, biome, etc.              
        // EntityRegistry.addSpawn(EntityElephant.class, 10, 1, 5, EnumCreatureType.creature, BiomeGenBase.savanna); //change the values to vary the spawn rarity, biome, etc.              
     }
 
     protected void addSpawnAllBiomes(EntityLiving parEntity, int parChance, int parMinGroup, int parMaxGroup)
     {
         
         /*
          *  For the biome type you can use an list, but unfortunately the built-in biomeList contains
          * null entries and will crash, so you need to clean up that list.
          * diesieben07 suggested the following code to remove the nulls and create list of all biomes
          */
         BiomeGenBase[] allBiomes = Iterators.toArray(Iterators.filter(Iterators.forArray(BiomeGenBase.getBiomeGenArray()), Predicates.notNull()), BiomeGenBase.class);
         for (int i=0; i<allBiomes.length; i++)
         {
             EntityRegistry.addSpawn(parEntity.getClass(), parChance, parMinGroup, parMaxGroup, EnumCreatureType.CREATURE, 
                  allBiomes[i]); //change the values to vary the spawn rarity, biome, etc.              
         }
     }
     
     
     /**
     * Register fuel handlers
     */
     protected void registerFuelHandlers()
     {
         // DEBUG
        System.out.println("Registering fuel handlers");
        
        // example: GameRegistry.registerFuelHandler(handler);
     }
 
    /**
     * Register event listeners
     */
    protected void registerEventListeners() 
    {
        // DEBUG
        System.out.println("Registering event listeners");

        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainGenEventHandler());
        MinecraftForge.ORE_GEN_BUS.register(new OreGenEventHandler());        

        // some events, especially tick, is handled on FML bus
        FMLCommonHandler.instance().bus().register(new FMLEventHandler());
    }
    
    /**
     * Register achievements
     */
    protected void registerAchievements()
    {
        BlockSmith.achievementTanningAHide = new Achievement("achievement.tanningahide", "tanningahide", 0, 0, Items.leather, (Achievement)null);
        BlockSmith.achievementTanningAHide.registerStat().initIndependentStat(); // Eclipse is having trouble chaining these in previous line
//      BlockSmith.achievementGiantSlayer = new Achievement("achievement.giantslayer", "giantslayer", 2, 1, (Item)null, BlockSmith.achievementTanningAHide).setSpecial();
//      BlockSmith.achievementGiantSlayer.registerStat(); // Eclipse is having trouble chaining this in previous line
        BlockSmith.craftTable = (Achievement) new Achievement("createDecraftTable", "createDecraftTable", 1 - 2 - 2, -1 - 3, BlockSmith.blockDeconstructor, null).registerStat();
        BlockSmith.deconstructAny = (Achievement) new Achievement("deconstructAnything", "deconstructAnything", 2 - 2, -2 - 2, Items.diamond_hoe, BlockSmith.craftTable).registerStat();
        BlockSmith.deconstructDiamondHoe = (Achievement) new Achievement("deconstructDiamondHoe", "deconstructDiamondHoe", 2 - 2, 0 - 2, Items.diamond_hoe, BlockSmith.deconstructAny).registerStat();
        BlockSmith.deconstructJunk = (Achievement) new Achievement("deconstructJunk", "deconstructJunk", 1 - 2, -1 - 2, Items.leather_boots, BlockSmith.deconstructAny).registerStat();
        BlockSmith.deconstructDiamondShovel = (Achievement) new Achievement("deconstructDiamondShovel", "deconstructDiamondShovel", 3 - 2, -1 - 2, Items.diamond_shovel, BlockSmith.deconstructAny).registerStat();
        BlockSmith.theHatStandAchievement = (Achievement) new Achievement("porteManteauAchievement", "porteManteauAchievement", 3 - 2, -4 - 2, Blocks.oak_fence, BlockSmith.craftTable).registerStat();
        AchievementPage.registerAchievementPage(new AchievementPage("BlockSmith",
                new Achievement[]
                {
                BlockSmith.craftTable, BlockSmith.deconstructAny, BlockSmith.deconstructDiamondHoe, BlockSmith.deconstructJunk, BlockSmith.deconstructDiamondShovel, BlockSmith.theHatStandAchievement
                }));

        BlockSmith.deconstructedItemsStat = (StatBasic) (new StatBasic("stat.deconstructeditems", new ChatComponentTranslation("stat.deconstructeditems", new Object[0])).registerStat());
        
    }
    
    protected void initItemStackRegistry()
    {
        return;
    }

    public void setItemStackRegistry(List parRegistry)
    {
        itemStackRegistry = parRegistry;
    }
    
    public List getItemStackRegistry()
    {
        return itemStackRegistry;
    }
        
    /*
     * Works directly on passed in ByteBuf to put ItemStack registry into packet payload to be sent to the server
     */
    public void convertItemStackListToPayload(ByteBuf parBuffer)
    {
        Iterator theIterator = itemStackRegistry.iterator();
       
        while (theIterator.hasNext())
        {          
            ItemStack theStack = (ItemStack) theIterator.next();
            
            // write item id and metadata
            parBuffer.writeInt(Item.getIdFromItem(theStack.getItem()));
            parBuffer.writeInt(theStack.getMetadata());
            
//            // DEBUG
//            System.out.println(Item.getIdFromItem(theStack.getItem())+" "+theStack.getMetadata());
            boolean hasNBT = theStack.hasTagCompound();
            parBuffer.writeBoolean(hasNBT);
            if (hasNBT)
            {
                // DEBUG
                System.out.println("The stack "+theStack.toString()+" has NBT = "+theStack.getTagCompound().toString());
                ByteBufUtils.writeTag(parBuffer, theStack.getTagCompound());
            }
            theIterator.remove(); // avoids a ConcurrentModificationException
        }
        
        return ;
    }


    /*
     * Provides a list of item stacks giving every registered item along with its metadata variants
     * based on a message payload from the client that gives the valid metadata values for those
     * items with variants. Also will include NBT for mods like Tinker's Construct that use NBT on the
     * ItemStacks to make variants instead of metadata.
     */
    public List<ItemStack> convertPayloadToItemStackList(ByteBuf theBuffer)
    {
        List<ItemStack> theList = new ArrayList();
        
        while (theBuffer.isReadable())
        {
            int theID = theBuffer.readInt();
            int theMetadata = theBuffer.readInt();
            ItemStack theStack = new ItemStack(Item.getItemById(theID), 1, theMetadata);
            
            // Handle the case of mods like Tinker's Construct that use NBT instead of metadata
            boolean hasNBT = theBuffer.readBoolean();
            if (hasNBT)
            {
                theStack.setTagCompound(ByteBufUtils.readTag(theBuffer));
                // DEBUG
                System.out.println("The stack "+theStack.toString()+" has NBT = "+theStack.getTagCompound().toString());
            }
            
           theList.add(theStack);
        }

        // DEBUG
        System.out.println(theList.toString());

        return theList;      
    }
}