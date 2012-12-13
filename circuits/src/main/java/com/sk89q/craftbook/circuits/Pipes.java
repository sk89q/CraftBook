package com.sk89q.craftbook.circuits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.PistonBaseMaterial;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.ICMechanic;
import com.sk89q.craftbook.ic.PipeInputIC;
import com.sk89q.craftbook.util.GeneralUtil;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class Pipes extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<Pipes> {

        CircuitsPlugin plugin;

        public Factory(CircuitsPlugin plugin) {

            this.plugin = plugin;
        }

        @Override
        public Pipes detect(BlockWorldVector pt) {

            int type = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));

            if (type == BlockID.PISTON_STICKY_BASE || type == BlockID.PISTON_BASE) return new Pipes(pt);

            return null;
        }

        public Pipes detect(BlockWorldVector pt, List<ItemStack> items) {

            int type = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));

            if (type == BlockID.PISTON_STICKY_BASE || type == BlockID.PISTON_BASE) return new Pipes(pt, items);

            return null;
        }
    }

    /**
     * Construct the mechanic for a location.
     *
     * @param pt
     */
    private Pipes(BlockWorldVector pt) {

        super();
    }

    private Pipes(BlockWorldVector pt, List<ItemStack> items) {

        super();
        this.items.addAll(items);
        startPipe(BukkitUtil.toBlock(pt));
    }

    private List<ItemStack> items = new ArrayList<ItemStack>();
    private List<BlockVector> visitedPipes = new ArrayList<BlockVector>();

    public void searchNearbyPipes(Block block) {

        for(int x = -1; x < 2; x++) {
            for(int y = -1; y < 2; y++) {
                for(int z = -1; z < 2; z++) {

                    Block off = block.getRelative(x, y, z);
                    BlockVector bv = BukkitUtil.toVector(off);
                    if(visitedPipes.contains(bv))
                        continue;

                    visitedPipes.add(bv);

                    if(off.getTypeId() == BlockID.GLASS) {

                        searchNearbyPipes(off);
                    }
                    else if(off.getTypeId() == BlockID.PISTON_BASE) {

                        PistonBaseMaterial p = (PistonBaseMaterial)off.getState().getData();
                        Block fac = off.getRelative(p.getFacing());
                        if(fac.getTypeId() == BlockID.CHEST || fac.getTypeId() == BlockID.DISPENSER) {
                            List<ItemStack> newItems = new ArrayList<ItemStack>();

                            for(ItemStack item : items) {
                                if(item == null)
                                    continue;
                                newItems.addAll(((InventoryHolder)fac.getState()).getInventory().addItem(item).values());
                            }

                            items.clear();
                            items.addAll(newItems);

                            if(!items.isEmpty())
                                searchNearbyPipes(block);
                        }
                        else if(fac.getTypeId() == BlockID.WALL_SIGN) {

                            if(CircuitsPlugin.getInst().icFactory == null)
                                continue;

                            try {
                                ICMechanic icmech = CircuitsPlugin.getInst().icFactory.detect(BukkitUtil.toWorldVector(fac));
                                if(icmech == null)
                                    continue;
                                if(!(icmech.getIC() instanceof PipeInputIC))
                                    continue;
                                List<ItemStack> newItems = ((PipeInputIC)icmech.getIC()).onPipeTransfer(BukkitUtil.toWorldVector(off), items);

                                items.clear();
                                items.addAll(newItems);

                                if(!items.isEmpty())
                                    searchNearbyPipes(block);
                            }
                            catch(Exception e){
                                Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
                            }
                        }
                    }
                }
            }
        }
    }

    public void startPipe(Block block) {

        visitedPipes.clear();

        if(block.getTypeId() == BlockID.PISTON_STICKY_BASE) {

            PistonBaseMaterial p = (PistonBaseMaterial)block.getState().getData();
            Block fac = block.getRelative(p.getFacing());
            if(fac.getTypeId() == BlockID.CHEST || fac.getTypeId() == BlockID.DISPENSER) {
                items.addAll(Arrays.asList(((InventoryHolder)fac.getState()).getInventory().getContents().clone()));
                ((InventoryHolder)fac.getState()).getInventory().clear();
                visitedPipes.add(BukkitUtil.toVector(fac));
                searchNearbyPipes(block);
                if(!items.isEmpty()) {
                    for(ItemStack item : items) {
                        if(item == null)
                            continue;
                        ((InventoryHolder)fac.getState()).getInventory().addItem(item);
                    }
                }
            }
            else if (!items.isEmpty()) {
                searchNearbyPipes(block);
                if(!items.isEmpty())
                    for(ItemStack item : items) {
                        block.getWorld().dropItemNaturally(block.getLocation().add(0.5,0.5,0.5), item);
                        if(item == null)
                            continue;
                    }
            }
        }
    }

    /**
     * Raised when an input redstone current changes.
     */
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        startPipe(event.getBlock());
    }
}