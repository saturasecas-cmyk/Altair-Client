            );
            int knobX = entry.enabled ? toggleX + 14 : toggleX + 2;
            graphics.fill(
                    knobX, y + 9,
                    knobX + 9, y + 17,
                    WHITE
            );
        }
        // Renderizar FPS si está activo (dentro del menú también).
        FPSDisplay.getInstance().render(graphics);
    }
    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }
        double mouseX = event.x();
        double mouseY = event.y();
        int sidebarWidth = 138;
        int contentX = panelX + sidebarWidth + 18;
        int contentY = panelY + 19 + 16 + 40;
        int cardWidth = panelWidth - sidebarWidth - 35;
        for (int i = 0; i < modules.size(); i++) {
            int y = contentY + i * 50;
            if (mouseX >= contentX - 5
                    && mouseX <= contentX + cardWidth
                    && mouseY >= y - 5
                    && mouseY <= y + 36) {
                ModuleEntry entry = modules.get(i);
                entry.enabled = !entry.enabled;
                if (entry.onToggle != null) {
                    entry.onToggle.run();
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
