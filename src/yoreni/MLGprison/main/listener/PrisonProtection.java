package yoreni.MLGprison.main.listener;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;
import java.util.List;

/**
 * Created on 2/15/2018.
 *
 * Not the best name but close enough
 *
 * @author RoboMWM
 */
public class PrisonProtection implements Listener
{
    private World prisonWorld;

    public PrisonProtection(JavaPlugin plugin)
    {
        prisonWorld = plugin.getServer().getWorld("prison");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    //Prevent placing, unless in creative
    @EventHandler(ignoreCancelled = true)
    private void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.getBlock().getWorld() != prisonWorld)
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        event.setCancelled(true);
    }

    //Prevent itemframes from being destroyed
    @EventHandler(ignoreCancelled = true)
    private void onEntityFrameDestroy(HangingBreakEvent event)
    {
        if (event.getEntity().getWorld() != prisonWorld)
            return;
        if (event.getCause() != HangingBreakEvent.RemoveCause.ENTITY)
            event.setCancelled(true);
    }
    @EventHandler(ignoreCancelled = true)
    private void onEntityFrameDestroy(HangingBreakByEntityEvent event)
    {
        if (event.getEntity().getWorld() != prisonWorld)
            return;
        if (event.getRemover().getType() != EntityType.PLAYER)
        {
            event.setCancelled(true);
            return;
        }
        Player player = (Player)event.getRemover();
        if (player.getGameMode() != GameMode.CREATIVE)
            event.setCancelled(true);
    }
    @EventHandler(ignoreCancelled = true)
    private void onEntityInsideFrameDestroy(EntityDamageByEntityEvent event)
    {
        if (event.getEntity().getWorld() != prisonWorld)
            return;
        if (event.getEntityType() != EntityType.ITEM_FRAME)
            return;
        if (event.getDamager().getType() != EntityType.PLAYER)
        {
            event.setCancelled(true);
            return;
        }
        Player player = (Player)event.getDamager();
        if (player.getGameMode() != GameMode.CREATIVE)
            event.setCancelled(true);
    }

    //Prevent breaking outside of mine y levels
    @EventHandler(ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent event)
    {
        if (event.getBlock().getWorld() != prisonWorld)
            return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        int y = event.getBlock().getLocation().getBlockY();

        if (y > 18 || y < 4)
        {
            event.setCancelled(true);
            return;
        }
    }

    //Prevent explosions breaking blocks outside of mine y levels
    @EventHandler(ignoreCancelled = true)
    private void onEntityExplosion(EntityExplodeEvent event)
    {
        removeNonMineBlocks(event.blockList());
    }
    @EventHandler(ignoreCancelled = true)
    private void onBlockExplosion(BlockExplodeEvent event)
    {
        removeNonMineBlocks(event.blockList());
    }
    //Helper method to avoid duplicate code for both explosion events
    private void removeNonMineBlocks(List<Block> blocks)
    {
        if (!blocks.isEmpty() && blocks.get(0).getWorld() != prisonWorld)
            return;

        Iterator<Block> blockIterator = blocks.iterator();
        while (blockIterator.hasNext())
        {
            int y = blockIterator.next().getLocation().getBlockY();
            if (y > 18 || y < 4)
                blockIterator.remove();
        }
    }
}
