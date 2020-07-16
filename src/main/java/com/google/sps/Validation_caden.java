// package com.google.sps;

// import com.google.sps.exceptions.*;
// import com.google.sps.results.*;
// import com.google.sps.proto.SimulationTraceProto.*;
// import java.util.Arrays;
// import java.util.ArrayList;
// import java.util.Hashtable;
// import java.util.List;
// import java.util.Map;

// public class Validation {
//   private static SimulationTrace simulationTrace;

//   private static ArrayList<Instruction> instructions;
//   private static Map<Integer, Instruction> instructionTagtoInstruction;
//   private static List<TraceEvent> traceEvents;

//   private static int[][] narrow;
//   private static int[][] wide;

//   public static int narrowSize;
//   public static int wideSize;

//   public static int numTiles;

//   // Memory access types.
//   public static final String NARROW_READ = "Narrow Read";
//   public static final String NARROW_WRITE = "Narrow Write";
//   public static final String WIDE_READ = "Wide Read";
//   public static final String WIDE_WRITE = "Wide Write";

//   public Validation(SimulationTrace simulationTrace) {
//     this.simulationTrace = simulationTrace;
//     traceEvents = simulationTrace.getTraceEventList();
//     instructions = new ArrayList<Instruction>();

//     narrowSize = simulationTrace.getNarrowMemorySizeBytes();
//     wideSize = simulationTrace.getWideMemorySizeBytes();
//     numTiles = simulationTrace.getNumTiles();

//     narrow = new int[numTiles][narrowSize];
//     wide = new int[numTiles][wideSize];

//     instructionTagtoInstruction = new Hashtable<Integer, Instruction>();
//   }

//   public static PreProcessResults preProcess() {
//     boolean isError = false;
//     String message = "Preprocessing completed successfully.";

//     instructions.addAll(simulationTrace.getInstructionList());

//     try {
//       relateTensorsToInstructions();
//     } catch (InvalidTensorAddressException e) {
//       if (!isError) {
//         message = e.getMessage();
//         isError = true;
//       }    
//     } catch (ArrayIndexOutOfBoundsException e) {
//       if (!isError) {
//         message = e.getMessage();
//         isError = true;
//       }    
//     }

//     relateIntructionTagtoInstructionTable();

//     return new PreProcessResults(isError, message, traceEvents.size());
//   }

//   public static ProcessResults process(long start, long end) {
//     try {
//       validateTraceEvents(start, end);
//     } catch (Exception e) {
//       return new ProcessResults(e, narrow, wide);
//     }

//     return new ProcessResults(null, narrow, wide);
//   }

//   /**
//    * Given an array showing the narrow and wide tensor allocations in memory, populates each
//    * instruction with the tensor that they operate on. Throws a InvalidTensorAddressException if the
//    * instruction operates on a memory address that does not hold a tensor.
//    */
//   private static void relateTensorsToInstructions()
//       throws InvalidTensorAddressException {
    
//   }

//   /** Given a list of instructions, maps each instruction tag to its corresponding instruction. */
//   private static void relateIntructionTagtoInstructionTable() {
//     for (Instruction instruction : instructions) {
//       int instructionTag = instruction.getTag();
//       System.out.println("instruction tag is " + instructionTag);
//       instructionTagtoInstruction.put(instructionTag, instruction);
//     }
//   }

//   /**
//    * Given a list of trace entries, validates that trace entries proceeded in the right order and
//    * operated on the correct traces.
//    */
//   private static void validateTraceEvents(long start, long end)
//       throws MemoryAccessException, InvalidTensorOperationException, InvalidMaskException, InvalidTensorReadException, Exception {
//     // Arrays to simulate the narrow and wide memories for each tile.
//     if (traceEvents.isEmpty()) {
//         throw new Exception(
//             "No trace entry to be validated "); 
//     }
//     // Iterates over each trace entry, ensures that it is operating on the correct tensor and
//     // validates based on if it is a write or a read.

//     long bound = (end <= traceEvents.size()) ? end : traceEvents.size();

//     for (long i = start; i < bound; i++) {
//       TraceEvent traceEvent = traceEvents.get((int) i);
//       // Gets the trace entries corresponding instruction and ensures it exists.
//       Instruction instruction = instructionTagtoInstruction.get(traceEvent.getInstructionTag());
//       if (instruction == null) {
//         throw new Exception(
//             "Instruction with key "
//                 + traceEvent.getInstructionTag()
//                 + " does not exist."); // TODO: May need to write custom exception
//       }

//       TraceEvent.AccessType accessType = traceEvent.getAccessType();
//       if (!traceEvent.hasAddress()) {
//         throw new Exception(
//           "Trace with access type " 
//             + traceEvent.getAccessType() 
//             + " and instruction " 
//             + traceEvent.getInstructionTag()
//             + " has no memory address associated with it.");
//        }
//       int address = traceEvent.getAddress();
//       int traceTensor = -1;

//       traceTensor = getTraceTensor(address, accessType, instruction);

//       int expectedTensor;

//       // Gets the expected tensor that the trace entry should be operating on based on which memory
//       // it accesses.
//       if (accessType == TraceEvent.AccessType.READ_NARROW
//           || accessType == TraceEvent.AccessType.WRITE_NARROW) {
//         expectedTensor = narrowAllocation[address];
//       } else {
//         expectedTensor = wideAllocation[address];
//       }

//       // If the trace is operating on the wrong tensor, throws an exception.
//       if (expectedTensor != traceTensor) {
//         throw new InvalidTensorOperationException(
//             address, expectedTensor, traceTensor, traceEvent.getInstructionTag(), accessType);
//       }

//       List<Boolean> masks = instruction.getMaskList();
//       if (masks.isEmpty()){
//           throw new InvalidMaskException(traceEvent.getInstructionTag(), traceEvent.getAccessType());
//       }
      
//       // If the trace entry is a write, performs a write validation. If it a read, performs a read
//       // validation.
//       if (accessType == TraceEvent.AccessType.WRITE_NARROW
//           || accessType == TraceEvent.AccessType.WRITE_WIDE) {
//         writeValidation(narrow, wide, masks, traceTensor, traceEvent);
//       } else if (accessType == TraceEvent.AccessType.READ_NARROW
//           || accessType == TraceEvent.AccessType.READ_WIDE) {
//         readValidation(narrow, wide, masks, traceTensor, traceEvent);
//       }
//     }
//   }

//   /**
//    * Returns the tensor that the trace entry is operating on based on its corresponding instruction.
//    */
//   private static int getTraceTensor(
//       int traceAddress, TraceEvent.AccessType traceAccessType, Instruction instruction)
//       throws Exception, MemoryAccessException {
//     TensorAccess tensorAccess = null;

//     // Tracks if the corresponding instruction has the trace entry's access type.
//     Boolean hasAccessType = true;

//     if (traceAccessType == TraceEvent.AccessType.READ_NARROW) {
//       if (instruction.hasNarrowRead()) {
//         tensorAccess = instruction.getNarrowRead();
//       } else {
//         hasAccessType = false;
//       }
//     } else if (traceAccessType == TraceEvent.AccessType.WRITE_NARROW) {
//       if (instruction.hasNarrowWrite()) { 
//         tensorAccess = instruction.getNarrowWrite();
//       } else {
//         hasAccessType = false;
//       }
//     } else if (traceAccessType == TraceEvent.AccessType.READ_WIDE) {
//       if (instruction.hasWideRead()) {
//         tensorAccess = instruction.getWideRead();
//       } else {
//         hasAccessType = false;
//       }
//     } else if (traceAccessType == TraceEvent.AccessType.WRITE_WIDE) {
//       if (instruction.hasWideWrite()) {
//         tensorAccess = instruction.getWideWrite();
//       } else {
//         hasAccessType = false;
//       }
//     } else {
//       throw new Exception(
//           "Trace entry at address " 
//             + traceAddress 
//             + " has invalid access type of " 
//             + traceAccessType 
//             + ".");
//     }

//     // Throws MemoryAccessException if instruction does not have the expected
//     // access type.
//     if (!hasAccessType) {
//       throw new MemoryAccessException(traceAccessType, instruction.getTag());
//     }

//     // Gets the corresponding tensor. Throws an exception if there is no tensor
//     // associated with the correct access type.
//     int tensor = -1;
//     if (tensorAccess.hasTensor()){
//       tensor = tensorAccess.getTensor();  
//     } 
//     else {
//       throw new Exception(
//           "Memory Access " 
//             + tensorAccess.getDescriptorForType().getName() 
//             + " for instruction " 
//             + instruction.getTag() 
//             + " has no tensor associated with it.");
//     }

//     return tensor;
//   }

//   /**
//    * Validates that the write validation has a corresponding tensor and writes it to the correct
//    * address in the memory arrays.
//    */
//   private static void writeValidation(
//       int[][] narrow, int[][] wide, List<Boolean> masks, int tensor, TraceEvent traceEvent) {  
//     int address = traceEvent.getAddress();
//     if (traceEvent.getAccessType() == TraceEvent.AccessType.WRITE_NARROW) {
//       // Iterate through the tiles.
//       for (int tile = 0; tile < numTiles; tile++) {
//         if (masks.get(tile)) {
//           // Write the tensor name in our replicated memory.
//           narrow[tile][address] = tensor;
//         }
//           }
//         }
//     if (traceEvent.getAccessType() == TraceEvent.AccessType.WRITE_WIDE) {
//       for (int tile = 0; tile < numTiles; tile++) {
//         if (masks.get(tile)) {
//           wide[tile][address] = tensor;
//         }
//       }
//     }
//   }

//   /**
//    * Validates that the tensor that the read trace entry is reading has been written before the read
//    * occurs.
//    */
//   private static void readValidation(
//       int[][] narrow, int[][] wide, List<Boolean> masks, int tensor, TraceEvent traceEvent)
//       throws InvalidTensorReadException {
//     int address = traceEvent.getAddress();

//     if (traceEvent.getAccessType() == TraceEvent.AccessType.READ_NARROW) {
//       for (int tile = 0; tile < numTiles; tile++) {
//         if (masks.get(tile)) {
//           if (narrow[tile][address] != tensor) {
//             throw new InvalidTensorReadException(
//                 tensor, tile, address, narrow[tile][address], "narrow");
//           }
//         }
//       }
//     } else if (traceEvent.getAccessType() == TraceEvent.AccessType.READ_WIDE) {
//       for (int tile = 0; tile < numTiles; tile++) {
//         if (masks.get(tile)) {
//           if (wide[tile][address] != tensor) {
//             throw new InvalidTensorReadException(
//                 tensor, tile, address, wide[tile][address], "wide");
//           }
//         }
//       }
//     }
//   }
// }

