package com.google.sps;

import static org.junit.Assert.*;

import com.google.sps.exceptions.*;
import com.google.sps.proto.MemaccessCheckerDataProto.*;
import java.lang.NoSuchMethodException;
import java.lang.NoSuchFieldException;
import java.lang.IllegalAccessException;
import java.lang.reflect.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

@RunWith(TJCodeTest.class)
@Suite.SuiteClasses({
    TJCodeTest.TestInstructionTagtoInstructionTable.class,
    TJCodeTest.TestReadValidate.class,
    TJCodeTest.TestGetTensor.class,
    TJCodeTest.TestRelateTensorLabelToTensorToTensorAllocation.class})
public final class TJCodeTest extends Suite {
  private static Class[] classes = {Validation.class};

  public TJCodeTest(Class<?> klass, RunnerBuilder builder) throws InitializationError {
      super(klass, builder);
  }

  public static class TestInstructionTagtoInstructionTable {
    private static Validation validation;
    private static Method relateInstructionTagToInstructionTable;
    private static Field instructions;
    private static Field instructionTagtoInstruction;
    private List<Instruction> testInstructions;
    private Map<Integer, Instruction> expected;

    @Before
    public void setUp() throws NoSuchMethodException, NoSuchFieldException {
      MemaccessCheckerData.Builder proto = MemaccessCheckerData.newBuilder();
      validation = new Validation(proto.build());

      relateInstructionTagToInstructionTable = 
          Validation.class.getDeclaredMethod("relateInstructionTagtoInstructionTable");

      instructions = Validation.class.getDeclaredField("instructions");
      instructionTagtoInstruction = 
          Validation.class.getDeclaredField("instructionTagtoInstruction");

      relateInstructionTagToInstructionTable.setAccessible(true);
      instructions.setAccessible(true);
      instructionTagtoInstruction.setAccessible(true);

      expected = new Hashtable<>();
    }

    @Test
    // An empty set of lists
    public void testEmptyInstructions() throws IllegalAccessException, InvocationTargetException {
      testInstructions = new ArrayList<>();
      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // A single instruction with no masks or memory accesses
    public void testSingleInstruction() throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addAllMask(Arrays.asList())
                      .build()));

      expected.put(
          0, Instruction.newBuilder().setName("A").setTag(0).addAllMask(Arrays.asList()).build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // Multiple instructions with no masks or memory accesses
    public void testMultipleInstructions() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addAllMask(Arrays.asList())
                      .build(),
                  Instruction.newBuilder()
                      .setName("B")
                      .setTag(1)
                      .addAllMask(Arrays.asList())
                      .build(),
                  Instruction.newBuilder()
                      .setName("C")
                      .setTag(2)
                      .addAllMask(Arrays.asList())
                      .build()));

      expected.put(
          0, Instruction.newBuilder().setName("A").setTag(0).addAllMask(Arrays.asList()).build());
      expected.put(
          1, Instruction.newBuilder().setName("B").setTag(1).addAllMask(Arrays.asList()).build());
      expected.put(
          2, Instruction.newBuilder().setName("C").setTag(2).addAllMask(Arrays.asList()).build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // A single instruction with memory access and no masks
    public void testSingleInstructionWithMemoryAccess() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addAllMask(Arrays.asList())
                      .addNarrowRead(12)
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder()
              .setName("A")
              .setTag(0)
              .addAllMask(Arrays.asList())
              .addNarrowRead(12)
              .build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // Multiple instructions with memory accesses and no masks
    public void testMultipleInstructionsWithMemoryAccess() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addAllMask(Arrays.asList())
                      .addNarrowRead(12)
                      .build(),
                  Instruction.newBuilder()
                      .setName("B")
                      .setTag(1)
                      .addAllMask(Arrays.asList())
                      .addNarrowWrite(13)
                      .build(),
                  Instruction.newBuilder()
                      .setName("C")
                      .setTag(2)
                      .addAllMask(Arrays.asList())
                      .addWideWrite(14)
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder()
              .setName("A")
              .setTag(0)
              .addAllMask(Arrays.asList())
              .addNarrowRead(12)
              .build());
      expected.put(
          1,
          Instruction.newBuilder()
              .setName("B")
              .setTag(1)
              .addAllMask(Arrays.asList())
              .addNarrowWrite(13)
              .build());
      expected.put(
          2,
          Instruction.newBuilder()
              .setName("C")
              .setTag(2)
              .addAllMask(Arrays.asList())
              .addWideWrite(14)
              .build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // Multiple instructions with no masks and with/without memory accesses
    public void testMultipleInstructionsMixedMemoryAccessTags() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions = 
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder().setName("A").setTag(0)
                      .addAllMask(Arrays.asList())
                      .addNarrowRead(12)
                      .build(),
                  Instruction.newBuilder().setName("B")
                      .setTag(1)
                      .addAllMask(Arrays.asList())                  
                      .build(),
                  Instruction.newBuilder().setName("C")
                      .setTag(2)
                      .addAllMask(Arrays.asList())
                      .addWideWrite(13)
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder()
              .setName("A")
              .setTag(0)
              .addAllMask(Arrays.asList())
              .addNarrowRead(12).build());
      expected.put(
          1, Instruction.newBuilder().setName("B").setTag(1).addAllMask(Arrays.asList()).build());
      expected.put(
          2, 
          Instruction.newBuilder()
              .setName("C")
              .setTag(2)
              .addAllMask(Arrays.asList())
              .addWideWrite(13).build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // A single instruction with a single mask and no memory accesses
    public void testSingleInstructionOneMask() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addAllMask(Arrays.asList(true))
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder().setName("A").setTag(0).addAllMask(Arrays.asList(true)).build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // A single instruction with multiple masks and no memory accesses
    public void testSingleInstructionMultipleMasks() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addAllMask(Arrays.asList(true, false, false))
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder()
              .setName("A")
              .setTag(0)
              .addAllMask(Arrays.asList(true, false, false))
              .build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // Multiple instructions with a single mask and no memory accesses
    public void testMultipleInstructionsOneMask() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addAllMask(Arrays.asList(true))
                      .build(),
                  Instruction.newBuilder()
                      .setName("B")
                      .setTag(1)
                      .addAllMask(Arrays.asList(false))
                      .build(),
                  Instruction.newBuilder()
                      .setName("C")
                      .setTag(2)
                      .addAllMask(Arrays.asList(true))
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder().setName("A").setTag(0).addAllMask(Arrays.asList(true)).build());
      expected.put(
          1,
          Instruction.newBuilder().setName("B").setTag(1).addAllMask(Arrays.asList(false)).build());
      expected.put(
          2,
          Instruction.newBuilder().setName("C").setTag(2).addAllMask(Arrays.asList(true)).build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // Multiple instructions with multiple masks and no memory accesses
    public void testMultipleInstructionsMultipleMasks() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addAllMask(Arrays.asList(true, false, false))
                      .build(),
                  Instruction.newBuilder()
                      .setName("B")
                      .setTag(1)
                      .addAllMask(Arrays.asList(false, true, false))
                      .build(),
                  Instruction.newBuilder()
                      .setName("C")
                      .setTag(2)
                      .addAllMask(Arrays.asList(true, true, true))
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder()
              .setName("A")
              .setTag(0)
              .addAllMask(Arrays.asList(true, false, false))
              .build());
      expected.put(
          1,
          Instruction.newBuilder()
              .setName("B")
              .setTag(1)
              .addAllMask(Arrays.asList(false, true, false))
              .build());
      expected.put(
          2,
          Instruction.newBuilder()
              .setName("C")
              .setTag(2)
              .addAllMask(Arrays.asList(true, true, true))
              .build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // Multiple instructions with no memory accesses and 0-3 masks
    public void testMultipleInstructionsMixedMasks() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addAllMask(Arrays.asList())
                      .build(),
                  Instruction.newBuilder()
                      .setName("B")
                      .setTag(1)
                      .addAllMask(Arrays.asList(false, true, false))
                      .build(),
                  Instruction.newBuilder()
                      .setName("C")
                      .setTag(2)
                      .addAllMask(Arrays.asList(true, true))
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder().setName("A").setTag(0).addAllMask(Arrays.asList()).build());
      expected.put(
          1,
          Instruction.newBuilder()
              .setName("B")
              .setTag(1)
              .addAllMask(Arrays.asList(false, true, false))
              .build());
      expected.put(
          2,
          Instruction.newBuilder()
              .setName("C")
              .setTag(2)
              .addAllMask(Arrays.asList(true, true))
              .build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // A single instruction with a single mask and a memory access
    public void testSingleInstructionOneMaskWithMemoryAccess() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addAllMask(Arrays.asList(false))
                      .addNarrowRead(12)
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder()
              .setName("A")
              .setTag(0)
              .addAllMask(Arrays.asList(false))
              .addNarrowRead(12)
              .build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // A single instruction with multiple masks and a memory access
      public void testSingleInstructionMultipleMasksWithMemoryAccess() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addAllMask(Arrays.asList(false, true, true))
                      .addNarrowRead(12)
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder()
              .setName("A")
              .setTag(0)
              .addAllMask(Arrays.asList(false, true, true))
              .addNarrowRead(12)
              .build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // Multiple instructions with a single mask and memory accesses
    public void testMultipleInstructionsOneMaskWithMemoryAccess() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addNarrowWrite(12)
                      .addAllMask(Arrays.asList(true))
                      .build(),
                  Instruction.newBuilder()
                      .setName("B")
                      .setTag(1)
                      .addNarrowRead(13)
                      .addAllMask(Arrays.asList(false))
                      .build(),
                  Instruction.newBuilder()
                      .setName("C")
                      .setTag(2)
                      .addWideRead(14)
                      .addAllMask(Arrays.asList(true))
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder()
              .setName("A")
              .setTag(0)
              .addNarrowWrite(12)
              .addAllMask(Arrays.asList(true))
              .build());
      expected.put(
          1,
          Instruction.newBuilder()
              .setName("B")
              .setTag(1)
              .addNarrowRead(13)
              .addAllMask(Arrays.asList(false))
              .build());
      expected.put(
          2,
          Instruction.newBuilder()
              .setName("C")
              .setTag(2)
              .addWideRead(14)
              .addAllMask(Arrays.asList(true))
              .build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // Multiple instructions with a single mask and with/without a memory access
    public void testMultipleInstructionsOneMaskMixedMemoryAccess() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addNarrowWrite(12)
                      .addAllMask(Arrays.asList(true))
                      .build(),
                  Instruction.newBuilder()
                      .setName("B")
                      .setTag(1)
                      .addAllMask(Arrays.asList(false))
                      .build(),
                  Instruction.newBuilder()
                      .setName("C")
                      .setTag(2)
                      .addWideRead(14)
                      .addAllMask(Arrays.asList(true))
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder()
              .setName("A")
              .setTag(0)
              .addNarrowWrite(12)
              .addAllMask(Arrays.asList(true))
              .build());
      expected.put(
          1,
          Instruction.newBuilder().setName("B").setTag(1).addAllMask(Arrays.asList(false)).build());
      expected.put(
          2,
          Instruction.newBuilder()
              .setName("C")
              .setTag(2)
              .addWideRead(14)
              .addAllMask(Arrays.asList(true))
              .build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // Multiple instructions with multiple masks and memory accesses
    public void testMultipleInstructionsMultipleMasksWithMemoryAccess() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addNarrowWrite(12)
                      .addAllMask(Arrays.asList(true, false, true))
                      .build(),
                  Instruction.newBuilder()
                      .setName("B")
                      .setTag(1)
                      .addNarrowRead(13)
                      .addAllMask(Arrays.asList(false, false))
                      .build(),
                  Instruction.newBuilder()
                      .setName("C")
                      .setTag(2)
                      .addWideRead(14)
                      .addAllMask(Arrays.asList(true, false, true, false))
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder()
              .setName("A")
              .setTag(0)
              .addNarrowWrite(12)
              .addAllMask(Arrays.asList(true, false, true))
              .build());
      expected.put(
          1,
          Instruction.newBuilder()
              .setName("B")
              .setTag(1)
              .addNarrowRead(13)
              .addAllMask(Arrays.asList(false, false))
              .build());
      expected.put(
          2,
          Instruction.newBuilder()
              .setName("C")
              .setTag(2)
              .addWideRead(14)
              .addAllMask(Arrays.asList(true, false, true, false))
              .build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }

    @Test
    // Multiple instructions with multiple masks and with/without memory access
    public void testMultipleInstructionsMultipleMasksMixedMemoryAccess() 
        throws IllegalAccessException, InvocationTargetException {
      testInstructions =
          new ArrayList<>(
              Arrays.asList(
                  Instruction.newBuilder()
                      .setName("A")
                      .setTag(0)
                      .addNarrowWrite(12)
                      .addAllMask(Arrays.asList(true, false, true))
                      .build(),
                   Instruction.newBuilder()
                      .setName("B")
                      .setTag(1)
                      .addAllMask(Arrays.asList(false, false))
                      .build(),
                  Instruction.newBuilder()
                      .setName("C")
                      .setTag(2)
                      .addWideRead(14)
                      .addAllMask(Arrays.asList(true, false, true, false))
                      .build()));

      expected.put(
          0,
          Instruction.newBuilder()
              .setName("A")
              .setTag(0)
              .addNarrowWrite(12)
              .addAllMask(Arrays.asList(true, false, true))
              .build());
      expected.put(
          1,
          Instruction.newBuilder()
              .setName("B")
              .setTag(1)
              .addAllMask(Arrays.asList(false, false))
              .build());
      expected.put(
          2,
          Instruction.newBuilder()
              .setName("C")
              .setTag(2)
              .addWideRead(14)
              .addAllMask(Arrays.asList(true, false, true, false))
              .build());

      instructions.set(validation, testInstructions);
      relateInstructionTagToInstructionTable.invoke(validation, null);

      assertEquals(expected, instructionTagtoInstruction.get(validation));
    }
  }

  public static class TestReadValidate {
    private static Validation validation;
    private static Method readValidation;
    private static Field numTiles;
    private int[][] narrow;
    private int[][] wide;

    private final Instruction INSTRUCTION_ONE =
        Instruction.newBuilder()
          .setTag(1)
          .addAllMask(
              Arrays.asList(
                  true, false, false, false, true, true, true, true, false, false, false, false,
                  false, true, true, true))
          .addNarrowRead(189)
          .build();

    private final TraceEvent TRACE_ONE = 
        TraceEvent.newBuilder()
            .setAccessType(TraceEvent.AccessType.NARROW_READ)
            .setInstructionTag(1)
            .setAddress(189)
            .setBytes(4)
            .build();

    private final Instruction INSTRUCTION_TWO =
        Instruction.newBuilder()
            .setTag(12)
            .addAllMask(
                Arrays.asList(
                    true, false, false, false, true, true, true, true, false, false, false, false,
                    false, false, false, false))
            .addNarrowRead(796)
            .addWideRead(10240)
            .build();

    private final TraceEvent TRACE_TWO_ONE = 
        TraceEvent.newBuilder()
            .setAccessType(TraceEvent.AccessType.NARROW_READ)
            .setInstructionTag(12)
            .setAddress(796)
            .setBytes(4)
            .build();

    private final TraceEvent TRACE_TWO_TWO = 
        TraceEvent.newBuilder()
            .setAccessType(TraceEvent.AccessType.WIDE_READ)
            .setInstructionTag(12)
            .setAddress(10240)
            .setBytes(4)
            .build();

    private final Instruction INSTRUCTION_THREE =
        Instruction.newBuilder()
            .setTag(5)
            .addAllMask(
                Arrays.asList(
                    true, true, true, true, true, true, true, true, false, false, false, false,
                    false, true, true, true))
            .addWideRead(2437)
            .build();

    private final TraceEvent TRACE_THREE = 
        TraceEvent.newBuilder()
            .setAccessType(TraceEvent.AccessType.WIDE_READ)
            .setInstructionTag(5)
            .setAddress(564)
            .setBytes(4)
            .build();

    private final TraceEvent TRACE_FOUR = 
        TraceEvent.newBuilder()
            .setAccessType(TraceEvent.AccessType.WIDE_WRITE)
            .setInstructionTag(5)
            .setAddress(564)
            .setBytes(4)
            .build();

    @Before
    public void setUp() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
      MemaccessCheckerData.Builder proto = MemaccessCheckerData.newBuilder();
      validation = new Validation(proto.build());

      readValidation = 
          Validation.class.getDeclaredMethod(
              "readValidation", 
              int[][].class, 
              int[][].class, 
              List.class, 
              int.class, 
              TraceEvent.class);

      numTiles = Validation.class.getDeclaredField("numTiles");

      readValidation.setAccessible(true);
      numTiles.setAccessible(true);

      numTiles.set(validation, 16);
      narrow = new int[16][128 * 1024];
      wide = new int[16][256 * 1024];
    }

    // Tests a valid read trace
    @Test
    public void testValidRead() throws InvalidTensorReadException, IllegalAccessException, InvocationTargetException {
      int i = 0;

      for (boolean mask : INSTRUCTION_ONE.getMaskList()) {
          if (mask) {
            Arrays.fill(
                narrow[i], 
                INSTRUCTION_ONE.getNarrowRead(0), 
                INSTRUCTION_ONE.getNarrowRead(0) + TRACE_ONE.getBytes() - 1, 
                0);
          }

          i++;
      }

      readValidation.invoke(validation, narrow, wide, INSTRUCTION_ONE.getMaskList(), 0, TRACE_ONE);
    }

    // Tests two valid read traces in a single instruction
    @Test
    public void testTwoValidRead() 
        throws InvalidTensorReadException, IllegalAccessException, InvocationTargetException {
      int i = 0;

      for (boolean mask : INSTRUCTION_TWO.getMaskList()) {
          if (mask) {
              Arrays.fill(
                  narrow[i], 
                  INSTRUCTION_TWO.getNarrowRead(0), 
                  INSTRUCTION_TWO.getNarrowRead(0) + TRACE_TWO_ONE.getBytes() - 1, 
                  0);
              Arrays.fill(
                  wide[i], 
                  INSTRUCTION_TWO.getWideRead(0), 
                  INSTRUCTION_TWO.getWideRead(0) + TRACE_TWO_TWO.getBytes() - 1, 
                  0);
          }

          i++;
      }

            
      readValidation.invoke(
          validation, narrow, wide, INSTRUCTION_TWO.getMaskList(), 0, TRACE_TWO_ONE);

            
      readValidation.invoke(
          validation, narrow, wide, INSTRUCTION_TWO.getMaskList(), 0, TRACE_TWO_TWO);
    }

    // Tests an invalid read trace
    @Test (expected = InvalidTensorReadException.class)
    public void testInvalidRead() 
        throws InvalidTensorReadException, IllegalAccessException, InvocationTargetException, 
            Throwable {
      int i = 0;

      for (boolean mask : INSTRUCTION_THREE.getMaskList()) {
        if (mask) {
            Arrays.fill(
                narrow[i], 
                INSTRUCTION_THREE.getWideRead(0), 
                INSTRUCTION_THREE.getWideRead(0) + TRACE_THREE.getBytes() - 1, 
                0);
        }

        i++;
      }

      try {
        readValidation.invoke(
            validation, narrow, wide, INSTRUCTION_THREE.getMaskList(), 1, TRACE_THREE);
      } catch (InvocationTargetException e) {
          throw e.getTargetException();
      }               
    }

    // Tests a valid and invalid read trace in a single instruction
    @Test (expected = InvalidTensorReadException.class)
    public void testValidandInvalidRead() 
        throws InvalidTensorReadException, IllegalAccessException, InvocationTargetException, 
            Throwable {
      int i = 0;

      for (boolean mask : INSTRUCTION_TWO.getMaskList()) {
        if (mask) {
          Arrays.fill(
              narrow[i], 
              INSTRUCTION_TWO.getNarrowRead(0), 
              INSTRUCTION_TWO.getNarrowRead(0) + TRACE_TWO_ONE.getBytes() - 1, 
              0);
        }

        i++;
      }

      try {
        readValidation.invoke(
            validation, narrow, wide, INSTRUCTION_TWO.getMaskList(), 0, TRACE_TWO_ONE);
            
        readValidation.invoke(
            validation, narrow, wide, INSTRUCTION_TWO.getMaskList(),  1, TRACE_TWO_TWO);
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }               
    }

    @Test (expected = Exception.class)
    public void testEmptyMaskList() 
        throws IllegalAccessException, InvocationTargetException, 
            Throwable {
      try {
        readValidation.invoke(
            validation, narrow, wide, new ArrayList<Boolean>(), 0, TRACE_TWO_ONE);
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }  
    }

    @Test (expected = Exception.class)
    public void testNonAllocatedArrays() 
        throws IllegalAccessException, InvocationTargetException, 
            Throwable {

      try {
        readValidation.invoke(
            validation, narrow, wide, INSTRUCTION_TWO.getMaskList(), 1, TRACE_TWO_ONE);
            
        readValidation.invoke(
            validation, narrow, wide, INSTRUCTION_TWO.getMaskList(),  1, TRACE_TWO_TWO);
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }
    }

    @Test
    public void testIncorrectAccessType() 
        throws IllegalAccessException, InvocationTargetException, 
            Throwable {

        readValidation.invoke(
            validation, narrow, wide, INSTRUCTION_THREE.getMaskList(), 0, TRACE_FOUR);
    }
  }

  public static class TestGetTensor {
    private static Validation validation;
    private static Method getTensor;
    private Map<Integer, TensorAllocation> tensorLabelToTensorAllocationNarrow;
    private Map<Integer, TensorAllocation> tensorLabelToTensorAllocationWide;
    private static int expectedTensor;
    private static int traceAddress;
    private static List<Integer> narrowReadTensorList;
    private static List<Integer> narrowWriteTensorList;
    private static List<Integer> wideReadTensorList;
    private static List<Integer> wideWriteTensorList;
    private static TensorAllocation.Builder alloBuilder = TensorAllocation.newBuilder();

    private static TensorAllocation ALLO_1;
    private static TensorAllocation ALLO_2;
    private static TensorAllocation ALLO_3;
    private static TensorAllocation ALLO_4;
    private static TensorAllocation ALLO_5;
    private static TensorAllocation ALLO_6;
    private static TensorAllocation ALLO_7;
    private static TensorAllocation ALLO_8;
    private static TensorAllocation ALLO_9;

    @Before
    public void setUp() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
      ALLO_1 = alloBuilder.setTensorLabel(1).setBaseAddress(1024).setSize(5).build();
      alloBuilder = alloBuilder.clear();

      ALLO_2 = alloBuilder.setTensorLabel(6).setBaseAddress(367).setSize(10).build();
      alloBuilder = alloBuilder.clear();

      ALLO_3 = alloBuilder.setTensorLabel(17).setBaseAddress(29).setSize(11).build();
      alloBuilder = alloBuilder.clear();

      ALLO_4 = alloBuilder.setTensorLabel(5).setBaseAddress(28).setSize(9).build();
      alloBuilder = alloBuilder.clear();

      ALLO_5 = alloBuilder.setTensorLabel(13).setBaseAddress(1029).setSize(3).build();
      alloBuilder = alloBuilder.clear();

      ALLO_6 = alloBuilder.setTensorLabel(27).setBaseAddress(1329).setSize(19).build();
      alloBuilder = alloBuilder.clear();

      ALLO_7 = alloBuilder.setTensorLabel(19).setBaseAddress(59).setSize(26).build();
      alloBuilder = alloBuilder.clear();

      ALLO_8 = alloBuilder.setTensorLabel(2).setBaseAddress(169).setSize(20).build();
      alloBuilder = alloBuilder.clear();

      ALLO_9 = alloBuilder.setTensorLabel(16).setBaseAddress(476).setSize(3).build();
      alloBuilder = alloBuilder.clear();

      MemaccessCheckerData.Builder proto = MemaccessCheckerData.newBuilder();
      validation = new Validation(proto.build());

      getTensor = 
          Validation.class.getDeclaredMethod(
              "getTensor",
              List.class,
              int.class, 
              Map.class,
              String.class);

      getTensor.setAccessible(true);

      narrowReadTensorList = 
          new ArrayList<>(Arrays.asList(ALLO_1.getTensorLabel(), ALLO_2.getTensorLabel(), ALLO_7.getTensorLabel()));

      narrowWriteTensorList = new ArrayList<Integer>(Arrays.asList(ALLO_6.getTensorLabel(), ALLO_8.getTensorLabel()));

      wideReadTensorList = new ArrayList<Integer>(Arrays.asList(ALLO_3.getTensorLabel(), ALLO_9.getTensorLabel()));

      wideWriteTensorList = new ArrayList<Integer>(Arrays.asList(ALLO_4.getTensorLabel(), ALLO_5.getTensorLabel()));

      tensorLabelToTensorAllocationNarrow = new Hashtable<>();
      tensorLabelToTensorAllocationNarrow.put(ALLO_1.getTensorLabel(), ALLO_1);
      tensorLabelToTensorAllocationNarrow.put(ALLO_2.getTensorLabel(), ALLO_2);
      tensorLabelToTensorAllocationNarrow.put(ALLO_7.getTensorLabel(), ALLO_7);
      tensorLabelToTensorAllocationNarrow.put(ALLO_6.getTensorLabel(), ALLO_6);
      tensorLabelToTensorAllocationNarrow.put(ALLO_8.getTensorLabel(), ALLO_8);

      tensorLabelToTensorAllocationWide = new Hashtable<>();
      tensorLabelToTensorAllocationWide.put(ALLO_3.getTensorLabel(), ALLO_3);
      tensorLabelToTensorAllocationWide.put(ALLO_9.getTensorLabel(), ALLO_9);
      tensorLabelToTensorAllocationWide.put(ALLO_4.getTensorLabel(), ALLO_4);
      tensorLabelToTensorAllocationWide.put(ALLO_5.getTensorLabel(), ALLO_5);
    }

    // ALLO_2
    @Test
    public void testCorrectNarrowRead() throws IllegalAccessException, InvocationTargetException {
      expectedTensor = ALLO_2.getTensorLabel();

      // Base address
      traceAddress = ALLO_2.getBaseAddress();     
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation,
                  narrowReadTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationNarrow,
                  "Narrow"));

      // Mid address
      traceAddress = ALLO_2.getBaseAddress() + ALLO_2.getSize() / 2;
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  narrowReadTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationNarrow,
                  "Narrow"));

      // End address
      traceAddress = ALLO_2.getBaseAddress() + ALLO_2.getSize() - 1;
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  narrowReadTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationNarrow,
                  "Narrow"));
    }

    // ALLO_8
    @Test
    public void testCorrectNarrowWrite() throws IllegalAccessException, InvocationTargetException {
      expectedTensor = ALLO_8.getTensorLabel();

      // Base address
      traceAddress = ALLO_8.getBaseAddress();     
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  narrowWriteTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationNarrow,
                  "Narrow"));

      // Mid address
      traceAddress = ALLO_8.getBaseAddress() + ALLO_8.getSize() / 2;
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  narrowWriteTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationNarrow,
                  "Narrow"));

      // End address
      traceAddress = ALLO_8.getBaseAddress() + ALLO_8.getSize() - 1;
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  narrowWriteTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationNarrow,
                  "Narrow"));
    }

    // ALLO_3
    @Test
    public void testCorrectWideRead() throws IllegalAccessException, InvocationTargetException {
      expectedTensor = ALLO_3.getTensorLabel();

      // Base address
      traceAddress = ALLO_3.getBaseAddress();     
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  wideReadTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationWide, 
                  "Wide"));

      // Mid address
      traceAddress = ALLO_3.getBaseAddress() + ALLO_3.getSize() / 2;
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  wideReadTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationWide, 
                  "Wide"));

      // End address
      traceAddress = ALLO_3.getBaseAddress() + ALLO_3.getSize() - 1;
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  wideReadTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationWide, 
                  "Wide"));
    }

    // ALLO_5
    @Test
    public void testCorrectWideWrite() throws IllegalAccessException, InvocationTargetException {
      expectedTensor = ALLO_5.getTensorLabel();

      // Base address
      traceAddress = ALLO_5.getBaseAddress();     
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                validation, 
                wideWriteTensorList, 
                traceAddress, 
                tensorLabelToTensorAllocationWide,
                "Wide"));

      // Mid address
      traceAddress = ALLO_5.getBaseAddress() + ALLO_5.getSize() / 2;
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                validation, 
                wideWriteTensorList, 
                traceAddress, 
                tensorLabelToTensorAllocationWide,
                "Wide"));

      // End address
      traceAddress = ALLO_5.getBaseAddress() + ALLO_5.getSize() - 1;
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  wideWriteTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationWide,
                  "Wide"));
    }

    // ALLO_7
    @Test
    public void testIncorrectNarrowReadBeforeBaseAddress() 
        throws IllegalAccessException, InvocationTargetException {
      expectedTensor = -1;

      // Before base address
      traceAddress = ALLO_7.getBaseAddress() - 1;

      assertEquals(
        expectedTensor, 
        (int) 
            getTensor.invoke(
                validation, 
                narrowReadTensorList, 
                traceAddress, 
                tensorLabelToTensorAllocationNarrow,
                "Narrow"));
    }
      
    // ALLO_7
    @Test
    public void testIncorrectNarrowReadAfterEndAddress() 
        throws IllegalAccessException, InvocationTargetException {
      expectedTensor = -1;

      // After end address
      traceAddress = ALLO_7.getBaseAddress() + ALLO_7.getSize();
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  narrowReadTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationNarrow,
                  "Narrow")); 
    }

    // ALLO_6
    @Test
    public void testIncorrectNarrowWriteBeforeBaseAddress() 
        throws IllegalAccessException, InvocationTargetException {
      expectedTensor = -1;

      // Before base address
      traceAddress = ALLO_6.getBaseAddress() - 1;     
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                validation, 
                narrowWriteTensorList, 
                traceAddress, 
                tensorLabelToTensorAllocationNarrow,
                "Narrow"));
    }
      
    // ALLO_6
    @Test
    public void testIncorrectNarrowWriteAfterEndAddress() 
        throws IllegalAccessException, InvocationTargetException {
      expectedTensor = -1;

      // After end address
      traceAddress = ALLO_6.getBaseAddress() + ALLO_6.getSize();
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                validation, 
                narrowWriteTensorList, 
                traceAddress, 
                tensorLabelToTensorAllocationNarrow,
                "Narrow"));
    }

    // ALLO_9
    @Test
    public void testIncorrectWideReadBeforeBaseAddress() 
        throws IllegalAccessException, InvocationTargetException {
      expectedTensor = -1;

      // Before base address
      traceAddress = ALLO_9.getBaseAddress() - 1;     
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                   wideReadTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationWide, 
                  "Wide"));
    }
      
    // ALLO_9
    @Test
    public void testIncorrectWideReadAfterEndAddress() 
        throws IllegalAccessException, InvocationTargetException {
      expectedTensor = -1;

      // After end address
      traceAddress = ALLO_9.getBaseAddress() + ALLO_9.getSize();
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  wideReadTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationWide, 
                  "Wide"));
    }

    // ALLO_4
    @Test
    public void testIncorrectWideWriteBeforeBaseAddress() 
        throws IllegalAccessException, InvocationTargetException {
      expectedTensor = -1;

      // Before base address
      traceAddress = ALLO_4.getBaseAddress() - 1;     
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  wideWriteTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationWide,
                  "Wide"));
    }

    // ALLO_4
    @Test
    public void testIncorrectWideWriteAfterEndAddress() 
        throws IllegalAccessException, InvocationTargetException {
      expectedTensor = -1;

      // After end address
      traceAddress = ALLO_4.getBaseAddress() + ALLO_4.getSize();
      assertEquals(
          expectedTensor, 
          (int) 
              getTensor.invoke(
                  validation, 
                  wideWriteTensorList, 
                  traceAddress, 
                  tensorLabelToTensorAllocationWide,
                  "Wide")); 
    }

    // ALLO_2
    @Test (expected = Exception.class)
    public void testNarrowReadEmptyAllocationTable() 
        throws IllegalAccessException, InvocationTargetException, Throwable {
      traceAddress = ALLO_2.getBaseAddress();

      try {
        getTensor.invoke(
          validation,
          narrowReadTensorList,
          traceAddress,
          new Hashtable<Integer, TensorAllocation>(),
          "Narrow");
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }
    }

    // ALLO_8
    @Test (expected = Exception.class)
    public void testNarrowWriteEmptyAllocationTable() 
        throws IllegalAccessException, InvocationTargetException, Throwable {
      traceAddress = ALLO_8.getBaseAddress();

      try {
        getTensor.invoke(
          validation,
          narrowReadTensorList,
          traceAddress,
          new Hashtable<Integer, TensorAllocation>(),
          "Narrow");
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }
    }

    // ALLO_3
    @Test (expected = Exception.class)
    public void testWideReadEmptyAllocationTable() 
        throws IllegalAccessException, InvocationTargetException, Throwable {
      traceAddress = ALLO_3.getBaseAddress();

      try {
        getTensor.invoke(
          validation,
          narrowReadTensorList,
          traceAddress,
          new Hashtable<Integer, TensorAllocation>(),
          "Wide");
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }
    }

    // ALLO_5
    @Test (expected = Exception.class)
    public void testWideWriteEmptyAllocationTable() 
        throws IllegalAccessException, InvocationTargetException, Throwable {
      traceAddress = ALLO_5.getBaseAddress();

      try {
        getTensor.invoke(
          validation,
          narrowReadTensorList,
          traceAddress,
          new Hashtable<Integer, TensorAllocation>(),
          "Wide");
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }
    }
  }

  public static class TestRelateTensorLabelToTensorToTensorAllocation {
    private static Validation validation;
    private static Method relateTensorLabelToTensorAllocation;
    
    private static TensorLayerAllocationTable layer1NarrowAllocations;
    private static TensorLayerAllocationTable layer1WideAllocations;
    private static TensorLayerAllocationTable layer2NarrowAllocations;
    private static TensorLayerAllocationTable layer2WideAllocations;
    
    private TensorTileAllocationTable tile1Allocations;
    private TensorTileAllocationTable tile2Allocations;
    private TensorTileAllocationTable tile3Allocations;
    
    private static Map<Integer, TensorAllocation> expectedTableTwoLayers;
    private static Map<Integer, TensorAllocation> expectedTableLayerOne;
    private static Map<Integer, TensorAllocation> expectedTableLayerTwo;
    
    private static TensorAllocation.Builder alloBuilder = TensorAllocation.newBuilder();

    private static TensorAllocation ALLO_1;
    private static TensorAllocation ALLO_2;
    private static TensorAllocation ALLO_3;
    private static TensorAllocation ALLO_4;
    private static TensorAllocation ALLO_5;
    private static TensorAllocation ALLO_6;
    private static TensorAllocation ALLO_7;
    private static TensorAllocation ALLO_8;
    private static TensorAllocation ALLO_9;

    @Before
    public void setUp() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
      ALLO_1 = alloBuilder.setTensorLabel(1).setBaseAddress(1024).setSize(5).build();
      alloBuilder = alloBuilder.clear();

      ALLO_2 = alloBuilder.setTensorLabel(6).setBaseAddress(367).setSize(10).build();
      alloBuilder = alloBuilder.clear();

      ALLO_3 = alloBuilder.setTensorLabel(17).setBaseAddress(29).setSize(11).build();
      alloBuilder = alloBuilder.clear();

      ALLO_4 = alloBuilder.setTensorLabel(5).setBaseAddress(28).setSize(9).build();
      alloBuilder = alloBuilder.clear();

      ALLO_5 = alloBuilder.setTensorLabel(13).setBaseAddress(1029).setSize(3).build();
      alloBuilder = alloBuilder.clear();

      ALLO_6 = alloBuilder.setTensorLabel(27).setBaseAddress(1329).setSize(19).build();
      alloBuilder = alloBuilder.clear();

      ALLO_7 = alloBuilder.setTensorLabel(19).setBaseAddress(59).setSize(26).build();
      alloBuilder = alloBuilder.clear();

      ALLO_8 = alloBuilder.setTensorLabel(2).setBaseAddress(169).setSize(20).build();
      alloBuilder = alloBuilder.clear();

      ALLO_9 = alloBuilder.setTensorLabel(16).setBaseAddress(476).setSize(3).build();
      alloBuilder = alloBuilder.clear();

      MemaccessCheckerData.Builder proto = MemaccessCheckerData.newBuilder();
      validation = new Validation(proto.build());

      relateTensorLabelToTensorAllocation = 
          Validation.class.getDeclaredMethod("relateTensorLabelToTensorAllocation", List.class);

      relateTensorLabelToTensorAllocation.setAccessible(true);

      TensorLayerAllocationTable.Builder layerBuilder = TensorLayerAllocationTable.newBuilder();
      TensorTileAllocationTable.Builder tileBuilder = TensorTileAllocationTable.newBuilder();

      List<TensorAllocation> allocations = new ArrayList<>();
      // Layer 1, narrow

      // Tile 1
      allocations = new ArrayList<TensorAllocation>(Arrays.asList(ALLO_1, ALLO_2));
      tile1Allocations = tileBuilder.addAllTensorAllocation(allocations).build();
      tileBuilder = tileBuilder.clear();

      // Tile 2
      allocations = new ArrayList<TensorAllocation>();
      tile2Allocations = tileBuilder.addAllTensorAllocation(allocations).build();
      tileBuilder = tileBuilder.clear();

      // Tile 3
      allocations = new ArrayList<TensorAllocation>(Arrays.asList(ALLO_6, ALLO_7));
      tile3Allocations = tileBuilder.addAllTensorAllocation(allocations).build();
      tileBuilder = tileBuilder.clear();

      layer1NarrowAllocations = 
          layerBuilder
              .setLayer("A")
              .addAllTensorTileAllocation(
                  new ArrayList<TensorTileAllocationTable>(
                      Arrays.asList(tile1Allocations, tile2Allocations, tile3Allocations)))
              .build();

      layerBuilder = layerBuilder.clear();

      // Layer 1, wide

      // Tile 1
      allocations = new ArrayList<TensorAllocation>(Arrays.asList(ALLO_3));
      tile1Allocations = tileBuilder.addAllTensorAllocation(allocations).build();
      tileBuilder = tileBuilder.clear();

      // Tile 2
      allocations = new ArrayList<TensorAllocation>(Arrays.asList(ALLO_4, ALLO_5));
      tile2Allocations = tileBuilder.addAllTensorAllocation(allocations).build();
      tileBuilder = tileBuilder.clear();

      // Tile 3
      allocations = new ArrayList<TensorAllocation>(Arrays.asList());
      tile3Allocations = tileBuilder.addAllTensorAllocation(allocations).build();
      tileBuilder = tileBuilder.clear();

      layer1WideAllocations = 
          layerBuilder
              .setLayer("A")
              .addAllTensorTileAllocation(
                  new ArrayList<TensorTileAllocationTable>(
                      Arrays.asList(tile1Allocations, tile2Allocations, tile3Allocations)))
              .build();

      layerBuilder = layerBuilder.clear();

      // Layer 2, narrow

      // Tile 1
      allocations = new ArrayList<TensorAllocation>(Arrays.asList(ALLO_8));
      tile1Allocations = tileBuilder.addAllTensorAllocation(allocations).build();
      tileBuilder = tileBuilder.clear();


      // Tile 2
      allocations = new ArrayList<TensorAllocation>(Arrays.asList());
      tile2Allocations = tileBuilder.addAllTensorAllocation(allocations).build();
      tileBuilder = tileBuilder.clear();
      allocations = new ArrayList<>();

      // Tile 3
      allocations = new ArrayList<TensorAllocation>(Arrays.asList());
      tile3Allocations = tileBuilder.addAllTensorAllocation(allocations).build();
      tileBuilder = tileBuilder.clear();
      allocations = new ArrayList<>();

      layer2NarrowAllocations = 
          layerBuilder
              .setLayer("B")
              .addAllTensorTileAllocation(
                  new ArrayList<TensorTileAllocationTable>(
                      Arrays.asList(tile1Allocations, tile2Allocations, tile3Allocations)))
              .build();

      layerBuilder = layerBuilder.clear();

      // Layer 2, wide

      // Tile 1
      allocations = new ArrayList<TensorAllocation>(Arrays.asList());
      tile1Allocations = tileBuilder.addAllTensorAllocation(allocations).build();
      tileBuilder = tileBuilder.clear();
      allocations = new ArrayList<>();

      // Tile 2
      allocations = new ArrayList<TensorAllocation>(Arrays.asList(ALLO_9));
      tile2Allocations = tileBuilder.addAllTensorAllocation(allocations).build();
      tileBuilder = tileBuilder.clear();
      allocations = new ArrayList<>();

      // Tile 3
      allocations = new ArrayList<TensorAllocation>(Arrays.asList());
      tile3Allocations = tileBuilder.addAllTensorAllocation(allocations).build();
      tileBuilder = tileBuilder.clear();
      allocations = new ArrayList<>();

      layer2WideAllocations = 
          layerBuilder
              .setLayer("B")
              .addAllTensorTileAllocation(
                  new ArrayList<TensorTileAllocationTable>(
                      Arrays.asList(tile1Allocations, tile2Allocations, tile3Allocations)))
              .build();

      layerBuilder = layerBuilder.clear();

      expectedTableTwoLayers = new Hashtable<>();
      expectedTableTwoLayers.put(ALLO_1.getTensorLabel(), ALLO_1);
      expectedTableTwoLayers.put(ALLO_2.getTensorLabel(), ALLO_2);
      expectedTableTwoLayers.put(ALLO_7.getTensorLabel(), ALLO_7);
      expectedTableTwoLayers.put(ALLO_6.getTensorLabel(), ALLO_6);
      expectedTableTwoLayers.put(ALLO_8.getTensorLabel(), ALLO_8);
      expectedTableTwoLayers.put(ALLO_3.getTensorLabel(), ALLO_3);
      expectedTableTwoLayers.put(ALLO_9.getTensorLabel(), ALLO_9);
      expectedTableTwoLayers.put(ALLO_4.getTensorLabel(), ALLO_4);
      expectedTableTwoLayers.put(ALLO_5.getTensorLabel(), ALLO_5);

      expectedTableLayerOne = new Hashtable<>();
      expectedTableLayerOne.put(ALLO_1.getTensorLabel(), ALLO_1);
      expectedTableLayerOne.put(ALLO_2.getTensorLabel(), ALLO_2);
      expectedTableLayerOne.put(ALLO_7.getTensorLabel(), ALLO_7);
      expectedTableLayerOne.put(ALLO_6.getTensorLabel(), ALLO_6);
      expectedTableLayerOne.put(ALLO_4.getTensorLabel(), ALLO_4);
      expectedTableLayerOne.put(ALLO_3.getTensorLabel(), ALLO_3);
      expectedTableLayerOne.put(ALLO_5.getTensorLabel(), ALLO_5);

      expectedTableLayerTwo = new Hashtable<>();
      expectedTableLayerTwo.put(ALLO_8.getTensorLabel(), ALLO_8);
      expectedTableLayerTwo.put(ALLO_9.getTensorLabel(), ALLO_9);
    }

    @Test
    public void testEmptyLayer() throws IllegalAccessException, InvocationTargetException {
      TensorLayerAllocationTable.Builder layerBuilder = TensorLayerAllocationTable.newBuilder();

      TensorLayerAllocationTable emptyLayer = 
          layerBuilder
              .setLayer("C")
              .addAllTensorTileAllocation(new ArrayList<TensorTileAllocationTable>(Arrays.asList()))
              .build();

      assertEquals(
          new Hashtable<Integer, TensorAllocation>(), 
          relateTensorLabelToTensorAllocation.invoke(
              validation, new ArrayList<TensorLayerAllocationTable>(Arrays.asList(emptyLayer))));
    }

    @Test
    public void testLayerOne() throws IllegalAccessException, InvocationTargetException {
      assertEquals(
          expectedTableLayerOne, 
          relateTensorLabelToTensorAllocation.invoke(
              validation, 
              new ArrayList<TensorLayerAllocationTable>(
                  Arrays.asList(layer1NarrowAllocations, layer1WideAllocations))));
    }

    @Test
    public void testLayerTwo() throws IllegalAccessException, InvocationTargetException {
      assertEquals(
          expectedTableLayerTwo, 
          relateTensorLabelToTensorAllocation.invoke(
              validation, 
              new ArrayList<TensorLayerAllocationTable>(
                  Arrays.asList(layer2NarrowAllocations, layer2WideAllocations))));
    }

    @Test
    public void testTwoLayers() throws IllegalAccessException, InvocationTargetException {
      assertEquals(
          expectedTableTwoLayers, 
          relateTensorLabelToTensorAllocation.invoke(
              validation, 
              new ArrayList<TensorLayerAllocationTable>(
                  Arrays.asList(
                      layer1NarrowAllocations, 
                      layer1WideAllocations, 
                      layer2NarrowAllocations, 
                      layer2WideAllocations))));
    }
  }
}
