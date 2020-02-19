package com.walker.jvox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * A 3D mesh, represented internally using a TriMesh object from the TriMesh2 C library.
 */
public class TriMesh implements AutoCloseable {

    static {
        // Load native library from path.
        final String libraryName = System.mapLibraryName("jvox-trimesh-natives");
        final String libraryExtension = libraryName.substring(libraryName.indexOf('.'));

        try(InputStream is = TriMesh.class.getResourceAsStream("/" + libraryName)) {

            if(is == null) {
                throw new IllegalStateException("This build of JVox-TriMesh is not compiled for your OS. Please use a different build or follow the compilation instructions on https://github.com/WalkerKnapp/jvox.");
            }

            Path tempPath = Files.createTempFile("jvox-trimesh-natives", libraryExtension);
            Files.copy(is, tempPath, StandardCopyOption.REPLACE_EXISTING);
            System.load(tempPath.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

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
