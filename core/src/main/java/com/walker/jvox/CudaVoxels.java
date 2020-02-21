package com.walker.jvox;

import java.nio.ByteBuffer;

/**
 * Represents voxels stored in CUDA memory.
 */
public class CudaVoxels extends Voxels {
    // Detects if the current environment is compatible with CUDA.
    private static final boolean cudaEnabled;
    static {
        cudaEnabled = initializeCuda();
    }

    /**
     * Create a new set of voxels given a gridsize in CUDA memory.
     */
    public CudaVoxels(int gridSize) {
        this.gridSize = gridSize;
        this.voxelData = createNewVoxelData(gridSize);
    }

    /**
     * Create a set of voxels to pre-existing memory representing either:
     * Compressed memory if the boolean compressed is set to true
     * Expanded memory if the boolean compressed is set to false.
     * @param voxelTable
     * @param gridSize
     */
    public CudaVoxels(long voxelTable, int gridSize, boolean compressed) {
        this(compressed ? voxelTable : 0, compressed ? 0 : voxelTable, gridSize);
    }

    public CudaVoxels(long compressedVoxelTable, long expandedVoxelTable, int gridSize) {
        this.gridSize = gridSize;
        this.voxelData = createVoxelDataFromExistingTables(compressedVoxelTable, expandedVoxelTable, gridSize);
    }

    @Override
    public void close() throws Exception {
        destroyVoxelData(voxelData);
    }

    @Override
    public ByteBuffer getCompressedVoxelTable() {
        return getCompressedVoxelTableBuffer(voxelData, gridSize);
    }

    @Override
    public ByteBuffer getExpandedVoxelTable() {
        return getExpandedVoxelTableBuffer(voxelData, gridSize);
    }

    @Override
    public void voxelize(TriMesh mesh) {
        nVoxelize(voxelData, mesh.triMeshPointer, gridSize);
    }

    public static boolean isSupported() {
        return cudaEnabled;
    }

    // Native Methods
    private static native boolean initializeCuda();

    private static native long createNewVoxelData(int gridSize);
    private static native long createVoxelDataFromExistingTables(long compressedTable, long expandedTable, int gridSize);
    private static native void nVoxelize(long voxelDataPointer, long meshPointer, int gridsize);
    private static native void destroyVoxelData(long voxelDataPointer);

    // Native Methods
    private static native ByteBuffer getCompressedVoxelTableBuffer(long pVoxelData, int gridSize);
    private static native ByteBuffer getExpandedVoxelTableBuffer(long pVoxelData, int gridSize);
}
