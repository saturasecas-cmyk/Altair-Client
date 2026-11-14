                dragOffsetY = (int) mouseY - ty;
                // Si estaba apagado, encenderlo al arrastrar para facilitar su uso
                if (!target.isEnabled()) {
                    target.setEnabled(true);
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dx,
            double dy
    ) {
        if (draggedTarget != null) {
            int newX = (int) event.x() - dragOffsetX;
            int newY = (int) event.y() - dragOffsetY;
            // Restringir a los bordes de la pantalla
            newX = Math.max(5, Math.min(width - draggedTarget.getWidth() - 5, newX));
            newY = Math.max(32, Math.min(height - draggedTarget.getHeight() - 5, newY));
            draggedTarget.setX(newX);
            draggedTarget.setY(newY);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggedTarget != null) {
            draggedTarget = null;
            return true;
        }
        return super.mouseReleased(event);
    }
    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.gui.setScreen(parentScreen);
        }
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
