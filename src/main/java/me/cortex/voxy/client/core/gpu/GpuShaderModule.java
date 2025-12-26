package me.cortex.voxy.client.core.gpu;

public interface GpuShaderModule extends AutoCloseable {
    @Override
    void close();
}
