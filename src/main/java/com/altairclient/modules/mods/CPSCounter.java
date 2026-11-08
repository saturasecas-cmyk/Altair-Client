package com.altairclient.modules.mods;

import com.altairclient.client.AltairScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(
        modid = "altairclient",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class CPSCounter {

    private static final CPSCounter INSTANCE = new CPSCounter();

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BACKGROUND = 0xAA10152A;
    private static final int PURPLE = 0xFF9B7BFF;

    private boolean enabled = false;
    private int x = 10;
    private int y = 30;

    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();

    private CPSCounter() {
    }

    public static CPSCounter getInstance() {
        return INSTANCE;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.font != null) {
            return minecraft.font.width("CPS: 00 | 00") + 13;
        }
        return 75;
    }

    public int getHeight() {
        return 18;
    }

    public void addLeftClick() {
        leftClicks.add(System.currentTimeMillis());
    }

    public void addRightClick() {
        rightClicks.add(System.currentTimeMillis());
    }

    public int getLeftCPS() {
        long now = System.currentTimeMillis();
        leftClicks.removeIf(time -> now - time > 1000);
        return leftClicks.size();
    }

    public int getRightCPS() {
        long now = System.currentTimeMillis();
        rightClicks.removeIf(time -> now - time > 1000);
        return rightClicks.size();
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        int lCps = getLeftCPS();
        int rCps = getRightCPS();

        String text = "CPS: " + lCps + " | " + rCps;

        int textWidth = minecraft.font.width(text);

        // Fondo del indicador
        graphics.fill(
                x - 5,
                y - 4,
                x + textWidth + 8,
                y + 14,
                BACKGROUND
        );

        // Línea decorativa morada
        graphics.fill(
                x - 5,
                y - 4,
                x - 2,
                y + 14,
                PURPLE
        );

        // Texto CPS
        graphics.text(
                minecraft.font,
                text,
                x,
                y,
                WHITE,
                false
        );
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Post event) {
        if (event.getAction() == GLFW.GLFW_PRESS) {
            int button = event.getInfo().button();
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                INSTANCE.addLeftClick();
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                INSTANCE.addRightClick();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderChatOverlay(CustomizeGuiOverlayEvent.Chat event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() == null) {
            INSTANCE.render(event.getGuiGraphics());
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AltairScreen)) {
            INSTANCE.render(event.getGuiGraphics());
        }
    }
}
