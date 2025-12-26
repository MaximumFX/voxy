package me.cortex.voxy.client.core.gpu;

public interface GpuFence extends AutoCloseable {
    @Override
    void close();
}
