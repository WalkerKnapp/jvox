#include "com_walker_jvox_Voxels.h"
#include "voxels.h"

jobject Java_com_walker_jvox_Voxels_getVoxelTableAsBuffer(JNIEnv *env, jclass jClazz, jlong pVoxelData) {
    auto *voxelData = reinterpret_cast<voxeldata *>(pVoxelData);

    return env->NewDirectByteBuffer(voxelData->voxtable, voxelData->voxtable_size);
}
