#include <util_cuda.h>
#include <TriMesh.h>
#include <glm/glm.hpp>
#include <util.h>

#include "voxels.h"
#include "com_walker_jvox_CudaVoxels.h"

// Declaration of CUDA functions
float* meshToGPU_thrust(const trimesh::TriMesh *mesh); // METHOD 3 to transfer triangles can be found in thrust_operations.cu(h)
void cleanup_thrust();
void voxelize(const voxinfo & v, float* triangle_data, unsigned int* vtable, bool useThrustPath, bool morton_code);

jboolean Java_com_walker_jvox_CudaVoxels_initializeCuda(JNIEnv *env, jclass jClazz) {
    return initCuda();
}

jlong Java_com_walker_jvox_CudaVoxels_createNewVoxelData(JNIEnv *env, jclass jClazz, jint jGridSize) {
    auto *voxData = static_cast<voxeldata *>(malloc(sizeof(voxeldata)));

    voxData->compressed_voxtable_size = get_compressed_voxtable_size(jGridSize);
    checkCudaErrors(cudaHostAlloc((void**)voxData->compressed_voxtable, voxData->compressed_voxtable_size, cudaHostAllocDefault));

    return reinterpret_cast<jlong>(voxData);
}

jlong Java_com_walker_jvox_CudaVoxels_createVoxelDataFromExistingTables(JNIEnv *env, jclass jClazz, jlong pCompressedTable, jlong pExpandedTable, jint jGridSize) {
    auto *voxData = static_cast<voxeldata *>(malloc(sizeof(voxeldata)));

    if(pCompressedTable) {
        voxData->compressed_voxtable_size = get_compressed_voxtable_size(jGridSize);
        voxData->compressed_voxtable = reinterpret_cast<unsigned int *>(pCompressedTable);
        voxData->compressed_voxtable_externally_managed = true;
    }
    if(pExpandedTable) {
        voxData->expanded_voxtable_size = get_expanded_voxtable_size(jGridSize);
        voxData->expanded_voxtable = reinterpret_cast<unsigned int *>(pExpandedTable);
        voxData->expanded_voxtable_externally_managed = true;
    }

    return reinterpret_cast<jlong>(voxData);
}

void Java_com_walker_jvox_CudaVoxels_nVoxelize(JNIEnv *env, jclass jClazz, jlong pVoxData, jlong pMesh, jint jGridSize) {
    auto *voxData = reinterpret_cast<voxeldata *>(pVoxData);
    auto *mesh = reinterpret_cast<trimesh::TriMesh *>(pMesh);

    // Set needed mesh flags
    mesh->need_faces();
    mesh->need_bbox();

    // Compute AABox
    AABox<glm::vec3> bbox_mesh(trimesh_to_glm(mesh->bbox.min), trimesh_to_glm(mesh->bbox.max));

    // Compute Voxelization Info
    voxinfo voxelization_info(createMeshBBCube<glm::vec3>(bbox_mesh), glm::uvec3(jGridSize, jGridSize, jGridSize), mesh->faces.size());

    if (!voxData->compressed_voxtable) {
        // TODO: Maybe check if an externally managed compressed table is set here.
        // Create a compressed voxtable to store results if none exists
        voxData->compressed_voxtable_size = get_compressed_voxtable_size(jGridSize);
        checkCudaErrors(cudaHostAlloc((void**)voxData->compressed_voxtable, voxData->compressed_voxtable_size, cudaHostAllocDefault));
    }

    float *mesh_triangles = meshToGPU_thrust(mesh);
    voxelize(voxelization_info, mesh_triangles, voxData->compressed_voxtable, true, false);
    cleanup_thrust();

    if(voxData->expanded_voxtable) {
        // Expand voxelized data into expanded_voxtable if it exists
        expand_compressed_voxtable(voxData);
    }
}

void Java_com_walker_jvox_CudaVoxels_destroyVoxelData(JNIEnv *env, jclass jClazz, jlong pVoxData) {
    auto *voxData = reinterpret_cast<voxeldata *>(pVoxData);

    if(voxData->compressed_voxtable && !voxData->compressed_voxtable_externally_managed) {
        cudaFreeHost(voxData->compressed_voxtable);
    }
    if(voxData->expanded_voxtable && !voxData->expanded_voxtable_externally_managed) {
        cudaFreeHost(voxData->expanded_voxtable);
    }

    free(voxData);
}

jobject Java_com_walker_jvox_CudaVoxels_getCompressedVoxelTableBuffer(JNIEnv *env, jclass jClazz, jlong pVoxelData, jint jGridSize) {
    auto *voxelData = reinterpret_cast<voxeldata *>(pVoxelData);

    if(!voxelData->compressed_voxtable) {
        // Create new compressed voxtable if one does not exist.
        voxelData->compressed_voxtable_size = get_compressed_voxtable_size(jGridSize);
        checkCudaErrors(cudaHostAlloc((void**)voxelData->compressed_voxtable, voxelData->compressed_voxtable_size, cudaHostAllocDefault));

        // If an expanded voxtable exists, compress data from it into new table
        if(voxelData->expanded_voxtable) {
            compress_expanded_voxtable(voxelData);
        }
    }

    return env->NewDirectByteBuffer(voxelData->compressed_voxtable, voxelData->compressed_voxtable_size);
}

jobject Java_com_walker_jvox_CudaVoxels_getExpandedVoxelTableBuffer(JNIEnv *env, jclass jClazz, jlong pVoxelData, jint jGridSize) {
    auto *voxelData = reinterpret_cast<voxeldata *>(pVoxelData);

    if(!voxelData->expanded_voxtable) {
        // Create new expanded voxtable if one does not exist.
        voxelData->expanded_voxtable_size = get_expanded_voxtable_size(jGridSize);
        checkCudaErrors(cudaHostAlloc((void**)voxelData->expanded_voxtable, voxelData->expanded_voxtable_size, cudaHostAllocDefault));

        // If a compressed voxtable exists, expand data from it into new table
        if(voxelData->compressed_voxtable) {
            expand_compressed_voxtable(voxelData);
        }
    }

    return env->NewDirectByteBuffer(voxelData->expanded_voxtable, voxelData->expanded_voxtable_size);
}
