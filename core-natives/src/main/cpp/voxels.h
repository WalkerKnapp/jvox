#ifndef JVOX_VOXELS_H
#define JVOX_VOXELS_H

struct voxeldata {
    size_t voxtable_size {};
    unsigned int *voxtable {};

    bool externally_managed = false;
};

#endif //JVOX_VOXELS_H
