package com.industrialcivilization.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.handlers.NetChapterSync;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.network.handlers.NetSettingSync;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.storage.QuestSettings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Preserves player progress while applying newer bundled quest definitions. */
final class QuestPackMigration {
    static final String CURRENT_PACK_NAME = "Industrial Civilization — Astra";
    static final String LEGACY_PACK_NAME = "Industrial Civilization — Phase 2";

    private QuestPackMigration() {}

    static boolean applyBundledUpdateIfNeeded() {
        String installedName = QuestSettings.INSTANCE.getProperty(NativeProps.PACK_NAME);
        int installedVersion = QuestSettings.INSTANCE.getProperty(NativeProps.PACK_VER);
        if (!isPackIdentity(installedName)) return false;

        File defaults = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
        if (!defaults.isFile()) return false;

        try {
            NBTTagCompound bundled = NBTConverter.JSONtoNBT_Object(
                JsonHelper.ReadFromFile(defaults), new NBTTagCompound(), true);
            QuestSettings bundledSettings = new QuestSettings();
            bundledSettings.readFromNBT(bundled.getCompoundTag("questSettings"));
            String bundledName = bundledSettings.getProperty(NativeProps.PACK_NAME);
            int bundledVersion = bundledSettings.getProperty(NativeProps.PACK_VER);
            if (!isPackIdentity(bundledName) || bundledVersion <= installedVersion) return false;

            backupCurrentFiles(installedVersion, bundledVersion);

            boolean editMode = QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE);
            boolean hardcore = QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE);
            NBTTagList progress = QuestDatabase.INSTANCE.writeProgressToNBT(new NBTTagList(), null);

            QuestSettings.INSTANCE.readFromNBT(bundled.getCompoundTag("questSettings"));
            QuestDatabase.INSTANCE.readFromNBT(bundled.getTagList("questDatabase", 10), false);
            QuestLineDatabase.INSTANCE.readFromNBT(bundled.getTagList("questLines", 10), false);
            QuestDatabase.INSTANCE.readProgressFromNBT(progress, false);
            QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, editMode);
            QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, hardcore);

            NetSettingSync.sendSync(null);
            NetQuestSync.quickSync(-1, true, true);
            NetChapterSync.sendSync(null, null);
            SaveLoadHandler.INSTANCE.resetUpdate();
            SaveLoadHandler.INSTANCE.markDirty();
            IndustrialCivilizationCore.LOGGER.info(
                "Automatically migrated Better Questing definitions from pack version {} ({}) to {} ({}); progress preserved",
                installedVersion, installedName, bundledVersion, bundledName);
            return true;
        } catch (Exception error) {
            IndustrialCivilizationCore.LOGGER.error(
                "Could not safely migrate bundled Better Questing definitions; existing quest data was left loaded",
                error);
            return false;
        }
    }

    static boolean isPackIdentity(String name) {
        return CURRENT_PACK_NAME.equals(name) || LEGACY_PACK_NAME.equals(name);
    }

    private static void backupCurrentFiles(int installedVersion, int bundledVersion) throws IOException {
        File worldDirectory = BQ_Settings.curWorldDir;
        if (worldDirectory == null) throw new IOException("Better Questing world directory is unavailable");
        File backupDirectory = new File(worldDirectory, "backup/industrialcivilization-pack-v"
            + installedVersion + "-before-v" + bundledVersion);
        Files.createDirectories(backupDirectory.toPath());
        copyIfPresent(new File(worldDirectory, "QuestDatabase.json"),
            new File(backupDirectory, "QuestDatabase.json"));
        copyIfPresent(new File(worldDirectory, "QuestProgress.json"),
            new File(backupDirectory, "QuestProgress.json"));
    }

    private static void copyIfPresent(File source, File destination) throws IOException {
        if (source.isFile()) {
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
