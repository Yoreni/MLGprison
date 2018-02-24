package yoreni.MLGprison.main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import yoreni.MLGprison.main.listener.PrisonProtection;


public class Main extends JavaPlugin implements Listener
{

	private static Economy eco = null;
	DecimalFormat abb = new DecimalFormat("0.######");

	DataFile rankups = new DataFile(this);

	DataFile data = new DataFile(this);

	DataFile warps = new DataFile(this);

	DataFile config = new DataFile(this);

	DataFile sell = new DataFile(this);


	public void onEnable()
	{
		if(!setupEconomy())
		{
			getServer().getLogger().severe("Vault eco could not be set up disabling plugin");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		getServer().getPluginManager().registerEvents(this,this);
		if (!getDataFolder().exists()) 
		{
			getDataFolder().mkdirs();
		}
		rankups.setup("rankups");
		data.setup("data");
		warps.setup("warps");
		sell.setup("sell");

		config.setup("config");
		config.addSetting("StarterDays",30);
		config.addSetting("StarterDaysAfterExpire",7);
		new PrisonProtection(this);
		int id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() 
		{
			public void run() 
			{
				Collection<? extends Player> list = Bukkit.getOnlinePlayers();
				for(Player player:list)
				{
					if(System.currentTimeMillis() > data.getLong(player.getUniqueId().toString() + ".Expire") && data.getInt(player.getUniqueId().toString() + ".Rank") >= 2)
					{
						long overflow = (data.getLong(player.getUniqueId().toString() + ".Expire") * System.currentTimeMillis()) * -1;
						overflow /= 1000;
						overflow /= 60;
						overflow /= 60;
						overflow /= 24;
						int ranksLose = (int) Math.floor(overflow / 7) + 1;
						data.changeInt(player.getUniqueId().toString() + ".Rank",ranksLose);
						if(data.getInt(player.getUniqueId().toString() + ".Rank") < 1) data.set(player.getUniqueId().toString() + ".Rank", 1);
						data.set(player.getUniqueId().toString() + ".Expire",System.currentTimeMillis() + (86400000L * config.getInt("StarterDaysAfterExpire")));
						player.sendMessage(ChatColor.RED + "You have been ranked down to " + rankups.getString(data.getInt(player.getUniqueId().toString() + ".Rank") + "Name") + " while you where away");
					}
				}
			}
		}, 0, 20 * 60);
	}

	public String format (double y)
	{
		if(y == 0) return "0";
		String[] Abbrivateions = {"","k"," Mill"," Bill"," Trill", " Quad", " Quint"," Sext"," Sept"," Oct"," Non"," Dec"};
		double round = Math.pow(10,2);
		int amp = (int) Math.floor(Math.log10(y) / 3);
		for(int b = 0;b <= 100;b++)
		{
			if (y >= 1000) y = y / 1000;
			else b = 100;

		}
		//if (y >= 100) round = 1;
		//else if (y >= 10) round = 10;
		y = Math.round(y *  (1 * round));
		y = y / round; 
		if (y == 1000)
		{
			y = 1;
			amp++;
		}
		return abb.format(y) + Abbrivateions[amp];
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if(!data.isSet(player.getUniqueId().toString()))
		{
			data.set(player.getUniqueId().toString() + ".Rank",1);
			data.set(player.getUniqueId().toString() + ".Expire",-1);
		}
		if(!player.getWorld().getName().equals("prison")) return;
		if(System.currentTimeMillis() > data.getLong(player.getUniqueId().toString() + ".Expire") && data.getInt(player.getUniqueId().toString() + ".Rank") >= 2)
		{
			long overflow = (data.getLong(player.getUniqueId().toString() + ".Expire") * System.currentTimeMillis()) * -1;
			overflow /= 1000;
			overflow /= 60;
			overflow /= 60;
			overflow /= 24;
			int ranksLose = (int) Math.floor(overflow / 7) + 1;
			data.changeInt(player.getUniqueId().toString() + ".Rank",ranksLose);
			if(data.getInt(player.getUniqueId().toString() + ".Rank") < 1) data.set(player.getUniqueId().toString() + ".Rank", 1);
			data.set(player.getUniqueId().toString() + ".Expire",System.currentTimeMillis() + (86400000L * config.getInt("StarterDaysAfterExpire")));
			player.sendMessage(ChatColor.RED + "You have been ranked down to " + rankups.getString(data.getInt(player.getUniqueId().toString() + ".Rank") + "Name") + " while you where away");
		}
		else if(data.getInt(player.getUniqueId().toString() + ".Rank") >= 2)
		{
			long left = data.getLong(player.getUniqueId().toString() + ".Expire") - System.currentTimeMillis();
			left /= 1000; //from ms to s
			left /= 60;//from s to m
			left /= 60;//from m to h
			player.sendMessage(ChatColor.RED + "You will be ranked down in " + ((int) Math.floor(left / 24)) + " days and " + (left % 24) + " hours. Use /renew <days> or /rankup to extend this time");
		}
	}

	public void onDisable()
	{

	}

	private boolean setupEconomy() 
	{
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		eco = rsp.getProvider();
		return eco != null;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		Player player = (Player) sender;
		/*if(label.equalsIgnoreCase("a"))
		{
			rankups.set("test", 1);
			try 
			{
				rankups.save(Frankups);
			} 
			catch (IOException IOException) 
			{
				// TODO Auto-generated catch block
				IOException.printStackTrace();
			}
			return true;
		}*/
		if(label.equalsIgnoreCase("MLGprison"))
		{
			if(player.hasPermission("MLGprison"))
			{
				if(args.length == 0)
				{
					player.sendMessage(ChatColor.GOLD + "MLGprison help");
					player.sendMessage(ChatColor.AQUA + "/MLGprison createrankup <name> <cost> " + ChatColor.WHITE + "Creates a rankup");
					player.sendMessage(ChatColor.AQUA + "/MLGprison editrankup <price|name> " + ChatColor.WHITE + "Edits a rankup");
					player.sendMessage(ChatColor.AQUA + "/MLGprison warpval <name> <value> " + ChatColor.WHITE + "Changes the warp value");
					player.sendMessage(ChatColor.AQUA + "/MLGprison setwarp <name> <value> " + ChatColor.WHITE + "Sets a warp of where you are standing");
					player.sendMessage(ChatColor.AQUA + "/MLGprison addmuti <rank> <amount> " + ChatColor.WHITE + "Sets a muti for a rank");
					player.sendMessage(ChatColor.AQUA + "/MLGprison additem <amount> " + ChatColor.WHITE + "Adds an item that can be sold");
					player.sendMessage(ChatColor.YELLOW + "/rankup " + ChatColor.WHITE + "Rankup if you have the  ");
					player.sendMessage(ChatColor.YELLOW + "/ranks " + ChatColor.WHITE + "Displays the ranks");
					player.sendMessage(ChatColor.YELLOW + "/pwarp <name> " + ChatColor.WHITE + "Used for warping");
					player.sendMessage(ChatColor.YELLOW + "/sellall " + ChatColor.WHITE + "sell all of your items");
					return true;
				}
				if(args[0].equalsIgnoreCase("createrankup"))
				{
					if(args.length == 3)
					{
						boolean run = true;
						int a = 1;
						while(run)
						{
							if(rankups.isSet(a + ""))
							{
								a++;
							}
							else
							{
								run = false;
							}
						}
						rankups.set(a + ".Name",args[1]);
						rankups.set(a + ".Price",Double.parseDouble(args[2]));
						player.sendMessage(ChatColor.AQUA + "Rankup " + args[1] + " created which costs  " + format(Double.parseDouble(args[2])));
						return true;
					}
				}
				else if(args[0].equalsIgnoreCase("editrankup"))
				{
					boolean run = true;
					int a = 1;
					while(run)
					{
						try
						{
							if(rankups.isSet(a + ""))
							{
								if(rankups.getString(a + ".Name").equalsIgnoreCase(args[2]))
								{
									run = false;
								}
								else a++;
							}
							else
							{
								run = false;
								player.sendMessage(ChatColor.RED + args[2] + " does not exist");
							}
						}
						catch(NullPointerException NullPointerException)
						{
							player.sendMessage(ChatColor.RED + "Usage /MLGprison editrankup price <rankname> <price>");
							player.sendMessage(ChatColor.RED + "Usage /MLGprison editrankup name <oldname> <newname>");
							return true;
						}
					}
					if(args[1].equalsIgnoreCase("price"))
					{
						rankups.set(a + ".Price",Double.parseDouble(args[3]));
						player.sendMessage(ChatColor.GREEN + "You have set the rankup cost to  " + format(Double.parseDouble(args[3])));
					}
					else if(args[1].equalsIgnoreCase("name"))
					{
						rankups.set(a + ".Name",args[3]);
						player.sendMessage(ChatColor.GREEN + "You have changed the name to " + args[3]);
					}
					else player.sendMessage(ChatColor.RED + "Usage /MLGprison editrankup <price:name>");
				}
				else if(args[0].equalsIgnoreCase("warpval"))
				{
					if(args.length == 3)
					{
						boolean run = true;
						int a = 1;
						while(run)
						{
							if(warps.isSet(a + ""))
							{
								if(warps.getString(a + ".Name").equalsIgnoreCase(args[1]))
								{
									run = false;
								}
								else a++;
							}
							else
							{
								run = false;
							}
						}
						warps.set(a + ".Value",(int) Double.parseDouble(args[2]));
						player.sendMessage(ChatColor.AQUA + "Warp " + args[1] + " has been set a warp value of " + args[2]);
					}
					else player.sendMessage(ChatColor.RED + "Usage /MLGprison warpval <name> <value>");
				}
				else if(args[0].equalsIgnoreCase("setwarp"))
				{
					boolean run = true;
					int a = 1;
					while(run)
					{
						if(warps.isSet(a + ""))
						{
							if(warps.getString(a + ".Name").equalsIgnoreCase(args[1]))
							{
								run = false;
							}
							else a++;
						}
						else
						{
							run = false;
						}
					}
					warps.set(a + ".World",player.getLocation().getWorld().getName());
					warps.set(a + ".X",player.getLocation().getX());
					warps.set(a + ".Y",player.getLocation().getY());
					warps.set(a + ".Z",player.getLocation().getZ());
					warps.set(a + ".Pitch",player.getLocation().getPitch());
					warps.set(a + ".Yaw",player.getLocation().getYaw());
					warps.set(a + ".Name",args[1]);
					warps.set(a + ".Value",(int) Double.parseDouble(args[2]));
					player.sendMessage(ChatColor.AQUA + "Warp " + args[1] + " has been set in World:" + player.getLocation().getWorld().getName() + " X:" + (int) player.getLocation().getX() + " Y:" + (int) player.getLocation().getY() + " Z:" + (int) player.getLocation().getZ() + " Pitch:" + (int) player.getLocation().getPitch() + " Yaw:" + (int) player.getLocation().getYaw());
					return true;
				}
				else if(args[0].equalsIgnoreCase("addmuti"))
				{
					try
					{
						boolean run = true;
						int a = 1;
						while(run)
						{
							if(rankups.isSet(a + ""))
							{
								if(rankups.getString(a + ".Name").equalsIgnoreCase(args[1]))
								{
									run = false;
								}
								else a++;
							}
							else
							{
								run = false;
							}
						}
						rankups.set(a + ".Muti",Double.parseDouble(args[2]));
						player.sendMessage(ChatColor.AQUA + "Rank " + rankups.getString(a + ".Name") + " now has a muti of x" + Double.parseDouble(args[2]));
					}
					catch(Exception Exception)
					{
						player.sendMessage(ChatColor.RED + "usage /MLGprison addmuti <rank> <amount>");
					}
				}
				else if(args[0].equalsIgnoreCase("additem"))
				{
					//try
					//{
					ItemStack hand = player.getItemInHand();
					if(hand == null)
					{
						player.sendMessage(ChatColor.RED + "Please hold an item in your hand");
						return true;
					}
					int id = hand.getTypeId();
					int meta = hand.getDurability();
					String a = id + ":" + meta;
					sell.set(a,Double.parseDouble(args[1]));
					player.sendMessage(ChatColor.AQUA + "Item " + a + " now sells for " + format(Double.parseDouble(args[1])));
					//}
					//catch(Exception Exception)
					//{
					//	Exception.printStackTrace();
					//	player.sendMessage(ChatColor.RED + "usage /MLGprison additem <amount>");
					//}
				}
				else
				{
					player.sendMessage(ChatColor.GOLD + "MLGprison help");
					player.sendMessage(ChatColor.AQUA + "/MLGprison createrankup <name> <cost> " + ChatColor.WHITE + "Creates a rankup");
					player.sendMessage(ChatColor.AQUA + "/MLGprison editrankup <price|name> " + ChatColor.WHITE + "Edits a rankup");
					player.sendMessage(ChatColor.AQUA + "/MLGprison warpval <name> <value> " + ChatColor.WHITE + "Changes the warp value");
					player.sendMessage(ChatColor.AQUA + "/MLGprison setwarp <name> <value> " + ChatColor.WHITE + "Sets a warp of where you are standing");
					player.sendMessage(ChatColor.AQUA + "/MLGprison addmuti <rank> <amount> " + ChatColor.WHITE + "Sets a muti for a rank");
					player.sendMessage(ChatColor.AQUA + "/MLGprison additem <amount> " + ChatColor.WHITE + "Adds an item that can be sold");
					player.sendMessage(ChatColor.YELLOW + "/rankup " + ChatColor.WHITE + "Rankup if you have the  ");
					player.sendMessage(ChatColor.YELLOW + "/ranks " + ChatColor.WHITE + "Displays the ranks");
					player.sendMessage(ChatColor.YELLOW + "/pwarp <name> " + ChatColor.WHITE + "Used for warping");
					player.sendMessage(ChatColor.YELLOW + "/sellall " + ChatColor.WHITE + "sell all of your items");
					return true;
				}
			}
			else player.sendMessage(ChatColor.RED + "m8 u dont have perms");
		}
		if(label.equalsIgnoreCase("renew"))
		{
			if(args.length > 0)
			{
				try
				{
					if((int) Double.parseDouble(args[0]) <= 0) player.sendMessage(ChatColor.RED + "Please use a number greater than 0");
					int rank = data.getInt(player.getUniqueId().toString() + ".Rank");
					if(rank == 1)
					{
						player.sendMessage(ChatColor.RED + "You dont need to renew your rank when your already at the lowest rank");
						return true;
					}
					double price = (rankups.getDouble((rank + 1) + ".Price") / 20) * (int) Double.parseDouble(args[0]);
					if(eco.getBalance(player) >= price)
					{
						data.changeLong(player.getUniqueId().toString() + ".Expire",(86400000L * (int) Double.parseDouble(args[0])));
						eco.withdrawPlayer(player,price);
						player.sendMessage(ChatColor.GREEN + "" + ((int) Double.parseDouble(args[0])) + " Days renewed for " + format(price));
						long left = data.getLong(player.getUniqueId().toString() + ".Expire") - System.currentTimeMillis();
						left /= 1000; //from ms to s
						left /= 60;//from s to m
						left /= 60;//from m to h
						player.sendMessage(ChatColor.GREEN + "You will be ranked down in " + ((int) Math.floor(left / 24)) + " days and " + (left % 24) + " hours");
					}
					else
					{
						player.sendMessage(ChatColor.RED + "You need more money to renew your rank by " + args[0] + " day(s)");
						player.sendMessage(ChatColor.RED + "It costs " + format((rankups.getDouble((rank + 1) + ".Price") / 20) * 1) + " to renew your rank by 1 day");
					}
				}
				catch(Exception Exception)
				{
					Exception.printStackTrace();
					player.sendMessage(ChatColor.RED + "Usage /renew <days>");
					player.sendMessage(ChatColor.RED + "Please enter a number");
					int rank = data.getInt(player.getUniqueId().toString() + ".Rank");
					player.sendMessage(ChatColor.RED + "It costs " + format((rankups.getDouble((rank + 1) + ".Price") / 20) * 1) + " to renew your rank by 1 day");
				}
			}
			else 
			{
				player.sendMessage(ChatColor.RED + "Usage /renew <days>");
				int rank = data.getInt(player.getUniqueId().toString() + ".Rank");
				player.sendMessage(ChatColor.RED + "It costs " + format((rankups.getDouble((rank + 1) + ".Price") / 20) * 1) + " to renew your rank by 1 day");
				long left = data.getLong(player.getUniqueId().toString() + ".Expire") - System.currentTimeMillis();
				left /= 1000; //from ms to s
				left /= 60;//from s to m
				left /= 60;//from m to h
				player.sendMessage(ChatColor.GREEN + "You will be ranked down in " + ((int) Math.floor(left / 24)) + " days and " + (left % 24) + " hours");
			}
		}
		if(label.equalsIgnoreCase("rankup"))
		{
			try
			{
				int rank = data.getInt(player.getUniqueId().toString() + ".Rank");
				if(rankups.getString((rank + 1) + ".Name") == null)
				{
					player.sendMessage(ChatColor.RED + "You are already the highest rank");
					return true;
				}
				if(eco.getBalance(player) >= rankups.getDouble((rank + 1) + ".Price"))
				{
					eco.withdrawPlayer(player,rankups.getDouble((rank + 1) + ".Price"));
					data.set(player.getUniqueId().toString() + ".Rank",rank + 1);
					player.sendMessage(ChatColor.YELLOW + "You have ranked up to " + rankups.getString((rank + 1) + ".Name") + " for " + format(rankups.getDouble((rank + 1) + ".Price")));
					if(rank + 1 == 2)
					{
						data.set(player.getUniqueId().toString() + ".Expire",System.currentTimeMillis() + (86400000L * config.getInt("StarterDays")));
					}
				}
				else
				{
					player.sendMessage(ChatColor.GREEN + "Your rank: " + ChatColor.BLUE + rankups.getString(rank + ".Name"));
					if (rankups.getDouble((rank + 1) + ".Price") - eco.getBalance(player) > 0.99) player.sendMessage(ChatColor.RED + "You need  " + format(rankups.getDouble((rank + 1) + ".Price") - eco.getBalance(player)) + " more to rankup");
					else 
					{
						double a = Math.round((rankups.getDouble((rank + 1) + ".Price") - eco.getBalance(player)) * 100);
						player.sendMessage(ChatColor.RED + "You need  " + a / 100 + " more to rankup");
					}
				}
			}
			catch(ArrayIndexOutOfBoundsException ArrayIndexOutOfBoundsException)
			{
				player.sendMessage(ChatColor.RED + "You are already the highest rank");
			}
		}
		if(label.equalsIgnoreCase("ranks"))
		{
			boolean run = true;
			int a = 1;
			while(run)
			{
				try
				{
					if(rankups.getString((a + 1) + ".Name").equals(null))
					{
						run = false;
						break;
					}
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&e" + rankups.getString(a + ".Name") + " &f-> &e" + rankups.getString((a + 1) + ".Name") + " &fCosts &e " + format(rankups.getDouble((a + 1) + ".Price"))));
					a++;
					//if(a > 3) run = false;
				}
				catch(NullPointerException NullPointerException)
				{
					run = false;
				}
			}
		}
		if(label.equalsIgnoreCase("sellall"))
		{
			if(player.getWorld().getName().equals("prison"))
			{
				Inventory inv = player.getInventory();
				int slot = 0;
				double money = 0;
				int sold = 0;
				while(slot < 36)
				{
					ItemStack item = inv.getItem(slot);
					if(item != null)
					{
						int id = item.getTypeId();
						int meta = item.getDurability();
						String a = id + ":" + meta;
						if(sell.isSet(a))
						{
							int amount = item.getAmount();
							sold += amount;
							money += sell.getDouble(a) * amount;
							item.setAmount(0);
						}
					}
					slot++;
				}
				int rank = data.getInt(player.getUniqueId().toString() + ".Rank");
				if(rankups.isSet(rank + ".Muti")) money *= rankups.getDouble(rank + ".Muti");
				eco.depositPlayer(player,money);
				if(sold == 0) player.sendMessage(ChatColor.RED + "You dont have any items to sell");
				else player.sendMessage(ChatColor.YELLOW + "Sold " + sold + " items for " + format(money));
			}
			else player.sendMessage(ChatColor.RED + "You can only sell items when ur in prison");
		}
		if(label.equalsIgnoreCase("pwarp"))
		{
			if(player.getWorld().getName().equals("prison"))
			{
				if(args.length == 1)
				{
					boolean run = true;
					int a = 1;
					while(run)
					{
						if(warps.isSet(a + ""))
						{
							if(warps.getString(a + ".Name").equalsIgnoreCase(args[0]))
							{
								run = false;
							}
							else a++;
						}
						else
						{
							run = false;
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThat warp does not exist"));
							return true;
						}
					}
					if(data.getInt(player.getUniqueId().toString() + ".Rank") >= warps.getInt(a + ".Value"))
					{
						Location warp = new Location(Bukkit.getWorld("world"),0,128,0);
						warp.setWorld(Bukkit.getWorld(warps.getString(a + ".World")));
						warp.setX(warps.getDouble(a + ".X"));
						warp.setY(warps.getDouble(a + ".Y"));
						warp.setZ(warps.getDouble(a + ".Z"));
						warp.setPitch((float) warps.getDouble(a + ".Pitch"));
						warp.setYaw((float) warps.getDouble(a + ".Yaw"));
						player.teleport(warp);
						player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&fWarping to &e" + args[0]));
					}
					else player.sendMessage(ChatColor.RED + "You're not a high enough rank to warp there");
				}
				else
				{
					ArrayList<String> wl = new ArrayList<String>();
					boolean run = true;
					int a  = 1;
					while(run)
					{
						if(warps.isSet(a + ""))
						{
							wl.add(warps.getString(a + ".Name"));
							a++;
						}
						else run = false;
					}
					player.sendMessage(ChatColor.YELLOW + "Warps " + ChatColor.WHITE + wl.toString().replace("[","").replace("]",""));
				}
			}
			else player.sendMessage(ChatColor.RED + "You can only warp when ur in prison");
		}
		return false;
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event)
	{
		Player player = event.getPlayer();
		if(player.getWorld().getName().equals("prison"))
		{
			long left = data.getLong(player.getUniqueId().toString() + ".Expire") - System.currentTimeMillis();
			left /= 1000; //from ms to s
			left /= 60;//from s to m
			left /= 60;//from m to h
			player.sendMessage(ChatColor.GREEN + "You will be ranked down in " + ((int) Math.floor(left / 24)) + " days and " + (left % 24) + " hours. Use /renew <days> or /rankup to extend this time");
		}
	}
}