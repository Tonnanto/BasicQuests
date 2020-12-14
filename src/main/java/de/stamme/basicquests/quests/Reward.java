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
		StringBuilder s = new StringBuilder("   ");
		if (items.length > 0) {
			for (ItemStack itemStack : items) {
				String name = StringFormatter.formatItemStack(itemStack);
				s.append("+ ").append(name).append("\n   ");
			}
		}
		return s.toString();
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		
		s.append(moneyString());
		
		if (s.length() > 0) { s.append("\n"); }
		
		s.append(itemString());
		
		return s.toString();
	}
}
