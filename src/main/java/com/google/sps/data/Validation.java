package com.google.sps.data;

import java.util.Hashtable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import com.google.sps.proto.SimulationTraceProto;
import com.google.sps.proto.SimulationTraceProto.TensorAllocation;

public class Validation {
    ArrayList<SimulationTraceProto.TensorAllocation> tensorAddress = new ArrayList();
   // HashTable<Allocation, new ArrayList<InstructionTag>()> tensorToAddress;
    public static void main(String[] args) {

      SimulationTraceProto.TensorAllocation.Builder alloBuilder = SimulationTraceProto.TensorAllocation.newBuilder();
      SimulationTraceProto.Instruction.Builder instructBuilder = SimulationTraceProto.Instruction.newBuilder();

    //   tensorToInstructions = relateTensorsToInstructions(alloBuilder.getTensorAllocationList(), instructBuilder.getInstructionList());
    }

    // private static HashTable relateTensorsToInstructions(List<TensorAllocation> allocations, List<Instruction> instructions) {
    //     for (TensorAllocation allocation : allocations) {
            
    //         tensorToInstructions

    //     }

    //     for (Instruction instruction : instructionTags) {

    //     }
    // }
    
    public static int[] getAllocationArray(List<TensorAllocation> allocations) {
        int[] memory = new int[128 * 1024];
        
        for (TensorAllocation allocation : allocations) {
            Arrays.fill(memory, allocation.getStartAddress(), allocation.getStartAddress() + allocation.getSize(), allocation.getLabel());
        }


        return memory;
    }
}
