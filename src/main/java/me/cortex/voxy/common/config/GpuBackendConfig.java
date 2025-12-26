package me.cortex.voxy.common.config;

public class GpuBackendConfig {
    public enum Backend {
        AUTO,
        VULKAN,
        OPENGL
    }

    public Backend backend = Backend.AUTO;
    public Backend fallback = Backend.OPENGL;

    public Backend resolve(boolean vulkanAvailable) {
        Backend requested = this.backend == null ? Backend.AUTO : this.backend;
        Backend fallbackBackend = this.fallback == null ? Backend.OPENGL : this.fallback;
        if (fallbackBackend == Backend.VULKAN && !vulkanAvailable) {
            fallbackBackend = Backend.OPENGL;
        }
        if (requested == Backend.AUTO) {
            return vulkanAvailable ? Backend.VULKAN : fallbackBackend;
        }
        if (requested == Backend.VULKAN && !vulkanAvailable) {
            return fallbackBackend;
        }
        return requested;
    }
}
