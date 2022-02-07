package com.iridium.iridiumskyblock.upgrades;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BeaconUpgrade extends UpgradeData {

    public BeaconUpgrade(int money, int crystals, int mobcoins, int prestigeRequired) {
        super(money, crystals, mobcoins, prestigeRequired);
    }
}
