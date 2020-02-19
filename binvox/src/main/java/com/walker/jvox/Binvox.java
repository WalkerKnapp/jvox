package com.walker.jvox;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Binvox {
    public static Voxels parseBinvox(Path binvoxPath) {
        return null;
    }

    public static void generateBinvox(Voxels voxels, Path binvoxPath) throws IOException {
        try(BufferedWriter writer = Files.newBufferedWriter(binvoxPath)) {
            // Write ASCII header
            writer.write("#binvox 1");
            writer.newLine();
            writer.write("dim " + voxels.gridSize + " " + voxels.gridSize + " " + voxels.gridSize);
            writer.newLine();
            writer.write("data");
            writer.newLine();

            // Write Binary Data
            boolean currentValue = false;
            int currentSeen = 1;

            for (int x = 0; x < voxels.gridSize; x++) {
                for (int y = 0; y < voxels.gridSize; y++) {
                    for (int z = 0; z < voxels.gridSize; z++) {
                        // Always write the first voxel
                        if (x == 0 && y == 0 && z == 0) {
                            currentValue = voxels.checkVoxel(0, 0, 0);
                            writer.write(currentValue ? 1 : 0);
                            currentSeen = 1;
                            continue;
                        }

                        // Check to see if voxel is a continuation or if it needs a new byte.
                        boolean nextValue = voxels.checkVoxel(x, y, z);
                        if(nextValue != currentValue || currentSeen == 255) {
                            writer.write(currentSeen);
                            currentSeen = 1;
                            currentValue = nextValue;
                            writer.write(currentValue ? 1 : 0);
                        } else {
                            currentSeen++;
                        }
                    }
                }
            }

            // Flush last amount seen
            writer.write(currentSeen);
        }
    }
}
