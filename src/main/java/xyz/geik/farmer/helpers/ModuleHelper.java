package xyz.geik.farmer.helpers;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import xyz.geik.farmer.Main;
import xyz.geik.farmer.modules.FarmerModule;
import xyz.geik.farmer.modules.production.Production;
import xyz.geik.glib.GLib;
import xyz.geik.glib.api.ModuleDisableEvent;
import xyz.geik.glib.api.ModuleEnableEvent;
import xyz.geik.glib.chat.ChatUtils;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Getter
public class ModuleHelper {

    private static ModuleHelper instance;
    private final List<FarmerModule> modules = new ArrayList<>();

    public synchronized static ModuleHelper getInstance() {
        if (instance == null) instance = new ModuleHelper();
        return instance;
    }

    public void loadModules() {
        unloadModules();
        loadModule(new Production());

        try {
            File folder = new File(Main.getInstance().getDataFolder(), "/modules");

            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (!file.getName().endsWith(".jar")) continue;

                FileInputStream fileInputStream = new FileInputStream(file.getAbsoluteFile());
                ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
                ClassLoader loader = ClassHelper.class.getClassLoader();
                URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, loader);

                for (ZipEntry zipEntry = zipInputStream.getNextEntry(); zipEntry != null; zipEntry = zipInputStream.getNextEntry()) {
                    if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".class")) {
                        String className = zipEntry.getName().replaceAll("/", ".").replaceAll(".class", "");
                        Class<?> loadedClass = urlClassLoader.loadClass(className);

                        if (loadedClass.getSuperclass().getName().endsWith("FarmerModule")) {
                            FarmerModule module = (FarmerModule) loadedClass.getDeclaredConstructor().newInstance();
                            loadModule(module);
                        }
                    }
                }

                urlClassLoader.close();
                fileInputStream.close();
                zipInputStream.close();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void loadModule(FarmerModule module) {
        Bukkit.getScheduler().runTask(GLib.getInstance(), () -> {
            Bukkit.getPluginManager().callEvent(new ModuleEnableEvent(module));
        });
        module.onEnable();
        modules.add(module);
    }

    public void unloadModules() {
        for (FarmerModule module : new ArrayList<>(modules)) {
            if (module.isEnabled()) {
                module.setEnabled(false);
                Bukkit.getScheduler().runTask(GLib.getInstance(), () -> {
                    Bukkit.getPluginManager().callEvent(new ModuleDisableEvent(module));
                });
                module.onDisable();
                String message = "&3[" + GLib.getInstance().getName() + "] &c" + module.getName() + " disabled.";
                ChatUtils.sendMessage(Bukkit.getConsoleSender(), message);
                modules.remove(module);
            }
        }
    }

    // Return null if Module is not loaded or invalid.
    public @Nullable FarmerModule getModule(String name) {
        for (FarmerModule module : modules) {
            if (module.getName().equalsIgnoreCase(name)) return module;
        }

        return null;
    }

    // Return null if Module is not loaded or invalid.
    public @Nullable <T extends FarmerModule> FarmerModule getModule(Class<T> classType) {
        for (FarmerModule module : modules) {
            if (module.getClass() == classType) return module;
        }

        return null;
    }

}