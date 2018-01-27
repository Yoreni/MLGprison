package yoreni.MLGprison.main;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;

public class DataFile 
{
	FileConfiguration config = null;
	File file = null;

	Main Main;
	
	public DataFile(Main main) 
	{
		this.Main = main;
	}
	
	public void setup(String path)
	{
		if (!Main.getDataFolder().exists()) 
		{
			Main.getDataFolder().mkdirs();
		}
		file = new File(Main.getDataFolder(),path + ".yml");
		if(!file.exists())
		{
			try 
			{
				file.createNewFile();
			} 
			catch (IOException IOException) 
			{
				IOException.printStackTrace();
			}
		}
		config = YamlConfiguration.loadConfiguration(file);
	}

	private void save()
	{
		try 
		{
			config.save(file);
		} 
		catch (IOException IOException) 
		{
			IOException.printStackTrace();
		}
	}
	
	public void addSetting(String x,Object v)
	{
		if(!config.isSet(x))
		{
			config.set(x,v);
			save();
		}
	}

	public void set(String s,Object x)
	{
		config.set(s,x);
		save();
	}

	public void changeDouble(String s,double x)
	{
		config.set(s,config.getDouble(s) + x);
		save();
	}

	public void changeInt(String s,int x)
	{
		config.set(s,config.getInt(s) + x);
		save();
	}

	public void changeLong(String s,long x)
	{
		config.set(s,config.getLong(s) + x);
		save();
	}

	public double getDouble(String s)
	{
		return config.getDouble(s);
	}

	public int getInt(String s)
	{
		return config.getInt(s);
	}

	public long getLong(String s)
	{
		return config.getLong(s);
	}

	public String getString(String s)
	{
		return config.getString(s);
	}

	public boolean isSet(String s)
	{
		return config.isSet(s);
	}

}
