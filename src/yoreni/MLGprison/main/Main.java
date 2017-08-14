package yoreni.MLGprison.main;

import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;


public class Main extends JavaPlugin implements Listener
{

	private static Economy eco = null;
	DecimalFormat abb = new DecimalFormat("0.######");

	FileConfiguration rankups = null;
	File Frankups = null;

	FileConfiguration data = null;
	File Fdata = null;

	FileConfiguration warps = null;
	File Fwarps = null;

	public void onEnable()
	{
		if(!setupEconomy())
		{
			Bukkit.shutdown();
		}
		getServer().getPluginManager().registerEvents(this,this);
		if (!getDataFolder().exists()) 
		{
			getDataFolder().mkdirs();
		}
		Frankups = new File(getDataFolder(), "rankups.yml");
		if(!Frankups.exists())
		{
			try 
			{
				Frankups.createNewFile();
			} 
			catch (IOException IOException) 
			{
				IOException.printStackTrace();
			}
		}
		rankups = YamlConfiguration.loadConfiguration(Frankups);
		Fdata = new File(getDataFolder(), "data.yml");
		if(!Fdata.exists())
		{
			try 
			{
				Fdata.createNewFile();
			} 
			catch (IOException IOException) 
			{
				IOException.printStackTrace();
			}
		}
		data = YamlConfiguration.loadConfiguration(Fdata);
		Fwarps = new File(getDataFolder(), "warps.yml");
		if(!Fwarps.exists())
		{
			try 
			{
				Fwarps.createNewFile();
			} 
			catch (IOException IOException) 
			{
				IOException.printStackTrace();
			}
		}
		warps = YamlConfiguration.loadConfiguration(Fwarps);
	}

	public String format (double y)
	{
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
		if(data.isSet(player.getUniqueId().toString())) return;
		data.set(player.getUniqueId().toString(),1);
		try 
		{
			data.save(Fdata);
		} 
		catch (IOException IOException) 
		{
			IOException.printStackTrace();
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
		if(label.equalsIgnoreCase("a"))
		{
			player.sendMessage(rankups + "");
			player.sendMessage(Frankups + "");
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
		}
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
					player.sendMessage(ChatColor.YELLOW + "/rankup " + ChatColor.WHITE + "Rankup if you have the  Ð");
					player.sendMessage(ChatColor.YELLOW + "/ranks " + ChatColor.WHITE + "Displays the ranks");
					player.sendMessage(ChatColor.YELLOW + "/pwarp <name> " + ChatColor.WHITE + "Used for warping");
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
						player.sendMessage(ChatColor.AQUA + "Rankup " + args[1] + " created which costs  Ð" + format(Double.parseDouble(args[2])));
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
						player.sendMessage(ChatColor.GREEN + "You have set the rankup cost to  Ð" + format(Double.parseDouble(args[3])));
					}
					else if(args[1].equalsIgnoreCase("name"))
					{
						rankups.set(a + ".Name",args[3]);
						player.sendMessage(ChatColor.GREEN + "You have changed the name to " + args[3]);
					}
					else player.sendMessage(ChatColor.RED + "Usage /MLGprison editrankup <price:name>");
					try 
					{
						warps.save(Fwarps);
					} 
					catch (IOException IOException) 
					{
						// TODO Auto-generated catch block
						IOException.printStackTrace();
					}
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
						try 
						{
							warps.save(Fwarps);
						} 
						catch (IOException IOException) 
						{
							// TODO Auto-generated catch block
							IOException.printStackTrace();
						}
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
					try 
					{
						warps.save(Fwarps);
					} 
					catch (IOException IOException) 
					{
						// TODO Auto-generated catch block
						IOException.printStackTrace();
					}
					player.sendMessage(ChatColor.AQUA + "Warp " + args[1] + " has been set in World:" + player.getLocation().getWorld().getName() + " X:" + (int) player.getLocation().getX() + " Y:" + (int) player.getLocation().getY() + " Z:" + (int) player.getLocation().getZ() + " Pitch:" + (int) player.getLocation().getPitch() + " Yaw:" + (int) player.getLocation().getYaw());
					return true;
				}
				else
				{
					player.sendMessage(ChatColor.GOLD + "MLGprison help");
					player.sendMessage(ChatColor.AQUA + "/MLGprison createrankup <name> <cost> " + ChatColor.WHITE + "Creates a rankup");
					player.sendMessage(ChatColor.AQUA + "/MLGprison editrankup <price|name> " + ChatColor.WHITE + "Edits a rankup");
					player.sendMessage(ChatColor.AQUA + "/MLGprison warpval <name> <value> " + ChatColor.WHITE + "Changes the warp value");
					player.sendMessage(ChatColor.AQUA + "/MLGprison setwarp <name> <value> " + ChatColor.WHITE + "Sets a warp of where you are standing");
					player.sendMessage(ChatColor.YELLOW + "/rankup " + ChatColor.WHITE + "Rankup if you have the  Ð");
					player.sendMessage(ChatColor.YELLOW + "/ranks " + ChatColor.WHITE + "Displays the ranks");
					player.sendMessage(ChatColor.YELLOW + "/pwarp <name> " + ChatColor.WHITE + "Used for warping");
					return true;
				}
			}
			else player.sendMessage(ChatColor.RED + "m8 u dont have perms");
		}
		if(label.equalsIgnoreCase("rankup"))
		{
			try
			{
				int rank = data.getInt(player.getUniqueId().toString());
				if(eco.getBalance(player) >= rankups.getDouble((rank + 1) + ".Price"))
				{
					eco.withdrawPlayer(player,rankups.getDouble((rank + 1) + ".Price"));
					data.set(player.getUniqueId().toString(),rank + 1);
					player.sendMessage(ChatColor.YELLOW + "You have ranked up to " + rankups.getString((rank + 1) + ".Name") + " for  Ð" + format(rankups.getDouble((rank + 1) + ".Price")));
					try 
					{
						data.save(Fdata);
					} 
					catch (IOException IOException) 
					{
						IOException.printStackTrace();
					}
				}
				else
				{
					player.sendMessage(ChatColor.GREEN + "You a rank " + ChatColor.BLUE + rankups.getString(rank + ".Name"));
					if (rankups.getDouble((rank + 1) + ".Price") - eco.getBalance(player) > 0.99) player.sendMessage(ChatColor.RED + "You need  Ð" + format(rankups.getDouble((rank + 1) + ".Price") - eco.getBalance(player)) + " more to rankup");
					else 
					{
						double a = Math.round((rankups.getDouble((rank + 1) + ".Price") - eco.getBalance(player)) * 100);
						player.sendMessage(ChatColor.RED + "You need  Ð" + a / 100 + " more to rankup");
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
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&e" + rankups.getString(a + ".Name") + " &f-> &e" + rankups.getString((a + 1) + ".Name") + " &fCosts &e Ð" + format(rankups.getDouble((a + 1) + ".Price"))));
					a++;
				}
				catch(ArrayIndexOutOfBoundsException ArrayIndexOutOfBoundsException)
				{
					run = false;
				}
			}
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
					if(data.getInt(player.getUniqueId().toString()) >= warps.getInt(a + ".Value"))
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
					else player.sendMessage(ChatColor.RED + "You are the a high euogth rank to warp there");
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
}
