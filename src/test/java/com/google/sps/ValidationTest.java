// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import static org.junit.Assert.*;

import com.google.sps.exceptions.*;
import com.google.sps.proto.SimulationTraceProto.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.util.Map;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/** */
@RunWith(ValidationTest.class)
@Suite.SuiteClasses({
  ValidationTest.TestAllocationMemory.class,
  ValidationTest.TestTensorToInstruction.class,
  ValidationTest.TestWriteValidation.class,
  ValidationTest.TestGetTraceTensor.class,
  ValidationTest.TestInstructionTagtoInstructionTable.class,
  ValidationTest.TestReadValidate.class
})

public final class ValidationTest extends Suite {
  private static TensorAllocation.Builder allocationBuilder = TensorAllocation.newBuilder();
  private static Instruction.Builder instructionBuilder = Instruction.newBuilder();
  private static Instruction.Builder expectedInstructionBuilder = Instruction.newBuilder();
  private static MemoryAccess.Builder memAccessBuilder = MemoryAccess.newBuilder();
  private static TraceEntry.Builder traceBuilder = TraceEntry.newBuilder();

  public ValidationTest(Class<?> klass, RunnerBuilder builder) throws InitializationError {
    super(klass, builder);
  }

  public static class TestAllocationMemory {
    private static int[] testMemory;
    private static ArrayList<TensorAllocation> testAllocation;
    private static int[] testNarrow;
    private static int[] testWide;
    private static ArrayList<Instruction> testInstruction;
    private static ArrayList<Instruction> expected;
    @Before
    public void setUp() {
      testMemory = new int[128 * 1024];
      Arrays.fill(testMemory, -1);
    }
    @Test
    public void testBuildAllocationMemorySplit() {
      // ** Separately allocates memory for different tensors **//
      testAllocation =
          new ArrayList<>(
              Arrays.asList(
                  new TensorAllocation[] {
                    allocationBuilder.setLabel(1).setStartAddress(8).setSize(1000).build(),
                    allocationBuilder.setLabel(2).setStartAddress(95678).setSize(216).build()
                  }));
      for (int i = 8; i < 1008; i++) {
        testMemory[i] = 1;
      }
      for (int i = 95678; i < 95894; i++) {
        testMemory[i] = 2;
      }
      assertArrayEquals(testMemory, Validation.getAllocationArray(testAllocation, 128));
    }
    @Test
    public void testAllocationMemoryEmpty() {
      // ** Does not allocate memory for empty tensor allocation **//
      testAllocation = new ArrayList<>();
      assertArrayEquals(testMemory, Validation.getAllocationArray(testAllocation, 128));
    }
    @Test
    public void testAllocationMemoryContinuous() {
      // ** Separately allocates memory for contiguous tensors **//
      testAllocation =
          new ArrayList(
              Arrays.asList(
                  new TensorAllocation[] {
                    allocationBuilder.setLabel(1).setStartAddress(0).setSize(65536).build(),
                    allocationBuilder.setLabel(2).setStartAddress(65536).setSize(65536).build()
                  }));
      for (int i = 0; i < 65536; i++) {
        testMemory[i] = 1;
      }
      for (int i = 65536; i < 131072; i++) {
        testMemory[i] = 2;
      }
      assertArrayEquals(testMemory, Validation.getAllocationArray(testAllocation, 128));
    }
    @Test
    public void TestAllocationMemoryNested() {
      // ** Treats nested memory allocations in a time sensitive format. Whichever tensor was added
      // last should occupy the memory space**//
      testAllocation =
          new ArrayList(
              Arrays.asList(
                  new TensorAllocation[] {
                    allocationBuilder.setLabel(1).setStartAddress(0).setSize(65536).build(),
                    allocationBuilder.setLabel(2).setStartAddress(4000).setSize(200).build()
                  }));
      for (int i = 0; i < 65536; i++) {
        testMemory[i] = 1;
      }
      for (int i = 4000; i < 4200; i++) {
        testMemory[i] = 2;
      }
      assertArrayEquals(testMemory, Validation.getAllocationArray(testAllocation, 128));
    }

    @Test
    public void TestAllocationMemoryOverlap() {
      // ** Treats nested memory allocations in a time sensitive format. Whichever tensor was added
      // last should occupy the memory space **//
      testAllocation =
          new ArrayList(
              Arrays.asList(
                  new TensorAllocation[] {
                    allocationBuilder.setLabel(1).setStartAddress(0).setSize(2000).build(),
                    allocationBuilder.setLabel(2).setStartAddress(1500).setSize(600).build()
                  }));

      for (int i = 0; i < 2000; i++) {
        testMemory[i] = 1;
      }

      for (int i = 1500; i < 2100; i++) {
        testMemory[i] = 2;
      }

      assertArrayEquals(testMemory, Validation.getAllocationArray(testAllocation, 128));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void TestAllocationMemoryOutofBounds() {
      testAllocation =
          new ArrayList(
              Arrays.asList(
                  new TensorAllocation[] {
                    allocationBuilder.setLabel(1).setStartAddress(0).setSize(200000).build()
                  }));

      assertArrayEquals(testMemory, Validation.getAllocationArray(testAllocation, 128));
    }
  }

  public static class TestTensorToInstruction {
    private static int[] testNarrow;
    private static int[] testWide;

    private static ArrayList<Instruction> testInstruction;
    private static ArrayList<Instruction> expected;

    @Before
    public void setUp() {
      testNarrow = new int[128 * 1024];
      testWide = new int[256 * 1024];

      Arrays.fill(testNarrow, -1);
      Arrays.fill(testWide, -1);

      testInstruction = new ArrayList<>();
    }

    @Test
    public void testEmptyInstruction() throws InvalidTensorAddressException {
      // Add no tensor field to an empty instruction //
      testInstruction = new ArrayList<>();

      Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
      assertEquals(Arrays.asList(), testInstruction);
    }

    @Test
    public void testEmptyMemoryAccess() throws InvalidTensorAddressException {
      // Add no tensor field to an instruction with empty memory access //
      testInstruction =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("fence")
                        .setTag(7)
                        .clearWideRead()
                        .clearWideWrite()
                        .clearNarrowRead()
                        .clearNarrowWrite()
                        .build()
                  }));
      expected =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    expectedInstructionBuilder
                        .setName("fence")
                        .setTag(7)
                        .clearNarrowRead()
                        .clearNarrowWrite()
                        .build()
                  }));

      Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);

      assertEquals(expected, testInstruction);
    }

    // 1 reads and 1 write -- Narrow
    @Test
    public void testReadAndWriteNarrow() throws InvalidTensorAddressException {
      // Update the tensor fields of an instruction that has a read and a write in the narrow
      // memory.//
      testNarrow[0] = 1;
      testNarrow[8] = 2;

      testInstruction =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("mixed")
                        .setTag(7)
                        .setNarrowRead(memAccessBuilder.setBaseAddress(0))
                        .setNarrowWrite(memAccessBuilder.setBaseAddress(8))
                        .clearWideRead()
                        .clearWideWrite()
                        .build()
                  }));
      expected =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("mixed")
                        .setTag(7)
                        .setNarrowRead(memAccessBuilder.setTensor(1).setBaseAddress(0))
                        .setNarrowWrite(memAccessBuilder.setTensor(2).setBaseAddress(8))
                        .clearWideRead()
                        .clearWideWrite()
                        .build()
                  }));
      Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
      assertEquals(expected, testInstruction);
    }

    @Test
    public void testReadAndWriteWide() throws InvalidTensorAddressException {
      // Update the tensor fields of an instruction that has a read and a write in the wide
      // memory.//
      testWide[0] = 1;
      testWide[8] = 2;
      testInstruction =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("mixed")
                        .setTag(7)
                        .setWideRead(memAccessBuilder.setBaseAddress(0))
                        .setWideWrite(memAccessBuilder.setBaseAddress(8))
                        .clearNarrowRead()
                        .clearNarrowWrite()
                        .build()
                  }));
      expected =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("mixed")
                        .setTag(7)
                        .setWideRead(memAccessBuilder.setTensor(1).setBaseAddress(0))
                        .setWideWrite(memAccessBuilder.setTensor(2).setBaseAddress(8))
                        .clearNarrowRead()
                        .clearNarrowWrite()
                        .build()
                  }));
      Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
      assertEquals(expected, testInstruction);
    }
    @Test
    public void testTwoReadsAndOneWrite() throws InvalidTensorAddressException {
      // Update the tensor fields of an instruction that has both reads and a write.//
      testNarrow[0] = 1;
      testWide[0] = 2;
      testWide[3170] = 3;
      testInstruction =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("mixed")
                        .setTag(7)
                        .setNarrowRead(memAccessBuilder.setBaseAddress(0))
                        .setWideRead(memAccessBuilder.setBaseAddress(0))
                        .setWideWrite(memAccessBuilder.setBaseAddress(3170))
                        .clearNarrowWrite()
                        .build()
                  }));
      expected =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("mixed")
                        .setTag(7)
                        .setNarrowRead(memAccessBuilder.setTensor(1).setBaseAddress(0))
                        .setWideRead(memAccessBuilder.setTensor(2).setBaseAddress(0))
                        .setWideWrite(memAccessBuilder.setTensor(3).setBaseAddress(3170))
                        .clearNarrowWrite()
                        .build()
                  }));
      Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
      assertEquals(expected, testInstruction);
    }
    @Test
    public void testOneReadsAndTwoWrite() throws InvalidTensorAddressException {
      // Update the tensor fields of an instruction that has a read and both writes.//
      testNarrow[0] = 1;
      testWide[0] = 2;
      testWide[3170] = 3;
      testInstruction =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("mixed")
                        .setTag(7)
                        .setNarrowWrite(memAccessBuilder.setBaseAddress(0))
                        .setWideRead(memAccessBuilder.setBaseAddress(0))
                        .setWideWrite(memAccessBuilder.setBaseAddress(3170))
                        .clearNarrowRead()
                        .build()
                  }));
      expected =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("mixed")
                        .setTag(7)
                        .setNarrowWrite(memAccessBuilder.setTensor(1).setBaseAddress(0))
                        .setWideRead(memAccessBuilder.setTensor(2).setBaseAddress(0))
                        .setWideWrite(memAccessBuilder.setTensor(3).setBaseAddress(3170))
                        .clearNarrowRead()
                        .build()
                  }));
      Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
      assertEquals(expected, testInstruction);
    }


    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testOutOfBoundTensor() throws InvalidTensorAddressException {
      // Out of bound error when the base address is out of bounds of the memory location.//
      testInstruction =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("mixed")
                        .setTag(7)
                        .setNarrowWrite(memAccessBuilder.setBaseAddress(250000))
                        .setWideRead(memAccessBuilder.setBaseAddress(0))
                        .setWideWrite(memAccessBuilder.setBaseAddress(3170))
                        .build()
                  }));
      expected =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("mixed")
                        .setTag(7)
                        .setNarrowWrite(memAccessBuilder.setTensor(1).setBaseAddress(250000))
                        .setWideRead(memAccessBuilder.setTensor(2).setBaseAddress(0))
                        .setWideWrite(memAccessBuilder.setTensor(3).setBaseAddress(3170))
                        .clearNarrowRead()
                        .build()
                  }));
      Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
      assertEquals(expected, testInstruction);
    }

    @Test(expected = InvalidTensorAddressException.class)
    public void testInvalidTensorAddress() throws InvalidTensorAddressException {
      // Throws an exception when the base address of the instruction does not contain a tensor.//
      testNarrow[0] = 1;
      testWide[0] = 2;
      testWide[3170] = 3;

      testInstruction =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("mixed")
                        .setTag(7)
                        .setNarrowWrite(memAccessBuilder.setBaseAddress(0))
                        .setWideRead(memAccessBuilder.setBaseAddress(1))
                        .setWideWrite(memAccessBuilder.setBaseAddress(3170))
                        .clearNarrowRead()
                        .build()
                  }));

      expected =
          new ArrayList(
              Arrays.asList(
                  new Instruction[] {
                    instructionBuilder
                        .setName("mixed")
                        .setTag(7)
                        .setNarrowWrite(memAccessBuilder.setTensor(1).setBaseAddress(0))
                        .setWideRead(memAccessBuilder.setTensor(2).setBaseAddress(1))
                        .setWideWrite(memAccessBuilder.setTensor(3).setBaseAddress(3170))
                        .clearNarrowRead()
                        .build()
                  }));

      Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);

      assertEquals(expected, testInstruction);
    }
  }

  public static class TestWriteValidation {
    private static int[][] testNarrow;
    private static int[][] testWide;
    private static int[][] expectedNarrow;
    private static int[][] expectedWide;
    private static int tensor;
    public static final TraceEntry.AccessType NARROWWRITE = TraceEntry.AccessType.WRITE_NARROW;
    public static final TraceEntry.AccessType WIDEWRITE = TraceEntry.AccessType.WRITE_WIDE;
    private static TraceEntry testTrace;

    @Before
    public void setUp() {
      testNarrow = new int[16][128 * 1024];
      testWide = new int[16][256 * 1024];
      expectedNarrow = new int[16][128 * 1024];
      expectedWide = new int[16][256 * 1024];
    }
 
    @Test
    public void testEmptyTrace() {  
      List<Boolean> mask = new ArrayList(); 
      for (int i = 0; i < 16; i++){
            mask.add(false);
        } 
      testTrace = traceBuilder.build();
      Validation.writeValidation(testNarrow, testWide, mask, tensor, testTrace); 
      assertEquals(expectedNarrow, testNarrow);
    }
    @Test
    public void testNonEmptyTraceNarrow() {
        tensor = 7;
        List<Boolean> mask = new ArrayList();
        for (int i = 0; i < 16; i++){
            mask.add(true);
        }
        testTrace =
                traceBuilder.setAccessType(NARROWWRITE).setInstructionTag(0).setAddress(1000).build();
                  
        for (int i = 0; i < 16; i++){
            expectedNarrow[i][1000] = 7;
        }
        Validation.writeValidation(testNarrow, testWide, mask, tensor, testTrace);
        
        assertArrayEquals(expectedNarrow, testNarrow);
    }
    @Test
    public void testNonEmptyTraceWide() {
        tensor = 7;
        List<Boolean> mask = new ArrayList();
        for (int i = 0; i < 16; i++){
            mask.add(true);
        }
        testTrace =
                traceBuilder.setAccessType(WIDEWRITE).setInstructionTag(0).setAddress(1000).build();
                  
        for (int i = 0; i < 16; i++){
            expectedWide[i][1000] = 7;
        }
        Validation.writeValidation(testNarrow, testWide, mask, tensor, testTrace);
        assertArrayEquals(expectedWide, testWide);
    }
    @Test
    public void testFalseMaskNarrow() {
        tensor = 7;
        List<Boolean> mask = new ArrayList();
        for (int i = 0; i < 16; i++){
            mask.add(false);
        }
        testTrace =
                traceBuilder.setAccessType(NARROWWRITE).setInstructionTag(0).setAddress(1200).build();
                  
        Validation.writeValidation(testNarrow, testWide, mask, tensor, testTrace);
        assertArrayEquals(expectedNarrow, testNarrow);
    }
    @Test
    public void testFalseMaskWide() {
        tensor = 7;
        List<Boolean> mask = new ArrayList();
        for (int i = 0; i < 16; i++){
            mask.add(false);
        }
        testTrace =
                traceBuilder.setAccessType(WIDEWRITE).setInstructionTag(0).setAddress(1200).build();
        Validation.writeValidation(testNarrow, testWide, mask, tensor, testTrace);
        assertArrayEquals(expectedWide, testWide);
    }
//different masks
    @Test
    public void testVariedMasksWide() {
        tensor = 7;
        List<Boolean> mask = new ArrayList();
        for (int i = 0; i < 8; i++){
            mask.add(false);
            mask.add(true);
        }
        testTrace =
                traceBuilder.setAccessType(WIDEWRITE).setInstructionTag(0).setAddress(1200).build();
        for (int i = 1; i < 16; i += 2){
            expectedWide[i][1200] = 7;
        }
        Validation.writeValidation(testNarrow, testWide, mask, tensor, testTrace);
        assertArrayEquals(expectedWide, testWide);
    }

//no write accesstype
    @Test 
    public void testNoWriteAccessType() {
        tensor = 7;
        List<Boolean> mask = new ArrayList();
        for (int i = 0; i < 16; i++){
            mask.add(false);
        } 
        testTrace =
                traceBuilder.setAccessType(TraceEntry.AccessType.READ_WIDE).setInstructionTag(0).setAddress(1200).build();
       Validation.writeValidation(testNarrow, testWide, mask, tensor, testTrace);
       assertArrayEquals(expectedWide, testWide);
    }
  }

  public static class TestGetTraceTensor {
        private Instruction.Builder instructionBuilder;
        private MemoryAccess.Builder memoryAccessBuilder;
        private int traceAddress;
        private TraceEntry.AccessType traceAccessType;
        private int expectedTensor;
        private int recievedTensor;

        @Before
        public void setUp() {
            instructionBuilder = Instruction.newBuilder();
            memoryAccessBuilder = MemoryAccess.newBuilder();
            traceAddress = 0;
        }

        // Trace is narrow read, instruction has narrow read, confirm returned tensor
        @Test
        public void testValidNarrowRead() throws Exception, MemoryAccessException {
            traceAccessType = TraceEntry.AccessType.READ_NARROW;

            instructionBuilder
                .setNarrowRead(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
                .setTag(0);

            expectedTensor = 2;

            recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is narrow write, instruction has narrow write, confirm returned tensor
        @Test
        public void testValidNarrowWrite() throws Exception, MemoryAccessException {
            traceAccessType = TraceEntry.AccessType.WRITE_NARROW;

            instructionBuilder
                .setNarrowWrite(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
                .setTag(0);

            expectedTensor = 2;

            recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is wide read, instruction has wide read, confirm returned tensor
        @Test
        public void testValidWideRead() throws Exception, MemoryAccessException {
            traceAccessType = TraceEntry.AccessType.READ_WIDE;

            instructionBuilder
                .setWideRead(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
                .setTag(0);

            expectedTensor = 2;

            recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is wide write, instruction has wide write, confirm returned tensor
        @Test
        public void testValidWideWrite() throws Exception, MemoryAccessException {
            traceAccessType = TraceEntry.AccessType.WRITE_WIDE;

            instructionBuilder
                .setWideWrite(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
                .setTag(0);

            expectedTensor = 2;

            recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is narrow read, instruction does not have narrow read, catch MAE
        @Test(expected = MemoryAccessException.class)
        public void testInvalidNarrowRead() throws Exception, MemoryAccessException {
            traceAccessType = TraceEntry.AccessType.READ_NARROW;

            instructionBuilder
                .setWideRead(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
                .setTag(0);

            expectedTensor = 2;

            recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is narrow write, instruction does not have narrow write, catch MAE
        @Test(expected = MemoryAccessException.class)
        public void testInvalidNarrowWrite() throws Exception, MemoryAccessException {
            traceAccessType = TraceEntry.AccessType.WRITE_NARROW;

            instructionBuilder
                .setWideWrite(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
                .setTag(0);

            expectedTensor = 2;

            recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is wide read, instruction does not have wide read, catch MAE
        @Test(expected = MemoryAccessException.class)
        public void testInvalidWideRead() throws Exception, MemoryAccessException {
            traceAccessType = TraceEntry.AccessType.READ_WIDE;

            instructionBuilder
                .setNarrowRead(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
                .setTag(0);

            expectedTensor = 2;

            recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is wide write, instruction does not have wide write, catch MAE
        @Test(expected = MemoryAccessException.class)
        public void testInvalidWideWrite() throws Exception, MemoryAccessException {
            traceAccessType = TraceEntry.AccessType.WRITE_WIDE;

            instructionBuilder
                .setNarrowWrite(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
                .setTag(0);

            expectedTensor = 2;

            recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace has null access type
        @Test(expected = Exception.class)
        public void testNullAccessTraceEntry() throws Exception, MemoryAccessException {
            traceAccessType = null;

            instructionBuilder
                .setNarrowRead(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
                .setTag(0);

            expectedTensor = 2;

            recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Instruction has the correct access type, but that MemoryAccess does not have 
        // a tensor associated with it.
        @Test(expected = Exception.class)
        public void testValidMemoryAccessWithoutCorrespondingTraceEntry() throws Exception, MemoryAccessException {
            traceAccessType = TraceEntry.AccessType.READ_NARROW;

            instructionBuilder
                .setNarrowRead(memoryAccessBuilder.setBaseAddress(0))
                .setTag(0);

            expectedTensor = 2;

            recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }
    }

        public static class TestInstructionTagtoInstructionTable {
        private List<Instruction> testInstructions;
        private Map<Integer, Instruction> expected;

        @Before
        public void setUp() {
            expected = new Hashtable<>();
        }

        @Test
        // An empty set of lists
        public void testEmptyInstructions() {
            testInstructions = new ArrayList<>();

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // A single instruction with no masks or memory accesses
        public void testSingleInstruction() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .build()));

            expected.put(
                0,
                Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList())
                    .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with no masks or memory accesses
        public void testMultipleInstructions() {
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
                0,
                Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList())
                    .build());
            expected.put(
                1,
                Instruction.newBuilder()
                    .setName("B")
                    .setTag(1).addAllMask(Arrays.asList())
                    .build());
            expected.put(
                2,
                Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .addAllMask(Arrays.asList())
                    .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // A single instruction with memory access and no masks
        public void testSingleInstructionWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .setNarrowRead(
                                MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                            .build()));

            expected.put(
                0,
                Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList())
                    .setNarrowRead(
                        MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                    .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with memory accesses and no masks
        public void testMultipleInstructionsWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .setNarrowRead(
                                MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                            .build(),
                        Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .addAllMask(Arrays.asList())
                            .setNarrowWrite(
                                MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                            .build(),
                        Instruction.newBuilder()
                            .setName("C")
                            .setTag(2)
                            .addAllMask(Arrays.asList())
                            .setWideWrite(
                                MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                            .build()));

            expected.put(
                0,
                Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList())
                    .setNarrowRead(
                        MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                    .build());
            expected.put(
                1,
                Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .addAllMask(Arrays.asList())
                    .setNarrowWrite(
                        MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                    .build());
            expected.put(
                2,
                Instruction.newBuilder()
                .setName("C")
                .setTag(2)
                .addAllMask(Arrays.asList())
                .setWideWrite(
                    MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with no masks and with/without memory accesses
        public void testMultipleInstructionsMixedMemoryAccessTags() {
            testInstructions = new ArrayList<>(Arrays.asList(
                Instruction.newBuilder().setName("A").setTag(0)
                    .addAllMask(Arrays.asList())
                    .setNarrowRead(MemoryAccess.newBuilder().addAllCounter(Arrays.asList())).build(),
                Instruction.newBuilder().setName("B")
                    .setTag(1)
                    .addAllMask(Arrays.asList()).build(),
                Instruction.newBuilder().setName("C")
                    .setTag(2)
                    .addAllMask(Arrays.asList())
                    .setWideWrite(MemoryAccess.newBuilder().addAllCounter(Arrays.asList())).build()));

            expected.put(0, Instruction.newBuilder()
                .setName("A")
                .setTag(0)
                .addAllMask(Arrays.asList())
                .setNarrowRead(MemoryAccess.newBuilder().addAllCounter(Arrays.asList())).build());
            expected.put(1, Instruction.newBuilder()
                .setName("B")
                .setTag(1)
                .addAllMask(Arrays.asList()).build());
            expected.put(2, Instruction.newBuilder()
                .setName("C")
                .setTag(2)
                .addAllMask(Arrays.asList())
                .setWideWrite(MemoryAccess.newBuilder().addAllCounter(Arrays.asList())).build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // A single instruction with a single mask and no memory accesses
        public void testSingleInstructionOneMask() {
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
                Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList(true))
                    .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // A single instruction with multiple masks and no memory accesses
        public void testSingleInstructionMultipleMasks() {
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

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with a single mask and no memory accesses
        public void testMultipleInstructionsOneMask() {
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
                    Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList(true))
                    .build());
            expected.put(
                1,
                    Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .addAllMask(Arrays.asList(false))
                    .build());
            expected.put(
                2,
                Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .addAllMask(Arrays.asList(true))
                    .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with multiple masks and no memory accesses
        public void testMultipleInstructionsMultipleMasks() {
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

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with no memory accesses and 0-3 masks
        public void testMultipleInstructionsMixedMasks() {
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
                    Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList())
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
                    .addAllMask(Arrays.asList(true, true))
                    .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // A single instruction with a single mask and a memory access
        public void testSingleInstructionOneMaskWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList(false))
                            .setNarrowRead(
                                MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .build()));

            expected.put(
                0,
                Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList(false))
                    .setNarrowRead(
                        MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // A single instruction with multiple masks and a memory access
        public void testSingleInstructionMultipleMasksWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList(false, true, true))
                            .setNarrowRead(
                                MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .build()));

            expected.put(
                0,
                Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList(false, true, true))
                    .setNarrowRead(
                        MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with a single mask and memory accesses
        public void testMultipleInstructionsOneMaskWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .setNarrowWrite(
                                MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true))
                            .build(),
                        Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .setNarrowRead(
                                MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(false))
                            .build(),
                        Instruction.newBuilder()
                            .setName("C")
                            .setTag(2)
                            .setWideRead(
                                MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true))
                            .build()));

            expected.put(
                0,
                Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .setNarrowWrite(
                        MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true))
                    .build());
            expected.put(
                1,
                Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .setNarrowRead(
                        MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(false))
                    .build());
            expected.put(
                2,
                Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .setWideRead(
                        MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true))
                    .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with a single mask and with/without a memory access
        public void testMultipleInstructionsOneMaskMixedMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .setNarrowWrite(
                                MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
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
                            .setWideRead(
                                MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true))
                            .build()));

            expected.put(
                0,
                Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .setNarrowWrite(
                        MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true))
                    .build());
            expected.put(
                1,
                Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .addAllMask(Arrays.asList(false))
                    .build());
            expected.put(
                2,
                Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .setWideRead(
                        MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true))
                    .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with multiple masks and memory accesses
        public void testMultipleInstructionsMultipleMasksWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .setNarrowWrite(
                                MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true, false, true))
                            .build(),
                        Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .setNarrowRead(
                                MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(false, false))
                            .build(),
                        Instruction.newBuilder()
                            .setName("C")
                            .setTag(2)
                            .setWideRead(
                                MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true, false, true, false))
                            .build()));

            expected.put(
                0,
                Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .setNarrowWrite(
                        MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true, false, true))
                    .build());
            expected.put(
                1,
                Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .setNarrowRead(
                        MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(false, false))
                    .build());
            expected.put(
                2,
                Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .setWideRead(
                        MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true, false, true, false))
                    .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with multiple masks and with/without memory access
        public void testMultipleInstructionsMultipleMasksMixedMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .setNarrowWrite(
                                MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
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
                            .setWideRead(
                                MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true, false, true, false))
                            .build()));

            expected.put(
                0,
                Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .setNarrowWrite(
                        MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
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
                    .setWideRead(
                        MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true, false, true, false))
                    .build());

            assertEquals(expected, Validation.relateIntructionTagtoInstructionTable(testInstructions));
        }
    }

    public static class TestReadValidate {
        // /* // Un-comment for testing purposes
        private final static int NUM_TILES = 16;

        public static boolean readValidation(
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

            return true;
        }
    // */
        private int[][] narrow;
        private int[][] wide;

        private final Instruction INSTRUCTION_ONE =
            Instruction.newBuilder()
                .setTag(1)
                .addAllMask(
                    Arrays.asList(
                        true, false, false, false, true, true, true, true, false, false, false, false,
                        false, true, true, true))
                .setNarrowRead(
                    MemoryAccess.newBuilder()
                        .setTensor(12)
                        .setBaseAddress(189)
                        .addAllCounter(Arrays.asList()))
                .build();

        private final TraceEntry TRACE_ONE = 
            TraceEntry.newBuilder()
                    .setAccessType(TraceEntry.AccessType.READ_NARROW)
                    .setInstructionTag(1)
                    .setAddress(189)
                    .build();

        private final Instruction INSTRUCTION_TWO =
            Instruction.newBuilder()
                .setTag(12)
                .addAllMask(
                    Arrays.asList(
                        true, false, false, false, true, true, true, true, false, false, false, false,
                        false, false, false, false))
                .setNarrowRead(
                    MemoryAccess.newBuilder()
                        .setTensor(9)
                        .setBaseAddress(796)
                        .addAllCounter(Arrays.asList()))
                .setWideRead(
                    MemoryAccess.newBuilder()
                        .setTensor(4)
                        .setBaseAddress(10240)
                        .addAllCounter(Arrays.asList()))
                .build();

        private final TraceEntry TRACE_TWO_ONE = 
            TraceEntry.newBuilder()
                .setAccessType(TraceEntry.AccessType.READ_NARROW)
                .setInstructionTag(12)
                .setAddress(796)
                .build();

        private final TraceEntry TRACE_TWO_TWO = 
            TraceEntry.newBuilder()
                .setAccessType(TraceEntry.AccessType.READ_WIDE)
                .setInstructionTag(12)
                .setAddress(10240)
                .build();

        private final Instruction INSTRUCTION_THREE =
            Instruction.newBuilder()
                .setTag(5)
                .addAllMask(
                    Arrays.asList(
                        true, true, true, true, true, true, true, true, false, false, false, false,
                        false, true, true, true))
                .setWideRead(
                    MemoryAccess.newBuilder()
                        .setTensor(12)
                        .setBaseAddress(2347)
                        .addAllCounter(Arrays.asList()))
                .build();

        private final TraceEntry TRACE_THREE = 
            TraceEntry.newBuilder()
              .setAccessType(TraceEntry.AccessType.READ_WIDE)
              .setInstructionTag(5)
              .setAddress(564)
              .build();

        @Before
        public void setUp() {
            narrow = new int[16][128 * 1024];
            wide = new int[16][256 * 1024];
        }

        // Tests a valid read trace
        @Test
        public void testValidRead() throws InvalidTensorReadException {
            int i = 0;

            for (boolean mask : INSTRUCTION_ONE.getMaskList()) {
                if (mask) {
                    narrow[i][INSTRUCTION_ONE.getNarrowRead().getBaseAddress()] = 
                        INSTRUCTION_ONE.getNarrowRead().getTensor(); 
                }

                i++;
            }

            assertTrue(
                readValidation(
                    narrow, 
                    wide, 
                    INSTRUCTION_ONE.getMaskList(), 
                    INSTRUCTION_ONE.getNarrowRead().getTensor(), 
                    TRACE_ONE));
        }

        // Tests two valid read traces in a single instruction
        @Test
        public void testTwoValidRead() throws InvalidTensorReadException {
            int i = 0;

            for (boolean mask : INSTRUCTION_TWO.getMaskList()) {
                if (mask) {
                    narrow[i][INSTRUCTION_TWO.getNarrowRead().getBaseAddress()] = 
                        INSTRUCTION_TWO.getNarrowRead().getTensor();

                    wide[i][INSTRUCTION_TWO.getWideRead().getBaseAddress()] = 
                        INSTRUCTION_TWO.getWideRead().getTensor();
                }

                i++;
            }

            assertTrue(
                readValidation(
                    narrow, 
                    wide, 
                    INSTRUCTION_TWO.getMaskList(), 
                    INSTRUCTION_TWO.getNarrowRead().getTensor(), 
                    TRACE_TWO_ONE));

            assertTrue(
                readValidation(
                    narrow, 
                    wide, 
                    INSTRUCTION_TWO.getMaskList(), 
                    INSTRUCTION_TWO.getWideRead().getTensor(), 
                    TRACE_TWO_TWO));
        }

        // Tests an invalid read trace
        @Test (expected = InvalidTensorReadException.class)
        public void testInvalidRead() throws InvalidTensorReadException {
            int i = 0;

            for (boolean mask : INSTRUCTION_THREE.getMaskList()) {
                if (mask) {
                    wide[i][INSTRUCTION_THREE.getWideRead().getBaseAddress()] = 
                        INSTRUCTION_THREE.getWideRead().getTensor(); 
                }

                i++;
            }

            assertTrue(
                readValidation(
                    narrow, 
                    wide, 
                    INSTRUCTION_THREE.getMaskList(), 
                    INSTRUCTION_THREE.getWideRead().getTensor(), 
                    TRACE_THREE));
        }

        // Tests a valid and invalid read trace in a single instruction
        @Test (expected = InvalidTensorReadException.class)
        public void testValidandInvalidRead() throws InvalidTensorReadException {
            int i = 0;

            for (boolean mask : INSTRUCTION_TWO.getMaskList()) {
                if (mask) {
                    narrow[i][INSTRUCTION_TWO.getNarrowRead().getBaseAddress()] = 
                        INSTRUCTION_TWO.getNarrowRead().getTensor();
                }

                i++;
            }

            assertTrue(
                readValidation(
                    narrow, 
                    wide, 
                    INSTRUCTION_TWO.getMaskList(), 
                    INSTRUCTION_TWO.getNarrowRead().getTensor(), 
                    TRACE_TWO_ONE));

            assertTrue(
                readValidation(
                    narrow, 
                    wide, 
                    INSTRUCTION_TWO.getMaskList(), 
                    INSTRUCTION_TWO.getWideRead().getTensor(), 
                    TRACE_TWO_TWO));
        }
    }
}
