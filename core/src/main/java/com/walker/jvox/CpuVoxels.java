package com.walker.jvox;

import java.nio.ByteBuffer;

/**
 * Represents voxels stored in CPU memory, either voxelized using the CPU algorithm or moved from CUDA memory
 */
public class CpuVoxels extends Voxels {
    private long voxelData;

    private CpuVoxels() {

    }

    @Override
    public void close() throws Exception {
        destroyVoxelData(voxelData);
    }

    public static CpuVoxels voxelize(TriMesh mesh, int gridSize) {
        CpuVoxels voxels = new CpuVoxels();
        voxels.voxelData = nVoxelize(mesh.triMeshPointer, gridSize);
        return voxels;
    }

    // Native Methods
    private static native long nVoxelize(long meshPointer, int gridsize);
    private static native void destroyVoxelData(long voxelDataPointer);
}
