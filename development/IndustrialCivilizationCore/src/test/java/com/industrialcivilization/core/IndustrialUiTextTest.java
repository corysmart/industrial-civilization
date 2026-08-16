package com.industrialcivilization.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class IndustrialUiTextTest {
    @Test
    public void preservesSmallValues() {
        assertEquals("0", IndustrialUiText.compactNumber(0));
        assertEquals("999", IndustrialUiText.compactNumber(999));
    }

    @Test
    public void compactsMachineScaleValues() {
        assertEquals("1k", IndustrialUiText.compactNumber(1000));
        assertEquals("1.2k", IndustrialUiText.compactNumber(1200));
        assertEquals("120k", IndustrialUiText.compactNumber(120000));
        assertEquals("8M", IndustrialUiText.compactNumber(8000000));
        assertEquals("40M", IndustrialUiText.compactNumber(40000000));
        assertEquals("1G", IndustrialUiText.compactNumber(999999999));
    }

    @Test
    public void formatsPowerScalingTelemetry() {
        assertEquals("4.0x", IndustrialUiText.speedMultiplier(4D));
        assertEquals("50x", IndustrialUiText.speedMultiplier(50D));
        assertEquals("19t", IndustrialUiText.ticksAsEta(19));
        assertEquals("2.0s", IndustrialUiText.ticksAsEta(40));
    }
}
