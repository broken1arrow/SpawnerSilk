package me.crylonz;

import me.crylonz.command.EditSpawnerCommandExecutor;
import me.crylonz.command.GiveSpawnerCommandExecutor;
import me.crylonz.command.SpawnerSilkCommandExecutor;
import me.crylonz.command.SpawnerSilkTabCompletion;
import me.crylonz.utils.SpawnerSilkConfig;
import me.crylonz.utils.SpawnerSilkUpdater;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;


public class SpawnerSilk extends JavaPlugin implements Listener {

	public static final Logger log = Logger.getLogger("Minecraft");
	public static HashMap<String, String> playersUUID = new HashMap<>();
	private SpawnerSilkProvider spawnerProvider;
	public File configFile = new File(getDataFolder(), "config.yml");

	//config data
	public SpawnerSilkConfig config = new SpawnerSilkConfig(this);

	@Override
	public void onEnable() {

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new SpawnerSilkListener(this), this);

		Metrics metrics = new Metrics(this, 5536);

		GiveSpawnerCommandExecutor giveSpawnerCommandExecutor = new GiveSpawnerCommandExecutor();
		EditSpawnerCommandExecutor editSpawnerCommandExecutor = new EditSpawnerCommandExecutor();

		this.getCommand("givespawner").setExecutor(giveSpawnerCommandExecutor);
		this.getCommand("editspawner").setExecutor(editSpawnerCommandExecutor);
		this.getCommand("sps").setExecutor(new SpawnerSilkCommandExecutor(this, giveSpawnerCommandExecutor, editSpawnerCommandExecutor));

		Objects.requireNonNull(getCommand("givespawner")).setTabCompleter(new SpawnerSilkTabCompletion());
		Objects.requireNonNull(getCommand("editspawner")).setTabCompleter(new SpawnerSilkTabCompletion());
		Objects.requireNonNull(getCommand("sps")).setTabCompleter(new SpawnerSilkTabCompletion());
		
		if (Bukkit.getPluginManager().getPlugin("ShopGUIPlus") != null) {
			this.spawnerProvider = new SpawnerSilkProvider();
			hookIntoShopGui();
		}

		for (Player play : Bukkit.getOnlinePlayers()) {
			playersUUID.put(play.getName(), play.getUniqueId().toString());
		}

		registerConfig();

		if (!configFile.exists()) {
			saveDefaultConfig();
		} else {
			config.updateConfig();
		}

		if (config.getBoolean("auto-update")) {
			SpawnerSilkUpdater updater = new SpawnerSilkUpdater(this, 322295, this.getFile(), SpawnerSilkUpdater.UpdateType.DEFAULT, true);
		}
	}

	private void hookIntoShopGui() {

		try {
			net.brcdev.shopgui.ShopGuiPlusApi.registerSpawnerProvider(spawnerProvider);
		} catch (net.brcdev.shopgui.exception.api.ExternalSpawnerProviderNameConflictException e) {
			this.getLogger().warning("Failed to hook into ShopGUI+: " + e.getMessage());
		}
	}

	@Override
	public void onDisable() {

	}

	public void registerConfig() {
		config.register("auto-update", true);
		config.register("need-silk-touch", true);
		config.register("pickaxe-mode", 5);
		config.register("drop-chance", 100);
		config.register("drop-egg-chance", 100);
		config.register("drop-mode", 0);
		config.register("explosion-drop-chance", 10);
		config.register("spawners-can-be-modified-by-egg", true);
		config.register("drop-to-inventory", false);
		config.register("use-egg", true);
		config.register("drop-in-creative", false);
		config.register("spawners-generate-xp", false);
	}

	public SpawnerSilkConfig getDataConfig() {
		return config;
	}

	public static Material getSpawnerMaterial() {
		if (Bukkit.getVersion().contains("1.12")) {
			return Material.getMaterial("MOB_SPAWNER");
		}
		return Material.SPAWNER;
	}
}
