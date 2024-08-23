package me.ikevoodoo.helix.api.screens.setup;

import me.ikevoodoo.helix.api.screens.pages.HelixPage;
import me.ikevoodoo.helix.api.screens.ScreenDimensions;

public interface HelixScreenSetup {

    HelixPage createPage(String id, String name, ScreenDimensions dimensions);

}
