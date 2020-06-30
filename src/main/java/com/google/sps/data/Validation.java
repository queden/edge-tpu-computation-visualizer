package com.google.sps.data;

import com.google.sps.exceptions.*;
import com.google.sps.proto.SimulationTraceProto.*;
import java.util.Arrays;
import java.util.List;

public class Validation {

  // Sizes in KB of narrow and wide memory.
  public static final int NARROW_SIZE = 128;
  public static final int WIDE_SIZE = 256;

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
    }
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
}
