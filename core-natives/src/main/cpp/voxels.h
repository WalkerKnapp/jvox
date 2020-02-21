#ifndef JVOX_VOXELS_H
#define JVOX_VOXELS_H

#include <Vec.h>

struct voxeldata {
    size_t compressed_voxtable_size {};
    unsigned int *compressed_voxtable {};
    bool compressed_voxtable_externally_managed = false;

    size_t expanded_voxtable_size {};
    unsigned int *expanded_voxtable {};
    bool expanded_voxtable_externally_managed = false;
};

inline size_t get_expanded_voxtable_size(int gridSize) {
    return static_cast<size_t>(ceil(static_cast<size_t>(gridSize)* static_cast<size_t>(gridSize)* static_cast<size_t>(gridSize)));
}

inline size_t get_compressed_voxtable_size(int gridSize) {
    return get_expanded_voxtable_size(gridSize) / 8.0f;
}

inline void expand_compressed_voxtable(voxeldata *voxData) {
    for(int i = 0; i < (voxData->compressed_voxtable_size/sizeof(unsigned int)); i++) {
        for (int bit_pos = 31; bit_pos >= 0; bit_pos--) {
            voxData->expanded_voxtable[(i * 32) + bit_pos] = voxData->compressed_voxtable[i] & (1 << bit_pos);
        }
    }
}

inline void compress_expanded_voxtable(voxeldata *voxData) {
    for(int i = 0; i < (voxData->compressed_voxtable_size/sizeof(unsigned int)); i++) {
        for (int bit_pos = 31; bit_pos >= 0; bit_pos--) {
            voxData->compressed_voxtable[i] |= voxData->expanded_voxtable[(i * 32) + bit_pos];
        }
    }
}

#endif //JVOX_VOXELS_H
