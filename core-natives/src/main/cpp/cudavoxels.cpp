#include <util_cuda.h>
#include <TriMesh.h>
#include <glm/glm.hpp>
#include <util.h>

#include "voxels.h"
#include "com_walker_jvox_CudaVoxels.h"

#include <thrust_operations.cuh>

// Declaration of CUDA functions
float* meshToGPU_thrust(const trimesh::TriMesh *mesh); // METHOD 3 to transfer triangles can be found in thrust_operations.cu(h)
void cleanup_thrust();
void voxelize(const voxinfo & v, float* triangle_data, unsigned int* vtable, bool useThrustPath, bool morton_code);

jboolean Java_com_walker_jvox_CudaVoxels_initializeCuda(JNIEnv *env, jclass jClazz) {
    return initCuda();
}

jlong Java_com_walker_jvox_CudaVoxels_nVoxelize__JI(JNIEnv *env, jclass jClazz, jlong pMesh, jint jGridSize) {
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

    float *mesh_triangles = meshToGPU_thrust(mesh);
    // Allocate voxtable on CUDA host memory
    checkCudaErrors(cudaHostAlloc((void**)voxData->voxtable, voxData->voxtable_size, cudaHostAllocDefault));

    voxelize(voxelization_info, mesh_triangles, voxData->voxtable, true, false);

    return reinterpret_cast<jlong>(voxData);
}

jlong Java_com_walker_jvox_CudaVoxels_nVoxelize__JJI(JNIEnv *env, jclass jClazz, jlong pMesh, jlong pExternalTable, jint jGridSize) {
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
    // Use external voxtable
    voxData->voxtable = reinterpret_cast<unsigned int *>(pExternalTable);
    voxData->externally_managed = true;

    float *mesh_triangles = meshToGPU_thrust(mesh);

    voxelize(voxelization_info, mesh_triangles, voxData->voxtable, true, false);

    return reinterpret_cast<jlong>(voxData);
}

void Java_com_walker_jvox_CudaVoxels_destroyVoxelData(JNIEnv *env, jclass jClazz, jlong pVoxData) {
    auto *voxData = reinterpret_cast<voxeldata *>(pVoxData);

    cleanup_thrust();

    if(!voxData->externally_managed) {
        cudaFreeHost(voxData->voxtable);
    }
    free(voxData);
}
