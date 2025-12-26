package me.cortex.voxy.client.core.gpu;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

final class VulkanGpuQueue {
    private final ExecutorService executor;

    VulkanGpuQueue() {
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "VulkanGpuQueue");
            thread.setDaemon(true);
            return thread;
        };
        this.executor = Executors.newSingleThreadExecutor(factory);
    }

    void submit(VulkanGpuCommandList commandList, VulkanGpuFence fence) {
        this.executor.execute(() -> {
            commandList.awaitSemaphores();
            commandList.execute();
            commandList.signalSemaphores();
            if (fence != null) {
                fence.signal();
            }
        });
    }
}
