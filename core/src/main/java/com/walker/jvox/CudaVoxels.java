package com.walker.jvox;

/**
 * Represents voxels stored in CUDA memory.
 */
public class CudaVoxels extends Voxels {
    // Detects if the current environment is compatible with CUDA.
    private static final boolean cudaEnabled;
    static {
        cudaEnabled = initializeCuda();
    }

    private long voxelData;

    private CudaVoxels() {

    }

    @Override
    public void close() throws Exception {
        destroyVoxelData(voxelData);
    }

    public static boolean isSupported() {
        return cudaEnabled;
    }

    public static CudaVoxels voxelize(TriMesh mesh, int gridSize) {
        CudaVoxels voxels = new CudaVoxels();
        voxels.voxelData = nVoxelize(mesh.triMeshPointer, gridSize);
        return voxels;
    }

    // Native Methods
    private static native boolean initializeCuda();

    private static native long nVoxelize(long meshPointer, int gridsize);
    private static native void destroyVoxelData(long voxelDataPointer);
}
