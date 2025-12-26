package me.cortex.voxy.client.core.gpu;

/**
 * Per-frame command recording interface.
 */
public interface GpuCommandList {
    void begin();

    void end();
}
