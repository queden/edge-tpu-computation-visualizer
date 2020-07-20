package com.google.sps;

import com.google.sps.exceptions.*;
import com.google.sps.proto.SimulationTraceProto.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ValidationNew {
  private static MemaccessCheckerData memaccessCheckerData;

  private static ArrayList<Instruction> instructions;
  private static int[] narrowAllocation;
  private static int[] wideAllocation;
  private static Map<Integer, Instruction> instructionTagtoInstruction;
  private static Map<Integer, TensorAllocation> tensorLabelToTensorAllocationNarrow;
  private static Map<Integer, TensorAllocation> tensorLabelToTensorAllocationWide;
  private static List<TraceEvent> traceEvents;

  public ValidationNew(MemaccessCheckerData memaccessCheckerData) {
    this.memaccessCheckerData = memaccessCheckerData;
    traceEvents = memaccessCheckerData.getTraceEventsList();
    instructions = new ArrayList<Instruction>();
    tensorLabelToTensorAllocationNarrow = new Hashtable<Integer, Instruction>();
    tensorLabelToTensorAllocationWide = new Hashtable<Integer, Instruction>();
  }

  // Sizes in KB of narrow and wide memory.
  public static final int NARROW_SIZE = 128;
  public static final int WIDE_SIZE = 256;

  // Number of tiles.
  public static final int NUM_TILES = 16;

  // Memory access types.
  public static final String NARROW_READ = "Narrow Read";
  public static final String NARROW_WRITE = "Narrow Write";
  public static final String WIDE_READ = "Wide Read";
  public static final String WIDE_WRITE = "Wide Write";

  public static void preProcess() {
    instructions.addAll(memaccessCheckerData.getInstructionList());

    // TODO: May need to catch ArrayIndexOutOfBoundsException
    narrowAllocation =
        getAllocationArray(memaccessCheckerData.getTensorAllocationNarrowList(), NARROW_SIZE);
    wideAllocation =
        getAllocationArray(memaccessCheckerData.getTensorAllocationWideList(), WIDE_SIZE);

    try {
      relateTensorsToInstructions();
    } catch (InvalidTensorAddressException e) {
      System.out.println(e.getMessage());
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println(e.getMessage());
    }

    relateIntructionTagtoInstructionTable();

    tensorLabelToTensorAllocationNarrow = relateTensorLabelToTensorAllocation(
        MemaccessCheckerData.getTensorLayerAllocationNarrowList());
    tensorLabelToTensorAllocationWide = relateTensorLabelToTensorAllocation(
        MemaccessCheckerData.getTensorLayerAllocationWideList());

  }

  public static void process(long start, long end) {
    try {
      validateTraceEvents(start, end);
    } catch (InvalidTensorOperationException e) {
      System.out.println(e.getMessage());
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    System.out.println("Simulation complete");
  }


  /**
   * Given an array showing the narrow and wide tensor allocations in memory, populates each
   * instruction with the tensor that they operate on. Throws a InvalidTensorAddressException if the
   * instruction operates on a memory address that does not hold a tensor.
   */
   //================================
   //!!!!!!!!!CADEN'S PART!!!!!!!!!!DON'T TOUCH!!!!!!!!!!!!
//   public static void relateTensorsToInstructions()
//       throws InvalidTensorAddressException {
//     // Loops over each instruction and fills in the tensor field for the instruction's operations.
//     for (int i = 0; i < instructions.size(); i++) {
//       // Merges instruction into a builder to allow changes to the tensor field.
//       Instruction instruction = instructions.get(i);
//       Instruction.Builder instructionBuilder = Instruction.newBuilder();
//       instructionBuilder.mergeFrom(instruction);

//       MemoryAccess.Builder memoryAccessBuilder = MemoryAccess.newBuilder();

//       // Looks at each of the possible memory accesses for the instruction.
//       if (instruction.hasNarrowRead()) {
//         memoryAccessBuilder.mergeFrom(instruction.getNarrowRead());

//         int baseAddress = memoryAccessBuilder.getBaseAddress();
//         int tensor = narrowAllocation[baseAddress];

//         // If there is a tensor at the location, add it to the instruction. Otherwise,
//         // throw an exception.
//         if (tensor >= 0) {
//           memoryAccessBuilder.setTensor(tensor);
//           instructionBuilder.setNarrowRead(memoryAccessBuilder.build());
//         } else {
//           throw new InvalidTensorAddressException(
//               baseAddress, instructionBuilder.getTag(), NARROW_READ);
//         }
//       }

//       if (instruction.hasNarrowWrite()) {
//         memoryAccessBuilder.mergeFrom(instruction.getNarrowWrite());

//         int baseAddress = memoryAccessBuilder.getBaseAddress();
//         int tensor = narrowAllocation[memoryAccessBuilder.getBaseAddress()];

//         if (tensor >= 0) {
//           memoryAccessBuilder.setTensor(tensor);
//           instructionBuilder.setNarrowWrite(memoryAccessBuilder.build());
//         } else {
//           throw new InvalidTensorAddressException(
//               baseAddress, instructionBuilder.getTag(), NARROW_WRITE);
//         }
//       }

//       if (instruction.hasWideRead()) {
//         memoryAccessBuilder.mergeFrom(instruction.getWideRead());

//         int baseAddress = memoryAccessBuilder.getBaseAddress();
//         int tensor = wideAllocation[memoryAccessBuilder.getBaseAddress()];

//         if (tensor >= 0) {
//           memoryAccessBuilder.setTensor(tensor);
//           instructionBuilder.setWideRead(memoryAccessBuilder.build());
//         } else {
//           throw new InvalidTensorAddressException(
//               baseAddress, instructionBuilder.getTag(), WIDE_READ);
//         }
//       }

//       if (instruction.hasWideWrite()) {
//         memoryAccessBuilder.mergeFrom(instruction.getWideWrite());

//         int baseAddress = memoryAccessBuilder.getBaseAddress();
//         int tensor = wideAllocation[memoryAccessBuilder.getBaseAddress()];

//         if (tensor >= 0) {
//           memoryAccessBuilder.setTensor(tensor);
//           instructionBuilder.setWideWrite(memoryAccessBuilder.build());
//         } else {
//           throw new InvalidTensorAddressException(
//               baseAddress, instructionBuilder.getTag(), WIDE_WRITE);
//         }
//       }

//       instructions.set(i, instructionBuilder.build());
//     }
//   }

   /**
   * Given the tensor allocations and the size of the corresponding memory, returns an array
   * representing that memory. -1 array entries represent no tensor present at that location, values
   * >= 0 represent a tensor with a label of that value.
   */ 
   // ====================Unnecessary======================
//   public static int[] getAllocationArray(List<TensorAllocation> allocations, int memorySize) {
//     int[] memory = new int[memorySize * 1024];
//     Arrays.fill(memory, -1);
//     for (TensorAllocation allocation : allocations) {
//       Arrays.fill(
//           memory,
//           allocation.getStartAddress(),
//           allocation.getStartAddress() + allocation.getSize(),
//           allocation.getLabel());
//     }

//     return memory;
//   }

  /** Given a list of instructions, maps each instruction tag to its corresponding instruction. */
  public static void relateIntructionTagtoInstructionTable() {
    for (Instruction instruction : instructions) {
      instructionTagtoInstruction.put(instruction.getTag(), instruction);
    }
  }

  /**
   * Given a list of trace entries, validates that trace entries proceeded in the right order and
   * operated on the correct traces.
   */
  public static void validateTraceEvents(long start, long end)
      throws Exception, InvalidTensorOperationException {
    // Arrays to simulate the narrow and wide memories for each tile.
    int[][] narrow = new int[NUM_TILES][NARROW_SIZE * 1024];
    int[][] wide = new int[NUM_TILES][WIDE_SIZE * 1024];
    if (traceEvents.isEmpty()) {
        throw new Exception(
            "No trace entry to be validated "); 
      }
    // Iterates over each trace entry, ensures that it is operating on the correct tensor and
    // validates based on if it is a write or a read.
    for (long i = start; i < end; i++) {
      TraceEvent traceEvent = traceEvents.get((int) i);
      // Gets the trace event's corresponding instruction and ensures it exists.
      Instruction instruction = instructionTagtoInstruction.get(traceEvent.getInstructionTag());
      if (instruction == null) {
        throw new Exception(
            "Instruction with key "
                + traceEvent.getInstructionTag()
                + " does not exist."); // TODO: May need to write custom exception
      }

      TraceEvent.AccessType accessType = traceEvent.getAccessType();
      int address = traceEvent.getAddress();
    
      int traceTensor = -1;
      // try catch would not be necessary because a default value of 0 will be set -- catch the unexpected tensor 
      try {
        traceTensor = getTraceTensor(address, accessType, instruction);
      } catch(Exception e) {
        System.out.println(e.getMessage());
      }
      int expectedTensor;

      // Gets the expected tensor that the trace entry should be operating on based on which memory
      // it accesses.
      if (accessType == TraceEvent.AccessType.READ_NARROW
          || accessType == TraceEvent.AccessType.WRITE_NARROW) {
        expectedTensor = narrowAllocation[address];
      } else {
        expectedTensor = wideAllocation[address];
      }

      // If the trace is operating on the wrong tensor, throws an exception.
      if (expectedTensor != traceTensor) {
        throw new InvalidTensorOperationException(
            address, expectedTensor, traceTensor, traceEvent.getInstructionTag(), accessType);
      }

      List<Boolean> masks = instruction.getMaskList();
      if (masks.isEmpty()){
          throw new InvalidMaskException(traceEvent.getInstructionTag(), traceEvent.getAccessType());
      }
      
      // If the trace entry is a write, performs a write validation. If it a read, performs a read
      // validation.
      if (accessType == TraceEvent.AccessType.WRITE_NARROW
          || accessType == TraceEvent.AccessType.WRITE_WIDE) {
          writeValidation(narrow, wide, masks, traceTensor, traceEvent);
      } else if (accessType == TraceEvent.AccessType.READ_NARROW
          || accessType == TraceEvent.AccessType.READ_WIDE) {
        try {
          readValidation(narrow, wide, masks, traceTensor, traceEvent);
        } catch (InvalidTensorReadException e) {
          System.out.println(e.getMessage());
        }
      }
    }
  }
  
  public static void relateTensorLabelToTensorAllocation(List <TensorLayerAllocationTable> TensorLayAllocs) {
    for (TensorLayerAllocationTable TensorLayAlloc : TensorLayAllocs) {
      List <TensorTileAllocationTable> TensorTileAllocs = TensorLayAlloc.getTensorTileAllocation();
      for (TensorTileAllocationTable TensorTileAlloc : TensorTileAllocs){
          List <TensorAllocation> tensorAllocs = TensorTileAlloc.getTensorAllocation();
          for (TensorAllocation tensorAlloc : tensorAllocs){
              tensorLabelToTensorAllocation.put(tensorAlloc.getTensorLabel(), tensorAlloc);
          }
      }  
    }
  }

  /**
   * Returns the tensor that the trace entry is operating on based on its corresponding instruction.
   */  
  public static int getTraceTensor(
      int traceAddress, TraceEvent.AccessType traceAccessType, Instruction instruction)
      throws Exception, MemoryAccessException {
    List<Integer> AccessTypeTensorList;
    int tensor;
    // Tracks if the corresponding instruction has the trace entry's access type.
    Boolean hasAccessType = true;

    if (traceAccessType == TraceEvent.AccessType.READ_NARROW) {
      if (instruction.getNarrowReadCount() != 0) {
        AccessTypeTensorList = instruction.getNarrowReadList();
        tensor = getTensor(AccessTypeTensorList, traceAddress, tensorLabelToTensorAllocationNarrow);
      } else {
        hasAccessType = false;
      }
    } else if (traceAccessType == TraceEvent.AccessType.WRITE_NARROW) {
      if (instruction.getNarrowWriteCount() != 0) { 
        AccessTypeTensorList = instruction.getNarrowWriteList();
        tensor = getTensor(AccessTypeTensorList, traceAddress, tensorLabelToTensorAllocationNarrow);
      } else {
        hasAccessType = false;
      }
    } else if (traceAccessType == TraceEvent.AccessType.READ_WIDE) {
      if (instruction.getWideReadCount() != 0) {
        AccessTypeTensorList = instruction.getWideReadList();
        tensor = getTensor(AccessTypeTensorList, traceAddress, tensorLabelToTensorAllocationWide);
      } else {
        hasAccessType = false;
      }
    } else if (traceAccessType == TraceEvent.AccessType.WRITE_WIDE) {
      if (instruction.getWideWriteCount() != 0) {
        AccessTypeTensorList = instruction.getWideWriteList();
        tensor = getTensor(AccessTypeTensorList, traceAddress, tensorLabelToTensorAllocationWide);
      } else {
        hasAccessType = false;
      }
    } else {
      throw new Exception(
          "Trace event at address " 
            + traceAddress 
            + " has invalid access type of " 
            + traceAccessType 
            + ".");
    }

    // Throws MemoryAccessException if instruction does not have the expected
    // access type.
    if (!hasAccessType) {
      throw new MemoryAccessException(traceAccessType, instruction.getTag());
    }

    // Throws an exception if there is no tensor
    // associated with the correct instruction access type.
    
    if (tensor == -1) {
      throw new Exception(
          "Instruction  " 
            + instruction.getName() 
            + " does not have the appropriate tensor associated with it."
            + "This may be due to invalid tensor or incorrect tensor event address. ") ; // notifying incorrect address error 
    }

    return tensor;
  }

/**
   * Retrieves the correct tensor depending on the specific access type instruction list. 
   * 
   */
  public static TensorAccess getTensor (List<Integer> AccessTypeTensorList, int traceAddress, 
            Map<Integer, TensorAllocation> tensorLabelToTensorAllocationTable) {
      int tensor = -1;
      for (int i = 0; AccessTypeTensorList.size(); i++) {
            int tensorAlloc = tensorLabelToTensorAllocationTable.get(AccessTypeTensorList.get(i));
            int start = tensorAlloc.getBaseAddress();
            int end = start + tensorAlloc.getSize();
            if (traceAddress >= start && traceAddress < end) {
                tensor = AccessTypeTensorList.get(i);
                break;
            }
        }
       return tensor; 
  }



  /**
   * Validates that the write validation has a corresponding tensor and writes it to the correct
   * address in the memory arrays.
   */
  public static void writeValidation(
      int[][] narrow, int[][] wide, List<Boolean> masks, int tensor, TraceEvent traceEvent) {  
    int address = traceEvent.getAddress();
    if (traceEvent.getAccessType() == TraceEvent.AccessType.WRITE_NARROW) {
      // Iterate through the tiles.
      for (int tile = 0; tile < NUM_TILES; tile++) {
        if (masks.get(tile)) {
          // Write the tensor name in our replicated memory.
          int endAddress = traceEvent.getBytes()  + address;
          for (int currentByte = address; currentByte < endAddress; currentByte ++) {
            narrow[tile][currentByte] = tensor;
          } 
        }
          }
        }
    if (traceEvent.getAccessType() == TraceEvent.AccessType.WRITE_WIDE) {
      for (int tile = 0; tile < NUM_TILES; tile++) {
        if (masks.get(tile)) {
          int endAddress = traceEvent.getBytes()  + address;
          for (int currentByte = address; currentByte < endAddress; currentByte ++) {
            wide[tile][currentByte] = tensor;
          }  
        }
      }
    }
  }

  /**
   * Validates that the tensor that the read trace entry is reading has been written before the read
   * occurs.
   */
  public static void readValidation(
      int[][] narrow, int[][] wide, List<Boolean> masks, int tensor, TraceEvent traceEvent)
      throws InvalidTensorReadException {
    int address = traceEvent.getAddress();

    if (traceEvent.getAccessType() == TraceEvent.AccessType.READ_NARROW) {
      for (int tile = 0; tile < NUM_TILES; tile++) {
        if (masks.get(tile)) {
          int endAddress = traceEvent.getBytes()  + address;  
          for (int currentByte = address; currentByte < endAddress; currentByte ++) {
            if (narrow[tile][currentByte] != tensor) {
                throw new InvalidTensorReadException(
                    tensor, tile, address, narrow[tile][address], "narrow");
            }
          }
        }
      }
    } else if (traceEvent.getAccessType() == TraceEvent.AccessType.READ_WIDE) {
      for (int tile = 0; tile < NUM_TILES; tile++) {
        if (masks.get(tile)) {
          int endAddress = traceEvent.getBytes()  + address;  
          for (int currentByte = address; currentByte < endAddress; currentByte ++) {  
            if (wide[tile][address] != tensor) {
                throw new InvalidTensorReadException(
                    tensor, tile, address, wide[tile][address], "wide");
            }
          }
        }
      }
    }
  }
}

