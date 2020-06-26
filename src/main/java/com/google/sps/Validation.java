package com.google.sps;

import java.util.HashTable;
import java.util.ArrayList;
import java.util.List;
import com.google.sps.SimulationTraceProto;

public class Validation {
    ArrayList<TensorAllocation> tensorAddress = new ArrayList();
   // HashTable<Allocation, new ArrayList<InstructionTag>()> tensorToAddress;
    public static void main(String[] args) {

      SimulationTraceProto.TensorAllocation.Builder alloBuilder = new SimulationTraceProto.TensorAllocation.Builder;
      SimulationTraceProto.Instruction.Builder instructBuilder = new SimulationTraceProto.Instruction.Builder;

      tensorToInstructions = relateTensorsToInstructions(alloBuilder.getTensorAllocationList(), instructBuilder.getInstructionList());
    }

    private static HashTable relateTensorsToInstructions(List<TensorAllocation> allocations, List<Instruction> instructions) {
        for (TensorAllocation allocation : allocations) {
            
            tensorToInstructions

        }

        for (Instruction instruction : instructionTags) {

        }
    }

    private static int[] getAllocationArray(List<TensorAllocation> allocations) {
        HashTable<int, TensorAllocation> labelToAllocation = new HashTable<int, TensorAllocation>();
        int[] memory = new int[128 * 1024];
        
        for (TensorAllocation allocation : allocations) {
            memory[allocation.getStartAddress()] = allocation.getLabel();
            labelToAllocation.put(allocation.getLabel(), allocation);
        }

        int i = 0;
        int curLabel = -1;
        int curSize = 0;
        int curTensorSize = 0

        while (i < memory.length) {
            if (memory[i] == 0 && curLabel != -1 && curSize < curTensorSize) {
                memory[i] = curLabel;
                i++;
                curSize++;
            }
            else if (memory[i] == 0) {
                i++;
            }
            else {
                curSize = 0;
                curTensorSize = labelToAllocation.get(memory[i]);
                curLabel = memory[i];
                i++;
            }
        }
    
    }
}
