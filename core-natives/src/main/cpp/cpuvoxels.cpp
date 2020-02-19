#include <glm/glm.hpp>
#include <util.h>
#include <cpu_voxelizer.h>
#include <TriMesh.h>
#include <com_walker_jvox_CpuVoxels.h>


#include "voxels.h"
#include "com_walker_jvox_CpuVoxels.h"

jlong Java_com_walker_jvox_CpuVoxels_createNewVoxelData(JNIEnv *env, jclass jClazz, jint jGridSize) {
    auto *voxData = static_cast<voxeldata *>(malloc(sizeof(voxeldata)));
    voxData->voxtable_size = static_cast<size_t>(ceil(static_cast<size_t>(jGridSize)* static_cast<size_t>(jGridSize)* static_cast<size_t>(jGridSize)) / 8.0f);
    voxData->voxtable = static_cast<unsigned int *>(calloc(1, voxData->voxtable_size));

    return reinterpret_cast<jlong>(voxData);
}

jlong Java_com_walker_jvox_CpuVoxels_nVoxelize__JI(JNIEnv *env, jclass jClazz, jlong pMesh, jint jGridSize) {
    auto *mesh = reinterpret_cast<trimesh::TriMesh *>(pMesh);

    // Set needed mesh flags
    mesh->need_faces();
    mesh->need_bbox();

    // Compute AABox
    AABox<glm::vec3> bbox_mesh(trimesh_to_glm(mesh->bbox.min), trimesh_to_glm(mesh->bbox.max));

    // Compute Voxelization Info
    voxinfo voxelization_info(createMeshBBCube<glm::vec3>(bbox_mesh), glm::uvec3(jGridSize, jGridSize, jGridSize), mesh->faces.size());

    // Create table to store voxels (1 voxel per bit)
    auto *voxData = static_cast<voxeldata *>(malloc(sizeof(voxeldata)));
    voxData->voxtable_size = static_cast<size_t>(ceil(static_cast<size_t>(voxelization_info.gridsize.x)* static_cast<size_t>(voxelization_info.gridsize.y)* static_cast<size_t>(voxelization_info.gridsize.z)) / 8.0f);
    voxData->voxtable = static_cast<unsigned int *>(calloc(1, voxData->voxtable_size));

    cpu_voxelizer::cpu_voxelize_mesh(voxelization_info, mesh, voxData->voxtable, false);

    return reinterpret_cast<jlong>(voxData);
}

jlong Java_com_walker_jvox_CpuVoxels_nVoxelize__JJI(JNIEnv *env, jclass jClazz, jlong pMesh, jlong pExternalTable, jint jGridSize) {
    auto *mesh = reinterpret_cast<trimesh::TriMesh *>(pMesh);

    // Set needed mesh flags
    mesh->need_faces();
    mesh->need_bbox();

    // Compute AABox
    AABox<glm::vec3> bbox_mesh(trimesh_to_glm(mesh->bbox.min), trimesh_to_glm(mesh->bbox.max));

    // Compute Voxelization Info
    voxinfo voxelization_info(createMeshBBCube<glm::vec3>(bbox_mesh), glm::uvec3(jGridSize, jGridSize, jGridSize), mesh->faces.size());

    // Create table to store voxels (1 voxel per bit)
    auto *voxData = static_cast<voxeldata *>(malloc(sizeof(voxeldata)));
    voxData->voxtable_size = static_cast<size_t>(ceil(static_cast<size_t>(voxelization_info.gridsize.x)* static_cast<size_t>(voxelization_info.gridsize.y)* static_cast<size_t>(voxelization_info.gridsize.z)) / 8.0f);
    // Use externally managed voxtable
    voxData->voxtable = reinterpret_cast<unsigned int *>(pExternalTable);
    voxData->externally_managed = true;

    cpu_voxelizer::cpu_voxelize_mesh(voxelization_info, mesh, voxData->voxtable, false);

    return reinterpret_cast<jlong>(voxData);
}

void Java_com_walker_jvox_CpuVoxels_destroyVoxelData(JNIEnv *env, jclass jClazz, jlong pVoxData) {
    auto *voxData = reinterpret_cast<voxeldata *>(pVoxData);

    if(!voxData->externally_managed) {
        free(voxData->voxtable);
    }
    free(voxData);
}
