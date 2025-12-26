package me.cortex.voxy.client.core.gpu;

public interface GpuPipeline extends AutoCloseable {
    @Override
    void close();
}
