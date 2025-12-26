package me.cortex.voxy.client.core.gpu;

import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.config.GpuBackendConfig;

public final class GpuBackendProvider {
    private GpuBackendProvider() {
    }

    public static GpuBackend create(GpuBackendConfig config) {
        GpuBackendConfig resolvedConfig = config == null ? new GpuBackendConfig() : config;
        boolean vulkanAvailable = VulkanGpuBackend.isSupported();
        GpuBackendConfig.Backend requested = resolvedConfig.backend == null
                ? GpuBackendConfig.Backend.AUTO
                : resolvedConfig.backend;
        GpuBackendConfig.Backend selected = resolvedConfig.resolve(vulkanAvailable);
        if (requested != selected && requested != GpuBackendConfig.Backend.AUTO) {
            Logger.info("GPU backend request " + requested + " unavailable, falling back to " + selected);
        }
        Logger.info("GPU backend selected: " + selected + " (vulkanAvailable=" + vulkanAvailable + ")");
        return switch (selected) {
            case VULKAN -> new VulkanGpuBackend();
            case OPENGL -> new OpenGlGpuBackend();
            case AUTO -> new OpenGlGpuBackend();
        };
    }
}
