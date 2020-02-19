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

    protected ByteBuffer cachedVoxelByteBuffer;

    protected Voxels() {

    }

    public int getGridSize() {
        return this.gridSize;
    }

    public boolean checkVoxel(int x, int y, int z) {
        int location = x + (y * gridSize) + (z * gridSize * gridSize);
        int byteLocation = location / Byte.SIZE;
        int bitLocation = (Byte.SIZE - 1) - (location % Byte.SIZE); // Bits are counted RtL, but arrays are indexed LtR

        return (getVoxelTable().get(byteLocation) & (1 << bitLocation)) == (1 << bitLocation);
    }

    public ByteBuffer getVoxelTable() {
        if(cachedVoxelByteBuffer == null) {
            return getVoxelTableAsBuffer(voxelData);
        } else {
            return cachedVoxelByteBuffer;
        }
    }

    /**
     * Chooses the fastest supported algorithm on the current system and voxelizes using it.
     * Creates a table to store the new voxels.
     * @return either a CpuVoxels or CudaVoxels instance.
     */
    public static Voxels voxelize(TriMesh mesh, int gridSize) {
        if(CudaVoxels.isSupported()) {
            return CudaVoxels.voxelize(mesh, gridSize);
        } else {
            return CpuVoxels.voxelize(mesh, gridSize);
        }
    }

    // Native Methods
    private static native ByteBuffer getVoxelTableAsBuffer(long pVoxelData);
}
