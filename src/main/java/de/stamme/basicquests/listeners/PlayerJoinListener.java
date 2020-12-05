package de.stamme.basicquests.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.stamme.basicquests.main.Main;
import de.stamme.basicquests.main.PlayerData;
import de.stamme.basicquests.main.QuestPlayer;
import de.stamme.basicquests.quest_generation.QuestGenerationException;
import de.stamme.basicquests.quest_generation.QuestGenerator;
import de.stamme.basicquests.quests.Quest;

public class PlayerJoinListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		FileConfiguration config = Main.plugin.getConfig();
		
		
//		// Test Item for Reward
//		ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
//		ItemMeta itemMeta = item.getItemMeta();
//		itemMeta.setDisplayName("GG Pickaxe");
//		itemMeta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 10, true);
//		itemMeta.addEnchant(Enchantment.MENDING, 1, true);
//		item.setItemMeta(itemMeta);
//		ItemStack[] rewardItems = {item};
//		
//		ItemStack[] bookReward = {new ItemStack(Material.BOOK, 5), new ItemStack(Material.LAPIS_LAZULI, 64)};
//		
//		// Test Quests
//		EnchantItemQuest q1 = new EnchantItemQuest(Material.DIAMOND_SWORD, Enchantment.DAMAGE_ALL, 1, 1, new Reward(BigDecimal.TEN, rewardItems));
//		EntityKillQuest q2 = new EntityKillQuest(EntityType.COW, 2, new Reward(new BigDecimal("20"), rewardItems));
//		BlockBreakQuest q3 = new BlockBreakQuest(Material.STONE, 5, new Reward(BigDecimal.TEN, rewardItems));
//		EnchantItemQuest q4 = new EnchantItemQuest(Material.BOOK, 3, new Reward(new BigDecimal("3459"), rewardItems));
//		GainLevelQuest q5 = new GainLevelQuest(2, new Reward(new BigDecimal("88.8"), bookReward));
//		ReachLevelQuest q6 = new ReachLevelQuest(player, 10, new Reward(new BigDecimal("250"), bookReward));
//		HarvestBlockQuest q7 = new HarvestBlockQuest(Material.MELON_SLICE, 20, new Reward(new BigDecimal("100"), rewardItems));
//		
//		Quest[] currentQuests = {q1, q2, q3, q4, q5, q6, q7};
//		Main.plugin.quests.put(player.getName(), currentQuests);
		
		// load player data from file - if not successful generate new QuestPlayer
		if (!PlayerData.loadPlayerData(player)) {
			Main.plugin.questPlayer.put(player.getUniqueId(), new QuestPlayer(player));
		}
		
		
		
		
		if (Main.plugin.questPlayer.containsKey(player.getUniqueId())) {
			QuestPlayer questPlayer = Main.plugin.questPlayer.get(player.getUniqueId());
			// Outputting 100 example quests in console (balancing purpose)
			for (int i = 0; i < 100; i++) {
				try {
					Quest q = QuestGenerator.generate(questPlayer);
					
					Main.log(q.getInfo(true));
					
				} catch (QuestGenerationException e) {
					Main.log(e.message);
					e.printStackTrace();
				}
				
			}
		}
		
	}
}
