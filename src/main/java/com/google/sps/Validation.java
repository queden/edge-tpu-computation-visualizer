package com.google.sps;

import com.google.sps.exceptions.*;
import com.google.sps.proto.SimulationTraceProto.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class Validation {

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

  /** Given a Simulation Trace, validates the trace and outputs any errors found */
  public static void validate(SimulationTrace simulationTrace) {
    List<Instruction> instructions = simulationTrace.getInstructionList();

    // TODO: May need to catch ArrayIndexOutOfBoundsException
    int[] narrowAllocation =
        getAllocationArray(simulationTrace.getTensorAllocationNarrowList(), NARROW_SIZE);
    int[] wideAllocation =
        getAllocationArray(simulationTrace.getTensorAllocationWideList(), WIDE_SIZE);

    try {
      relateTensorsToInstructions(narrowAllocation, wideAllocation, instructions);
    } catch (InvalidTensorAddressException e) {
      System.out.println(e.getMessage());
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println(e.getMessage());
    }

    Map<Integer, Instruction> instructionTagtoInstruction =
        relateIntructionTagtoInstructionTable(instructions);
  }

  /**
   * Given an array showing the narrow and wide tensor allocations in memory, populates each
   * instruction with the tensor that they operate on. Throws a InvalidTensorAddressException if the
   * instruction operates on a memory address that does not hold a tensor.
   */
  public static void relateTensorsToInstructions(
      int[] narrowAllocation, int[] wideAllocation, List<Instruction> instructions)
      throws InvalidTensorAddressException {
    // Loops over each instruction and fills in the tensor field for the instruction's operations.
    for (int i = 0; i < instructions.size(); i++) {
      // Merges instruction into a builder to allow changes to the tensor field.
      Instruction instruction = instructions.get(i);
      Instruction.Builder instructionBuilder = Instruction.newBuilder();
      instructionBuilder.mergeFrom(instruction);

      MemoryAccess.Builder memoryAccessBuilder = MemoryAccess.newBuilder();

      // Looks at each of the possible memory accesses for the instruction.
      if (instruction.hasNarrowRead()) {
        memoryAccessBuilder.mergeFrom(instruction.getNarrowRead());

        int baseAddress = memoryAccessBuilder.getBaseAddress();
        int tensor = narrowAllocation[baseAddress];

        // If there is a tensor at the location, add it to the instruction. Otherwise,
        // throw an exception.
        if (tensor >= 0) {
          memoryAccessBuilder.setTensor(tensor);
          instructionBuilder.setNarrowRead(memoryAccessBuilder.build());
        } else {
          throw new InvalidTensorAddressException(
              baseAddress, instructionBuilder.getTag(), NARROW_READ);
        }
      }

      if (instruction.hasNarrowWrite()) {
        memoryAccessBuilder.mergeFrom(instruction.getNarrowWrite());

        int baseAddress = memoryAccessBuilder.getBaseAddress();
        int tensor = narrowAllocation[memoryAccessBuilder.getBaseAddress()];

        if (tensor >= 0) {
          memoryAccessBuilder.setTensor(tensor);
          instructionBuilder.setNarrowWrite(memoryAccessBuilder.build());
        } else {
          throw new InvalidTensorAddressException(
              baseAddress, instructionBuilder.getTag(), NARROW_WRITE);
        }
      }

      if (instruction.hasWideRead()) {
        memoryAccessBuilder.mergeFrom(instruction.getWideRead());

        int baseAddress = memoryAccessBuilder.getBaseAddress();
        int tensor = wideAllocation[memoryAccessBuilder.getBaseAddress()];

        if (tensor >= 0) {
          memoryAccessBuilder.setTensor(tensor);
          instructionBuilder.setWideRead(memoryAccessBuilder.build());
        } else {
          throw new InvalidTensorAddressException(
              baseAddress, instructionBuilder.getTag(), WIDE_READ);
        }
      }

      if (instruction.hasWideWrite()) {
        memoryAccessBuilder.mergeFrom(instruction.getWideWrite());

        int baseAddress = memoryAccessBuilder.getBaseAddress();
        int tensor = wideAllocation[memoryAccessBuilder.getBaseAddress()];

        if (tensor >= 0) {
          memoryAccessBuilder.setTensor(tensor);
          instructionBuilder.setWideWrite(memoryAccessBuilder.build());
        } else {
          throw new InvalidTensorAddressException(
              baseAddress, instructionBuilder.getTag(), WIDE_WRITE);
        }
      }

      instructions.set(i, instructionBuilder.build());
    }
  }

   /**
   * Given the tensor allocations and the size of the corresponding memory, returns an array
   * representing that memory. -1 array entries represent no tensor present at that location, values
   * >= 0 represent a tensor with a label of that value.
   */
  public static int[] getAllocationArray(List<TensorAllocation> allocations, int memorySize) {
    int[] memory = new int[memorySize * 1024];

    Arrays.fill(memory, -1);

    for (TensorAllocation allocation : allocations) {
      Arrays.fill(
          memory,
          allocation.getStartAddress(),
          allocation.getStartAddress() + allocation.getSize(),
          allocation.getLabel());
    }

    return memory;
  }

  /** Given a list of instructions, maps each instruction tag to its corresponding instruction. */
  public static Map<Integer, Instruction> relateIntructionTagtoInstructionTable(
      List<Instruction> instructions) {
    Map<Integer, Instruction> instructionTagtoInstruction = new Hashtable<>();

    for (Instruction instruction : instructions) {
      instructionTagtoInstruction.put(instruction.getTag(), instruction);
    }

    return instructionTagtoInstruction;
  }

  /**
   * Given a list of trace entries, validates that trace entries proceeded in the right order and
   * operated on the correct traces.
   */
  public static void validateTraceEntries(
      List<TraceEntry> traceEntries,
      Map<Integer, Instruction> instructionTagtoInstruction,
      int[] wideAllocation,
      int[] narrowAllocation)
      throws Exception, InvalidTensorOperationException {
    // Arrays to simulate the narrow and wide memories for each tile.
    int[][] narrow = new int[NUM_TILES][NARROW_SIZE * 1024];
    int[][] wide = new int[NUM_TILES][WIDE_SIZE * 1024];

    // Iterates over each trace entry, ensures that it is operating on the correct tensor and
    // validates based on if it is a write or a read.
    for (TraceEntry traceEntry : traceEntries) {
      // Gets the trace entries corresponding instruction and ensures it exists.
      Instruction instruction = instructionTagtoInstruction.get(traceEntry.getInstructionTag());
      if (instruction == null) {
        throw new Exception(
            "Instruction with key "
                + traceEntry.getInstructionTag()
                + " does not exist."); // TODO: May need to write custom exception
      }

      TraceEntry.AccessType accessType = traceEntry.getAccessType();

      int address = traceEntry.getAddress();
      int traceTensor = -1;
      try {
        traceTensor = getTraceTensor(address, accessType, instruction);
      } catch(Exception e) {
        System.out.println(e.getMessage());
      }
      int expectedTensor;

      // Gets the expected tensor that the trace entry should be operating on based on which memory
      // it accesses.
      if (accessType == TraceEntry.AccessType.READ_NARROW
          || accessType == TraceEntry.AccessType.WRITE_NARROW) {
        expectedTensor = narrowAllocation[address];
      } else {
        expectedTensor = wideAllocation[address];
      }

      // If the trace is operating on the wrong tensor, throws an exception.
      if (expectedTensor != traceTensor) {
        throw new InvalidTensorOperationException(
            address, expectedTensor, traceTensor, traceEntry.getInstructionTag(), accessType);
      }

      List<Boolean> masks = instruction.getMaskList();
      if (masks.isEmpty()){
          throw new InvalidMaskException(traceEntry.getInstructionTag(), traceEntry.getAccessType());
      }
      
      // If the trace entry is a write, performs a write validation. If it a read, performs a read
      // validation.
      if (accessType == TraceEntry.AccessType.WRITE_NARROW
          || accessType == TraceEntry.AccessType.WRITE_WIDE) {
          writeValidation(narrow, wide, masks, traceTensor, traceEntry);
      } else if (accessType == TraceEntry.AccessType.READ_NARROW
          || accessType == TraceEntry.AccessType.READ_WIDE) {
        try {
          readValidation(narrow, wide, masks, traceTensor, traceEntry);
        } catch (InvalidTensorReadException e) {
          System.out.println(e.getMessage());
        }
      }
    }
  }

  /**
   * Returns the tensor that the trace entry is operating on based on its corresponding instruction.
   */
  public static int getTraceTensor(
      int traceAddress, TraceEntry.AccessType traceAccessType, Instruction instruction)
      throws Exception, MemoryAccessException {
    MemoryAccess memoryAccess = null;

    // Tracks if the corresponding instruction has the trace entry's access type.
    Boolean hasAccessType = true;

    if (traceAccessType == TraceEntry.AccessType.READ_NARROW) {
      if (instruction.hasNarrowRead()) {
        memoryAccess = instruction.getNarrowRead();
      } else {
        hasAccessType = false;
      }
    } else if (traceAccessType == TraceEntry.AccessType.WRITE_NARROW) {
      if (instruction.hasNarrowWrite()) { 
        memoryAccess = instruction.getNarrowWrite();
      } else {
        hasAccessType = false;
      }
    } else if (traceAccessType == TraceEntry.AccessType.READ_WIDE) {
      if (instruction.hasWideRead()) {
        memoryAccess = instruction.getWideRead();
      } else {
        hasAccessType = false;
      }
    } else if (traceAccessType == TraceEntry.AccessType.WRITE_WIDE) {
      if (instruction.hasWideWrite()) {
        memoryAccess = instruction.getWideWrite();
      } else {
        hasAccessType = false;
      }
    } else {
      throw new Exception(
          "Trace entry at address " 
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

    // Gets the corresponding tensor. Throws an exception if there is no tensor
    // associated with the correct access type.
    int tensor = -1;
    if (memoryAccess.hasTensor()){
      tensor = memoryAccess.getTensor();  
    } 
    else {
      throw new Exception(
          "Memory Access " 
            + memoryAccess.getDescriptorForType().getName() 
            + " for instruction " 
            + instruction.getTag() 
            + " has no tensor associated with it.");
    }

    return tensor;
  }

  /**
   * Validates that the write validation has a corresponding tensor and writes it to the correct
   * address in the memory arrays.
   */
  public static void writeValidation(
      int[][] narrow, int[][] wide, List<Boolean> masks, int tensor, TraceEntry traceEntry) {
    int address = traceEntry.getAddress();
    if (traceEntry.getAccessType() == TraceEntry.AccessType.WRITE_NARROW) {
      // Iterate through the tiles.
      for (int tile = 0; tile < NUM_TILES; tile++) {
        if (masks.get(tile)) {
          // Write the tensor name in our replicated memory.
          narrow[tile][address] = tensor;
        }
          }
        }
    if (traceEntry.getAccessType() == TraceEntry.AccessType.WRITE_WIDE) {
      for (int tile = 0; tile < NUM_TILES; tile++) {
        if (masks.get(tile)) {
          wide[tile][address] = tensor;
        }
      }
    }
  }

  /**
   * Validates that the tensor that the read trace entry is reading has been written before the read
   * occurs.
   */
  public static void readValidation(
      int[][] narrow, int[][] wide, List<Boolean> masks, int tensor, TraceEntry traceEntry)
      throws InvalidTensorReadException {
    int address = traceEntry.getAddress();

    if (traceEntry.getAccessType() == TraceEntry.AccessType.READ_NARROW) {
      for (int tile = 0; tile < NUM_TILES; tile++) {
        if (masks.get(tile)) {
          if (narrow[tile][address] != tensor) {
            throw new InvalidTensorReadException(
                tensor, tile, address, narrow[tile][address], "narrow");
          }
        }
      }
    } else if (traceEntry.getAccessType() == TraceEntry.AccessType.READ_WIDE) {
      for (int tile = 0; tile < NUM_TILES; tile++) {
        if (masks.get(tile)) {
          if (wide[tile][address] != tensor) {
            throw new InvalidTensorReadException(
                tensor, tile, address, wide[tile][address], "wide");
          }
        }
      }
    }
  }
}

