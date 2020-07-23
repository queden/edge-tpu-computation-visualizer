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
    TJCodeTest.TestReadValidate.class})
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

      readValidation.invoke(validation,narrow, wide, INSTRUCTION_ONE.getMaskList(), 0, TRACE_ONE);
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
  }
}
