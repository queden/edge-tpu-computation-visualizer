package com.google.sps;

import com.google.sps.exceptions.*;
import com.google.sps.proto.MemaccessCheckerDataProto.*;
import com.google.sps.results.*;
import com.google.sps.structures.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.io.StringWriter;
import java.io.PrintWriter;

public class Validation {
  private static MemaccessCheckerData memaccessCheckerData;

  private static List<TensorLayerAllocationTable> tensorLayerAllocationNarrow;
  private static List<TensorLayerAllocationTable> tensorLayerAllocationWide;

  private static Hashtable<Pair, TensorAllocation> layerTensorLabelToTensorAllocationNarrow;
  private static Hashtable<Pair, TensorAllocation> layerTensorLabelToTensorAllocationWide;

  private static ArrayList<Instruction> instructions;
  private static Map<Integer, Instruction> instructionTagtoInstruction;
  private static List<TraceEvent> traceEvents;

  private static int[][] narrow;
  private static int[][] wide;

  private static int narrowSize;
  private static int wideSize;

  private static int numTiles;

  private static long validationEnd; 

  // Memory access types.
  public static final String NARROW_READ = "Narrow Read";
  public static final String NARROW_WRITE = "Narrow Write";
  public static final String WIDE_READ = "Wide Read";
  public static final String WIDE_WRITE = "Wide Write";
  public static final String NARROW = "Narrow";
  public static final String WIDE = "Wide";

  public Validation(MemaccessCheckerData memaccessCheckerData) {
    this.memaccessCheckerData = memaccessCheckerData;

    tensorLayerAllocationNarrow = memaccessCheckerData.getTensorLayerAllocationNarrowList();
    tensorLayerAllocationWide = memaccessCheckerData.getTensorLayerAllocationWideList();

    layerTensorLabelToTensorAllocationNarrow = new Hashtable<Pair, TensorAllocation>();
    layerTensorLabelToTensorAllocationWide = new Hashtable<Pair, TensorAllocation>();

    traceEvents = memaccessCheckerData.getTraceEventList();
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

    instructions.addAll(memaccessCheckerData.getInstructionList());

    relateInstructionTagtoInstructionTable();

    try {
      relateTensorsToInstructions(tensorLayerAllocationNarrow, true);
    } catch (InvalidTensorAddressException e) {
      if (!isError) {
        message = e.getMessage();
        isError = true;
      }
    } catch (Exception e) {
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
    } catch (Exception e) {
      if (!isError) {
        message = e.getMessage();
        isError = true;
      }
    }

    return new PreProcessResults(isError, message, traceEvents.size(), numTiles, narrowSize, wideSize, tensorLayerAllocationNarrow, tensorLayerAllocationWide);
  }

  public static ProcessResults process(long start, long end) {
    List<Delta> narrowDeltas = new ArrayList<Delta>();
    List<Delta> wideDeltas = new ArrayList<Delta>();
    
    try {
      validateTraceEvents(start, end, narrowDeltas, wideDeltas);
    } catch (Exception e) {
      return new ProcessResults(e, true, validationEnd, narrowDeltas, wideDeltas);
    }

    return new ProcessResults(null, false, validationEnd, narrowDeltas, wideDeltas);
  }

  /**
   * Given an array showing the narrow and wide tensor allocations in memory, populates each
   * instruction with the tensor that they operate on. Throws a InvalidTensorAddressException if the
   * instruction operates on a memory address that does not hold a tensor.
   */
  private static void relateTensorsToInstructions(
      List<TensorLayerAllocationTable> tensorLayerAllocationTable, boolean isNarrow)
      throws Exception, InvalidTensorAddressException {
    // Get the layer to instruction map.
    Hashtable<String, List<Integer>> layerToInstructionTable = getLayerToInstructionTable();

    // Loops over each layer in the tensorLayerAllocation table to update their instructions.
    for (TensorLayerAllocationTable tensorLayerAllocation : tensorLayerAllocationTable) {
      String curLayer = tensorLayerAllocation.getLayer();

      // Gets first tileAllocation, this could change if allocations are different across tile.
      TensorTileAllocationTable tensorTileAllocationTable = getTileUnion(tensorLayerAllocation, isNarrow);

      // Get the list of tensors allocated on the tile.
      ArrayList<TensorAllocation> tensorAllocationList = new ArrayList<TensorAllocation>();
      tensorAllocationList.addAll(tensorTileAllocationTable.getTensorAllocationList());

      // Creates an interval tree holding the address intervals for the tile's tensors allocated.
      ArrayList<AddressInterval> addressIntervalList = new ArrayList<AddressInterval>();
      for (TensorAllocation tensorAllocation : tensorAllocationList) {
        addressIntervalList.add(new AddressInterval(tensorAllocation));
      }

      IntervalTree<AddressInterval> addressIntervalTree = null;

      try {
        addressIntervalTree =
          new IntervalTree<AddressInterval>(addressIntervalList);
      } catch (OverlappingIntervalsException e) {
        AddressInterval firstTensorAllocation = (AddressInterval) e.getFirstInterval();
        AddressInterval secondTensorAllocation = (AddressInterval) e.getSecondInterval();

        throw new Exception(
          "Overlapping tensors "
          + firstTensorAllocation
          + " and "
          + secondTensorAllocation
          + " found on layer "
          + curLayer
          + "."
        );
      }

      // Gets all of the instructions operating on the current layer.
      List<Integer> layerInstructions = layerToInstructionTable.get(curLayer);

      Instruction.Builder instructionBuilder = Instruction.newBuilder();

      // Loops through the instructions on this layer and updates their tensor based off of the
      // layer's interval tree.

      if (layerInstructions != null) {
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
                // Clear current values of reads and writes, which refer to base addresses
                instructionBuilder.clearNarrowRead();
                instructionBuilder.clearNarrowWrite();

                instructionBuilder.addAllNarrowRead(reads);
                instructionBuilder.addAllNarrowWrite(writes);
            } else {
                // Clear current values of reads and writes, which refer to base addresses
                instructionBuilder.clearWideRead();
                instructionBuilder.clearWideWrite();

                instructionBuilder.addAllWideRead(reads);
                instructionBuilder.addAllWideWrite(writes);
            }

            instructionTagtoInstruction.put(layerInstructionTag, instructionBuilder.build());

            instructionBuilder.clear();
        }
      }

    }
  }

  private static TensorTileAllocationTable getTileUnion(TensorLayerAllocationTable tensorLayerAllocation, boolean isNarrow) throws Exception {
    List<TensorTileAllocationTable> layerTileAllocationTables = tensorLayerAllocation.getTensorTileAllocationList();

    // Assuming all tiles need to be there 
    if (layerTileAllocationTables.size() != numTiles) {
      throw new Exception(
          "Tensor layer allocation table has "
          + layerTileAllocationTables.size()
          + " tiles, expecting "
          + numTiles
          + "."
      );
    }

    Hashtable<Integer, Integer> layerTensorLabelToMaxSize = new Hashtable<Integer, Integer>();

    HashSet<TensorAllocation> tensorSet = new HashSet<TensorAllocation>();

    TensorTileAllocationTable.Builder unionTileBuilder = TensorTileAllocationTable.newBuilder();

    for (TensorTileAllocationTable tensorTileAllocation : layerTileAllocationTables) {
      for (TensorAllocation tensor : tensorTileAllocation.getTensorAllocationList()) {
        Integer max = layerTensorLabelToMaxSize.get(tensor.getTensorLabel());

        // If tensor does not have a max, 
        if (max == null) {
          tensorSet.add(tensor);
          max = 0;
        }

        max = Math.max(max, tensor.getSize());
        layerTensorLabelToMaxSize.put(tensor.getTensorLabel(), max);
      } 
    }

    for (TensorAllocation tensor : tensorSet) {
      int curTensorLabel = tensor.getTensorLabel();
      Integer maxSize = layerTensorLabelToMaxSize.get(curTensorLabel);

      if (maxSize == null) {
        maxSize = 0;
      }

      if (!isNarrow) {
        maxSize /= 32;
      }

      TensorAllocation.Builder unionTensorBuilder = TensorAllocation.newBuilder();
      unionTensorBuilder.mergeFrom(tensor);
      unionTensorBuilder.setSize(maxSize);

      TensorAllocation unionTensor = unionTensorBuilder.build();
      Pair layerLabelPair = new Pair(tensorLayerAllocation.getLayer(), curTensorLabel);

      unionTileBuilder.addTensorAllocation(unionTensor);
      
      if (isNarrow) {
        layerTensorLabelToTensorAllocationNarrow.put(layerLabelPair, unionTensor);
      } else {
        layerTensorLabelToTensorAllocationWide.put(layerLabelPair, unionTensor);
      }
      
    }

    return unionTileBuilder.build();
  }

  /** Returns a map of the layer to the corresponding instructions that operate in that layer. */
  private static Hashtable<String, List<Integer>> getLayerToInstructionTable() {
    Hashtable<String, List<Integer>> layerToInstructionTable =
        new Hashtable<String, List<Integer>>();

    // Loops over the instructions, finds the instruction's layer corresponding instruction list
    // and adds the instruciton to that list
    for (Instruction instruction : instructions) {
      String instructionLayer = instruction.getLayer();
      List<Integer> layerInstructions = layerToInstructionTable.get(instructionLayer);

      if (layerInstructions == null) {
        layerInstructions = new ArrayList<Integer>();
      }

      layerInstructions.add(instruction.getTag());
      layerToInstructionTable.put(instructionLayer, layerInstructions);
    }
    
    return layerToInstructionTable;
  }

  /** Given a list of instructions, maps each instruction tag to its corresponding instruction. */
  private static void relateInstructionTagtoInstructionTable() {
    for (Instruction instruction : instructions) {
      instructionTagtoInstruction.put(instruction.getTag(), instruction);
    }
  }

  /**
   * Given a list of trace entries, validates that trace entries proceeded in the right order and
   * operated on the correct traces.
   */
  public static void validateTraceEvents(long start, long end, List<Delta> narrowDeltas, List<Delta> wideDeltas)
      throws Exception, InvalidTensorOperationException, InvalidTensorReadException, MemoryAccessException {
    validationEnd = start;

    if (traceEvents.isEmpty()) {
      throw new Exception("No trace entry to be validated.");
    }
    // Iterates over each trace entry, ensures that it is operating on the correct tensor and
    // validates based on if it is a write or a read.
    for (long i = start; i < end; i++) {
      validationEnd = i;

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
      int address = traceEvent.getAddress() * traceEvent.getBytes();

      int traceTensor = getTraceTensor(address, accessType, instruction);

      List<Boolean> masks = instruction.getMaskList();
      if (masks.isEmpty()) {
        throw new InvalidMaskException(traceEvent.getInstructionTag(), traceEvent.getAccessType());
      }

      String layer = instruction.getLayer();

      // If the trace entry is a write, performs a write validation. If it a read, performs a read
      // validation.
      if (accessType == TraceEvent.AccessType.NARROW_WRITE
          || accessType == TraceEvent.AccessType.WIDE_WRITE) {
        writeValidation(layer, masks, traceTensor, traceEvent, narrowDeltas, wideDeltas);
      } else if (accessType == TraceEvent.AccessType.NARROW_READ
          || accessType == TraceEvent.AccessType.WIDE_READ) {
        readValidation(layer, masks, traceTensor, traceEvent);
      }
    }
  }


  /**
   * Returns the tensor that the trace entry is operating on based on its corresponding instruction.
   */
  private static int getTraceTensor(
      int traceAddress, TraceEvent.AccessType traceAccessType, Instruction instruction)
      throws Exception, MemoryAccessException {
    List<Integer> accessTypeTensorList;
    int tensor = -1;
    // Tracks if the corresponding instruction has the trace entry's access type.
    Boolean hasAccessType = true;
    String layer = instruction.getLayer();

    if (traceAccessType == TraceEvent.AccessType.NARROW_READ) {
      if (instruction.getNarrowReadCount() != 0) {
        accessTypeTensorList = instruction.getNarrowReadList();
        tensor = getTensor(accessTypeTensorList, traceAddress, layerTensorLabelToTensorAllocationNarrow, layer, NARROW);
      } else {
        hasAccessType = false;
      }
    } else if (traceAccessType == TraceEvent.AccessType.NARROW_WRITE) {
      if (instruction.getNarrowWriteCount() != 0) {
        accessTypeTensorList = instruction.getNarrowWriteList();
        tensor = getTensor(accessTypeTensorList, traceAddress, layerTensorLabelToTensorAllocationNarrow, layer, NARROW);
      } else {
        hasAccessType = false;
      }
    } else if (traceAccessType == TraceEvent.AccessType.WIDE_READ) {
      if (instruction.getWideReadCount() != 0) {
        accessTypeTensorList = instruction.getWideReadList();
        tensor = getTensor(accessTypeTensorList, traceAddress, layerTensorLabelToTensorAllocationWide, layer, WIDE);
      } else {
        hasAccessType = false;
      }
    } else if (traceAccessType == TraceEvent.AccessType.WIDE_WRITE) {
      if (instruction.getWideWriteCount() != 0) {
        accessTypeTensorList = instruction.getWideWriteList();
        tensor = getTensor(accessTypeTensorList, traceAddress, layerTensorLabelToTensorAllocationWide, layer, WIDE);
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

    System.out.println(tensor);
    // Throws an exception if there is no tensor
    // associated with the correct instruction access type.
    if (tensor == -1) {
      throw new Exception(
          "Instruction  "
              + instruction
              + " does not have the appropriate tensor associated with it. This may be due to"
              + " invalid tensor or incorrect tensor event"
              + " address."
              + " Trace info: "
              + "(trace address: "
              + traceAddress
              + " trace access type: "
              + traceAccessType
              + ")"); // notifying incorrect address error
    }
    return tensor;
  }

  /** Retrieves the correct tensor depending on the specific access type instruction list. */
  private static int getTensor (
      List<Integer> accessTypeTensorList,
      int traceAddress,
      Hashtable<Pair, TensorAllocation> tensorLabelToTensorAllocationTable,
      String layer,
      String memoryType) throws Exception {

    int tensor = -1;

    if (tensorLabelToTensorAllocationTable.size() == 0){
        throw new Exception(
          "The "
              + memoryType.toLowerCase()
              + " allocation table is empty."
              + " ");
    }

    for (int i = 0; i < accessTypeTensorList.size(); i++) {

      int possibleTensorLabel = accessTypeTensorList.get(i); 

      Pair pair = new Pair(layer, possibleTensorLabel); 
      TensorAllocation tensorAlloc =
          tensorLabelToTensorAllocationTable.get(pair);

      int start = tensorAlloc.getBaseAddress();
      int end = start + tensorAlloc.getSize();

      if (traceAddress >= start && traceAddress < end) {
        System.out.println("Trace is within interval!");
        tensor = accessTypeTensorList.get(i);
        break;
      }
    }
    return tensor;
  }

  /**
   * Validates that the write validation has a corresponding tensor and writes it to the correct
   * address in the memory arrays.
   */
  private static void writeValidation(String layer, List<Boolean> masks, int tensor, TraceEvent traceEvent, List<Delta> narrowDeltas, List<Delta> wideDeltas) throws Exception {
    int address = traceEvent.getAddress() * traceEvent.getBytes();
    int tile = traceEvent.getTile();
    int instruction = traceEvent.getInstructionTag();

    if (traceEvent.getAccessType() == TraceEvent.AccessType.NARROW_WRITE) {
      // Iterate through the tiles.
      if (masks.get(tile)) {
      // Write the tensor name in replicated memory.
        int endAddress = traceEvent.getBytes() + address;
        for (int currentByte = address; currentByte < endAddress; currentByte++) {
          narrow[tile][currentByte] = tensor;
          narrowDeltas.add(new Delta(layer, tile, currentByte, tensor));
        }
      } else {
        throw new Exception(
          "Trace event writing to address "
          + address
          + " operates on tile "
          + tile 
          + ", while its corresponding instruction "
          + instruction
          + " does not."
        );
      }
    }
    if (traceEvent.getAccessType() == TraceEvent.AccessType.WIDE_WRITE) {
      // Iterate through the tiles.
      if (masks.get(tile)) {
      // Write the tensor name in replicated memory.
        int endAddress = traceEvent.getBytes() + address;
        for (int currentByte = address; currentByte < endAddress; currentByte++) {
          wide[tile][currentByte] = tensor;
          wideDeltas.add(new Delta(layer, tile, currentByte, tensor));
        }
      } else {
        throw new Exception(
          "Trace event writing to address "
          + address
          + " operates on tile "
          + tile 
          + ", while its corresponding instruction "
          + instruction
          + " does not."
        );
      }
    }
  }
  /**
   * Validates that the tensor that the read trace entry is reading has been written before the read
   * occurs.
   */
  private static void readValidation(String layer, List<Boolean> masks, int tensor, TraceEvent traceEvent)
      throws Exception, InvalidTensorReadException {
    int address = traceEvent.getAddress() * traceEvent.getBytes();
    int tile = traceEvent.getTile();
    int instruction = traceEvent.getInstructionTag();

    if (traceEvent.getAccessType() == TraceEvent.AccessType.NARROW_READ) {
      if (masks.get(tile)) {
        int endAddress = traceEvent.getBytes() + address;
        for (int currentByte = address; currentByte < endAddress; currentByte++) {
          if (narrow[tile][currentByte] != tensor) {
            throw new InvalidTensorReadException(
              tensor, layer, instruction, tile, address, narrow[tile][address], "narrow");
          }
        }
      } else {
        throw new Exception(
          "Trace event reading address "
          + address
          + " operates on tile "
          + tile 
          + ", while its corresponding instruction "
          + instruction
          + " does not."
        );
      }
    } else if (traceEvent.getAccessType() == TraceEvent.AccessType.WIDE_READ) {
      if (masks.get(tile)) {
        int endAddress = traceEvent.getBytes() + address;
        for (int currentByte = address; currentByte < endAddress; currentByte++) {
          if (wide[tile][currentByte] != tensor) {
            throw new InvalidTensorReadException(
              tensor, layer, instruction, tile, address, wide[tile][currentByte], "wide");
          }
        }
      } else {
        throw new Exception(
          "Trace event at reading address "
          + address
          + " operates on tile "
          + tile 
          + ", while its corresponding instruction "
          + instruction
          + " does not."
        );
      }
    }
  }

  private static class Pair {
    private String layer;
    private int tensorLabel;

    public Pair(String layer, int tensorLabel) {
      this.layer = layer;
      this.tensorLabel = tensorLabel;
    }

    public String getLayer() {
      return layer;
    }

    public int getTensorLabel() {
      return tensorLabel;
    }

    @Override
    public String toString() {
      return "(layer: "
            + layer
            + ", tensor label: "
            + tensorLabel
            + ")";
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof Pair)) {
        return false;
      }
      return layer.equals(((Pair) other).layer) && tensorLabel == ((Pair) other).tensorLabel;
    }

    @Override
    public int hashCode() {
      int result = 17;
      result = 31 * result + layer.hashCode();
      result = 31 * result + tensorLabel;
      return result;
    }
  }

  /**
   * The individual node for the interval tree, holds the interval and the corresponding tensor
   * label
   */
  private static class AddressInterval implements Interval {
    private final int label; // The label of the tensor with the corresponding address interval
    private final int start; // The start address of the address interval
    private final int end; // The end address of the address interval
    /** Creates a AddressInterval from a TensorAllocation object */
    public AddressInterval(TensorAllocation tensorAllocation) {
      this.label = tensorAllocation.getTensorLabel();
      this.start = tensorAllocation.getBaseAddress();
      this.end = tensorAllocation.getBaseAddress() + tensorAllocation.getSize() - 1;
    }
    public AddressInterval(int label, int start, int end) {
      this.label = label;
      this.start = start;
      this.end = end;
    }
    /**
     * Creates a size one address interval, used in the algorithm to represent the base address of
     * an instruction
     */
    public AddressInterval(int start) {
      this.label =
          -1; // If it is solely one address corresponding to an instruction, it will not a have a
              // label.
      this.start = start;
      this.end = start;
    }
    /**
     * Returns the label of the tensor with the corresponding address interval, -1 if its the base
     * address of an instruction
     */
    public int label() {
      return label;
    }
    /** Returns the start address of the address interval */
    @Override
    public int start() {
      return start;
    }
    /** Returns the end address of the address interval */
    @Override
    public int end() {
      return end;
    }
    @Override
    public String toString() {
      return "(label: " + label + ", start: " + start + ", end: " + end + ")";
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