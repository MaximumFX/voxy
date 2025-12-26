package me.cortex.voxy.client.core.gpu;

public interface GpuBuffer extends AutoCloseable {
    @Override
    void close();
}
