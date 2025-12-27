package me.cortex.voxy.client.core.gpu;

import java.io.IOException;
import java.io.InputStream;

public final class GpuShaderResources {
    private GpuShaderResources() {
    }

    public static byte[] loadSpirv(String id) {
        String resourcePath = spirvResourcePath(id);
        try (InputStream stream = GpuShaderResources.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing SPIR-V shader resource: " + resourcePath);
            }
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SPIR-V shader resource: " + resourcePath, e);
        }
    }

    static String spirvResourcePath(String id) {
        String[] parts = id.split(":", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalArgumentException("Invalid shader id: " + id);
        }
        return "assets/" + parts[0] + "/shaders/" + parts[1] + ".spv";
    }
}