package ca.drmc.emeraldexchange;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EmeraldExchange extends JavaPlugin {
	private LazyConfig config;
	private static final Logger log = Logger.getLogger("Minecraft");
	public static Economy econ = null;

	public void onDisable() {
		//Self explanatory
		log.info(String.format("[%s] - Disabled!", getDescription().getName()));
	}

	public void onEnable() {
		//Check if there is an economy plugin and initialize econ if there is
		if (!setupEconomy() ) {
			//No economy plugin is found, disable plugin
			log.info(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		//Setup config
		config = new LazyConfig(this);
		config.load();

		log.info(String.format("[%s] - Enabled!", getDescription().getName()));
	}

	private boolean setupEconomy() {
		//Check if vault is loaded
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		//Checks if an economy plugin is loaded (not quite sure how, I stole it from the vault example plugin code)
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		//Economy plugin is loaded, initialize econ variable
		econ = rsp.getProvider();
		return econ != null;
	}

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if(!(sender instanceof Player)) {
			//Make sure the console doesn't use this :P
			log.info("Only players are supported for Emerald Exchange!");
			return true;
		}
		Player player = (Player) sender;
		if(command.getLabel().equals("test-ee")){
			//A test command that displays your balance
			sender.sendMessage(String.format("You have %s", econ.format(econ.getBalance(player.getName()))));
			return true;
		}else if(command.getLabel().equals("ee")){
			//Main command
			if(args.length == 2){
				if(args[0].equals("buy")){ //Player is buying emeralds
					int amount;
					try {
						//Use try and catch to make sure they don't type a string instead of a number for the amount
						amount = Integer.parseInt(args[1]);
					}catch(Exception e){
						//Amount is not a number
						sender.sendMessage("[" + ChatColor.GREEN + "EmeraldExchange" + ChatColor.WHITE + "] The amount to buy must be a number.");
						return true;
					}
					double balance = econ.getBalance(player.getName()); //Get player's balance
					int cost = amount * config.buyprice; //Calculate the cost of how many emeralds they want to buy
					if(cost > balance){
						//Not enough money
						sender.sendMessage("[" + ChatColor.GREEN + "EmeraldExchange" + ChatColor.WHITE + "] You don't have enough money to buy " + amount + " emeralds.");
						return true;
					}
					EconomyResponse r = econ.withdrawPlayer(player.getName(), cost); //Attempt to withdraw the amount
					if(r.transactionSuccess()){
						//Withdraw was successful
						String e = (amount > 1) ? "emeralds" : "emerald"; //If there is only 1 emerald, don't say emeralds
						PlayerInventory i = player.getInventory(); //Get player inventory
						ItemStack is = new ItemStack(Material.EMERALD, amount); //Make an ItemStack of emeralds
						i.addItem(is); //Give the player the emeralds
						sender.sendMessage("[" + ChatColor.GREEN + "EmeraldExchange" + ChatColor.WHITE + "] You bought " + amount + " " + e + " for " + econ.format((double) cost));
						return true;
					}else{
						//Something went wrong with the transaction
						sender.sendMessage("[" + ChatColor.GREEN + "EmeraldExchange" + ChatColor.WHITE + "] An error occured :(");
						return true;
					}
				}else if(args[0].equals("sell")){ //Player is selling emeralds
					int amount;
					try {
						//Make sure it's a number like before
						amount = Integer.parseInt(args[1]);
					}catch(Exception e){
						sender.sendMessage("[" + ChatColor.GREEN + "EmeraldExchange" + ChatColor.WHITE + "] The amount to sell must be a number.");
						return true;
					}
					int cost = amount * config.sellprice; //Calculate price
					PlayerInventory i = player.getInventory(); //Get player inventory
					if(i.contains(Material.EMERALD, amount)){ //Check if inventory contains enough emeralds
						//enough emeralds in inventory
						EconomyResponse r = econ.depositPlayer(player.getName(), cost); //Attempt to deposit the amount
						if(r.transactionSuccess()){
							//Success, remove emeralds from inventory
							ItemStack is = new ItemStack(Material.EMERALD, amount);
							i.removeItem(is);
							String e = (amount > 1) ? "emeralds" : "emerald";
							sender.sendMessage("[" + ChatColor.GREEN + "EmeraldExchange" + ChatColor.WHITE + "] You sold " + amount + " " + e + " for " + econ.format((double) cost));
							return true;
						}else{
							//Something went wrong with the transaction
							sender.sendMessage("[" + ChatColor.GREEN + "EmeraldExchange" + ChatColor.WHITE + "] An error occured :(");
							return true;
						}
					}else{
						//Not enough emeralds
						sender.sendMessage("[" + ChatColor.GREEN + "EmeraldExchange" + ChatColor.WHITE + "] You need more emeralds.");
						return true;
					}
				}
				//Any argument that's not buy or sell
				sender.sendMessage("[" + ChatColor.GREEN + "EmeraldExchange" + ChatColor.WHITE + "] Try that again. Usage: ");
				return false;
			}else if(args.length == 1){
				if(args[0].equals("price")){ //Price check command
					sender.sendMessage("[" + ChatColor.GREEN + "EmeraldExchange" + ChatColor.WHITE + "] Buy price: " + econ.format((double) config.buyprice) + ", sell price: " + econ.format((double) config.sellprice) + ".");
					return true;
				}
				
				sender.sendMessage("[" + ChatColor.GREEN + "EmeraldExchange" + ChatColor.WHITE + "] Try that again. Usage: ");
				return false;
			}else{
				sender.sendMessage("[" + ChatColor.GREEN + "EmeraldExchange" + ChatColor.WHITE + "] Try that again. Usage: ");
				return false;
			}
		}
		return false;

	}

}

