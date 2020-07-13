// package com.google.sps;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Map;
// import java.util.Hashtable;
// import java.util.List;
// import com.google.sps.exceptions.*;
// import com.google.sps.proto.SimulationTraceProto.*;
// import static org.junit.Assert.*;
// import org.junit.Before;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.junit.runners.JUnit4;
// import org.junit.runners.Suite;
// import org.junit.runners.model.InitializationError;
// import org.junit.runners.model.RunnerBuilder;

// @RunWith(TestsCaden.class)
// @Suite.SuiteClasses({TestsCaden.TestGetTraceTensor.class})
// public final class TestsCaden extends Suite {

//     public TestsCaden(Class<?> klass, RunnerBuilder builder) throws InitializationError {
//         super(klass, builder);
//     }

//     public static class TestGetTraceTensor {
//         private Instruction.Builder instructionBuilder;
//         private MemoryAccess.Builder memoryAccessBuilder;
//         private int traceAddress;
//         private TraceEntry.AccessType traceAccessType;
//         private int expectedTensor;
//         private int recievedTensor;

//         @Before
//         public void setUp() {
//             instructionBuilder = Instruction.newBuilder();
//             memoryAccessBuilder = MemoryAccess.newBuilder();
//             traceAddress = 0;
//         }

//         // Trace is narrow read, instruction has narrow read, confirm returned tensor
//         @Test
//         public void testValidNarrowRead() throws Exception, MemoryAccessException {
//             traceAccessType = TraceEntry.AccessType.READ_NARROW;

//             instructionBuilder
//                 .setNarrowRead(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
//                 .setTag(0);

//             expectedTensor = 2;

//             recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
//             assertEquals(expectedTensor, recievedTensor);
//         }

//         // Trace is narrow write, instruction has narrow write, confirm returned tensor
//         @Test
//         public void testValidNarrowWrite() throws Exception, MemoryAccessException {
//             traceAccessType = TraceEntry.AccessType.WRITE_NARROW;

//             instructionBuilder
//                 .setNarrowWrite(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
//                 .setTag(0);

//             expectedTensor = 2;

//             recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
//             assertEquals(expectedTensor, recievedTensor);
//         }

//         // Trace is wide read, instruction has wide read, confirm returned tensor
//         @Test
//         public void testValidWideRead() throws Exception, MemoryAccessException {
//             traceAccessType = TraceEntry.AccessType.READ_WIDE;

//             instructionBuilder
//                 .setWideRead(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
//                 .setTag(0);

//             expectedTensor = 2;

//             recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
//             assertEquals(expectedTensor, recievedTensor);
//         }

//         // Trace is wide write, instruction has wide write, confirm returned tensor
//         @Test
//         public void testValidWideWrite() throws Exception, MemoryAccessException {
//             traceAccessType = TraceEntry.AccessType.WRITE_WIDE;

//             instructionBuilder
//                 .setWideWrite(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
//                 .setTag(0);

//             expectedTensor = 2;

//             recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
//             assertEquals(expectedTensor, recievedTensor);
//         }

//         // Trace is narrow read, instruction does not have narrow read, catch MAE
//         @Test(expected = MemoryAccessException.class)
//         public void testInvalidNarrowRead() throws Exception, MemoryAccessException {
//             traceAccessType = TraceEntry.AccessType.READ_NARROW;

//             instructionBuilder
//                 .setWideRead(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
//                 .setTag(0);

//             expectedTensor = 2;

//             recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
//             assertEquals(expectedTensor, recievedTensor);
//         }

//         // Trace is narrow write, instruction does not have narrow write, catch MAE
//         @Test(expected = MemoryAccessException.class)
//         public void testInvalidNarrowWrite() throws Exception, MemoryAccessException {
//             traceAccessType = TraceEntry.AccessType.WRITE_NARROW;

//             instructionBuilder
//                 .setWideWrite(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
//                 .setTag(0);

//             expectedTensor = 2;

//             recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
//             assertEquals(expectedTensor, recievedTensor);
//         }

//         // Trace is wide read, instruction does not have wide read, catch MAE
//         @Test(expected = MemoryAccessException.class)
//         public void testInvalidWideRead() throws Exception, MemoryAccessException {
//             traceAccessType = TraceEntry.AccessType.READ_WIDE;

//             instructionBuilder
//                 .setNarrowRead(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
//                 .setTag(0);

//             expectedTensor = 2;

//             recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
//             assertEquals(expectedTensor, recievedTensor);
//         }

//         // Trace is wide write, instruction does not have wide write, catch MAE
//         @Test(expected = MemoryAccessException.class)
//         public void testInvalidWideWrite() throws Exception, MemoryAccessException {
//             traceAccessType = TraceEntry.AccessType.WRITE_WIDE;

//             instructionBuilder
//                 .setNarrowWrite(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
//                 .setTag(0);

//             expectedTensor = 2;

//             recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
//             assertEquals(expectedTensor, recievedTensor);
//         }

//         // Trace has null access type
//         @Test(expected = Exception.class)
//         public void testNullAccessTraceEntry() throws Exception, MemoryAccessException {
//             traceAccessType = null;

//             instructionBuilder
//                 .setNarrowRead(memoryAccessBuilder.setTensor(2).setBaseAddress(0))
//                 .setTag(0);

//             expectedTensor = 2;

//             recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
//             assertEquals(expectedTensor, recievedTensor);
//         }

//         // Instruction has the correct access type, but that MemoryAccess does not have 
//         // a tensor associated with it.
//         @Test(expected = Exception.class)
//         public void testValidMemoryAccessWithoutCorrespondingTraceEntry() throws Exception, MemoryAccessException {
//             traceAccessType = TraceEntry.AccessType.READ_NARROW;

//             instructionBuilder
//                 .setNarrowRead(memoryAccessBuilder.setBaseAddress(0))
//                 .setTag(0);

//             expectedTensor = 2;

//             recievedTensor = Validation.getTraceTensor(traceAddress, traceAccessType, instructionBuilder.build());
            
//             assertEquals(expectedTensor, recievedTensor);
//         }
//     }
// }
