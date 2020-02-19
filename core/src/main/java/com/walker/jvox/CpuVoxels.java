package com.walker.jvox;

import java.nio.ByteBuffer;

/**
 * Represents voxels stored in CPU memory, either voxelized using the CPU algorithm or moved from CUDA memory
 */
public class CpuVoxels extends Voxels {
    /**
     * Create a new set of voxels given a gridsize
     */
    public CpuVoxels(int gridSize) {
        this.gridSize = gridSize;
        this.voxelData = createNewVoxelData(gridSize);
    }

    private CpuVoxels(long voxelData, int gridSize) {
        this.voxelData = voxelData;
        this.gridSize = gridSize;
    }

    @Override
    public void close() throws Exception {
        destroyVoxelData(voxelData);
    }

    /**
     * Voxelize to a newly created table.
     *
     * @param mesh
     * @param gridSize
     * @return
     */
    public static CpuVoxels voxelize(TriMesh mesh, int gridSize) {
        return new CpuVoxels(nVoxelize(mesh.triMeshPointer, gridSize), gridSize);
    }

    /**
     * Voxelize to a table in external memory.
     *
     * @param mesh
     * @param externalTable
     * @param gridSize
     * @return
     */
    public static CpuVoxels voxelize(TriMesh mesh, long externalTable, int gridSize) {
        return new CpuVoxels(nVoxelize(mesh.triMeshPointer, externalTable, gridSize), gridSize);
    }

    // Native Methods
    private static native long createNewVoxelData(int gridSize);
    private static native long nVoxelize(long meshPointer, int gridsize);
    private static native long nVoxelize(long meshPointer, long externalTable, int gridsize);
    private static native void destroyVoxelData(long voxelDataPointer);
}
