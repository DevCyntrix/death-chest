package com.github.devcyntrix.deathchest.api;

import com.github.devcyntrix.deathchest.feature.chest.DeathChestModel;

public interface ChestListener {

    void onCreate(DeathChestModel model);

    void onDestroy(DeathChestModel model);

    void onLoad(DeathChestModel model);

    void onUnload(DeathChestModel model);


}
