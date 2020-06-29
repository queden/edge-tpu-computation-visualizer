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
 
package com.google.sps.data;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.sps.proto.SimulationTraceProto;
import com.google.sps.proto.SimulationTraceProto.TensorAllocation;
import com.google.sps.proto.SimulationTraceProto.Instruction;
import com.google.sps.proto.SimulationTraceProto.MemoryAccess;
import com.google.sps.proto.SimulationTraceProto.Counter;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
 
/** */
@RunWith(ValidationTest.class)
@Suite.SuiteClasses({ValidationTest.TestAllocationMemory.class, ValidationTest.TestTensorToInstruction.class})
//@Suite.SuiteClasses({ValidationTest.TestTensorToInstruction.class})
 
public final class ValidationTest extends Suite {
    private static SimulationTraceProto.TensorAllocation.Builder allocationBuilder = 
        SimulationTraceProto.TensorAllocation.newBuilder();
 
    private static SimulationTraceProto.Instruction.Builder instructionBuilder = 
        SimulationTraceProto.Instruction.newBuilder();
    private static SimulationTraceProto.Instruction.Builder expectedInstructionBuilder = 
        SimulationTraceProto.Instruction.newBuilder();
    
    private static SimulationTraceProto.MemoryAccess.Builder memAccessBuilder = 
        SimulationTraceProto.MemoryAccess.newBuilder();
 
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
            testAllocation = new ArrayList<>(Arrays.asList(new TensorAllocation[]{
                allocationBuilder.setLabel(1).setStartAddress(8).setSize(1000).build(), 
                allocationBuilder.setLabel(2).setStartAddress(95678).setSize(216).build()}));
 
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
            testAllocation = new ArrayList<>();
 
            assertArrayEquals(testMemory, Validation.getAllocationArray(testAllocation, 128));
        }
 
        @Test
        public void testAllocationMemoryContinuous() {
            
            testAllocation = new ArrayList(Arrays.asList(new TensorAllocation[]{
                allocationBuilder.setLabel(1).setStartAddress(0).setSize(65536).build(), 
                allocationBuilder.setLabel(2).setStartAddress(65536).setSize(65536).build()}));

           
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
            testAllocation = new ArrayList(Arrays.asList(new TensorAllocation[]{
                allocationBuilder.setLabel(1).setStartAddress(0).setSize(65536).build(), 
                allocationBuilder.setLabel(2).setStartAddress(4000).setSize(200).build()}));
           
 
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
            testAllocation = new ArrayList(Arrays.asList(new TensorAllocation[]{
                allocationBuilder.setLabel(1).setStartAddress(0).setSize(2000).build(), 
                allocationBuilder.setLabel(2).setStartAddress(1500).setSize(600).build()}));
            
           
 
            for (int i = 0; i < 2000; i++) {
                testMemory[i] = 1;
            }
    
            for (int i = 1500; i < 2100; i++) {
                testMemory[i] = 2;
            }
    
            assertArrayEquals(testMemory, Validation.getAllocationArray(testAllocation, 128));
        }
 
        @Test (expected = IndexOutOfBoundsException.class)
        public void TestAllocationMemoryOutofBounds() {
            testAllocation = new ArrayList(Arrays.asList(new TensorAllocation[]{
                allocationBuilder.setLabel(1).setStartAddress(0).setSize(200000).build()}));
            
            
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
            testInstruction = new ArrayList<>();
        }
    //empty instructions
        @Test
        public void testEmptyInstruction() {
            
            testInstruction = new ArrayList<>();
            
            Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
            assertEquals(Arrays.asList(), testInstruction);
        }
    //empty memory access
        @Test
        public void testEmptyMemoryAccess() {
            System.out.println(testInstruction);
            testInstruction = new ArrayList(Arrays.asList(new Instruction[]{
                instructionBuilder.setName("fence").setTag(7)
                .clearWideRead()
                .clearWideWrite()
                .clearNarrowRead()
                .clearNarrowWrite().build()}));
          //  testInstruction.clear();
            expected = new ArrayList(Arrays.asList(new Instruction[]{
                expectedInstructionBuilder.setName("fence").setTag(7)
                .clearNarrowRead()
                .clearNarrowWrite().build()}));
            
            
            Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
            
           assertEquals(expected, testInstruction);
        }
 
        //1 reads and 1 write -- Narrow
        @Test
        public void testReadAndWriteNarrow() {
           
            testNarrow[0] = 1;
            testNarrow[8] = 2;
            
            testInstruction = new ArrayList(Arrays.asList(new Instruction[]{
                instructionBuilder.setName("mixed").setTag(7)
                .setNarrowRead(memAccessBuilder.setBaseAddress(0))
                .setNarrowWrite(memAccessBuilder.setBaseAddress(8))
                .clearWideRead()
                .clearWideWrite()
                .build()}));
            expected = new ArrayList(Arrays.asList(new Instruction[]{
                instructionBuilder.setName("mixed").setTag(7)
                .setNarrowRead(memAccessBuilder.setTensor(1).setBaseAddress(0))
                .setNarrowWrite(memAccessBuilder.setTensor(2).setBaseAddress(8))
                .clearWideRead()
                .clearWideWrite().build()}));
            Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
           assertEquals(expected, testInstruction);
        } 
 
        //1 reads and 1 write -- Wide
        @Test
        public void testReadAndWriteWide() {
           
            testWide[0] = 1;
            testWide[8] = 2;
            
            testInstruction = new ArrayList(Arrays.asList(new Instruction[]{
                instructionBuilder.setName("mixed").setTag(7)
                .setWideRead(memAccessBuilder.setBaseAddress(0))
                .setWideWrite(memAccessBuilder.setBaseAddress(8))
                .clearNarrowRead()
                .clearNarrowWrite()
                .build()}));
          
            expected = new ArrayList(Arrays.asList(new Instruction[]{
                instructionBuilder.setName("mixed").setTag(7)
                .setWideRead(memAccessBuilder.setTensor(1).setBaseAddress(0))
                .setWideWrite(memAccessBuilder.setTensor(2).setBaseAddress(8))
                .clearNarrowRead()
                .clearNarrowWrite()
                .build()}));
           
            Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
            
           assertEquals(expected, testInstruction);
        } 
 
        //2 reads and 1 write -- Wide
        @Test
        public void testTwoReadsAndOneWrite() {
           
            testNarrow[0] = 1;
            testWide[0] = 2;
            testWide[3170] = 3;
            
            testInstruction = new ArrayList(Arrays.asList(new Instruction[]{
                instructionBuilder.setName("mixed").setTag(7)
                .setNarrowRead(memAccessBuilder.setBaseAddress(0))
                .setWideRead(memAccessBuilder.setBaseAddress(0))
                .setWideWrite(memAccessBuilder.setBaseAddress(3170))
                .clearNarrowWrite()
                .build()}));
          
            expected = new ArrayList(Arrays.asList(new Instruction[]{
                instructionBuilder.setName("mixed").setTag(7)
                .setNarrowRead(memAccessBuilder.setTensor(1).setBaseAddress(0))
                .setWideRead(memAccessBuilder.setTensor(2).setBaseAddress(0))
                .setWideWrite(memAccessBuilder.setTensor(3).setBaseAddress(3170))
                .clearNarrowWrite()
                .build()}));
           
            Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
            
           assertEquals(expected, testInstruction);
        } 
 
        //2 reads and 1 write -- Wide
        @Test
        public void testOneReadsAndTwoWrite() {
           
            testNarrow[0] = 1;
            testWide[0] = 2;
            testWide[3170] = 3;
            
            testInstruction = new ArrayList(Arrays.asList(new Instruction[]{
                instructionBuilder.setName("mixed").setTag(7)
                .setNarrowWrite(memAccessBuilder.setBaseAddress(0))
                .setWideRead(memAccessBuilder.setBaseAddress(0))
                .setWideWrite(memAccessBuilder.setBaseAddress(3170))
                .clearNarrowRead()
                .build()}));
          
            expected = new ArrayList(Arrays.asList(new Instruction[]{
                instructionBuilder.setName("mixed").setTag(7)
                .setNarrowWrite(memAccessBuilder.setTensor(1).setBaseAddress(0))
                .setWideRead(memAccessBuilder.setTensor(2).setBaseAddress(0))
                .setWideWrite(memAccessBuilder.setTensor(3).setBaseAddress(3170))
                .clearNarrowRead()
                .build()}));
           
            Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
            
           assertEquals(expected, testInstruction);
        } 

        //outbound
        @Test (expected = IndexOutOfBoundsException.class)
        public void testOutOfBoundTensor() {
            testInstruction = new ArrayList(Arrays.asList(new Instruction[]{
                instructionBuilder.setName("mixed").setTag(7)
                .setNarrowWrite(memAccessBuilder.setBaseAddress(200000))
                .setWideRead(memAccessBuilder.setBaseAddress(0))
                .setWideWrite(memAccessBuilder.setBaseAddress(3170))
                .build()}));
            Validation.relateTensorsToInstructions(testNarrow, testWide, testInstruction);
            assertEquals(Arrays.asList(), testInstruction);
        }
 
 
    }    
}