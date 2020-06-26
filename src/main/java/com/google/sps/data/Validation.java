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


    public static void main(String[] args) {
        // TensorAllocation.Builder allocationBuilderNarrow = SimulationTraceProto.TensorAllocation.newBuilder();
        // TensorAllocation.Builder allocationBuilderWide = SimulationTraceProto.TensorAllocation.newBuilder();

        // TensorAllocation allocation1 = allocationBuilderNarrow.setLabel(1).setStartAddress(8).setSize(1000).build(); 
        // TensorAllocation allocation2 = allocationBuilderNarrow.setLabel(2).setStartAddress(95678).setSize(216).build();


        // Instruction.Builder instructionBuilder = SimulationTraceProto.Instruction.newBuilder();
        // MemoryAccess memAcc1 = MemoryAccess.newBuilder().setBaseAddress 

        // SimulationTrace.Builder simulationTraceBuilder = SimulationTrace.newBuilder();
        // simulationTraceBuilder.addTensorAllocationNarrow(allocation1);
        // simulationTraceBuilder.addTensorAllocationNarrow(allocation2);
        // simulationTraceBuilder.addTensorAllocationNarrow(allocation1);
        // simulationTraceBuilder.addTensorAllocationNarrow(allocation2);

        // validate(simulationTraceBuilder.build());

    //   tensorToInstructions = relateTensorsToInstructions(alloBuilder.getTensorAllocationList(), instructBuilder.getInstructionList());
    }

    public static void validate(SimulationTrace simulationTrace) {
        // SimulationTraceProto.TensorAllocation.Builder alloBuilder = SimulationTraceProto.TensorAllocation.newBuilder();
        // SimulationTraceProto.Instruction.Builder instructBuilder = SimulationTraceProto.Instruction.newBuilder();

        List<Instruction> instructions = simulationTrace.getInstructions();

        int[] narrowAllocation = getAllocationArray(simulationTrace.getTensorAllocationNarrowList(), NARROW_SIZE);
        int[] wideAllocation = getAllocationArray(simulationTrace.getTensorAllocationWideList(), WIDE_SIZE);

        relateTensorsToInstructions(narrowAllocation, wideAllocaiton, instructions);
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
                memoryAccessBuilder.setTensor(tensor);
                instructionBuilder.setNarrowRead(memoryAccessBuilder.build());
            }
            if (instruction.hasNarrowWrite()) {
                memoryAccessBuilder.mergeFrom(instruction.getNarrowWrite());
                int tensor = narrowAllocation[memoryAccessBuilder.getBaseAddress()];
                memoryAccessBuilder.setTensor(tensor);
                instructionBuilder.setNarrowWrite(memoryAccessBuilder.build());
            }
            if (instruction.hasWideRead()) {
                memoryAccessBuilder.mergeFrom(instruction.getWideRead());
                int tensor = wideAllocation[memoryAccessBuilder.getBaseAddress()];
                memoryAccessBuilder.setTensor(tensor);
                instructionBuilder.setWideRead(memoryAccessBuilder.build());
            }
            if (instruction.hasWideWrite()) {
                memoryAccessBuilder.mergeFrom(instruction.getWideWrite());
                int tensor = wideAllocation[memoryAccessBuilder.getBaseAddress()];
                memoryAccessBuilder.setTensor(tensor);
                instructionBuilder.setWideWrite(memoryAccessBuilder.build());
            }

            instructions.set(i, instructionBuilder.build());
        }
    }

    public static int[] getAllocationArray(List<TensorAllocation> allocations, int memorySize) {
        int[] memory = new int[memorySize * 1024];
        
        for (TensorAllocation allocation : allocations) {
            Arrays.fill(memory, allocation.getStartAddress(), allocation.getStartAddress() + allocation.getSize(), allocation.getLabel());
        }

        return memory;
    }
}
