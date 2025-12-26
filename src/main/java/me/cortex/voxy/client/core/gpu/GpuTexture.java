package me.cortex.voxy.client.core.gpu;

public interface GpuTexture extends AutoCloseable {
    @Override
    void close();
}
