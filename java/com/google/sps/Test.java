import java.util.Map;
import java.util.Hashtable;
import java.util.List;

public class Test {
    Map<Integer, Instruction> instructionTagtoInstruction = new Hashtable<>();

    public static void relateIntructionTagtoInstructionTable(List<Instruction> instructions) {
        for (Instruction instruction : instructions) {
            instructionTagtoInstruction.put(instruction.getTag(), instruction);
        }
    }
}
