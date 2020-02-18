package com.walker.jvox;

public abstract class Voxels implements AutoCloseable {
    /**
     * Chooses the fastest supported algorithm on the current system and voxelizes using it.
     * @return either a CpuVoxels or CudaVoxels instance.
     */
    public static Voxels voxelize(TriMesh mesh, int gridSize) {
        if(CudaVoxels.isSupported()) {
            return CudaVoxels.voxelize(mesh, gridSize);
        } else {
            return CpuVoxels.voxelize(mesh, gridSize);
        }
    }
}
