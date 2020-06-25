package com.google.sps;

import java.util.HashTable;
import java.util.ArrayList;
import java.util.List;
import com.google.sps.ValidationProto;

public class Main {
    HashTable<Allocation, new ArrayList<InstructionTag>()> tensorToInstructions;
    public static void main(String[] args) {
      System.out.println("Hello world!");

      ValidationProto.Allocation.Builder alloBuilder = new ValidationProto.Allocation.Builder;
      ValidationProto.InstructionTag.Builder instructBuilder = new ValidationProto.InstructionTag.Builder;

      tensorToInstructions = relateTensorsToInstructions(alloBuilder.getTensorAllocationList(), instructBuilder.getInstructionTagList());
    }

    private static HashTable relateTensorsToInstructions(List<Allocation> allocations, List<InstructionTag> instructionTags) {
        for (Allocation allocation : allocations) {
            tensorToInstructions
        }

        for (InstructionTag instruction : instructionTags) {

        }
    }
}
