package com.walker.jvox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IllegalFormatException;
import java.util.function.Function;

public class Binvox {
    public static Voxels parseBinvox(Path binvoxPath) throws IOException {
        if(CudaVoxels.isSupported()) {
            return parseBinvox(binvoxPath, CudaVoxels::new);
        } else {
            return parseBinvox(binvoxPath, CpuVoxels::new);
        }
    }

    public static Voxels parseBinvox(Path binvoxPath, Function<Integer, ? extends Voxels> voxelCreator) throws IOException {
        try(BufferedReader reader = Files.newBufferedReader(binvoxPath)) {
            if(!reader.readLine().startsWith("#binvox")) {
                throw new IllegalStateException("File is not a binvox file: " + binvoxPath.toString());
            }

            Voxels voxels = null;

            String line;
            while ((line = reader.readLine()) != null) {
                // TODO: Handle translation and scaling
                if(line.startsWith("dim")) {
                    String[] split = line.split(" ");

                    if(split.length < 4) {
                        throw new IllegalStateException("Binvox format improperly formatted: Not enough dim arguments: " + binvoxPath.toString());
                    }

                    int gridSize = Integer.parseInt(split[1]); // TODO: Handle non-cube grids
                    voxels = voxelCreator.apply(gridSize);

                } else if (line.startsWith("data")) {
                    if(voxels == null) {
                        throw new IllegalStateException("Binvox format improperly formatted: No dim line: " + binvoxPath.toString());
                    }

                    ByteBuffer voxelTable = voxels.getExpandedVoxelTable();
                    // TODO: Allow for compressed binvox
                    int count = reader.read(voxelTable.asCharBuffer());
                }
            }

            return voxels;
        }
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
