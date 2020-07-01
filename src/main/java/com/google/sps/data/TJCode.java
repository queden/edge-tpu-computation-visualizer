package com.google.sps.data;

import java.util.Arrays;
import java.util.Map;
import java.util.Hashtable;
import java.util.List;
import com.google.sps.proto.SimulationTraceProto;
import com.google.sps.proto.SimulationTraceProto.Instruction;
import com.google.sps.proto.SimulationTraceProto.TraceEntry;
import com.google.sps.proto.SimulationTraceProto.MemoryAccess;
import com.google.sps.exceptions.*;

public class TJCode {
    // Must be commented out for testing purposes
    // private static Map<Integer, Instruction> instructionTagtoInstruction = new Hashtable<>();

    public static Map<Integer, Instruction> relateIntructionTagtoInstructionTable(List<Instruction> instructions) {
        // Must be un-commented for testing purposes
        Map<Integer, Instruction> instructionTagtoInstruction = new Hashtable<>();

        for (Instruction instruction : instructions) {
            instructionTagtoInstruction.put(instruction.getTag(), instruction);
        }

        return instructionTagtoInstruction;
    }

    private static int[][] narrowMemory = new int[16][128 * 1024];
    private static int[][] wideMemory = new int[16][256 * 1024];

    public static void readValidation(List<TraceEntry> traces, Map<Integer, Instruction> tagToInstructions) throws InvalidTensorReadException {
        Boolean[] maskList;
        MemoryAccess memoryAccess;
        Instruction instruction;
        int tensor;
        int address;

        for (TraceEntry trace : traces) {
            instruction = tagToInstructions.get(trace.getInstructionTag());

            if (trace.getAccessType() == TraceEntry.AccessType.READ_NARROW) {
                memoryAccess = instruction.getNarrowRead();
                maskList = (Boolean[]) Arrays.asList(instruction.getMaskList()).toArray();

                for (int tile = 0; tile < 16; tile++) {
                    if (maskList[tile]) {
                        tensor = memoryAccess.getTensor();
                        address = trace.getAddress();

                        if (narrowMemory[tile][address] != tensor) {
                            throw new InvalidTensorReadException(tensor, tile, address, narrowMemory[tile][address], "narrow");
                        }
                    }
                }
            } else if (trace.getAccessType() == TraceEntry.AccessType.READ_WIDE) {
                memoryAccess = instruction.getWideRead();
                maskList = (Boolean[]) Arrays.asList(instruction.getMaskList()).toArray();

                for (int tile = 0; tile < 16; tile++) {
                    if (maskList[tile]) {
                        tensor = memoryAccess.getTensor();
                        address = trace.getAddress();

                        if (wideMemory[tile][address] != tensor) {
                            throw new InvalidTensorReadException(tensor, tile, address, wideMemory[tile][address], "wide");
                        }
                    }
                }
            }
        }
    }
}
