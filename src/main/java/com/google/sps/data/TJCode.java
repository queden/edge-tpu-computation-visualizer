package com.google.sps.data;

import java.util.Map;
import java.util.Hashtable;
import java.util.List;
import com.google.sps.proto.SimulationTraceProto;
import com.google.sps.proto.SimulationTraceProto.Instruction;

public class TJCode {
    private static Map<Integer, Instruction> instructionTagtoInstruction = new Hashtable<>();

    public static Map<Integer, Instruction> relateIntructionTagtoInstructionTable(List<Instruction> instructions) {
        // Map<Integer, Instruction> instructionTagtoInstruction = new Hashtable<>();

        for (Instruction instruction : instructions) {
            instructionTagtoInstruction.put(instruction.getTag(), instruction);
        }

        return instructionTagtoInstruction;
    }
}
