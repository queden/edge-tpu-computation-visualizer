package com.google.sps;

import java.util.HashTable;
import java.util.ArrayList;
import java.util.List;
import com.google.sps.ValidationProto;

public class Main {
    HashTable<Allocation, new ArrayList<InstructionTag>()> tensorToInstructions;
    public static void main(String[] args) {
      System.out.println("Hello world!");

      ValidationProto.TensorAllocation.Builder alloBuilder = new ValidationProto.TensorAllocation.Builder;
      ValidationProto.Instruction.Builder instructBuilder = new ValidationProto.Instruction.Builder;

      tensorToInstructions = relateTensorsToInstructions(alloBuilder.getTensorAllocationList(), instructBuilder.getInstructionList());
    }

    private static HashTable relateTensorsToInstructions(List<TensorAllocation> allocations, List<Instruction> instructions) {
        for (TensorAllocation allocation : allocations) {
            tensorToInstructions
        }

        for (Instruction instruction : instructionTags) {

        }
    }
}
