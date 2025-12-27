package me.cortex.voxy.client.core.gpu;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

final class VulkanGpuFence implements GpuFence {
    private final AtomicBoolean signaled = new AtomicBoolean(false);
    private final CountDownLatch latch = new CountDownLatch(1);

    void signalInternal() {
        if (this.signaled.compareAndSet(false, true)) {
            this.latch.countDown();
        }
    }

    @Override
    public boolean signaled() {
        return this.signaled.get();
    }

    @Override
    public void signal() {
        signalInternal();
    }

    @Override
    public void await() {
        if (this.signaled.get()) {
            return;
        }
        try {
            this.latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        signalInternal();
    }

    @Override
    public String toString() {
        return "VulkanGpuFence{signaled=" + signaled() + "}";
    }
}
