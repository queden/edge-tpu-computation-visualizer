package com.google.sps.data;

import com.google.sps.exceptions.*;
import com.google.sps.proto.SimulationTraceProto.*;
import java.util.Map;
import java.util.List;

public class TraceIsValid {

    // Sizes in KB of narrow and wide memory.
    public static final int NARROW_SIZE = 128;
    public static final int WIDE_SIZE = 256;

    // Number of tiles.
    public static final int NUM_TILES = 16;

    // Memory access types.
    public static final String NARROW_READ = "Narrow Read";
    public static final String NARROW_WRITE = "Narrow Write";
    public static final String WIDE_READ = "Wide Read";
    public static final String WIDE_WRITE = "Wide Write";

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

    public static void validateTraceEntries (List<TraceEntry> traceEntries, Map<Integer, Instruction> instructionTagtoInstruction, int[] wideAllocation, int[] narrowAllocation) throws Exception {
        int[][] narrow = new int[NUM_TILES][NARROW_SIZE * 1024];
        int[][] wide = new int[NUM_TILES][WIDE_SIZE * 1024];
        
        for (TraceEntry traceEntry : traceEntries) {
            Instruction instruction = instructionTagtoInstruction.get(traceEntry.getInstructionTag());
            TraceEntry.AccessType accessType = traceEntry.getAccessType();
            int address = traceEntry.getAddress();

            int traceTensor = getTracesTensor(address, accessType, instruction);
            int expectedTensor;

            if (accessType == TraceEntry.AccessType.READ_NARROW || accessType == TraceEntry.AccessType.WRITE_NARROW) {
                expectedTensor = narrowAllocation[address];
            } else {
                expectedTensor = wideAllocation[address];
            }

            if (expectedTensor != traceTensor) {
                throw 
            }
        }
    }
}