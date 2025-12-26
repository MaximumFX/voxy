package me.cortex.voxy.client.core.gpu;

public interface GpuFence extends AutoCloseable {
    boolean signaled();

    default void free() {
        close();
    }

    @Override
    void close();
}
