package com.altairclient.compat;

/** Small stand-in for the 26.x mouse event so the screens keep the same code on 1.20. */
public record MouseButtonEvent(double x, double y, int button) {
}
