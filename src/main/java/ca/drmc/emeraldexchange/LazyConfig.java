package ca.drmc.emeraldexchange;

import org.bukkit.plugin.Plugin;

public class LazyConfig extends Config {
	public LazyConfig(Plugin p){
		this.setFile(p);
	}
	//Nice and easy lazy config, just define the variables and it does everything for you
	public int buyprice = 10;
	public int sellprice = 10;
}
