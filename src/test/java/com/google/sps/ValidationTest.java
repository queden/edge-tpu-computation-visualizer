package com.google.sps;

import com.google.sps.exceptions.*;
import com.google.sps.proto.MemaccessCheckerDataProto.*;
import com.google.sps.structures.Delta;

import java.lang.NoSuchMethodException;
import java.lang.NoSuchFieldException;
import java.lang.IllegalAccessException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

@RunWith(ValidationTest.class)
@Suite.SuiteClasses({
    ValidationTest.TestInstructionTagtoInstructionTable.class,
    ValidationTest.TestReadValidate.class,
    ValidationTest.TestGetTensor.class,
    ValidationTest.TestGetTraceTensor.class,
    ValidationTest.TestRelateTensorsToInstructions.class,
    ValidationTest.TestGetLayerToInstructionTable.class,
    ValidationTest.TestWriteValidation.class,
    ValidationTest.TestValidateTraceEvents.class})
public final class ValidationTest extends Suite {
  private static Class[] classes = {Validation.class};

  public ValidationTest(Class<?> klass, RunnerBuilder builder) throws InitializationError {
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
    private static final String layer = "input";

    private final Instruction INSTRUCTION_ONE =
        Instruction.newBuilder()
          .setTag(1)
          .addAllMask(
              Arrays.asList(
                  true, false, false, false, true, true, true, true, false, false, false, false,
                  false, true, true, true))
          .addNarrowRead(189)
          .setLayer(layer)
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
            .setLayer(layer)
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
            .setLayer(layer)
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
              String.class, 
              List.class, 
              int.class, 
              TraceEvent.class);

      numTiles = Validation.class.getDeclaredField("numTiles");

      readValidation.setAccessible(true);
      numTiles.setAccessible(true);

      numTiles.set(validation, 16);
      narrow = new int[16][128 * 1024];
      wide = new int[16][256 * 1024];

      Field mNarrow = Validation.class.getDeclaredField("narrow");
      Field mWide = Validation.class.getDeclaredField("wide");
      mNarrow.setAccessible(true);
      mWide.setAccessible(true);

      mNarrow.set(validation, narrow);
      mWide.set(validation, wide);
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

      readValidation.invoke(validation, layer, INSTRUCTION_ONE.getMaskList(), 0, TRACE_ONE);
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
          validation, layer, INSTRUCTION_TWO.getMaskList(), 0, TRACE_TWO_ONE);

            
      readValidation.invoke(
          validation, layer, INSTRUCTION_TWO.getMaskList(), 0, TRACE_TWO_TWO);
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
            validation, layer, INSTRUCTION_THREE.getMaskList(), 1, TRACE_THREE);
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
            validation, layer, INSTRUCTION_TWO.getMaskList(), 0, TRACE_TWO_ONE);
            
        readValidation.invoke(
            validation, layer, INSTRUCTION_TWO.getMaskList(),  1, TRACE_TWO_TWO);
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
            validation, layer, new ArrayList<Boolean>(), 0, TRACE_TWO_ONE);
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
            validation, layer, INSTRUCTION_TWO.getMaskList(), 1, TRACE_TWO_ONE);
            
        readValidation.invoke(
            validation, layer, INSTRUCTION_TWO.getMaskList(),  1, TRACE_TWO_TWO);
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }
    }

    @Test
    public void testIncorrectAccessType() 
        throws IllegalAccessException, InvocationTargetException, 
            Throwable {

        readValidation.invoke(
            validation, layer, INSTRUCTION_THREE.getMaskList(), 0, TRACE_FOUR);
    }
  }

  public static class TestGetTensor {
    private static Validation validation;
    private static Method getTensor;
    private Map<Object, TensorAllocation> tensorLabelToTensorAllocationNarrow;
    private Map<Object, TensorAllocation> tensorLabelToTensorAllocationWide;
    private static int expectedTensor;
    private static int traceAddress;
    private static List<Integer> narrowReadTensorList;
    private static List<Integer> narrowWriteTensorList;
    private static List<Integer> wideReadTensorList;
    private static List<Integer> wideWriteTensorList;
    private static TensorAllocation.Builder alloBuilder = TensorAllocation.newBuilder();

    private static final String NARROW = "narrow";
    private static final String WIDE = "wide";
    private static final String LAYER = "input";

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
    public void setUp() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, 
    ClassNotFoundException, InstantiationException, InvocationTargetException {
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
              Hashtable.class,
              String.class,
              String.class);

      getTensor.setAccessible(true);

      narrowReadTensorList = 
          new ArrayList<>(Arrays.asList(ALLO_1.getTensorLabel(), ALLO_2.getTensorLabel(), ALLO_7.getTensorLabel()));

      narrowWriteTensorList = new ArrayList<Integer>(Arrays.asList(ALLO_6.getTensorLabel(), ALLO_8.getTensorLabel()));

      wideReadTensorList = new ArrayList<Integer>(Arrays.asList(ALLO_3.getTensorLabel(), ALLO_9.getTensorLabel()));

      wideWriteTensorList = new ArrayList<Integer>(Arrays.asList(ALLO_4.getTensorLabel(), ALLO_5.getTensorLabel()));

      Class<?> enclosingClass = Class.forName("com.google.sps.Validation");
      Object enclosingInstance = enclosingClass.getDeclaredConstructor(MemaccessCheckerData.class).newInstance(proto.build());

      Class<?> innerClass = Class.forName("com.google.sps.Validation$Pair");
      Constructor<?> ctor = innerClass.getDeclaredConstructor(String.class, int.class);

      ctor.setAccessible(true);

      tensorLabelToTensorAllocationNarrow = new Hashtable<>();
      tensorLabelToTensorAllocationNarrow.put(ctor.newInstance(LAYER, ALLO_1.getTensorLabel()), ALLO_1);
      tensorLabelToTensorAllocationNarrow.put(ctor.newInstance(LAYER, ALLO_2.getTensorLabel()), ALLO_2);
      tensorLabelToTensorAllocationNarrow.put(ctor.newInstance(LAYER, ALLO_7.getTensorLabel()), ALLO_7);
      tensorLabelToTensorAllocationNarrow.put(ctor.newInstance(LAYER, ALLO_6.getTensorLabel()), ALLO_6);
      tensorLabelToTensorAllocationNarrow.put(ctor.newInstance(LAYER, ALLO_8.getTensorLabel()), ALLO_8);

      tensorLabelToTensorAllocationWide = new Hashtable<>();
      tensorLabelToTensorAllocationWide.put(ctor.newInstance(LAYER, ALLO_3.getTensorLabel()), ALLO_3);
      tensorLabelToTensorAllocationWide.put(ctor.newInstance(LAYER, ALLO_9.getTensorLabel()), ALLO_9);
      tensorLabelToTensorAllocationWide.put(ctor.newInstance(LAYER, ALLO_4.getTensorLabel()), ALLO_4);
      tensorLabelToTensorAllocationWide.put(ctor.newInstance(LAYER, ALLO_5.getTensorLabel()), ALLO_5);
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
                  LAYER,
                  NARROW));

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
                  LAYER,
                  NARROW));

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
                  LAYER,
                  NARROW));
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
                  LAYER,
                  NARROW));

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
                  LAYER,
                  NARROW));

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
                  LAYER,
                  NARROW));
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
                  LAYER,
                  WIDE));

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
                  LAYER,
                  WIDE));

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
                  LAYER,
                  NARROW));
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
                LAYER,
                WIDE));

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
                LAYER,
                WIDE));

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
                  LAYER,
                  WIDE));
    }

    // ALLO_7
    @Test
    public void testIncorrectNarrowRead() throws IllegalAccessException, InvocationTargetException {
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
                  LAYER,
                  NARROW));

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
                  LAYER,
                  NARROW));
    }

    // ALLO_6
    @Test
    public void testIncorrectNarrowWrite() throws IllegalAccessException, InvocationTargetException {
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
                LAYER,
                NARROW));

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
                LAYER,
                NARROW));
    }

    // ALLO_9
    @Test
    public void testIncorrectWideRead() throws IllegalAccessException, InvocationTargetException {
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
                  LAYER,
                  WIDE));

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
                  LAYER,
                  WIDE));
    }

    // ALLO_4
    @Test
    public void testIncorrectWideWrite() throws IllegalAccessException, InvocationTargetException {
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
                  LAYER,
                  WIDE));

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
                  LAYER,
                  WIDE));
    }
  }

  public static class TestGetTraceTensor {
        private Validation validation;
        private Instruction.Builder instructionBuilder;
        private ArrayList<Integer> accessList;
        private int traceAddress;
        private int expectedTensor;
        private int recievedTensor;
        private TraceEvent.AccessType traceAccessType;
        private long cycle;

        private Method mGetTraceTensor;
        private Field mLayerTensorLabelToTensorAllocationNarrow;
        private Field mLayerTensorLabelToTensorAllocationWide;
        private Field mNumTiles;

        @Before
        public void setUp() throws NoSuchMethodException,
        NoSuchFieldException, IllegalArgumentException, IllegalAccessException,
        ClassNotFoundException, InstantiationException, InvocationTargetException {
            instructionBuilder = Instruction.newBuilder();
            accessList = new ArrayList<Integer>();

            MemaccessCheckerData.Builder protoBuilder = MemaccessCheckerData.newBuilder();

            Validation validation = new Validation(protoBuilder.build());

            mGetTraceTensor = Validation.class.getDeclaredMethod("getTraceTensor", long.class, int.class, TraceEvent.AccessType.class, Instruction.class);
            mGetTraceTensor.setAccessible(true);

            mLayerTensorLabelToTensorAllocationNarrow = Validation.class.getDeclaredField("layerTensorLabelToTensorAllocationNarrow");
            mLayerTensorLabelToTensorAllocationNarrow.setAccessible(true);

            mLayerTensorLabelToTensorAllocationWide = Validation.class.getDeclaredField("layerTensorLabelToTensorAllocationWide");
            mLayerTensorLabelToTensorAllocationWide.setAccessible(true);

            mNumTiles = Validation.class.getDeclaredField("numTiles");
            mNumTiles.setAccessible(true);

            mNumTiles.set(validation, 1);

            Class<?> enclosingClass = Class.forName("com.google.sps.Validation");
            Object enclosingInstance = enclosingClass.getDeclaredConstructor(MemaccessCheckerData.class).newInstance(protoBuilder.build());

            Class<?> innerClass = Class.forName("com.google.sps.Validation$Pair");
            Constructor<?> ctor = innerClass.getDeclaredConstructor(String.class, int.class);

            ctor.setAccessible(true);

            Map<Object, TensorAllocation> narrowMap = new Hashtable<Object, TensorAllocation>();
            Map<Object, TensorAllocation> wideMap = new Hashtable<Object, TensorAllocation>();

            TensorAllocation.Builder tensorBuilder = TensorAllocation.newBuilder();
            tensorBuilder.setTensorLabel(2).setBaseAddress(0).setSize(45);

            TensorAllocation tensor1 = tensorBuilder.build();

            tensorBuilder = TensorAllocation.newBuilder();
            tensorBuilder.setTensorLabel(47).setBaseAddress(45).setSize(45);

            TensorAllocation tensor2 = tensorBuilder.build();

            narrowMap.put(ctor.newInstance("input", 2), tensor1);
            narrowMap.put(ctor.newInstance("input", 47), tensor2);
            narrowMap.put(ctor.newInstance("layer2", 2), tensor1);

            wideMap.put(ctor.newInstance("input", 2), tensor1);
            wideMap.put(ctor.newInstance("layer2", 47), tensor2);

            mLayerTensorLabelToTensorAllocationNarrow.set(validation, narrowMap);
            mLayerTensorLabelToTensorAllocationWide.set(validation, wideMap);

            cycle = 0;
        }

        // Trace is narrow read, instruction has narrow read, confirm returned tensor
        @Test
        public void testValidNarrowRead() throws Exception, MemoryAccessException {
            traceAccessType = TraceEvent.AccessType.NARROW_READ;

            traceAddress = 0;

            accessList.addAll(Arrays.asList(2));

            instructionBuilder
                .addAllNarrowRead(accessList)
                .setTag(0)
                .setLayer("input");

            expectedTensor = 2;
            recievedTensor = (int) mGetTraceTensor.invoke(validation, cycle, traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is narrow write, instruction has narrow write, confirm returned tensor
        @Test
        public void testValidNarrowWrite() throws Exception, MemoryAccessException {
            traceAccessType = TraceEvent.AccessType.NARROW_WRITE;

            traceAddress = 0;

            accessList.addAll(Arrays.asList(2));

            instructionBuilder
                .addAllNarrowWrite(accessList)
                .setTag(0)
                .setLayer("input");

            expectedTensor = 2;

            recievedTensor = (int) mGetTraceTensor.invoke(validation, cycle, traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is wide read, instruction has wide read, confirm returned tensor
        @Test
        public void testValidWideRead() throws Exception, MemoryAccessException {
            traceAccessType = TraceEvent.AccessType.WIDE_READ;

            traceAddress = 0;

            accessList.addAll(Arrays.asList(2));

            instructionBuilder
                .addAllWideRead(accessList)
                .setTag(0)
                .setLayer("input");

            expectedTensor = 2;

            recievedTensor = (int) mGetTraceTensor.invoke(validation, cycle, traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is wide write, instruction has wide write, confirm returned tensor
        @Test
        public void testValidWideWrite() throws Exception, MemoryAccessException {
            traceAccessType = TraceEvent.AccessType.WIDE_WRITE;

            traceAddress = 0;

            accessList.addAll(Arrays.asList(2));

            instructionBuilder
                .addAllWideWrite(accessList)
                .setTag(0)
                .setLayer("input");

            expectedTensor = 2;

            recievedTensor = (int) mGetTraceTensor.invoke(validation, cycle, traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is narrow read, instruction does not have narrow read, catch MAE
        @Test(expected = MemoryAccessException.class)
        public void testInvalidNarrowRead() throws Exception, MemoryAccessException, Throwable {
            traceAccessType = TraceEvent.AccessType.NARROW_READ;

            traceAddress = 0;
            
            accessList.addAll(Arrays.asList(2));

            instructionBuilder
                .addAllWideRead(accessList)
                .setTag(0)
                .setLayer("input");

            expectedTensor = 2;

            try {
                recievedTensor = (int) mGetTraceTensor.invoke(validation, cycle, traceAddress, traceAccessType, instructionBuilder.build());
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is narrow write, instruction does not have narrow write, catch MAE
        @Test(expected = MemoryAccessException.class)
        public void testInvalidNarrowWrite() throws Exception, MemoryAccessException, Throwable {
            traceAccessType = TraceEvent.AccessType.NARROW_WRITE;

            traceAddress = 0;

            accessList.addAll(Arrays.asList(2));

            instructionBuilder
                .addAllNarrowRead(accessList)
                .setTag(0)
                .setLayer("input");

            expectedTensor = 2;

            try {
                recievedTensor = (int) mGetTraceTensor.invoke(validation, cycle, traceAddress, traceAccessType, instructionBuilder.build());
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }

            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is wide read, instruction does not have wide read, catch MAE
        @Test(expected = MemoryAccessException.class)
        public void testInvalidWideRead() throws Exception, MemoryAccessException, Throwable {
            traceAccessType = TraceEvent.AccessType.WIDE_READ;

            traceAddress = 0;

            accessList.addAll(Arrays.asList(2));

            instructionBuilder
                .addAllNarrowRead(accessList)
                .setTag(0)
                .setLayer("input");

            expectedTensor = 2;

            try {
                recievedTensor = (int) mGetTraceTensor.invoke(validation, cycle, traceAddress, traceAccessType, instructionBuilder.build());
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }

            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is wide write, instruction does not have wide write, catch MAE
        @Test(expected = MemoryAccessException.class)
        public void testInvalidWideWrite() throws Exception, MemoryAccessException, Throwable {
            traceAccessType = TraceEvent.AccessType.WIDE_WRITE;
            
            traceAddress = 0;

            accessList.addAll(Arrays.asList(2));

            instructionBuilder
                .addAllNarrowWrite(accessList)
                .setTag(0)
                .setLayer("input");

            expectedTensor = 2;

            try {
                recievedTensor = (int) mGetTraceTensor.invoke(validation, cycle, traceAddress, traceAccessType, instructionBuilder.build());
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }

            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace has null access type
        @Test(expected = Exception.class)
        public void testNullAccessTraceEvent() throws Exception, MemoryAccessException, Throwable {
            traceAccessType = null;

            traceAddress = 0;

            accessList.addAll(Arrays.asList(2));

            instructionBuilder
                .addAllWideRead(accessList)
                .setTag(0)
                .setLayer("input");

            expectedTensor = 2;

            try {
                recievedTensor = (int) mGetTraceTensor.invoke(validation, cycle, traceAddress, traceAccessType, instructionBuilder.build());
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }

            assertEquals(expectedTensor, recievedTensor);
        }

        // Instruction does not have a layer associated with it
        @Test(expected = Exception.class)
        public void testInstructionHasNoLayer() throws Exception, MemoryAccessException, Throwable {
            traceAccessType = TraceEvent.AccessType.WIDE_READ;

            traceAddress = 0;

            accessList.addAll(Arrays.asList(2));

            instructionBuilder
                .addAllWideRead(accessList)
                .setTag(0);

            expectedTensor = 2;

            try {
                recievedTensor = (int) mGetTraceTensor.invoke(validation, cycle, traceAddress, traceAccessType, instructionBuilder.build());
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }

            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace Event operates on a tensor allocated on another layer, but not this one
        @Test(expected  = Exception.class)
        public void testTraceEventNonOnTensor() throws Exception, MemoryAccessException, Throwable {
            traceAccessType = TraceEvent.AccessType.NARROW_READ;

            traceAddress = 47;

            accessList.addAll(Arrays.asList(2));

            instructionBuilder
                .addAllNarrowRead(accessList)
                .setTag(0)
                .setLayer("layer2");

            expectedTensor = 2;

            try {
                recievedTensor = (int) mGetTraceTensor.invoke(validation, cycle, traceAddress, traceAccessType, instructionBuilder.build());
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }

            assertEquals(expectedTensor, recievedTensor);
        }
    }

    public static class TestRelateTensorsToInstructions {
        private Validation validation;
        private Instruction.Builder instructionBuilder;
        private List<TensorLayerAllocationTable> tensorLayerAllocationTable;

        private Method mRelateTensorsToInstructions;
        private Field mInstructions;
        private Field mInstructionTagToInstruction;
        private Field mNumTiles;

        @Before
        public void setUp() throws NoSuchMethodException,
        NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
            instructionBuilder = Instruction.newBuilder();

            MemaccessCheckerData.Builder protoBuilder = MemaccessCheckerData.newBuilder();

            Validation validation = new Validation(protoBuilder.build());

            mNumTiles = Validation.class.getDeclaredField("numTiles");
            mNumTiles.setAccessible(true);

            mNumTiles.set(validation, 1);

            mRelateTensorsToInstructions = Validation.class.getDeclaredMethod("relateTensorsToInstructions", List.class, boolean.class);
            mRelateTensorsToInstructions.setAccessible(true);

            mInstructions = Validation.class.getDeclaredField("instructions");
            mInstructions.setAccessible(true);

            mInstructionTagToInstruction = Validation.class.getDeclaredField("instructionTagtoInstruction");
            mInstructionTagToInstruction.setAccessible(true);           
        }

        // Empty allocation table, make sure nothing happens
        @Test
        public void testEmptyAllocationTable() throws InvalidTensorAddressException, IllegalAccessException, InvocationTargetException {
            mInstructions.set(validation, new ArrayList<Instruction>());

            mInstructionTagToInstruction.set(validation, new Hashtable<Integer, Instruction>());

            List<TensorLayerAllocationTable> emptyTable = new ArrayList<TensorLayerAllocationTable>();

            mRelateTensorsToInstructions.invoke(validation, emptyTable, true);
            
            // Make sure it runs without doing anything
            assertEquals(new Hashtable<Integer, Instruction>(), mInstructionTagToInstruction.get(validation));
        }

        // Empty allocation table, make sure nothing happens
        @Test
        public void testNonEmptyAllocationTableEmptyInstructions() throws InvalidTensorAddressException, IllegalAccessException, InvocationTargetException {
            mInstructions.set(validation, new ArrayList<Instruction>());

            mInstructionTagToInstruction.set(validation, new Hashtable<Integer, Instruction>());

            List<TensorLayerAllocationTable> table = createTensorLayerAllocationTable(1, 1);

            mRelateTensorsToInstructions.invoke(validation, table, true);
            
            // Make sure it runs without doing anything
            assertEquals(new Hashtable<Integer, Instruction>(), mInstructionTagToInstruction.get(validation));
        }        

        // One layer allocation table with valid instruction
        @Test
        public void testOneLayerAllocationTable() throws InvalidTensorAddressException, IllegalAccessException, InvocationTargetException {
            ArrayList<Instruction> instructions = new ArrayList<Instruction>();

            instructionBuilder.setLayer("0")
                              .setTag(1)
                              .addNarrowWrite(50);

            Instruction instruction = instructionBuilder.build();

            instructions.add(instruction);

            Hashtable<Integer, Instruction> tagToInstruction = new Hashtable<Integer, Instruction>();

            tagToInstruction.put(1, instruction);

            mInstructions.set(validation, instructions);
            mInstructionTagToInstruction.set(validation, tagToInstruction);

            List<TensorLayerAllocationTable> table = createTensorLayerAllocationTable(1, 1);

            mRelateTensorsToInstructions.invoke(validation, table, true);
            
            Hashtable<Integer, Instruction> resultingTable = (Hashtable<Integer, Instruction>) mInstructionTagToInstruction.get(validation);

            Instruction resultingInstruction = resultingTable.get(1);

            assertEquals(1, resultingInstruction.getNarrowWrite(0));
        }

        // Multiple layer allocation table with valid instruction
        @Test
        public void testMultiLayerAllocationTable() throws InvalidTensorAddressException, IllegalAccessException, InvocationTargetException {
            ArrayList<Instruction> instructions = new ArrayList<Instruction>();

            instructionBuilder.setLayer("0")
                              .setTag(1)
                              .addNarrowWrite(50);

            Instruction instruction1 = instructionBuilder.build();

            instructions.add(instruction1);

            instructionBuilder.clear()
                              .setLayer("1")
                              .setTag(2)
                              .addNarrowRead(67);

            Instruction instruction2 = instructionBuilder.build();

            instructions.add(instruction2);

            Hashtable<Integer, Instruction> tagToInstruction = new Hashtable<Integer, Instruction>();

            tagToInstruction.put(1, instruction1);
            tagToInstruction.put(2, instruction2);

            mInstructions.set(validation, instructions);
            mInstructionTagToInstruction.set(validation, tagToInstruction);

            List<TensorLayerAllocationTable> table = createTensorLayerAllocationTable(2, 1);

            mRelateTensorsToInstructions.invoke(validation, table, true);
            
            Hashtable<Integer, Instruction> resultingTable = (Hashtable<Integer, Instruction>) mInstructionTagToInstruction.get(validation);

            Instruction resultingInstruction1 = resultingTable.get(1);
            Instruction resultingInstruction2 = resultingTable.get(2);

            assertEquals(1, resultingInstruction1.getNarrowWrite(0));
            assertEquals(2, resultingInstruction2.getNarrowRead(0));
        }

        // One layer allocation table with multiple tensors allocated
        @Test
        public void testOneLayerMultipleTensors() throws InvalidTensorAddressException, IllegalAccessException, InvocationTargetException {
            ArrayList<Instruction> instructions = new ArrayList<Instruction>();

            instructionBuilder.setLayer("0")
                              .setTag(1)
                              .addNarrowWrite(150);

            Instruction instruction1 = instructionBuilder.build();

            instructions.add(instruction1);

            Hashtable<Integer, Instruction> tagToInstruction = new Hashtable<Integer, Instruction>();

            tagToInstruction.put(1, instruction1);

            mInstructions.set(validation, instructions);
            mInstructionTagToInstruction.set(validation, tagToInstruction);

            List<TensorLayerAllocationTable> table = createTensorLayerAllocationTable(1, 3);

            mRelateTensorsToInstructions.invoke(validation, table, true);
            
            Hashtable<Integer, Instruction> resultingTable = (Hashtable<Integer, Instruction>) mInstructionTagToInstruction.get(validation);

            Instruction resultingInstruction1 = resultingTable.get(1);

            assertEquals(2, resultingInstruction1.getNarrowWrite(0));
        }

        // Multiple layer allocation table with invalid instruction
        @Test (expected = InvalidTensorAddressException.class)
        public void testMultiLayerAllocationTableWithInvalidInstruction() throws InvalidTensorAddressException, IllegalAccessException, InvocationTargetException, Throwable {
            ArrayList<Instruction> instructions = new ArrayList<Instruction>();

            instructionBuilder.setLayer("0")
                              .setTag(1)
                              .addNarrowWrite(267);

            Instruction instruction1 = instructionBuilder.build();

            instructions.add(instruction1);

            Hashtable<Integer, Instruction> tagToInstruction = new Hashtable<Integer, Instruction>();

            tagToInstruction.put(1, instruction1);

            mInstructions.set(validation, instructions);
            mInstructionTagToInstruction.set(validation, tagToInstruction);

            List<TensorLayerAllocationTable> table = createTensorLayerAllocationTable(2, 2);

            try {
                mRelateTensorsToInstructions.invoke(validation, table, true);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
            
            Hashtable<Integer, Instruction> resultingTable = (Hashtable<Integer, Instruction>) mInstructionTagToInstruction.get(validation);

            Instruction resultingInstruction1 = resultingTable.get(1);

            assertEquals(1, resultingInstruction1.getNarrowWrite(0));
        }

        // Multiple layer allocation table with invalid instruction
        @Test (expected = InvalidTensorAddressException.class)
        public void testMultiLayerAllocationTableWithInstructionInvalidOnCurrentLayer() throws InvalidTensorAddressException, IllegalAccessException, InvocationTargetException, Throwable {
            ArrayList<Instruction> instructions = new ArrayList<Instruction>();

            instructionBuilder.setLayer("1")
                              .setTag(1)
                              .addNarrowWrite(150);

            Instruction instruction1 = instructionBuilder.build();

            instructions.add(instruction1);

            Hashtable<Integer, Instruction> tagToInstruction = new Hashtable<Integer, Instruction>();

            tagToInstruction.put(1, instruction1);

            mInstructions.set(validation, instructions);
            mInstructionTagToInstruction.set(validation, tagToInstruction);

            int[] dist = {2, 1, 2};

            List<TensorLayerAllocationTable> table = createTensorLayerAllocationTable(dist);

            try {
                mRelateTensorsToInstructions.invoke(validation, table, true);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
            
            Hashtable<Integer, Instruction> resultingTable = (Hashtable<Integer, Instruction>) mInstructionTagToInstruction.get(validation);

            Instruction resultingInstruction1 = resultingTable.get(1);

            assertEquals(1, resultingInstruction1.getNarrowWrite(0));
        }

        /** 
        * Helper method that creates a tensor layer allocation table for a specified number of layers and tensors.
        * Input array's length is number of layers, each entry is tensors on that layer
        */
        public static List<TensorLayerAllocationTable> createTensorLayerAllocationTable(int[] distribution) {
            List<TensorLayerAllocationTable> table = new ArrayList<TensorLayerAllocationTable>();

            TensorLayerAllocationTable.Builder tlatBuilder = TensorLayerAllocationTable.newBuilder();
            TensorTileAllocationTable.Builder ttatBuilder = TensorTileAllocationTable.newBuilder();
            TensorAllocation.Builder taBuilder = TensorAllocation.newBuilder();

            int tensorLabel = 1;

            for (int i = 0; i < distribution.length; i++) {
                for (int j = 0; j < distribution[i]; j++) {
                    taBuilder.setTensorLabel(tensorLabel);
                    taBuilder.setBaseAddress(j * 100);
                    taBuilder.setSize(100);

                    ttatBuilder.addTensorAllocation(0, taBuilder.build());

                    tensorLabel++;

                    taBuilder.clear();
                }
                tlatBuilder.setLayer(String.valueOf(i)).addTensorTileAllocation(ttatBuilder.build());

                ttatBuilder.clear();

                table.add(tlatBuilder.build());

                tlatBuilder.clear();
            }

            return table;
        }

        /**
        * Creates a tensor layer allocation table with a specified number of layers, each with that number of tensors.
        */
        public static List<TensorLayerAllocationTable> createTensorLayerAllocationTable(int numLayers, int numTensors) {
            int[] dist = new int[numLayers];

            for (int i = 0; i < numLayers; i++) {
                dist[i] = numTensors;
            }

            return createTensorLayerAllocationTable(dist);
        }
    }

    public static class TestGetLayerToInstructionTable {
        private Validation validation;
        private Instruction.Builder instructionBuilder;
        private Hashtable<String, List<Integer>> expected;

        private Method mGetLayerToInstructionTable;
        private Field mInstructions;

        @Before
        public void setUp() throws NoSuchMethodException,
        NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
            instructionBuilder = Instruction.newBuilder();

            expected = new Hashtable<String, List<Integer>>();

            MemaccessCheckerData.Builder protoBuilder = MemaccessCheckerData.newBuilder();

            Validation validation = new Validation(protoBuilder.build());

            mGetLayerToInstructionTable = Validation.class.getDeclaredMethod("getLayerToInstructionTable");
            mGetLayerToInstructionTable.setAccessible(true);

            mInstructions = Validation.class.getDeclaredField("instructions");
            mInstructions.setAccessible(true);
        }

        // No instructions
        @Test
        public void testEmptyInstructionList() throws IllegalAccessException, InvocationTargetException {
            List<Instruction> instructionList = new ArrayList<Instruction>();

            mInstructions.set(validation, instructionList);

            Hashtable<String, List<Integer>> result = (Hashtable<String, List<Integer>>) mGetLayerToInstructionTable.invoke(validation);

            assertEquals(expected, result);
        }

        // One instruction
        @Test
        public void testOneInstructionList() throws IllegalAccessException, InvocationTargetException {
            List<Instruction> instructionList = new ArrayList<Instruction>();

            List<Integer> instructionTagList = new ArrayList<Integer>();

            instructionBuilder.setTag(1)
                              .setLayer("1")
                              .addNarrowRead(50);

            instructionList.add(instructionBuilder.build());
            instructionTagList.add(1);

            instructionBuilder.clear();

            mInstructions.set(validation, instructionList);

            expected.put("1", instructionTagList);

            Hashtable<String, List<Integer>> result = (Hashtable<String, List<Integer>>) mGetLayerToInstructionTable.invoke(validation);

            assertEquals(expected, result);
        }

        // Multiple instructions on the same layer
        @Test
        public void testMultipleInstructionListSameLayer() throws IllegalAccessException, InvocationTargetException {
            List<Instruction> instructionList = new ArrayList<Instruction>();

            List<Integer> instructionTagList = new ArrayList<Integer>();

            instructionBuilder.setTag(1)
                              .setLayer("1")
                              .addNarrowRead(50);

            instructionList.add(instructionBuilder.build());
            instructionTagList.add(1);

            instructionBuilder.clear();

            instructionBuilder.setTag(2)
                              .setLayer("1")
                              .addNarrowRead(100);

            instructionList.add(instructionBuilder.build());
            instructionTagList.add(2);

            instructionBuilder.clear();

            instructionBuilder.setTag(3)
                              .setLayer("1")
                              .addNarrowRead(150);

            instructionList.add(instructionBuilder.build());
            instructionTagList.add(3);

            instructionBuilder.clear();

            mInstructions.set(validation, instructionList);

            expected.put("1", instructionTagList);

            Hashtable<String, List<Integer>> result = (Hashtable<String, List<Integer>>) mGetLayerToInstructionTable.invoke(validation);

            assertEquals(expected, result);
        }

        // Multiple Instructions each on different layers
        @Test
        public void testMultipleInstructionListDifferentLayers() throws IllegalAccessException, InvocationTargetException {
            List<Instruction> instructionList = new ArrayList<Instruction>();

            List<Integer> instructionTagList1 = new ArrayList<Integer>();
            List<Integer> instructionTagList2 = new ArrayList<Integer>();
            List<Integer> instructionTagList3 = new ArrayList<Integer>();

            instructionBuilder.setTag(1)
                              .setLayer("1")
                              .addNarrowRead(50);

            instructionList.add(instructionBuilder.build());
            instructionTagList1.add(1);

            instructionBuilder.clear();

            instructionBuilder.setTag(2)
                              .setLayer("2")
                              .addNarrowRead(100);

            instructionList.add(instructionBuilder.build());
            instructionTagList2.add(2);

            instructionBuilder.clear();

            instructionBuilder.setTag(3)
                              .setLayer("3")
                              .addNarrowRead(150);

            instructionList.add(instructionBuilder.build());
            instructionTagList3.add(3);

            instructionBuilder.clear();

            mInstructions.set(validation, instructionList);

            expected.put("1", instructionTagList1);
            expected.put("2", instructionTagList2);
            expected.put("3", instructionTagList3);

            Hashtable<String, List<Integer>> result = (Hashtable<String, List<Integer>>) mGetLayerToInstructionTable.invoke(validation);

            assertEquals(expected, result);
        }
    }

    public static class TestWriteValidation {
        private Validation validation;    
        private static List<Delta> testNarrow;
        private static List<Delta> testWide;
        private static List<Delta> expectedNarrow;
        private static List<Delta> expectedWide;
        private static int tensor;
        private static String layer;
        private static int tile;
        public static  TraceEvent.AccessType NARROWWRITE;
        public static  TraceEvent.AccessType WIDEWRITE;
        private static TraceEvent testTrace;

        private Method mWriteValidation;
        private Field mNumTiles;

    
        private static TraceEvent.Builder traceBuilder;

        @Before
        public void setUp() throws NoSuchMethodException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        layer = "input";
        testNarrow = new ArrayList<Delta>();
        testWide = new ArrayList<Delta>();
        expectedNarrow = new ArrayList<Delta>();
        expectedWide = new ArrayList<Delta>();
        tile = 2;
        traceBuilder = TraceEvent.newBuilder();
        NARROWWRITE = TraceEvent.AccessType.NARROW_WRITE;
        WIDEWRITE = TraceEvent.AccessType.WIDE_WRITE;

        MemaccessCheckerData.Builder protoBuilder = MemaccessCheckerData.newBuilder();
        validation = new Validation(protoBuilder.build());

        Field mNarrow = Validation.class.getDeclaredField("narrow");
        Field mWide = Validation.class.getDeclaredField("wide");
        mNarrow.setAccessible(true);
        mWide.setAccessible(true);

        mNarrow.set(validation, new int[16][128 * 1024]);
        mWide.set(validation, new int[16][256 * 1024]);

        mWriteValidation = Validation.class.getDeclaredMethod("writeValidation", String.class, List.class, int.class, TraceEvent.class, List.class, List.class);
                mWriteValidation.setAccessible(true);
        mNumTiles = Validation.class.getDeclaredField("numTiles");
                mNumTiles.setAccessible(true);
        mNumTiles.set(validation, 16);
        
        }
        @Test
        public void testEmptyTrace() throws IllegalAccessException, InvocationTargetException, Throwable{  
        List<Boolean> mask = new ArrayList(); 
        for (int i = 0; i < 16; i++){
                mask.add(true);
            } 
        testTrace = traceBuilder.build();
            mWriteValidation.invoke(validation, layer, mask, tensor, testTrace, testNarrow, testWide);
        assertEquals(expectedNarrow, testNarrow);
        }
        @Test
        public void testEmptyByte() throws IllegalAccessException, InvocationTargetException, Throwable{  
        List<Boolean> mask = new ArrayList(); 
        for (int i = 0; i < 16; i++){
                mask.add(true);
            } 
        testTrace =
                    traceBuilder.setAccessType(NARROWWRITE).setTile(tile).setInstructionTag(0).setAddress(1000).build();
            mWriteValidation.invoke(validation, layer, mask, tensor, testTrace, testNarrow, testWide);
        assertEquals(expectedNarrow, testNarrow);
        }
        @Test
        public void testNonEmptyTraceNarrow() throws IllegalAccessException, InvocationTargetException, Throwable{
            tensor = 7;
            List<Boolean> mask = new ArrayList();
            for (int i = 0; i < 16; i++){
                mask.add(true);
            }
            testTrace =
                    traceBuilder.setAccessType(NARROWWRITE).setBytes(1).setTile(tile).setInstructionTag(0).setAddress(1000).build();
                    

            Delta delta = new Delta(layer, tile, 1000, tensor);
            expectedNarrow.add(delta);
        
            mWriteValidation.invoke(validation, layer, mask, tensor, testTrace, testNarrow, testWide);
            
            assertEquals(expectedNarrow, testNarrow);
        }
        @Test
        public void testNonEmptyTraceWide() throws IllegalAccessException, InvocationTargetException, Throwable{
            tensor = 7;
            List<Boolean> mask = new ArrayList();
            for (int i = 0; i < 16; i++){
                mask.add(true);
            }
            testTrace =
                    traceBuilder.setAccessType(WIDEWRITE).setInstructionTag(0).setTile(tile).setBytes(1).setAddress(1000).build();
                    
            Delta delta = new Delta(layer, tile, 1000, tensor);
            expectedWide.add(delta);

            mWriteValidation.invoke(validation, layer, mask, tensor, testTrace, testNarrow, testWide);
            
            assertEquals(expectedWide, testWide);
        }
        @Test
        public void testFourBytesTraceNarrow() throws IllegalAccessException, InvocationTargetException, Throwable{
            tensor = 7;
            List<Boolean> mask = new ArrayList();
            for (int i = 0; i < 16; i++){
                mask.add(true);
            }
            testTrace =
                    traceBuilder.setAccessType(NARROWWRITE).setBytes(4).setTile(tile).setInstructionTag(0).setAddress(1000).build();
                    
            for (int i = 4000; i < 4004; i++){
                Delta delta = new Delta(layer, tile, i, tensor);
                expectedNarrow.add(delta);
            }

            mWriteValidation.invoke(validation, layer, mask, tensor, testTrace, testNarrow, testWide);
            
            assertEquals(expectedNarrow, testNarrow);
        }
        @Test
        public void testFourBytesTraceWide() throws IllegalAccessException, InvocationTargetException, Throwable{
            tensor = 7;
            List<Boolean> mask = new ArrayList();
            for (int i = 0; i < 16; i++){
                mask.add(true);
            }
            testTrace =
                    traceBuilder.setAccessType(WIDEWRITE).setInstructionTag(0).setBytes(4).setAddress(1000).setTile(tile).build();
                    
            for (int i = 4000; i < 4004; i++){
                Delta delta = new Delta(layer, tile, i, tensor);
                expectedWide.add(delta);
            }

            mWriteValidation.invoke(validation, layer, mask, tensor, testTrace, testNarrow, testWide);
            
            assertEquals(expectedWide, testWide);
        }
        @Test(expected = Exception.class)
        public void testFalseMaskNarrow() throws IllegalAccessException, InvocationTargetException, Throwable{
            tensor = 7;
            List<Boolean> mask = new ArrayList();
            for (int i = 0; i < 16; i++){
                mask.add(false);
            }
            testTrace =
                    traceBuilder.setAccessType(NARROWWRITE).setInstructionTag(0).setTile(tile).setAddress(1200).build();
                    
            try {
                mWriteValidation.invoke(validation, layer, mask, tensor, testTrace, testNarrow, testWide);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }        
            
            assertEquals(expectedNarrow, testNarrow);
        }
        @Test(expected = Exception.class)
        public void testFalseMaskWide() throws IllegalAccessException, InvocationTargetException, Throwable{
            tensor = 7;
            List<Boolean> mask = new ArrayList();
            for (int i = 0; i < 16; i++){
                mask.add(false);
            }
            testTrace =
                    traceBuilder.setAccessType(WIDEWRITE).setInstructionTag(0).setTile(tile).setAddress(1200).build();
            try {
                mWriteValidation.invoke(validation, layer, mask, tensor, testTrace, testNarrow, testWide);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
            assertEquals(expectedWide, testWide);
        }
        //different masks
        @Test
        public void testVariedMasksWide() throws IllegalAccessException, InvocationTargetException, Throwable{
            tensor = 7;
            tile = 1;
            List<Boolean> mask = new ArrayList();

            for (int i = 0; i < 8; i++){
                mask.add(false);
                mask.add(true);
            }
            testTrace =
                    traceBuilder.setAccessType(WIDEWRITE).setBytes(1).setInstructionTag(0).setTile(tile).setAddress(1200).build();
            
            Delta delta = new Delta(layer, tile, 1200, tensor);
            expectedWide.add(delta);

            mWriteValidation.invoke(validation, layer, mask, tensor, testTrace, testNarrow, testWide);
            assertEquals(expectedWide, testWide);
        }

    //no write accesstype
        @Test 
        public void testNoWriteAccessType() throws IllegalAccessException, InvocationTargetException, Throwable{
            tensor = 7;
            List<Boolean> mask = new ArrayList();
            for (int i = 0; i < 16; i++){
                mask.add(false);
            } 
            testTrace =
                    traceBuilder.setAccessType(TraceEvent.AccessType.WIDE_READ).setInstructionTag(0).setTile(tile).setAddress(1200).build();
            mWriteValidation.invoke(validation, layer, mask, tensor, testTrace, testNarrow, testWide);
        assertEquals(expectedWide, testWide);
        }
    }
    public static class TestValidateTraceEvents {
        private Validation validation;    
        private static int start;
        private static int end;
        private ArrayList<TraceEvent> traceEventList;
        private List<Delta> narrowDeltas;
        private List<Delta> wideDeltas;
        private static Map<Integer, Instruction> instructionTagtoInstruction;
        private static Map<Object, TensorAllocation> wideMap;
        private static Map<Object, TensorAllocation> narrowMap;
        private static TraceEvent.Builder traceBuilder;
        private static Instruction.Builder instructionBuilder;
        private static TensorAllocation.Builder tensorBuilder;
        private static TraceEvent traceEvent_0;
        private static TraceEvent traceEvent_1;
        private static TraceEvent traceEvent_2;
        private static TraceEvent traceEvent_3;
        private static TensorAllocation tensor_1;
        private static TensorAllocation tensor_2;
        private static TensorAllocation tensor_3;
        private static Instruction instruction_1;
        private static Instruction instruction_2;
        private static TraceEvent returnedTraceEvent;
        private static  TraceEvent.AccessType NARROWWRITE;
        private static  TraceEvent.AccessType WIDEWRITE;
        private static  TraceEvent.AccessType NARROWREAD;
        private static  TraceEvent.AccessType WIDEREAD;
        private Method mValidateTraceEvents;

        private Field mLayerTensorLabelToTensorAllocationWide;
        private Field mLayerTensorLabelToTensorAllocationNarrow;
        private Field mInstructionTagtoInstruction;
        private Field mTraceEvents;
        private Field mNarrow;
        private Field mWide;
        private Constructor<?> ctor;

        @Before
        public void setUp() throws NoSuchMethodException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException,
            ClassNotFoundException, InstantiationException, InvocationTargetException {
        traceEventList = new ArrayList<TraceEvent>();
        NARROWWRITE = TraceEvent.AccessType.NARROW_WRITE;
        NARROWREAD = TraceEvent.AccessType.NARROW_READ;
        WIDEWRITE = TraceEvent.AccessType.WIDE_WRITE;
        wideMap = new Hashtable<Object, TensorAllocation>();
        narrowMap = new Hashtable<Object, TensorAllocation>();
        instructionTagtoInstruction = new Hashtable<Integer, Instruction>();
        traceBuilder = TraceEvent.newBuilder();
        narrowDeltas = new ArrayList<Delta>();
        wideDeltas = new ArrayList<Delta>();
        instructionBuilder = Instruction.newBuilder();
        tensorBuilder = TensorAllocation.newBuilder();
        MemaccessCheckerData.Builder protoBuilder = MemaccessCheckerData.newBuilder();

        validation = new Validation(protoBuilder.build());

        Class<?> enclosingClass = Class.forName("com.google.sps.Validation");
        Object enclosingInstance = enclosingClass.getDeclaredConstructor(MemaccessCheckerData.class).newInstance(protoBuilder.build());

        Class<?> innerClass = Class.forName("com.google.sps.Validation$Pair");
        ctor = innerClass.getDeclaredConstructor(String.class, int.class);

        ctor.setAccessible(true);
        
        instructionBuilder = Instruction.newBuilder();
        mInstructionTagtoInstruction = Validation.class.getDeclaredField("instructionTagtoInstruction");
                mInstructionTagtoInstruction.setAccessible(true);
        mLayerTensorLabelToTensorAllocationWide = Validation.class.getDeclaredField("layerTensorLabelToTensorAllocationWide");
                mLayerTensorLabelToTensorAllocationWide.setAccessible(true);
        mLayerTensorLabelToTensorAllocationNarrow = Validation.class.getDeclaredField("layerTensorLabelToTensorAllocationNarrow");
                mLayerTensorLabelToTensorAllocationNarrow.setAccessible(true);
        mTraceEvents = Validation.class.getDeclaredField("traceEvents");
                mTraceEvents.setAccessible(true);
        mValidateTraceEvents = Validation.class.getDeclaredMethod("validateTraceEvents", long.class, long.class, List.class, List.class);
                mValidateTraceEvents.setAccessible(true);

        mNarrow = Validation.class.getDeclaredField("narrow");
        mWide = Validation.class.getDeclaredField("wide");
        mNarrow.setAccessible(true);
        mWide.setAccessible(true);

        mNarrow.set(validation, new int[16][128 * 1024]);
        mWide.set(validation, new int[16][256 * 1024]);
        }
        @Test(expected = Exception.class)
        public void testEmptyTraceEventList() 
        throws IllegalAccessException, InvocationTargetException, Throwable, Exception{  

        start = 0;
        end = 9;
        mValidateTraceEvents.invoke(validation, start, end, narrowDeltas, wideDeltas); 
        }
        @Test(expected = Exception.class)
        public void testEmptyMask() 
        throws IllegalAccessException, InvocationTargetException, Exception {  
        
        start = 0;
        end = 1;
        traceEvent_1 = traceBuilder.setAddress(100).setBytes(1).setAccessType(NARROWWRITE).setInstructionTag(10).build();
        traceEventList.add(traceEvent_1);
        
        instruction_1 = instructionBuilder.setLayer("1").setTag(10).build();
        
        mTraceEvents.set(validation, traceEventList);
        instructionTagtoInstruction.put(10,instruction_1);
        mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
        mValidateTraceEvents.invoke(validation, start, end, narrowDeltas, wideDeltas); 
        }
        @Test(expected = Exception.class)
        public void testEmptyInstruction() 
        throws IllegalAccessException, InvocationTargetException, Exception {  
        
        start = 0;
        end = 1;
        traceEvent_1 = traceBuilder.setAddress(100).setBytes(1).setAccessType(NARROWWRITE).setInstructionTag(12).build();
        traceEventList.add(traceEvent_1);
        
        instruction_1 = instructionBuilder.setLayer("1").setTag(10).build();
        
        mTraceEvents.set(validation, traceEventList);
        instructionTagtoInstruction.put(10,instruction_1);
        mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
        mValidateTraceEvents.invoke(validation, start, end, narrowDeltas, wideDeltas); 
        }
        @Test
        public void testNonEmptyTraceList() 
        throws IllegalAccessException, InvocationTargetException, Exception {  
        start = 0;
        end = 1;
        traceEvent_2 = traceBuilder.setAddress(250).setBytes(1).setAccessType(WIDEWRITE).setInstructionTag(10).build();
        traceEventList.add(traceEvent_2);
        mTraceEvents.set(validation, traceEventList);
        
        List<Boolean> mask = new ArrayList(); 
            for (int i = 0; i < 16; i++){
                mask.add(true);
            }
        instruction_1 = instructionBuilder.setLayer("1").setTag(10).addWideWrite(8).addAllMask(mask).build();
        instructionTagtoInstruction.put(10,instruction_1);
        mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
    
        tensor_1 = tensorBuilder.setTensorLabel(8).setBaseAddress(250).setSize(45).build();
        wideMap.put(ctor.newInstance("1", 8),tensor_1);
        mLayerTensorLabelToTensorAllocationWide.set(validation,wideMap);

        mValidateTraceEvents.invoke(validation, start, end, narrowDeltas, wideDeltas); 

        }
        @Test
        public void testMultipleTraceEvents() 
        throws IllegalAccessException, InvocationTargetException, Exception {  
        start = 0;
        end = 3;

        traceEvent_0 = traceBuilder.setAddress(24).setBytes(1).setAccessType(NARROWWRITE).setInstructionTag(10).build();
        traceEvent_1 = traceBuilder.setAddress(24).setBytes(1).setAccessType(NARROWREAD).setInstructionTag(10).build();
        traceEvent_2 = traceBuilder.setAddress(250).setBytes(1).setAccessType(WIDEWRITE).setInstructionTag(10).build();
        traceEvent_3 = traceBuilder.setAddress(1000).setBytes(1).setAccessType(WIDEWRITE).setInstructionTag(12).build();
        traceEventList.add(traceEvent_0);
        traceEventList.add(traceEvent_1);
        traceEventList.add(traceEvent_2);
        traceEventList.add(traceEvent_3);
        mTraceEvents.set(validation, traceEventList);
        
        List<Boolean> mask = new ArrayList(); 
            for (int i = 0; i < 16; i++){
                mask.add(true);
            }
        instruction_1 = instructionBuilder.setLayer("1").setTag(10).addNarrowWrite(5).addWideWrite(8).addNarrowRead(5).addAllMask(mask).build();
        instruction_2 = instructionBuilder.setLayer("14").setTag(12).addWideWrite(12).addAllMask(mask).build();
        instructionTagtoInstruction.put(10,instruction_1);
        instructionTagtoInstruction.put(12,instruction_2);
        mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
    
        tensor_1 = tensorBuilder.setTensorLabel(5).setBaseAddress(24).setSize(200).build();
        tensor_2 = tensorBuilder.setTensorLabel(8).setBaseAddress(250).setSize(100).build();
        tensor_3 = tensorBuilder.setTensorLabel(12).setBaseAddress(956).setSize(45).build();
        narrowMap.put(ctor.newInstance("1", 5), tensor_1);
        wideMap.put(ctor.newInstance("1", 8),tensor_2);
        wideMap.put(ctor.newInstance("14", 8),tensor_3);

        mLayerTensorLabelToTensorAllocationNarrow.set(validation,narrowMap);
        mLayerTensorLabelToTensorAllocationWide.set(validation,wideMap); 
        mValidateTraceEvents.invoke(validation, start, end, narrowDeltas, wideDeltas); 
        }
        @Test(expected = Exception.class)
        public void testOutOfBoundTensorAllocation() 
        throws IllegalAccessException, InvocationTargetException, Exception {  
        start = 0;
        end = 3;
        traceEvent_1 = traceBuilder.setAddress(32).setBytes(1).setAccessType(NARROWREAD).setInstructionTag(10).build();
        traceEvent_2 = traceBuilder.setAddress(250).setBytes(1).setAccessType(WIDEWRITE).setInstructionTag(10).build();
        traceEvent_3 = traceBuilder.setAddress(1000).setBytes(1).setAccessType(WIDEWRITE).setInstructionTag(12).build();
        traceEventList.add(traceEvent_1);
        traceEventList.add(traceEvent_2);
        traceEventList.add(traceEvent_3);
        mTraceEvents.set(validation, traceEventList);
        
        List<Boolean> mask = new ArrayList(); 
            for (int i = 0; i < 16; i++){
                mask.add(true);
            }
        instruction_1 = instructionBuilder.setLayer("1").setName("first").setTag(10).addWideWrite(8).addNarrowRead(5).addAllMask(mask).build();
        instruction_2 = instructionBuilder.setLayer("14").setName("second").setTag(12).addWideWrite(12).addAllMask(mask).build();
        instructionTagtoInstruction.put(10,instruction_1);
        instructionTagtoInstruction.put(12,instruction_2);
        mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
    
        tensor_1 = tensorBuilder.setTensorLabel(5).setBaseAddress(24).setSize(32).build();
        tensor_2 = tensorBuilder.setTensorLabel(8).setBaseAddress(250).setSize(100).build();
        tensor_3 = tensorBuilder.setTensorLabel(12).setBaseAddress(955).setSize(45).build();
        narrowMap.put(ctor.newInstance("1", 5), tensor_1);
        wideMap.put(ctor.newInstance("1", 8),tensor_2);
        wideMap.put(ctor.newInstance("14", 8),tensor_3);
        mLayerTensorLabelToTensorAllocationNarrow.set(validation,narrowMap);
        mLayerTensorLabelToTensorAllocationWide.set(validation,wideMap);

        mValidateTraceEvents.invoke(validation, start, end, narrowDeltas, wideDeltas); 
        }
        @Test(expected = Exception.class)
        public void testInvalidAccessType() 
        throws IllegalAccessException, InvocationTargetException, Exception {  
        start = 0;
        end = 2;
        traceEvent_1 = traceBuilder.setAddress(32).setBytes(1).setAccessType(NARROWREAD).setInstructionTag(10).build();
        traceEvent_2 = traceBuilder.setAddress(250).setBytes(1).setAccessType(WIDEWRITE).setInstructionTag(10).build();
        traceEventList.add(traceEvent_1);
        traceEventList.add(traceEvent_2);
        mTraceEvents.set(validation, traceEventList);
        
        List<Boolean> mask = new ArrayList(); 
            for (int i = 0; i < 16; i++){
                mask.add(true);
            }
        instruction_1 = instructionBuilder.setLayer("1").setName("first").setTag(10).addAllMask(mask).build();
        instructionTagtoInstruction.put(10,instruction_1);
        mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
    
        tensor_1 = tensorBuilder.setTensorLabel(5).setBaseAddress(24).setSize(32).build();
        tensor_2 = tensorBuilder.setTensorLabel(8).setBaseAddress(250).setSize(100).build();
        narrowMap.put(ctor.newInstance("1", 5), tensor_1);
        wideMap.put(ctor.newInstance("1", 8),tensor_2);
        mLayerTensorLabelToTensorAllocationNarrow.set(validation,narrowMap);
        mLayerTensorLabelToTensorAllocationWide.set(validation,wideMap);

        mValidateTraceEvents.invoke(validation, start, end, narrowDeltas, wideDeltas); 
        }
        @Test(expected = Exception.class)
        public void testEmptyTable() 
        throws IllegalAccessException, InvocationTargetException, Exception {  
        start = 0;
        end = 2;
        traceEvent_1 = traceBuilder.setAddress(32).setBytes(1).setAccessType(NARROWREAD).setInstructionTag(10).build();
        traceEventList.add(traceEvent_1);
        mTraceEvents.set(validation, traceEventList);
        
        List<Boolean> mask = new ArrayList(); 
            for (int i = 0; i < 16; i++){
                mask.add(true);
            }
        instruction_1 = instructionBuilder.setLayer("1").setName("first").setTag(10).addNarrowRead(8).addAllMask(mask).build();
        instructionTagtoInstruction.put(10,instruction_1);
        mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
    
        tensor_1 = tensorBuilder.setTensorLabel(5).setBaseAddress(24).setSize(32).build();
        narrowMap.put(ctor.newInstance("1", 5), tensor_1);

        mValidateTraceEvents.invoke(validation, start, end, narrowDeltas, wideDeltas); 
        }
    }
}
