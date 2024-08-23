package me.ikevoodoo.helix.screens;

import me.ikevoodoo.helix.api.screens.HelixScreenOpenResult;
import me.ikevoodoo.helix.api.screens.components.HelixPageComponent;
import me.ikevoodoo.helix.screens.page.HelixScreenPage;
import org.jetbrains.annotations.Nullable;

public class BukkitHelixScreenOpenResult implements HelixScreenOpenResult {

    private final int state;
    private final HelixScreenPage page;

    public BukkitHelixScreenOpenResult(int state, HelixScreenPage page) {
        this.state = state;
        this.page = page;
    }

    @Override
    public int state() {
        return this.state;
    }

    @Override
    public @Nullable HelixPageComponent component(String id) {
        return this.page.getComponent(id);
    }

    @Override
    public String toString() {
        return "BukkitHelixScreenOpenResult[" +
                "page=" + page +
                ", state=" + state +
                ']';
    }
}
