package com.fullwall.Citizens.Economy;

import org.bukkit.entity.Player;

import com.fullwall.Citizens.Citizens;
import com.fullwall.Citizens.Economy.EconomyHandler.Operation;
import com.fullwall.Citizens.Traders.ItemPrice;
import com.fullwall.Citizens.Utils.PropertyPool;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class IconomyInterface {

	public static boolean playerHasEnough(String name, double amount) {
		if (iConomy.getBank().hasAccount(name))
			return iConomy.getBank().getAccount(name).hasEnough(amount);
		else
			return false;
	}

	public static String getCurrency() {
		return iConomy.getBank().getCurrency();
	}

	public static String getRemainder(Operation op, Player player) {
		int price = PropertyPool.getPrice(op.toString().toLowerCase()
				.replace("_", "-")
				+ "-iconomy");
		Account acc = iConomy.getBank().getAccount(player.getName());
		return "" + (price - acc.getBalance());
	}

	public static boolean hasEnough(Player player, Operation op) {
		int price = PropertyPool.getPrice(op.toString().toLowerCase()
				.replace("_", "-")
				+ "-iconomy");
		return playerHasEnough(player.getName(), price);
	}

	public static boolean hasEnough(ItemPrice price, Player player) {
		return playerHasEnough(player.getName(), price.getPrice());
	}

	public static int pay(Player player, Operation op) {
		int price = PropertyPool.getPrice(op.toString().toLowerCase()
				.replace("_", "-")
				+ "-iconomy");
		Account acc = iConomy.getBank().getAccount(player.getName());
		acc.subtract(price);
		Citizens.log.info("" + price);
		Citizens.log.info("" + acc.getBalance());
		return price;
	}
}
