
package com.altairclient;

import com.altairclient.client.AltairKeyHandler;
import net.minecraftforge.fml.common.Mod;

@Mod(AltairClient.MOD_ID)
public class AltairClient {

    public static final String MOD_ID = "altairclient";

    public AltairClient() {
        AltairKeyHandler.register();
    }
}
