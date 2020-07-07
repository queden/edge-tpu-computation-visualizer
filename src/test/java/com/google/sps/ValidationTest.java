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
import com.google.sps.proto.SimulationTraceProto;
import com.google.sps.proto.SimulationTraceProto.Instruction;
import com.google.sps.proto.SimulationTraceProto.TensorAllocation;
import com.google.sps.proto.SimulationTraceProto.TraceEntry;
import java.util.ArrayList;
import java.util.List;
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
  ValidationTest.TestWriteValidation.class
})

public final class ValidationTest extends Suite {
  private static SimulationTraceProto.TensorAllocation.Builder allocationBuilder =
      SimulationTraceProto.TensorAllocation.newBuilder();
  private static SimulationTraceProto.Instruction.Builder instructionBuilder =
      SimulationTraceProto.Instruction.newBuilder();
  private static SimulationTraceProto.Instruction.Builder expectedInstructionBuilder =
      SimulationTraceProto.Instruction.newBuilder();
  private static SimulationTraceProto.MemoryAccess.Builder memAccessBuilder =
      SimulationTraceProto.MemoryAccess.newBuilder();
  private static SimulationTraceProto.TraceEntry.Builder traceBuilder =
      SimulationTraceProto.TraceEntry.newBuilder();
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
    public void testEmptyTrace() throws Exception {  
      List<Boolean> mask = new ArrayList(); 
      for (int i = 0; i < 16; i++){
            mask.add(false);
        } 
      testTrace = traceBuilder.build();
      Validation.writeValidation(testNarrow, testWide, mask, tensor, testTrace); 
      assertEquals(expectedNarrow, testNarrow);
    }
    @Test
    public void testNonEmptyTraceNarrow() throws Exception {
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
    public void testNonEmptyTraceWide() throws Exception {
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
    public void testFalseMaskNarrow() throws Exception {
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
    public void testFalseMaskWide() throws Exception {
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
    public void testVariedMasksWide() throws Exception {
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
//empty mask -- throw exception
    @Test 
    public void testEmptyMask() throws Exception {
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
//no accesstype
    @Test(expected = Exception.class)
    public void testNoAccessType() throws Exception {
        tensor = 7;
        List<Boolean> mask = new ArrayList();
        for (int i = 0; i < 16; i++){
            mask.add(false);
        } 
        testTrace =
                traceBuilder.clearAccessType().setInstructionTag(0).setAddress(1200).build();
       Validation.writeValidation(testNarrow, testWide, mask, tensor, testTrace);
       assertArrayEquals(expectedWide, testWide);
    }
//no write accesstype
    @Test 
    public void testNoWriteAccessType() throws Exception {
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
//no address
    @Test(expected = Exception.class)
    public void testNoAddress() throws Exception {
        tensor = 7;
        List<Boolean> mask = new ArrayList();
        for (int i = 0; i < 16; i++){
            mask.add(false);
        } 
        testTrace =
                traceBuilder.setAccessType(WIDEWRITE).setInstructionTag(0).clearAddress().build();
       Validation.writeValidation(testNarrow, testWide, mask, tensor, testTrace);
       assertArrayEquals(expectedWide, testWide);
    }
  }
}
