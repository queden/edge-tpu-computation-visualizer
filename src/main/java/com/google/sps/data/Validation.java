package com.google.sps.data;

import java.util.Hashtable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import com.google.sps.proto.SimulationTraceProto;
import com.google.sps.proto.SimulationTraceProto.*;

public class Validation {
    
    public static final int NARROW_SIZE = 128;
    public static final int WIDE_SIZE = 256;

    public static void validate(SimulationTrace simulationTrace) {
        List<Instruction> instructions = simulationTrace.getInstructionList();

        int[] narrowAllocation = getAllocationArray(simulationTrace.getTensorAllocationNarrowList(), NARROW_SIZE);
        int[] wideAllocation = getAllocationArray(simulationTrace.getTensorAllocationWideList(), WIDE_SIZE);

        relateTensorsToInstructions(narrowAllocation, wideAllocation, instructions);
    }

    public static void relateTensorsToInstructions(int[] narrowAllocation, int[] wideAllocation, List<Instruction> instructions) {
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            Instruction.Builder instructionBuilder = Instruction.newBuilder();
            instructionBuilder.mergeFrom(instruction);

            MemoryAccess.Builder memoryAccessBuilder = MemoryAccess.newBuilder();

            if (instruction.hasNarrowRead()) {
                memoryAccessBuilder.mergeFrom(instruction.getNarrowRead());
                int tensor = narrowAllocation[memoryAccessBuilder.getBaseAddress()];
                if (tensor >= 0) {
                    memoryAccessBuilder.setTensor(tensor);
                    instructionBuilder.setNarrowRead(memoryAccessBuilder.build());
                }
                else {
                    memoryAccessBuilder.clear();
                }
            }
            if (instruction.hasNarrowWrite()) {
                memoryAccessBuilder.mergeFrom(instruction.getNarrowWrite());
                int tensor = narrowAllocation[memoryAccessBuilder.getBaseAddress()];
                if (tensor >= 0) {
                    memoryAccessBuilder.setTensor(tensor);
                    instructionBuilder.setNarrowWrite(memoryAccessBuilder.build());
                }
                else {
                    memoryAccessBuilder.clear();
                }
            }
            if (instruction.hasWideRead()) {
                memoryAccessBuilder.mergeFrom(instruction.getWideRead());
                int tensor = wideAllocation[memoryAccessBuilder.getBaseAddress()];
                if (tensor >= 0) {
                    memoryAccessBuilder.setTensor(tensor);
                    instructionBuilder.setWideRead(memoryAccessBuilder.build());
                }
                else {
                    memoryAccessBuilder.clear();
                }   
            }
            if (instruction.hasWideWrite()) {
                memoryAccessBuilder.mergeFrom(instruction.getWideWrite());
                int tensor = wideAllocation[memoryAccessBuilder.getBaseAddress()];
                if (tensor >= 0) {
                    memoryAccessBuilder.setTensor(tensor);
                    instructionBuilder.setWideWrite(memoryAccessBuilder.build());
                }
                else {
                    memoryAccessBuilder.clear();
                }
            }
            instructions.set(i, instructionBuilder.build());
        }
    }

    public static int[] getAllocationArray(List<TensorAllocation> allocations, int memorySize) {
        int[] memory = new int[memorySize * 1024];

        Arrays.fill(memory, -1);
        
        for (TensorAllocation allocation : allocations) {
            Arrays.fill(memory, allocation.getStartAddress(), allocation.getStartAddress() + allocation.getSize(), allocation.getLabel());
        }

        return memory;
    }
}
