package com.google.sps;

import java.util.HashTable;
import java.util.ArrayList;
import java.util.List;
import com.google.sps.SimulationTraceProto;

public class Main {
    ArrayList<TensorAllocation> tensorAddress = new ArrayList();
   // HashTable<Allocation, new ArrayList<InstructionTag>()> tensorToAddress;
    public static void main(String[] args) {
      System.out.println("Hello world!");
      SimulationTraceProto.TraceAllocation.Builder tensorAllo = new SimulationTraceProto.TraceAllocation.Builder;
      SimulationTraceProto.TraceAllocation.Builder alloBuilder = new SimulationTraceProto.Allocation.Builder;
      SimulationTraceProto.Instruction.Builder instructBuilder = new SimulationTraceProto.Instruction.Builder;

      tensorToInstructions = relateTensorsToInstructions(alloBuilder.getTensorAllocationList(), instructBuilder.getInstructionList());
      
    }

    private static HashTable relateTensorsToInstructions(List<Allocation> allocations, List<InstructionTag> instructionTags) {
        for (Allocation allocation : allocations) {
            
            tensorToInstructions;
            
        }

        for (InstructionTag instruction : instructionTags) {

        }
    }
}
