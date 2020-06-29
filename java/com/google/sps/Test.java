import java.util.Map;
import java.util.Hashtable;
import java.util.List;

public class Test {
    Instruction.Builder instructionBuilder = Instruction.newBuilder();
    Trace.Builder traceBuilder = Trace.newBuilder();

    Map<Integer, Instruction> instructionTagtoInstruction = new Hashtable<>();
    Map<Integer, List<Trace>> instructionTagtoTraces = new Hashtable<>();

    public static void relateIntructionTagtoInstructionTable(List<Instruction> instructions) {
        Instruction.Builder instruction;

        for (Instruction i : instructions) {
            instruction = instructionBuilder.mergeFrom(i);

            instructionTagtoInstruction.put(instruction.getTag(), instruction.build());
        }
    }

    public static void relateInstructionTagtoTracesTable(List<Trace> traces) {
        Trace.Builder trace;

        for (Trace t : traces) {
            trace = traceBuilder.mergeFrom(t);
            int traceTag = trace.getInstructionTag();

            if (instructionTagtoTraces.get(traceTag)) == null) {
                instructionTagtoTraces.put((traceTag), new ArrayList<Trace>(Arrays.asList(trace.build())));
            } else {
                instructionTagtoTraces.get(traceTag).add(trace.build());
            }
        }
    }
}
