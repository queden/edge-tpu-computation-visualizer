package com.google.sps.data;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;
import com.google.sps.proto.SimulationTraceProto;

public class Validation {
    ArrayList<SimulationTraceProto.TensorAllocation> tensorAddress = new ArrayList();
   // HashTable<Allocation, new ArrayList<InstructionTag>()> tensorToAddress;
    public static void main(String[] args) {

      SimulationTraceProto.TensorAllocation.Builder alloBuilder = new SimulationTraceProto.TensorAllocation.newBuilder();
      SimulationTraceProto.Instruction.Builder instructBuilder = new SimulationTraceProto.Instruction.newBuilder();

    //   tensorToInstructions = relateTensorsToInstructions(alloBuilder.getTensorAllocationList(), instructBuilder.getInstructionList());
    }

    // private static HashTable relateTensorsToInstructions(List<TensorAllocation> allocations, List<Instruction> instructions) {
    //     for (TensorAllocation allocation : allocations) {
            
    //         tensorToInstructions

    //     }

    //     for (Instruction instruction : instructionTags) {

    //     }
    // }

    private static int[] getAllocationArray(List<TensorAllocation> allocations) {
        Hashtable<Integer, TensorAllocation> labelToAllocation = new Hashtable<Integer, TensorAllocation>();
        int[] memory = new int[128 * 1024];
        
        for (TensorAllocation allocation : allocations) {
            memory[allocation.getStartAddress()] = allocation.getLabel();
            labelToAllocation.put(allocation.getLabel(), allocation);
        }

        int i = 0;
        int curLabel = -1;
        int curSize = 0;
        int curTensorSize = 0;

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

        return memory;
    }
}
