package de.stamme.basicquests.quests;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.StringFormatter;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

public class Reward implements Serializable {
	private static final long serialVersionUID = 1970784300296164425L;
	
	public BigDecimal money = BigDecimal.ZERO;
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

	public String moneyString() {
		String s = "";
		if (money.compareTo(BigDecimal.ZERO) > 0) {
			s += Main.getEconomy().format(money.doubleValue());
		}
		return s;
	}
	
	public String itemString() {
		StringBuilder s = new StringBuilder("   ");
		if (items.size() > 0) {
			for (ItemStack itemStack: items) {
				String name = StringFormatter.formatItemStack(itemStack);
				s.append("+ ").append(name).append("\n   ");
			}
		}
		return s.toString();
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();

		if (money.compareTo(BigDecimal.ZERO) > 0)
			s.append(moneyString());

		s.append("\n");

		s.append(itemString());
		
		return s.toString();
	}
}
