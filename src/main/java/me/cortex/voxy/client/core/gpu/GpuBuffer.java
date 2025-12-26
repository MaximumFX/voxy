package me.cortex.voxy.client.core.gpu;

public interface GpuBuffer extends AutoCloseable {
    int id();

    long size();

    boolean isSparse();

    GpuBuffer zero();

    GpuBuffer zeroRange(long offset, long size);

    GpuBuffer fill(int data);

    GpuBuffer name(String name);

    default long mappedAddress() {
        throw new UnsupportedOperationException("Mapped buffer address not available");
    }

    default void free() {
        close();
    }

    @Override
    void close();
}
