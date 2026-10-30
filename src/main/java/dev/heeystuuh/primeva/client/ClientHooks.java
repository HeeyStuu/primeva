package dev.heeystuuh.primeva.client;

import net.minecraft.client.Minecraft;

public final class ClientHooks {
    public static void openGrimoire() {
        Minecraft.getInstance().setScreen(new GrimoireScreen());
    }

    private ClientHooks() {}
}
