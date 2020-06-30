// comment explanation 
// * tests/conditions to check

package com.google.sps.data;

import com.google.sps.exceptions.*;
import com.google.sps.proto.SimulationTraceProto.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class WriteValidation {
    int[][] narrow = new int[16][128 * 1024];
    int[][] wide = new int[16][256 * 1024];
    public static void writeValidation(
        int[][] narrow, int[][] wide, List<TraceEntry> traces, List<Instruction> instructions, Map<Integer, Instruction> instructionTagtoInstruction){
        for (trace : traces) {
            if (trace.getAccessType() == WRITE_NARROW) {
                // get what the instruction is 
                    // * check for empty / non-existant instruction
                Instruction instruction = instructionTagtoInstruction.get(trace.getInstructionTag());
                // get the list of masks for each tile
                List<Boolean> masks = instruction.getMaskList();
                // itterate through the tiles
                for (int tile = 0; tile < 16; tile++) {
                    if (masks[tile]) {
                        // get the tensor name
                            //* empty tensor name
                        MemoryAccess narrowWrite = instruction.getNarrowWrite();
                        int tensor = narrowWrite.getTensor();
                        // write the tensor name in our replicated memory 
                        narrow[tile][trace.getAddress()] = tensor;
                    }
                }
            }
            if (trace.getAccessType() == WRITE_WIDE) {
                Instruction instruction = instructionTagtoInstruction.get(trace.getInstructionTag());
                List<Boolean> masks = instruction.getMaskList();
                for (int tile = 0; tile < 16; tile++) {
                    if (masks[tile]) {
                        MemoryAccess narrowWrite = instruction.getWideWrite();
                        int tensor = narrowWrite.getTensor();
                        narrow[tile][trace.getAddress()] = tensor;
                    }
                }
            }
        }
    }
}