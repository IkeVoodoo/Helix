package me.ikevoodoo.helix.api.items.variables;

import me.ikevoodoo.helix.api.storage.HelixDataStorage;
import org.bukkit.persistence.PersistentDataType;

public interface HelixItemVariables extends HelixDataStorage {

    HelixItemVariables getOrCreateSection(String key);

    <T, Z> PersistentDataType<T, Z> getType(String key);

    <T, Z> void set(String key, PersistentDataType<T, Z> type, Z value);

    <Z> Z getValue(String key);
}
