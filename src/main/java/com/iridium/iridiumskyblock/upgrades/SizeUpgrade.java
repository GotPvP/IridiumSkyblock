package com.iridium.iridiumskyblock.upgrades;

import com.iridium.iridiumcore.dependencies.fasterxml.annotation.JsonIgnore;
import com.iridium.iridiumcore.utils.Placeholder;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
public class SizeUpgrade extends UpgradeData {
    public int size;

    public SizeUpgrade(int money, int crystals, int mobcoins, int prestigeRequired, int size) {
        super(money, crystals, mobcoins, prestigeRequired);
        this.size = size;
    }

    @JsonIgnore
    @Override
    public List<Placeholder> getPlaceholders() {
        return Collections.singletonList(new Placeholder("size", String.valueOf(size)));
    }
}
