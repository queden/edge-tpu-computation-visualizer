package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Hashtable;
import java.util.List;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import java.lang.reflect.Constructor;

import com.google.sps.exceptions.*;
import com.google.sps.proto.MemaccessCheckerDataProto.*;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

@RunWith(TestsCaden.class)
@Suite.SuiteClasses({TestsCaden.TestGetTraceTensor.class, TestsCaden.TestRelateTensorsToInstructions.class, TestsCaden.TestGetLayerToInstructionTable.class})
public final class TestsCaden extends Suite {

    public TestsCaden(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
    }

    public static class TestGetTraceTensor {
        private Validation validation;
        private Instruction.Builder instructionBuilder;
        private ArrayList<Integer> accessList;
        private int traceAddress;
        private int expectedTensor;
        private int recievedTensor;
        private TraceEvent.AccessType traceAccessType;

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

            mGetTraceTensor = Validation.class.getDeclaredMethod("getTraceTensor", int.class, TraceEvent.AccessType.class, Instruction.class);
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
            recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
            
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

            recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
            
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

            recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
            
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

            recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
            
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
                recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
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
                recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
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
                recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
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
                recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
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
                recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
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
                recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
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
                recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
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
}
