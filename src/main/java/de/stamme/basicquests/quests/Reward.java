package de.stamme.basicquests.quests;

import java.io.Serializable;
import java.math.BigDecimal;

import org.bukkit.inventory.ItemStack;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.StringFormatter;

public class Reward implements Serializable {
	private static final long serialVersionUID = 1970784300296164425L;
	
	public BigDecimal money = BigDecimal.ZERO;
	public ItemStack[] items;
	
	public Reward(BigDecimal money, ItemStack[] items) {
		this.money = money;
		this.items = items;
	}
	
	public Reward() {
		this.items = new ItemStack[0];
	}
	
	public Reward(BigDecimal money) {
		this.money = money;
		this.items = new ItemStack[0];
	}

	public String moneyString() {
		String s = "";
		if (money.compareTo(BigDecimal.ZERO) > 0) {
			s += Main.getEconomy().format(money.doubleValue());
		}
		return s;
	}
	
	public String itemString() {
		String s = "   ";
		if (items.length > 0) { 
			for (int i = 0; i < items.length; i++) {
				ItemStack itemStack = items[i];
				String name = StringFormatter.formatItemStack(itemStack);

				s += "+ " + name + "\n   ";
			}
		}
		return s;
	}
	
	public String toString() {
		String s = "";
		
		s += moneyString();
		
		if (s.length() > 0) { s += "\n"; }
		
		s += itemString();
		
		return s;
	}
}
