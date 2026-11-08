    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        int fps = minecraft.getFps();
        String text = "FPS: " + fps;
        int x = 10;
        int y = 10;
        int textWidth = minecraft.font.width(text);
        // Fondo del indicador.
        graphics.fill(
                x - 5,
                y - 4,
                x + textWidth + 8,
                y + 14,
                BACKGROUND
        );
        // Línea decorativa.
        graphics.fill(
                x - 5,
                y - 4,
                x - 2,
                y + 14,
                PURPLE
        );
        // Texto FPS.
        graphics.text(
                minecraft.font,
                text,
                x,
                y,
                WHITE,
                false
        );
    }
}
