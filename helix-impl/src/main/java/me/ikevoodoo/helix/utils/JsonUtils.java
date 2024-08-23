package me.ikevoodoo.helix.utils;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import me.ikevoodoo.helix.repo.PluginRepositoryVersions;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonUtils {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Version.class, (JsonDeserializer<Version>) (json, typeOfT, context) -> Version.parse(json.getAsString()))
            .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> json == null ? null : new Date(json.getAsLong()))
            .registerTypeAdapter(PluginRepositoryVersions.class, (JsonDeserializer<PluginRepositoryVersions>) (json, typeOfT, context) -> {
                var map = json.getAsJsonObject().asMap().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsString()));

                return new PluginRepositoryVersions(map);
            })
            .create();


}
