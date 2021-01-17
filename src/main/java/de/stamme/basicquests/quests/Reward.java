package de.stamme.basicquests.quests;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.util.StringFormatter;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

public class Reward implements Serializable {
	private static final long serialVersionUID = 1970784300296164425L;
	
	public BigDecimal money = BigDecimal.ZERO;
	public int xp;
	public ArrayList<ItemStack> items;
	public ArrayList<String> materialNames;

//	public Reward(BigDecimal money, ArrayList<ItemStack> items) {
//		this.money = money;
//		this.items = items;
//	}
	
	public Reward() {
		this.items = new ArrayList<ItemStack>();
	}
	
	public Reward(BigDecimal money) {
		this.money = money;
		this.items = new ArrayList<>();
	}

	public Reward(ArrayList<ItemStack> items) {
		this.items = items;
	}

	public Reward(int xp) {
		this.xp = xp;
		this.items = new ArrayList<>();
	}

	public String moneyString() {
		String s = "";
		if (money.compareTo(BigDecimal.ZERO) > 0) {
			s += Main.getEconomy().format(money.doubleValue());
		}
		return s;
	}

	public String xpString() {
		String s = "";
		if (xp > 0) {
			s += xp + " XP";
		}
		return s;
	}
	
	public String itemString() {
		StringBuilder s = new StringBuilder("   ");
		if (items.size() > 0) {
			for (ItemStack itemStack: items) {
				String name = StringFormatter.formatItemStack(itemStack);
				s.append("\n   ").append("+ ").append(name);
			}
		}
		return s.toString();
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();

		if (money.compareTo(BigDecimal.ZERO) > 0)
			s.append(moneyString());

		else if (xp > 0)
			s.append(xpString());

		s.append(itemString());
		
		return s.toString();
	}
}
