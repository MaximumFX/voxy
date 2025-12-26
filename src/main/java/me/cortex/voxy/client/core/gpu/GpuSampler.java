package me.cortex.voxy.client.core.gpu;

public interface GpuSampler extends AutoCloseable {
    @Override
    void close();
}
