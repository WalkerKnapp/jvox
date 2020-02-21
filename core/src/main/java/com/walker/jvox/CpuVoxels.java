package com.walker.jvox;

import java.nio.ByteBuffer;

/**
 * Represents voxels stored in CPU memory, either voxelized using the CPU algorithm or moved from CUDA memory
 */
public class CpuVoxels extends Voxels {
    /**
     * Create a new set of voxels given a gridsize in memory.
     */
    public CpuVoxels(int gridSize) {
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
    public CpuVoxels(long voxelTable, int gridSize, boolean compressed) {
        this(compressed ? voxelTable : 0, compressed ? 0 : voxelTable, gridSize);
    }

    public CpuVoxels(long compressedVoxelTable, long expandedVoxelTable, int gridSize) {
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

    // Native Methods
    private static native long createNewVoxelData(int gridSize);
    private static native long createVoxelDataFromExistingTables(long compressedTable, long expandedTable, int gridSize);
    private static native void nVoxelize(long voxelDataPointer, long meshPointer, int gridsize);
    private static native void destroyVoxelData(long voxelDataPointer);

    private static native ByteBuffer getCompressedVoxelTableBuffer(long pVoxelData, int gridSize);
    private static native ByteBuffer getExpandedVoxelTableBuffer(long pVoxelData, int gridSize);
}
