package com.walker.jvox;

import java.io.File;
import java.nio.file.Path;

/**
 * A 3D mesh, represented internally using a TriMesh object from the TriMesh2 C library.
 */
public class TriMesh implements AutoCloseable {

    public long triMeshPointer;

    private TriMesh() {

    }

    @Override
    public void close() {
        destroy(triMeshPointer);
    }

    public static TriMesh parseMesh(Path meshPath) {
        return parseMesh(meshPath.toString());
    }

    public static TriMesh parseMesh(File meshFile) {
        return parseMesh(meshFile.getPath());
    }

    public static TriMesh parseMesh(String meshPath) {
        TriMesh mesh = new TriMesh();
        mesh.triMeshPointer = read(meshPath);
        return mesh;
    }

    // Native Methods
    private static native long read(String filename);
    private static native void write(long triMeshPointer, String filename);
    private static native void destroy(long triMeshPointer);
}
