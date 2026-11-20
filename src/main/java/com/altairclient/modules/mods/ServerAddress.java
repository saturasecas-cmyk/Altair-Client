package com.altairclient.modules.mods;

import com.altairclient.client.Anim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ServerData;

/** Server Address: muestra la IP del servidor en el que juegas. */
public class ServerAddress extends HudElement {

    private static final ServerAddress INSTANCE = new ServerAddress();

    private final Anim.Smooth widthAnim = new Anim.Smooth();
    private String lastText = "IP: play.example.net";

    private ServerAddress() {
        super(10, 130);
    }

    public static ServerAddress getInstance() {
        return INSTANCE;
    }

    private String currentText(Minecraft mc) {
        ServerData server = mc.getCurrentServer();
        if (server != null && server.ip != null && !server.ip.isEmpty()) {
            return "IP: " + server.ip;
        }
        return mc.getSingleplayerServer() != null ? "IP: singleplayer" : "IP: --";
    }

    @Override
    public int getWidth() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.font != null) {
            return mc.font.width(lastText);
        }
        return 90;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    protected void draw(GuiGraphicsExtractor graphics, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        lastText = preview ? "IP: play.example.net" : currentText(mc);
        int width = Math.round(widthAnim.update(mc.font.width(lastText), 20.0F));
        box(graphics, width, getHeight(), CYAN);
        graphics.text(mc.font, "IP: ", getX(), getY(), MUTED, false);
        graphics.text(mc.font, lastText.substring(4), getX() + mc.font.width("IP: "), getY(), WHITE, false);
    }
}
