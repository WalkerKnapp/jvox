#include "com_walker_jvox_TriMesh.h"
#include <TriMesh.h>

jlong Java_com_walker_jvox_TriMesh_read(JNIEnv *env, jclass jClazz, jstring jFilename) {
    auto *isCopy = new jboolean();
    *isCopy = JNI_FALSE;
    const char *filename = env->GetStringUTFChars(jFilename, isCopy);

    trimesh::TriMesh *mesh = trimesh::TriMesh::read(filename);

    delete isCopy;
    env->ReleaseStringUTFChars(jFilename, filename);

    return reinterpret_cast<jlong>(mesh);
}

void Java_com_walker_jvox_TriMesh_write(JNIEnv *env, jclass jClazz, jlong pTriMesh, jstring jFilename) {
    auto *isCopy = new jboolean();
    *isCopy = JNI_FALSE;
    const char *filename = env->GetStringUTFChars(jFilename, isCopy);

    reinterpret_cast<trimesh::TriMesh *>(pTriMesh)->write(filename);

    delete isCopy;
    env->ReleaseStringUTFChars(jFilename, filename);
}

void Java_com_walker_jvox_TriMesh_destroy(JNIEnv *env, jclass jClazz, jlong pTriMesh) {
    delete reinterpret_cast<trimesh::TriMesh *>(pTriMesh);
}