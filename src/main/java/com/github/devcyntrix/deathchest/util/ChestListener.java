package com.github.devcyntrix.deathchest.util;

import com.github.devcyntrix.deathchest.DeathChestModel;

public interface ChestListener {

    void onCreate(DeathChestModel model);

    void onDestroy(DeathChestModel model);

    void onLoad(DeathChestModel model);

    void onUnload(DeathChestModel model);


}
