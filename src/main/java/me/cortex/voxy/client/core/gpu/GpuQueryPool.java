package me.cortex.voxy.client.core.gpu;

public interface GpuQueryPool extends AutoCloseable {
    @Override
    void close();
}
