package com.altairclient.client;

/**
 * Utilidades de animación: valores que se acercan suavemente a su objetivo (independiente
 * de los FPS), contadores con destello al subir o bajar, y mezcla de colores.
 */
public final class Anim {

    private static final int FLASH_UP = 0xFF5DFFA8;
    private static final int FLASH_DOWN = 0xFFFF5C77;

    private Anim() {
    }

    public static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    public static float easeOut(float t) {
        float k = 1.0F - clamp(t, 0.0F, 1.0F);
        return 1.0F - k * k * k;
    }

    /** Mezcla dos colores ARGB (t = 0 da c0, t = 1 da c1). */
    public static int mix(int c0, int c1, float t) {
        t = clamp(t, 0.0F, 1.0F);
        int a = (int) (((c0 >>> 24) & 0xFF) + ((((c1 >>> 24) & 0xFF) - ((c0 >>> 24) & 0xFF)) * t));
        int r = (int) (((c0 >> 16) & 0xFF) + ((((c1 >> 16) & 0xFF) - ((c0 >> 16) & 0xFF)) * t));
        int g = (int) (((c0 >> 8) & 0xFF) + ((((c1 >> 8) & 0xFF) - ((c0 >> 8) & 0xFF)) * t));
        int b = (int) ((c0 & 0xFF) + (((c1 & 0xFF) - (c0 & 0xFF)) * t));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Multiplica la opacidad de un color ARGB. */
    public static int scaleAlpha(int color, float factor) {
        int a = (int) (((color >>> 24) & 0xFF) * clamp(factor, 0.0F, 1.0F));
        return (a << 24) | (color & 0x00FFFFFF);
    }

    /** Valor que se acerca de forma exponencial a su objetivo; no depende de los FPS. */
    public static final class Smooth {
        private float value;
        private boolean started = false;
        private long last;

        public Smooth() {
        }

        public float update(float target, float speed) {
            long now = System.nanoTime();
            if (!started) {
                started = true;
                value = target;
                last = now;
                return value;
            }
            float dt = Math.min(0.1F, (now - last) / 1.0E9F);
            last = now;
            value += (target - value) * (1.0F - (float) Math.exp(-speed * dt));
            if (Math.abs(target - value) < 0.0005F) {
                value = target;
            }
            return value;
        }

        public float get() {
            return value;
        }

        public void snap(float v) {
            value = v;
            started = true;
            last = System.nanoTime();
        }
    }

    /** Número que cuenta hasta su valor nuevo y destella en verde al subir o en rojo al bajar. */
    public static final class Counter {
        private final Smooth smooth = new Smooth();
        private final float threshold;
        private float lastTarget = Float.NaN;
        private float flash = 0.0F;
        private int direction = 0;
        private long lastTime = 0L;

        /** threshold: cambio mínimo del objetivo para que cuente como "subida" o "bajada". */
        public Counter(float threshold) {
            this.threshold = threshold;
        }

        public float update(float target, float speed) {
            long now = System.nanoTime();
            float dt = lastTime == 0L ? 0.0F : Math.min(0.1F, (now - lastTime) / 1.0E9F);
            lastTime = now;

            if (!Float.isNaN(lastTarget) && Math.abs(target - lastTarget) >= threshold) {
                direction = target > lastTarget ? 1 : -1;
                flash = 1.0F;
            }
            lastTarget = target;
            flash = Math.max(0.0F, flash - dt * 2.2F);
            return smooth.update(target, speed);
        }

        public float flash() {
            return flash;
        }

        /** Color del texto: el base, mezclado con verde (subida) o rojo (bajada) mientras dura el destello. */
        public int color(int base) {
            if (flash <= 0.0F) {
                return base;
            }
            return mix(base, direction > 0 ? FLASH_UP : FLASH_DOWN, flash * 0.85F);
        }
    }
}
