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
  // Proto message of the selected file
  private static MemaccessCheckerData memaccessCheckerData;

  // Tile allocations across layers
  private static List<TensorLayerAllocationTable> tensorLayerAllocationNarrow;
  private static List<TensorLayerAllocationTable> tensorLayerAllocationWide;

  // Relation of layer and tensor label to narrow/wide tensor allocations
  private static Hashtable<Pair, TensorAllocation> layerTensorLabelToTensorAllocationNarrow;
  private static Hashtable<Pair, TensorAllocation> layerTensorLabelToTensorAllocationWide;

  // Instruction structures for quick lookup
  private static ArrayList<Instruction> instructions;
  private static Map<Integer, Instruction> instructionTagtoInstruction;
  
  private static List<TraceEvent> traceEvents;

  // Narrow/wide memory allocation state representations
  private static int[][] narrow;
  private static int[][] wide;

  private static int narrowSize;
  private static int wideSize;

  private static int numTiles;

  // Index at which an error in validating a traceEvent was encountered
  private static long validationEnd; 

  // Memory access types.
  public static final String NARROW_READ = "Narrow Read";
  public static final String NARROW_WRITE = "Narrow Write";
  public static final String WIDE_READ = "Wide Read";
  public static final String WIDE_WRITE = "Wide Write";
  public static final String NARROW = "Narrow";
  public static final String WIDE = "Wide";

  /**
   * Constructs a validation object to be used for validation
   *
   * @param memaccessCheckerData proto message of the selected file
   */
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

  /**
   * Preprocesses the validation object and its instructions
   *
   * @return information about the object's preprocessing, 
   *         more specifically whether or not there was an error with the instructions themselves
   */
  public static PreProcessResults preProcess() {
    boolean isError = false;
    String message = "Preprocessing completed successfully.";

    instructions.addAll(memaccessCheckerData.getInstructionList());

    // Generates a HashMap relating instruction tags to their instructions
    relateInstructionTagtoInstructionTable();

    try {
      // Generates a Hastable relating each layer's tensors to their tensor allocations (narrow)
      relateTensorsToInstructions(tensorLayerAllocationNarrow, true);
    } catch (InvalidTensorAddressException e) {
      if (!isError) {
        message = e.getMessage();
        isError = true;
      }
    } catch (Exception e) {
      // Overlapping tensor memory allocations on a specific layer
      if (!isError) {
        message = e.getMessage();
        isError = true;
      }
    }

    try {
      // Generates a Hastable relating each layer's tensors to their tensor allocations (wide)
      relateTensorsToInstructions(tensorLayerAllocationWide, false);
    } catch (InvalidTensorAddressException e) {
      if (!isError) {
        message = e.getMessage();
        isError = true;
      }
    } catch (Exception e) {
      // Overlapping tensor memory allocations on a specific layer
      if (!isError) {
        message = e.getMessage();
        isError = true;
      }
    }

    return new PreProcessResults(
        isError, 
        message, 
        traceEvents.size(), 
        numTiles, 
        narrowSize, 
        wideSize, 
        tensorLayerAllocationNarrow, 
        tensorLayerAllocationWide);
  }

  /**
   * Processes and attempts to validate the specified chunk of traces
   *
   * @param start is the starting index of traceEvents
   * @param end is the ending index of traceEvents
   * @return the processing results of the specified chunk of traces up to 
   *         end, or the point at which an error was encountered
   */
  public static ProcessResults process(long start, long end) {
    List<Delta> narrowDeltas = new ArrayList<Delta>();
    List<Delta> wideDeltas = new ArrayList<Delta>();
    
    try {
      validateTraceEvents(start, end, narrowDeltas, wideDeltas);
    } catch (Exception e) {
      /*
        Possible errors:
          - Lack of mask(s) assigned to a given instruction and trace event
          - A complete lack of provided trace events
       */
      return new ProcessResults(e, true, validationEnd, narrowDeltas, wideDeltas);
    }

    return new ProcessResults(null, false, validationEnd, narrowDeltas, wideDeltas);
  }

  /**
   * Given an array showing the narrow and wide tensor allocations in memory, populates each
   * instruction with the tensor that they operate on. Throws a InvalidTensorAddressException if the
   * instruction operates on a memory address that does not hold a tensor.
   *
   * @param tensorLayerAllocationTable is the collection of tensor allocations per layer and per tile
   * @param isNarrow dictates whether the allocations being processed are narrow or wide
   * @throws Exception if there are overlapping tensor memory allocations on a specific layer
   * @throws InvalidTensorAddressException if  there is a lack of a tensor assigned to a base address in narrow/wide memory
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
      TensorTileAllocationTable tensorTileAllocationTable = 
          getTileUnion(tensorLayerAllocation, isNarrow);

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
                    throw new InvalidTensorAddressException(
                        address.start(), layerInstructionTag, isNarrow);
                }
                int tensorLabel = tensorAddressInterval.label();
                reads.set(i, tensorLabel);
            }

            for (int i = 0; i < writes.size(); i++) {
                AddressInterval address = new AddressInterval(writes.get(i));
                AddressInterval tensorAddressInterval = addressIntervalTree.containsAddress(address);
                if (tensorAddressInterval == null) {
                    throw new InvalidTensorAddressException(
                        address.start(), layerInstructionTag, isNarrow);
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

  /**
   * Goes over all tiles, finds all tensor allocations, and makes a union tile that 
   * has all tensor allocations with a size of the max size found across all tiles.
   *
   * @param tensorLayerAllocation is the per tile tensor allocations within this layer
   * @param isNarrow dictates whether the tensor allocations are narrow or wide
   * @throws Exception if the layer does not have the same number of tiles as expected by the proto
   * @return the merged tensor allocations within each tile on this layer
   */
  private static TensorTileAllocationTable getTileUnion(
      TensorLayerAllocationTable tensorLayerAllocation, boolean isNarrow) throws Exception {
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

  /** Returns a map of the layer to the corresponding instructions that operate in that layer.
   *
   * @return the table to relating each instruction to the layer it operates on
   */
  private static Hashtable<String, List<Integer>> getLayerToInstructionTable() {
    Hashtable<String, List<Integer>> layerToInstructionTable =
        new Hashtable<String, List<Integer>>();

    // Loops over the instructions, finds the instruction's layer corresponding instruction list
    // and adds the instruction to that list
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
   * 
   * @param start is the beginning index of the traceEvents to validate
   * @param end is the ending index of the traceEvents to validate
   * @param narrowDeltas is the list of altered narrow memory locations after processing the traceEvents
   * @param wideDeltas is the list of altered wide memory locations after processing the traceEvents
   * @throws Exception if there is a complete lack of provided trace events in an instruction or 
   *                   there is a non-existent instruction corresponding to an existing trace event
   * @throws InvalidMaskException if there is a lack of lack of mask(s) assigned to a given instruction
   */
  private static void validateTraceEvents(long start, long end, List<Delta> narrowDeltas, List<Delta> wideDeltas)
      throws Exception, InvalidMaskException {
    validationEnd = start;

    if (traceEvents.isEmpty()) {
      throw new Exception("No trace event to be validated.");
    }
    // Iterates over each trace event, ensures that it is operating on the correct tensor and
    // validates based on if it is a write or a read.
    for (long i = start; i < end; i++) {
      validationEnd = i;

      TraceEvent traceEvent = traceEvents.get((int) i);
      // Gets the trace event's corresponding instruction and ensures it exists.
      Instruction instruction = instructionTagtoInstruction.get(traceEvent.getInstructionTag());
      if (instruction == null) {

        throw new Exception(
            "Instruction with tag "
                + traceEvent.getInstructionTag()
                + " does not exist.");
      }

      TraceEvent.AccessType accessType = traceEvent.getAccessType();
      int address = traceEvent.getAddress() * traceEvent.getBytes();
      long cycle = traceEvent.getCycle();

      int traceTensor = getTraceTensor(cycle, address, accessType, instruction);

      List<Boolean> masks = instruction.getMaskList();
      if (masks.isEmpty()) {
        throw new InvalidMaskException(traceEvent.getInstructionTag(), traceEvent.getAccessType());
      }

      String layer = instruction.getLayer();

      // If the trace event is a write, performs a write validation. If it a read, performs a read
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
   * Returns the tensor that the trace event is operating on based on its corresponding instruction.
   *
   * @param traceAddress is the base address of the traceEvent
   * @param traceAccessType is the access type of the traceEvent (narrow/wide read/write)
   * @param instruction is the instruction that the traceEvent belongs to
   * @throws Exception if the traceEvent has an access type that is not narrow/wide read/write or if
                       there is a lack of a tensor associated to an instruction
   * @throws MemoryAccessException if attempting to perform a memory access operation from an instruction
   *                               that does not contain its trace event
   * @return the tensor operated on by this traceEvent
   */
  private static int getTraceTensor(
      long cycle, int traceAddress, TraceEvent.AccessType traceAccessType, Instruction instruction)
      throws Exception, MemoryAccessException {
    List<Integer> accessTypeTensorList;
    int tensor = -1;
    // Tracks if the corresponding instruction has the trace event's access type.
    Boolean hasAccessType = true;
    String layer = instruction.getLayer();

    if (layer.isEmpty()) {
      throw new Exception("Instruction "
            + instruction.getTag()
            + " is not associated with a layer."
      );
    }

    if (traceAccessType == TraceEvent.AccessType.NARROW_READ) {
      if (instruction.getNarrowReadCount() != 0) {
        accessTypeTensorList = instruction.getNarrowReadList();
        tensor = 
          getTensor(
              accessTypeTensorList, 
              traceAddress, 
              layerTensorLabelToTensorAllocationNarrow, 
              layer, 
              NARROW);
      } else {
        hasAccessType = false;
      }
    } else if (traceAccessType == TraceEvent.AccessType.NARROW_WRITE) {
      if (instruction.getNarrowWriteCount() != 0) {
        accessTypeTensorList = instruction.getNarrowWriteList();
        tensor = 
            getTensor(
                accessTypeTensorList, 
                traceAddress, 
                layerTensorLabelToTensorAllocationNarrow, 
                layer, 
                NARROW);
      } else {
        hasAccessType = false;
      }
    } else if (traceAccessType == TraceEvent.AccessType.WIDE_READ) {
      if (instruction.getWideReadCount() != 0) {
        accessTypeTensorList = instruction.getWideReadList();
        tensor = 
            getTensor(
                accessTypeTensorList, 
                traceAddress, 
                layerTensorLabelToTensorAllocationWide, 
                layer, 
                WIDE);
      } else {
        hasAccessType = false;
      }
    } else if (traceAccessType == TraceEvent.AccessType.WIDE_WRITE) {
      if (instruction.getWideWriteCount() != 0) {
        accessTypeTensorList = instruction.getWideWriteList();
        tensor = 
            getTensor(
                accessTypeTensorList, 
                traceAddress, 
                layerTensorLabelToTensorAllocationWide, 
                layer, 
                WIDE);
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
          "Instruction \""
              + instruction.getName()
              + "\" (tag: "
              + instruction.getTag()
              + ", layer: "
              + instruction.getLayer()
              + ") does not have the appropriate tensor associated with it. This may be due to"
              + " invalid tensor or incorrect tensor event"
              + " address."
              + " Trace info: "
              + "(trace address: "
              + traceAddress
              + " trace access type: "
              + traceAccessType
              + " cycle: "
              + cycle
              + ")"); // notifying incorrect address error
    }
    return tensor;
  }

  /** Retrieves the correct tensor depending on the specific access type instruction list.
   *
   * @param accessTypeTensorList is the list of narrow/wide read/write traceEvents contained in this instruction
   * @param traceAddress is the base address of this traceEvent
   * @param tensorLabelToTensorAllocationTable is the relation of layer and tensor label to narrow/wide tensor allocations
   * @param layer is the layer this traceEvent is operating on
   * @param memoryType dictates whether this traceEvent is dealing with narrow or wide memory
   * @throws Exception if the narrow/wide memory allocation table is empty
   * @return the tensor associated with this traceEvent
   */
  private static int getTensor(
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
        tensor = accessTypeTensorList.get(i);
        break;
      }
    }
    return tensor;
  }

  /**
   * Validates that the write validation has a corresponding tensor and writes it to the correct
   * address in the memory arrays.
   *
   * @param layer is the layer this traceEvent is operating on
   * @param masks is the mask list of this traceEvent's instruction
   * @param tensor is the tensor this traceEvent is operating on
   * @param traceEvent is the traceEvent currently being validated
   * @param narrowDeltas is the list of narrow memory locations altered in this chunk of traceEvent processing
   * @param wideDeltas is the list of wide memory locations altered in this chunk of traceEvent processing
   * @throws Exception if attempting to write to a memory location on a tile that is in this 
   *                   traceEvent but not its corresponding instruction
   */
  private static void writeValidation(
      String layer, 
      List<Boolean> masks, 
      int tensor, 
      TraceEvent traceEvent, 
      List<Delta> narrowDeltas, 
      List<Delta> wideDeltas) 
      throws Exception {
    int address = traceEvent.getAddress() * traceEvent.getBytes();
    int tile = traceEvent.getTile();
    int instruction = traceEvent.getInstructionTag();
    long cycle = traceEvent.getCycle();

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
          "Trace event at cycle "
          + cycle 
          + " writing to address "
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
          "Trace event at cycle "
          + cycle 
          + " writing to address "
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
   * Validates that the tensor that the read trace event is reading has been written before the read
   * occurs.
   * 
   * @param layer is the layer this traceEvent is operating on
   * @param masks is the mask list of this traceEvent's instruction
   * @param tensor is the tensor this traceEvent is operating on
   * @param traceEvent is the traceEvent currently being validated
   * @throws Exception if attempting to read from a memory location on a tile that is in this 
   *                   traceEvent but not its corresponding instruction
   * @throws InvalidTensorReadException if a traceEvent is attempting to read from 
   *                                    a memory location that has not yet been allocated
   */
  private static void readValidation(String layer, List<Boolean> masks, int tensor, TraceEvent traceEvent)
      throws Exception, InvalidTensorReadException {
    int address = traceEvent.getAddress() * traceEvent.getBytes();
    int tile = traceEvent.getTile();
    int instruction = traceEvent.getInstructionTag();
    long cycle = traceEvent.getCycle();

    if (traceEvent.getAccessType() == TraceEvent.AccessType.NARROW_READ) {
      if (masks.get(tile)) {
        int endAddress = traceEvent.getBytes() + address;
        for (int currentByte = address; currentByte < endAddress; currentByte++) {
          if (narrow[tile][currentByte] != tensor) {
            throw new InvalidTensorReadException(
              tensor, layer, instruction, tile, address, narrow[tile][currentByte], "narrow", cycle);
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
              tensor, layer, instruction, tile, address, wide[tile][currentByte], "wide", cycle);
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

  /**
   * Object to group a tensor and the layer it operates on.
   */
  private static class Pair {
    private String layer;
    private int tensorLabel;

    /**
     * Creates a Pair out of the specified layer and tensor.
     */
    public Pair(String layer, int tensorLabel) {
      this.layer = layer;
      this.tensorLabel = tensorLabel;
    }

    /**
     * Returns the layer name of this Pair object's layer
     */
    public String getLayer() {
      return layer;
    }

    /**
     * Returns the tensor label of this Pair object's layer
     */
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