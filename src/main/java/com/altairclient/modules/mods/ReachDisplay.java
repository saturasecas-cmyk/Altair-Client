                y + 14,
                BACKGROUND
        );
        // Línea decorativa azul/cian
        graphics.fill(
                x - 5,
                y - 4,
                x - 2,
                y + 14,
                CYAN
        );
        // Texto Reach
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
    public static void onAttackEntity(AttackEntityEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null && event.getEntity() == mc.player && event.getTarget() != null) {
            Entity target = event.getTarget();
            Vec3 eye = mc.player.getEyePosition();
            AABB box = target.getBoundingBox();
            double cx = Math.max(box.minX, Math.min(eye.x, box.maxX));
            double cy = Math.max(box.minY, Math.min(eye.y, box.maxY));
            double cz = Math.max(box.minZ, Math.min(eye.z, box.maxZ));
            double dist = eye.distanceTo(new Vec3(cx, cy, cz));
            INSTANCE.setReach(dist);
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
