package com.walker.jvox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class Voxels implements AutoCloseable {

    static {
        // Load native library from path.
        final String libraryName = System.mapLibraryName("jvox-core-natives");
        final String libraryExtension = libraryName.substring(libraryName.indexOf('.'));

        try(InputStream is = TriMesh.class.getResourceAsStream("/" + libraryName)) {

            if(is == null) {
                throw new IllegalStateException("This build of JVox is not compiled for your OS. Please use a different build or follow the compilation instructions on https://github.com/WalkerKnapp/jvox.");
            }

            Path tempPath = Files.createTempFile("jvox-core-natives", libraryExtension);
            Files.copy(is, tempPath, StandardCopyOption.REPLACE_EXISTING);
            System.load(tempPath.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected long voxelData;
    protected int gridSize;

    protected ByteBuffer cachedCompressedVoxelByteBuffer;
    protected ByteBuffer cachedExpandedVoxelByteBuffer;

    protected Voxels() {

    }

    public abstract ByteBuffer getCompressedVoxelTable();

    public abstract ByteBuffer getExpandedVoxelTable();

    public abstract void voxelize(TriMesh mesh);

    public int getGridSize() {
        return this.gridSize;
    }

    public boolean checkVoxel(int x, int y, int z) {
        if (cachedExpandedVoxelByteBuffer != null) {
            return checkVoxelExpanded(x, y, z);
        } else {
            return checkVoxelCompressed(x, y, z);
        }
    }

    private boolean checkVoxelCompressed(int x, int y, int z) {
        int location = x + (y * gridSize) + (z * gridSize * gridSize);
        int byteLocation = location / Byte.SIZE;
        int bitLocation = (Byte.SIZE - 1) - (location % Byte.SIZE); // Bits are counted RtL, but arrays are indexed LtR

        return (getCompressedVoxelTable().get(byteLocation) & (1 << bitLocation)) == (1 << bitLocation);
    }

    private boolean checkVoxelExpanded(int x, int y, int z) {
        int location = x + (y * gridSize) + (z * gridSize * gridSize);

        return getExpandedVoxelTable().get(location) > 0;
    }

    public static int getCompressedVoxelTableSize(int gridSize) {
        return (gridSize * gridSize * gridSize) / Byte.SIZE;
    }

    public static int getExpandedVoxelTableSize(int gridSize) {
        return (gridSize * gridSize * gridSize);
    }

    /**
     * Chooses the fastest supported algorithm on the current system and voxelizes using it.
     * Creates a table to store the new voxels.
     * @return either a CpuVoxels or CudaVoxels instance.
     */
    public static Voxels voxelizeFromNew(TriMesh mesh, int gridSize) {
        Voxels voxels;

        if(CudaVoxels.isSupported()) {
            voxels = new CudaVoxels(gridSize);
        } else {
            voxels = new CpuVoxels(gridSize);
        }

        voxels.voxelize(mesh);
        return voxels;
    }
}
