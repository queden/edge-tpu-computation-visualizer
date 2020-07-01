package com.google.sps.data;

import com.google.sps.exceptions.*;
import com.google.sps.proto.SimulationTraceProto.*;
import java.util.Map;
import java.util.List;

public class TraceIsValid {
    public static int getTracesTensor(int traceAddress, TraceEntry.AccessType traceAccessType, Instruction instruction) {
        MemoryAccess memoryAccess;
        
        if (traceAccessType == TraceEntry.AccessType.READ_NARROW) {
            memoryAccess = instruction.getNarrowRead();
        } else if (traceAccessType == TraceEntry.AccessType.WRITE_NARROW) {
            memoryAccess = instruction.getNarrowWrite();
        } else if (traceAccessType == TraceEntry.AccessType.READ_WIDE) {
            memoryAccess = instruction.getWideRead();
        } else {
            memoryAccess = instruction.getWideWrite();
        }

        int tensor = memoryAccess.getTensor();

        return tensor;
    } 

    public static void validateTraceEntries (List<TraceEntry> traceEntries, Map<Integer, Instruction> instructionTagtoInstruction) throws Exception {
        int[][] narrow = new int[16][128 * 1024];
        int[][] wide = new int[16][256 * 1024];
        
        for (TraceEntry traceEntry : traceEntries) {
            Instruction instruction = instructionTagtoInstruction.get(traceEntry.getInstructionTag());
            List<Boolean> masks = instruction.getMaskList();
            TraceEntry.AccessType accessType = traceEntry.getAccessType();
            int address = traceEntry.getAddress();

            int traceTensor = getTracesTensor(traceEntry.getAddress(), accessType, instruction);
            for (int tile = 0; tile < 16; tile++) {
                if (masks.get(tile)) {
                    Boolean valid;
                    if (accessType == TraceEntry.AccessType.READ_NARROW || accessType == TraceEntry.AccessType.WRITE_NARROW) {
                        valid = narrow[tile][address] == traceTensor;
                    } else {
                        valid = wide[tile][address] == traceTensor;
                    }

                    if (!valid) {
                        throw new Exception("TODO: Write custom exception"); //TODO: Write Custom Exception
                    }
                }
            }
        }
    }
}