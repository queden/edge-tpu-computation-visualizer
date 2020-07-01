// comment explanation 
// * tests/conditions to check

package com.google.sps.data;

import com.google.sps.exceptions.*;
import com.google.sps.proto.SimulationTraceProto.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class WriteValidator {
    int[][] narrow = new int[16][128 * 1024];
    int[][] wide = new int[16][256 * 1024];

    public static void writeValidation(
        int[][] narrow, int[][] wide, Map<Integer, Instruction> instructionTagtoInstruction, List<TraceEntry> traces){
        for (int i = 0; i < traces.size(); i++) {
            TraceEntry trace = traces.get(i);
             // get the list of masks for each tile
            List<Boolean> masks = instruction.getMaskList();
            if (trace.getAccessType().toString() == "WRITE_NARROW") {
                // get what the instruction is 
                    // * check for empty / non-existant instruction
                Instruction instruction = instructionTagtoInstruction.get(trace.getInstructionTag()); 
                // itterate through the tiles
                for (int tile = 0; tile < 16; tile++) {
                    if (masks.get(tile)) {
                        // get the tensor name
                            //* empty tensor name
                        MemoryAccess narrowWrite = instruction.getNarrowWrite();
                        int tensor = narrowWrite.getTensor();
                        // write the tensor name in our replicated memory 
                        narrow[tile][trace.getAddress()] = tensor;
                    }
                }
            }
            if (trace.getAccessType().toString() == "WRITE_WIDE") {
                Instruction instruction = instructionTagtoInstruction.get(trace.getInstructionTag());
                for (int tile = 0; tile < 16; tile++) {
                    if (masks.get(tile)) {
                        MemoryAccess narrowWrite = instruction.getWideWrite();
                        int tensor = narrowWrite.getTensor();
                        wide[tile][trace.getAddress()] = tensor;
                    }
                }
            }
        }
    }
}