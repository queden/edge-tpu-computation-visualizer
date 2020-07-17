package com.google.sps;

import com.google.sps.exceptions.*;
import com.google.sps.results.*;
import com.google.sps.proto.MemaccessCheckerDataProto.*;
import com.google.sps.structures.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class Validation_caden {
  private static MemaccessCheckerData memaccessCheckerData;

  private static List<TensorLayerAllocationTable> tensorLayerAllocationNarrow;
  private static List<TensorLayerAllocationTable> tensorLayerAllocationWide;

  private static ArrayList<Instruction> instructions;
  private static Map<Integer, Instruction> instructionTagtoInstruction;
  private static List<TraceEvent> traceEvents;

  private static int[][] narrow;
  private static int[][] wide;

  public static int narrowSize;
  public static int wideSize;

  public static int numTiles;

  // Memory access types.
  public static final String NARROW_READ = "Narrow Read";
  public static final String NARROW_WRITE = "Narrow Write";
  public static final String WIDE_READ = "Wide Read";
  public static final String WIDE_WRITE = "Wide Write";

  public Validation_caden(MemaccessCheckerData memaccessCheckerData) {
    this.memaccessCheckerData = memaccessCheckerData;

    tensorLayerAllocationNarrow = memaccessCheckerData.getTensorLayerAllocationNarrowList();
    tensorLayerAllocationWide = memaccessCheckerData.getTensorLayerAllocationWideList();

    traceEvents = memaccessCheckerData.getTraceEventsList();
    instructions = new ArrayList<Instruction>();

    narrowSize = memaccessCheckerData.getNarrowMemorySizeBytes();
    wideSize = memaccessCheckerData.getWideMemorySizeBytes();
    numTiles = memaccessCheckerData.getNumTiles();

    narrow = new int[numTiles][narrowSize];
    wide = new int[numTiles][wideSize];

    instructionTagtoInstruction = new Hashtable<Integer, Instruction>();
  }

  public static PreProcessResults preProcess() {
    boolean isError = false;
    String message = "Preprocessing completed successfully.";

    instructions.addAll(memaccessCheckerData.getInstructionsList());

    relateIntructionTagtoInstructionTable();

    try {
      relateTensorsToInstructions(tensorLayerAllocationNarrow, true);
    } catch (InvalidTensorAddressException e) {
      if (!isError) {
        message = e.getMessage();
        isError = true;
      }    
    } catch (ArrayIndexOutOfBoundsException e) {
      if (!isError) {
        message = e.getMessage();
        isError = true;
      }    
    }

    try {
      relateTensorsToInstructions(tensorLayerAllocationWide, false);
    } catch (InvalidTensorAddressException e) {
      if (!isError) {
        message = e.getMessage();
        isError = true;
      }    
    } catch (ArrayIndexOutOfBoundsException e) {
      if (!isError) {
        message = e.getMessage();
        isError = true;
      }    
    }

    return new PreProcessResults(isError, message, traceEvents.size());
  }

  public static ProcessResults process(long start, long end) {
    try {
    //   validateTraceEvents(start, end);
    } catch (Exception e) {
      return new ProcessResults(e, narrow, wide);
    }

    return new ProcessResults(null, narrow, wide);
  }

  /**
   * Given an array showing the narrow and wide tensor allocations in memory, populates each
   * instruction with the tensor that they operate on. Throws a InvalidTensorAddressException if the
   * instruction operates on a memory address that does not hold a tensor.
   */
  private static void relateTensorsToInstructions(List<TensorLayerAllocationTable> tensorLayerAllocationTable, boolean isNarrow)
      throws InvalidTensorAddressException {
    // Get the layer to instruction map.
    Hashtable<String, List<Integer>> layerToInstructionTable = getLayerToInstructionTable();

    // Loops over each layer in the tensorLayerAllocation table to update their instructions.
    for (TensorLayerAllocationTable tensorLayerAllocation : tensorLayerAllocationTable) {
        String curLayer = tensorLayerAllocation.getLayer();

        // Gets first tileAllocation, this could change if allocations are different across tile. 
        TensorTileAllocationTable tensorTileAllocationTable = tensorLayerAllocation.getTensorTileAllocation(0);

        // Get the list of tensors allocated on the tile.
        ArrayList<TensorAllocation> tensorAllocationList = new ArrayList<TensorAllocation>();
        tensorAllocationList.addAll(tensorTileAllocationTable.getTensorAllocationList());

        // Creates an interval tree holding the address intervals for the tile's tensors allocated. 
        ArrayList<AddressInterval> addressIntervalList = new ArrayList<AddressInterval>();
        for (TensorAllocation tensorAllocation : tensorAllocationList) {
            addressIntervalList.add(new AddressInterval(tensorAllocation));
        }
        IntervalTree<AddressInterval> addressIntervalTree = new IntervalTree<AddressInterval>(addressIntervalList);

        // Gets all of the instructions operating on the current layer.
        List<Integer> layerInstructions = layerToInstructionTable.get(curLayer);

        Instruction.Builder instructionBuilder = Instruction.newBuilder();

        // Loops through the instructions on this layer and updates their tensor based off of the layer's interval tree.
        for (int layerInstructionTag : layerInstructions) {
            instructionBuilder.mergeFrom(instructionTagtoInstruction.get(layerInstructionTag));

            // Gets the corresponding reads and writes, depending on if the memory is narrow or wide.
            ArrayList<Integer> reads = new ArrayList<Integer>();
            ArrayList<Integer> writes = new ArrayList<Integer>();

            if (isNarrow) {
                reads.addAll(instructionBuilder.getNarrowReadList());
                writes.addAll(instructionBuilder.getNarrowWriteList());

            } else {
                reads.addAll(instructionBuilder.getWideReadList());
                writes.addAll(instructionBuilder.getWideWriteList());
            }

            // Gets the operation's corresponding tensors from the interval tree and assigns them.
            for (int i = 0; i < reads.size(); i++) {
                AddressInterval address = new AddressInterval(reads.get(i));
                // Returns the address interval of the tensor that the address falls in
                AddressInterval tensorAddressInterval = addressIntervalTree.containsAddress(address);

                if (tensorAddressInterval == null) {
                    throw new InvalidTensorAddressException(address.start(), layerInstructionTag, isNarrow);
                }
                int tensorLabel = tensorAddressInterval.label();
                
                reads.set(i, tensorLabel);
            }

            for (int i = 0; i < writes.size(); i++) {
                AddressInterval address = new AddressInterval(writes.get(i));
                AddressInterval tensorAddressInterval = addressIntervalTree.containsAddress(address);

                if (tensorAddressInterval == null) {
                    throw new InvalidTensorAddressException(address.start(), layerInstructionTag, isNarrow);
                }

                int tensorLabel = tensorAddressInterval.label();
                
                writes.set(i, tensorLabel);
            }

            if (isNarrow) {
                instructionBuilder.addAllNarrowRead(reads);
                instructionBuilder.addAllNarrowWrite(writes);
            } else {
                instructionBuilder.addAllWideRead(reads);
                instructionBuilder.addAllWideWrite(writes);
            }   

            instructionTagtoInstruction.put(layerInstructionTag, instructionBuilder.build());
        }
    }
    
  }

  /**
   * Returns a map of the layer to the corresponding instructions that operate in that layer.
   */
  private static Hashtable<String, List<Integer>> getLayerToInstructionTable() {
      Hashtable<String, List<Integer>> layerToInstructionTable = new Hashtable<String, List<Integer>>();

      // Loops over the instructions, finds the instruction's layer corresponding instruction list
      // and adds the instruciton to that list
      for (Instruction instruction : instructions) {
          List<Integer> layerInstructions = layerToInstructionTable.get(instruction.getLayer());

          if (layerInstructions == null) {
              layerInstructions = new ArrayList<Integer>();
          }

          layerInstructions.add(instruction.getTag());
      }

      return layerToInstructionTable;
  }

  /** Given a list of instructions, maps each instruction tag to its corresponding instruction. */
  private static void relateIntructionTagtoInstructionTable() {
    for (Instruction instruction : instructions) {
      int instructionTag = instruction.getTag();
      System.out.println("instruction tag is " + instructionTag);
      instructionTagtoInstruction.put(instructionTag, instruction);
    }
  }

  /**
   * Given a list of trace entries, validates that trace entries proceeded in the right order and
   * operated on the correct traces.
   */
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
//       if (accessType == TraceEvent.AccessType.NARROW_READ
//           || accessType == TraceEvent.AccessType.NARROW_WRITE) {
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
//       if (accessType == TraceEvent.AccessType.NARROW_WRITE
//           || accessType == TraceEvent.AccessType.WIDE_WRITE) {
//         writeValidation(narrow, wide, masks, traceTensor, traceEvent);
//       } else if (accessType == TraceEvent.AccessType.NARROW_READ
//           || accessType == TraceEvent.AccessType.WIDE_READ) {
//         readValidation(narrow, wide, masks, traceTensor, traceEvent);
//       }
//     }
//   }

  /**
   * Returns the tensor that the trace entry is operating on based on its corresponding instruction.
   */
//   private static int getTraceTensor(
//       int traceAddress, TraceEvent.AccessType traceAccessType, Instruction instruction)
//       throws Exception, MemoryAccessException {
//     TensorAccess tensorAccess = null;

//     // Tracks if the corresponding instruction has the trace entry's access type.
//     Boolean hasAccessType = true;

//     if (traceAccessType == TraceEvent.AccessType.NARROW_READ) {
//       if (instruction.hasNarrowRead()) {
//         tensorAccess = instruction.getNarrowRead();
//       } else {
//         hasAccessType = false;
//       }
//     } else if (traceAccessType == TraceEvent.AccessType.NARROW_WRITE) {
//       if (instruction.hasNarrowWrite()) { 
//         tensorAccess = instruction.getNarrowWrite();
//       } else {
//         hasAccessType = false;
//       }
//     } else if (traceAccessType == TraceEvent.AccessType.WIDE_READ) {
//       if (instruction.hasWideRead()) {
//         tensorAccess = instruction.getWideRead();
//       } else {
//         hasAccessType = false;
//       }
//     } else if (traceAccessType == TraceEvent.AccessType.WIDE_WRITE) {
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
//     if (traceEvent.getAccessType() == TraceEvent.AccessType.NARROW_WRITE) {
//       // Iterate through the tiles.
//       for (int tile = 0; tile < numTiles; tile++) {
//         if (masks.get(tile)) {
//           // Write the tensor name in our replicated memory.
//           narrow[tile][address] = tensor;
//         }
//           }
//         }
//     if (traceEvent.getAccessType() == TraceEvent.AccessType.WIDE_WRITE) {
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

//     if (traceEvent.getAccessType() == TraceEvent.AccessType.NARROW_READ) {
//       for (int tile = 0; tile < numTiles; tile++) {
//         if (masks.get(tile)) {
//           if (narrow[tile][address] != tensor) {
//             throw new InvalidTensorReadException(
//                 tensor, tile, address, narrow[tile][address], "narrow");
//           }
//         }
//       }
//     } else if (traceEvent.getAccessType() == TraceEvent.AccessType.WIDE_READ) {
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


    private static class AddressInterval implements Interval {

        private final int label;
        private final int start;
        private final int end;

        public AddressInterval(TensorAllocation tensorAllocation) {
            this.label = tensorAllocation.getTensorLabel();
            this.start = tensorAllocation.getBaseAddress();
            this.end = tensorAllocation.getBaseAddress() + tensorAllocation.getSize();
        }
        
        public AddressInterval(int label, int start, int end) {
            this.label = label;
            this.start = start;
            this.end = end;
        }
        
        public AddressInterval(int start) {
            this.label = -1; // If it is solely one address corresponding to an instruction, it will not a have a label.
            this.start = start;
            this.end = start;
        }

        public int label() {
            return label;
        }
        
        @Override
        public int start() {
            return start;
        }

        @Override
        public int end() {
            return end;
        }
        
        @Override
        public String toString() {
            return "label: " + label + " start: " + start + " end: " + end;
        }
        
        @Override
        public boolean equals(Object other) {
            // No need for null check. The instanceof operator returns false if (other == null).
            if (!(other instanceof AddressInterval)) {
                return false;
            }

            return start == ((AddressInterval) other).start && end == ((AddressInterval) other).end;
        }
        
        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + start;
            result = 31 * result + end;
            return result;
        }
    }
}

