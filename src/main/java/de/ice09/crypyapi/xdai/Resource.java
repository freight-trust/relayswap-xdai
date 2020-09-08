package com.freighttrust.relayswap.xdai;

import org.apache.commons.lang3.RandomUtils;

public class Resource {
	 
    private static String[] marketrate = {
            "📡 AMB - Arbitrary Message Bridge connecting...",
            "6585c8778e87c27f3c2486d9792d3ccdd73eaf05945f0f15f16bb39b3f550669",
            "bd793c5d7758be96d73b02d8ae772575f9adb1c032a1c555027d4dc22c443504",
            "49f786da4963e5b0c4c0930f689b9a9b874bd8f99ed2aee5f4b316cc08bfa2c5",
            "b292c97739a37335e47ba710a214fae53292b7731f7f9c87c21e0d3e6162051e",
            "3f3ba10e12300d6b699560829a71c4a4926f4effa4c3c2c7073b2f6abc3d2a9a",
            "64e8c8f4256de571cb482a19bb5e8d2b67c0a9eb12693e119861a070cc5e182f",
            " 🆘 - VIOLATION  "
    };

    public static String getCurrentMarketRate() {
        return marketrate[RandomUtils.nextInt(0, marketrate.length)];
    }


}
