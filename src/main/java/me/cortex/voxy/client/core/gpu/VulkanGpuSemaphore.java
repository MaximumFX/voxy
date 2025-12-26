package me.cortex.voxy.client.core.gpu;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

final class VulkanGpuSemaphore {
    private final AtomicReference<CompletableFuture<Void>> future =
            new AtomicReference<>(new CompletableFuture<>());

    void signal() {
        future.get().complete(null);
    }

    void await() {
        future.get().join();
    }

    void reset() {
        future.set(new CompletableFuture<>());
    }

    boolean isSignaled() {
        return future.get().isDone();
    }
}
