package me.cortex.voxy.client.core.gpu;

import java.util.concurrent.atomic.AtomicBoolean;

final class VulkanGpuFence implements GpuFence {
    private final AtomicBoolean signaled = new AtomicBoolean(false);

    void signal() {
        this.signaled.set(true);
    }

    @Override
    public boolean signaled() {
        return this.signaled.get();
    }

    @Override
    public void close() {
        signal();
    }

    @Override
    public String toString() {
        return "VulkanGpuFence{signaled=" + signaled() + "}";
    }
}
