package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Hashtable;
import java.util.List;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

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
@Suite.SuiteClasses({TestsCaden.TestGetTraceTensor.class})
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
        private Field mTensorLabelToTensorAllocationNarrow;
        private Field mTensorLabelToTensorAllocationWide;

        @Before
        public void setUp() throws NoSuchMethodException,
        NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
            instructionBuilder = Instruction.newBuilder();
            accessList = new ArrayList<Integer>();
            traceAddress = 0;

            MemaccessCheckerData.Builder protoBuilder = MemaccessCheckerData.newBuilder();

            Validation validation = new Validation(protoBuilder.build());

            mGetTraceTensor = Validation.class.getDeclaredMethod("getTraceTensor", int.class, TraceEvent.AccessType.class, Instruction.class);
            mGetTraceTensor.setAccessible(true);

            mTensorLabelToTensorAllocationNarrow = Validation.class.getDeclaredField("tensorLabelToTensorAllocationNarrow");
            mTensorLabelToTensorAllocationNarrow.setAccessible(true);

            mTensorLabelToTensorAllocationWide = Validation.class.getDeclaredField("tensorLabelToTensorAllocationWide");
            mTensorLabelToTensorAllocationWide.setAccessible(true);

            Map<Integer, TensorAllocation> narrowMap = new Hashtable<Integer, TensorAllocation>();
            Map<Integer, TensorAllocation> wideMap = new Hashtable<Integer, TensorAllocation>();

            TensorAllocation.Builder tensorBuilder = TensorAllocation.newBuilder();
            tensorBuilder.setTensorLabel(2).setBaseAddress(0).setSize(45);

            TensorAllocation tensor1 = tensorBuilder.build();

            tensorBuilder = TensorAllocation.newBuilder();
            tensorBuilder.setTensorLabel(47).setBaseAddress(45).setSize(45);

            TensorAllocation tensor2 = tensorBuilder.build();

            narrowMap.put(2, tensor1);
            narrowMap.put(47, tensor2);

            wideMap.put(2, tensor1);
            wideMap.put(47, tensor2);

            mTensorLabelToTensorAllocationNarrow.set(validation, narrowMap);
            mTensorLabelToTensorAllocationWide.set(validation, wideMap);
        }

        // Trace is narrow read, instruction has narrow read, confirm returned tensor
        @Test
        public void testValidNarrowRead() throws Exception, MemoryAccessException {
            traceAccessType = TraceEvent.AccessType.NARROW_READ;

            accessList.addAll(Arrays.asList(2, 47));

            instructionBuilder
                .addAllNarrowRead(accessList)
                .setTag(0);

            expectedTensor = 2;


            recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is narrow write, instruction has narrow write, confirm returned tensor
        @Test
        public void testValidNarrowWrite() throws Exception, MemoryAccessException {
            traceAccessType = TraceEvent.AccessType.NARROW_WRITE;

            accessList.addAll(Arrays.asList(2, 47));

            instructionBuilder
                .addAllNarrowWrite(accessList)
                .setTag(0);

            expectedTensor = 2;

            recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is wide read, instruction has wide read, confirm returned tensor
        @Test
        public void testValidWideRead() throws Exception, MemoryAccessException {
            traceAccessType = TraceEvent.AccessType.WIDE_READ;

            accessList.addAll(Arrays.asList(2, 47));

            instructionBuilder
                .addAllWideRead(accessList)
                .setTag(0);
            expectedTensor = 2;

            recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is wide write, instruction has wide write, confirm returned tensor
        @Test
        public void testValidWideWrite() throws Exception, MemoryAccessException {
            traceAccessType = TraceEvent.AccessType.WIDE_WRITE;

            accessList.addAll(Arrays.asList(2, 47));

            instructionBuilder
                .addAllWideWrite(accessList)
                .setTag(0);

            expectedTensor = 2;

            recievedTensor = (int) mGetTraceTensor.invoke(validation, traceAddress, traceAccessType, instructionBuilder.build());
            
            assertEquals(expectedTensor, recievedTensor);
        }

        // Trace is narrow read, instruction does not have narrow read, catch MAE
        @Test(expected = MemoryAccessException.class)
        public void testInvalidNarrowRead() throws Exception, MemoryAccessException, Throwable {
            traceAccessType = TraceEvent.AccessType.NARROW_READ;
            
            accessList.addAll(Arrays.asList(2, 47));

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

        // Trace is narrow write, instruction does not have narrow write, catch MAE
        @Test(expected = MemoryAccessException.class)
        public void testInvalidNarrowWrite() throws Exception, MemoryAccessException, Throwable {
            traceAccessType = TraceEvent.AccessType.NARROW_WRITE;

            accessList.addAll(Arrays.asList(2, 47));

            instructionBuilder
                .addAllNarrowRead(accessList)
                .setTag(0);

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

            accessList.addAll(Arrays.asList(2, 47));

            instructionBuilder
                .addAllNarrowRead(accessList)
                .setTag(0);

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
            
            accessList.addAll(Arrays.asList(2, 47));

            instructionBuilder
                .addAllNarrowWrite(accessList)
                .setTag(0);

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

            accessList.addAll(Arrays.asList(2, 47));

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

        // TODO: This will have a lot to do with tensor allocation table
        // // Instruction has the correct access type, but that MemoryAccess does not have 
        // // a tensor associated with it.
        // @Test(expected = Exception.class)
        // public void testValidMemoryAccessWithoutCorrespondingTraceEvent() throws Exception, MemoryAccessException {
        //     traceAccessType = TraceEvent.AccessType.READ_NARROW;

        //     instructionBuilder
        //         .setNarrowRead(memoryAccessBuilder.setBaseAddress(0))
        //         .setTag(0);

        //     expectedTensor = 2;

        //     recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
        //     assertEquals(expectedTensor, recievedTensor);
        //}
    }
}
