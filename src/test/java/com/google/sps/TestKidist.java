// package com.google.sps;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Map;
// import java.util.Hashtable;
// import java.util.List;

// import java.lang.reflect.Method;
// import java.lang.reflect.Field;
// import java.lang.reflect.InvocationTargetException;

// import com.google.sps.exceptions.*;
// import com.google.sps.proto.MemaccessCheckerDataProto.*;

// import static org.junit.Assert.*;
// import org.junit.Before;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.junit.runners.JUnit4;
// import org.junit.runners.Suite;
// import org.junit.runners.model.InitializationError;
// import org.junit.runners.model.RunnerBuilder;

// @RunWith(TestKidist.class)
// @Suite.SuiteClasses({
//     TestKidist.TestWriteValidation.class,
//     TestKidist.TestValidateTraceEvents.class})
// public final class TestKidist extends Suite {

//     public TestKidist(Class<?> klass, RunnerBuilder builder) throws InitializationError {
//         super(klass, builder);
//     }

//     public static class TestWriteValidation {
//     private Validation validation;    
//     private static int[][] testNarrow;
//     private static int[][] testWide;
//     private static int[][] expectedNarrow;
//     private static int[][] expectedWide;
//     private static int tensor;
//     public static  TraceEvent.AccessType NARROWWRITE;
//     public static  TraceEvent.AccessType WIDEWRITE;
//     private static TraceEvent testTrace;

//     private Method mWriteValidation;
//     private Field mNumTiles;

   
//     private static TraceEvent.Builder traceBuilder;

//     @Before
//     public void setUp() throws NoSuchMethodException,
//         NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
//       testNarrow = new int[16][128 * 1024];
//       testWide = new int[16][256 * 1024];
//       expectedNarrow = new int[16][128 * 1024];
//       expectedWide = new int[16][256 * 1024];
//       traceBuilder = TraceEvent.newBuilder();
//       NARROWWRITE = TraceEvent.AccessType.NARROW_WRITE;
//       WIDEWRITE = TraceEvent.AccessType.WIDE_WRITE;

//       MemaccessCheckerData.Builder protoBuilder = MemaccessCheckerData.newBuilder();
//       Validation validation = new Validation(protoBuilder.build());

//       mWriteValidation = Validation.class.getDeclaredMethod("writeValidation", int[][].class, int[][].class, List.class, int.class, TraceEvent.class);
//             mWriteValidation.setAccessible(true);
//       mNumTiles = Validation.class.getDeclaredField("numTiles");
//             mNumTiles.setAccessible(true);
//       mNumTiles.set(validation, 16);
      
//     }
//     @Test
//     public void testEmptyTrace() throws IllegalAccessException, InvocationTargetException, Throwable{  
//       List<Boolean> mask = new ArrayList(); 
//       for (int i = 0; i < 16; i++){
//             mask.add(true);
//         } 
//       testTrace = traceBuilder.build();
//       mWriteValidation.invoke(validation, testNarrow, testWide, mask, tensor, testTrace); 
//       assertEquals(expectedNarrow, testNarrow);
//     }
//     @Test
//     public void testEmptyByte() throws IllegalAccessException, InvocationTargetException, Throwable{  
//       List<Boolean> mask = new ArrayList(); 
//       for (int i = 0; i < 16; i++){
//             mask.add(true);
//         } 
//       testTrace =
//                 traceBuilder.setAccessType(NARROWWRITE).setInstructionTag(0).setAddress(1000).build();
//       mWriteValidation.invoke(validation, testNarrow, testWide, mask, tensor, testTrace); 
//       assertEquals(expectedNarrow, testNarrow);
//     }
//     @Test
//     public void testNonEmptyTraceNarrow() throws IllegalAccessException, InvocationTargetException, Throwable{
//         tensor = 7;
//         List<Boolean> mask = new ArrayList();
//         for (int i = 0; i < 16; i++){
//             mask.add(true);
//         }
//         testTrace =
//                 traceBuilder.setAccessType(NARROWWRITE).setBytes(1).setInstructionTag(0).setAddress(1000).build();
                  
//         for (int i = 0; i < 16; i++){
//             expectedNarrow[i][1000] = 7;
//         }
        
        
//         mWriteValidation.invoke(validation, testNarrow, testWide, mask, tensor, testTrace);
        
//         assertArrayEquals(expectedNarrow, testNarrow);
//     }
//     @Test
//     public void testNonEmptyTraceWide() throws IllegalAccessException, InvocationTargetException, Throwable{
//         tensor = 7;
//         List<Boolean> mask = new ArrayList();
//         for (int i = 0; i < 16; i++){
//             mask.add(true);
//         }
//         testTrace =
//                 traceBuilder.setAccessType(WIDEWRITE).setInstructionTag(0).setBytes(1).setAddress(1000).build();
                  
//         for (int i = 0; i < 16; i++){
//             expectedWide[i][1000] = 7;
//         }
//         mWriteValidation.invoke(validation, testNarrow, testWide, mask, tensor, testTrace);
        
//         assertArrayEquals(expectedWide, testWide);
//     }
//     @Test
//     public void testFourBytesTraceNarrow() throws IllegalAccessException, InvocationTargetException, Throwable{
//         tensor = 7;
//         List<Boolean> mask = new ArrayList();
//         for (int i = 0; i < 16; i++){
//             mask.add(true);
//         }
//         testTrace =
//                 traceBuilder.setAccessType(NARROWWRITE).setBytes(4).setInstructionTag(0).setAddress(1000).build();
                  
//         for (int i = 0; i < 16; i++){
//             for (int j = 1000; j < 1004; j++){
//                 expectedNarrow[i][j] = 7;
//             }
//         }
//         mWriteValidation.invoke(validation, testNarrow, testWide, mask, tensor, testTrace);
        
//         assertArrayEquals(expectedNarrow, testNarrow);
//     }
//     @Test
//     public void testFourBytesTraceWide() throws IllegalAccessException, InvocationTargetException, Throwable{
//         tensor = 7;
//         List<Boolean> mask = new ArrayList();
//         for (int i = 0; i < 16; i++){
//             mask.add(true);
//         }
//         testTrace =
//                 traceBuilder.setAccessType(WIDEWRITE).setInstructionTag(0).setBytes(4).setAddress(1000).build();
                  
//         for (int i = 0; i < 16; i++){
//             for (int j = 1000; j < 1004; j++){
//                 expectedWide[i][j] = 7;
//             }
//         }
//         mWriteValidation.invoke(validation, testNarrow, testWide, mask, tensor, testTrace);
        
//         assertArrayEquals(expectedWide, testWide);
//     }
//     @Test
//     public void testFalseMaskNarrow() throws IllegalAccessException, InvocationTargetException, Throwable{
//         tensor = 7;
//         List<Boolean> mask = new ArrayList();
//         for (int i = 0; i < 16; i++){
//             mask.add(false);
//         }
//         testTrace =
//                 traceBuilder.setAccessType(NARROWWRITE).setInstructionTag(0).setAddress(1200).build();
                  
//         mWriteValidation.invoke(validation, testNarrow, testWide, mask, tensor, testTrace);
//         assertArrayEquals(expectedNarrow, testNarrow);
//     }
//     @Test
//     public void testFalseMaskWide() throws IllegalAccessException, InvocationTargetException, Throwable{
//         tensor = 7;
//         List<Boolean> mask = new ArrayList();
//         for (int i = 0; i < 16; i++){
//             mask.add(false);
//         }
//         testTrace =
//                 traceBuilder.setAccessType(WIDEWRITE).setInstructionTag(0).setAddress(1200).build();
//         mWriteValidation.invoke(validation, testNarrow, testWide, mask, tensor, testTrace);
//         assertArrayEquals(expectedWide, testWide);
//     }
// //different masks
//     @Test
//     public void testVariedMasksWide() throws IllegalAccessException, InvocationTargetException, Throwable{
//         tensor = 7;
//         List<Boolean> mask = new ArrayList();
//         for (int i = 0; i < 8; i++){
//             mask.add(false);
//             mask.add(true);
//         }
//         testTrace =
//                 traceBuilder.setAccessType(WIDEWRITE).setBytes(1).setInstructionTag(0).setAddress(1200).build();
//         for (int i = 1; i < 16; i += 2){
//             expectedWide[i][1200] = 7;
//         }
//         mWriteValidation.invoke(validation, testNarrow, testWide, mask, tensor, testTrace);
//         assertArrayEquals(expectedWide, testWide);
//     }

// //no write accesstype
//     @Test 
//     public void testNoWriteAccessType() throws IllegalAccessException, InvocationTargetException, Throwable{
//         tensor = 7;
//         List<Boolean> mask = new ArrayList();
//         for (int i = 0; i < 16; i++){
//             mask.add(false);
//         } 
//         testTrace =
//                 traceBuilder.setAccessType(TraceEvent.AccessType.WIDE_READ).setInstructionTag(0).setAddress(1200).build();
//        mWriteValidation.invoke(validation, testNarrow, testWide, mask, tensor, testTrace);
//        assertArrayEquals(expectedWide, testWide);
//     }
//   }
//   public static class TestValidateTraceEvents {
//     private Validation validation;    
//     private static int start;
//     private static int end;
//     private ArrayList<TraceEvent> traceEventList;
//     private static Map<Integer, Instruction> instructionTagtoInstruction;
//     private static Map<Integer, TensorAllocation> wideMap;
//     private static Map<Integer, TensorAllocation> narrowMap;
//     private static TraceEvent.Builder traceBuilder;
//     private static Instruction.Builder instructionBuilder;
//     private static TensorAllocation.Builder tensorBuilder;
//     private static TraceEvent traceEvent_1;
//     private static TraceEvent traceEvent_2;
//     private static TraceEvent traceEvent_3;
//     private static TensorAllocation tensor_1;
//     private static TensorAllocation tensor_2;
//     private static TensorAllocation tensor_3;
//     private static Instruction instruction_1;
//     private static Instruction instruction_2;
//     private static TraceEvent returnedTraceEvent;
//     private static  TraceEvent.AccessType NARROWWRITE;
//     private static  TraceEvent.AccessType WIDEWRITE;
//     private static  TraceEvent.AccessType NARROWREAD;
//     private static  TraceEvent.AccessType WIDEREAD;
//     private Method mValidateTraceEvents;

//     private Field mtensorLabeltoTensorAllocationWide;
//     private Field mtensorLabeltoTensorAllocationNarrow;
//     private Field mInstructionTagtoInstruction;
//     private Field mtraceEvents;

//     @Before
//     public void setUp() throws NoSuchMethodException,
//         NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
//       traceEventList = new ArrayList<TraceEvent>();
//       NARROWWRITE = TraceEvent.AccessType.NARROW_WRITE;
//       NARROWREAD = TraceEvent.AccessType.NARROW_READ;
//       WIDEWRITE = TraceEvent.AccessType.WIDE_WRITE;
//       wideMap = new Hashtable<Integer, TensorAllocation>();
//       narrowMap = new Hashtable<Integer, TensorAllocation>();
//       instructionTagtoInstruction = new Hashtable<Integer, Instruction>();
//       traceBuilder = TraceEvent.newBuilder();
//       instructionBuilder = Instruction.newBuilder();
//       tensorBuilder = TensorAllocation.newBuilder();
//       MemaccessCheckerData.Builder protoBuilder = MemaccessCheckerData.newBuilder();

//       Validation validation = new Validation(protoBuilder.build());
      
//       instructionBuilder = Instruction.newBuilder();
//       mInstructionTagtoInstruction = Validation.class.getDeclaredField("instructionTagtoInstruction");
//             mInstructionTagtoInstruction.setAccessible(true);
//       mtensorLabeltoTensorAllocationWide = Validation.class.getDeclaredField("tensorLabelToTensorAllocationWide");
//             mtensorLabeltoTensorAllocationWide.setAccessible(true);
//       mtensorLabeltoTensorAllocationNarrow = Validation.class.getDeclaredField("tensorLabelToTensorAllocationNarrow");
//             mtensorLabeltoTensorAllocationNarrow.setAccessible(true);
//       mtraceEvents = Validation.class.getDeclaredField("traceEvents");
//             mtraceEvents.setAccessible(true);
//       mValidateTraceEvents = Validation.class.getDeclaredMethod("validateTraceEvents", long.class, long.class);
//              mValidateTraceEvents.setAccessible(true);
//     }
//     @Test(expected = Exception.class)
//     public void testEmptyTraceEventList() 
//     throws IllegalAccessException, InvocationTargetException, Throwable, Exception{  

//       start = 0;
//       end = 9;
//       mValidateTraceEvents.invoke(validation, start, end); 
//     }
//     @Test(expected = Exception.class)
//     public void testEmptyMask() 
//     throws IllegalAccessException, InvocationTargetException, Exception {  
     
//       start = 0;
//       end = 1;
//       traceEvent_1 = traceBuilder.setAddress(100).setAccessType(NARROWWRITE).setInstructionTag(10).build();
//      traceEventList.add(traceEvent_1);
     
//       instruction_1 = instructionBuilder.setLayer("1").setTag(10).build();
      
//       mtraceEvents.set(validation, traceEventList);
//       instructionTagtoInstruction.put(10,instruction_1);
//       mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
//       mValidateTraceEvents.invoke(validation, start, end);
//     }
//     @Test(expected = Exception.class)
//     public void testEmptyInstruction() 
//     throws IllegalAccessException, InvocationTargetException, Exception {  
     
//       start = 0;
//       end = 1;
//       traceEvent_1 = traceBuilder.setAddress(100).setAccessType(NARROWWRITE).setInstructionTag(12).build();
//       traceEventList.add(traceEvent_1);
     
//       instruction_1 = instructionBuilder.setLayer("1").setTag(10).build();
      
//       mtraceEvents.set(validation, traceEventList);
//       instructionTagtoInstruction.put(10,instruction_1);
//       mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
//       mValidateTraceEvents.invoke(validation, start, end);
//     }
//     @Test
//     public void testNonEmptyTraceList() 
//     throws IllegalAccessException, InvocationTargetException, Exception {  
//       start = 0;
//       end = 1;
//       traceEvent_2 = traceBuilder.setAddress(250).setAccessType(WIDEWRITE).setInstructionTag(10).build();
//       traceEventList.add(traceEvent_2);
//       mtraceEvents.set(validation, traceEventList);
     
//       List<Boolean> mask = new ArrayList(); 
//         for (int i = 0; i < 16; i++){
//             mask.add(true);
//         }
//       instruction_1 = instructionBuilder.setLayer("1").setTag(10).addWideWrite(8).addAllMask(mask).build();
//       instructionTagtoInstruction.put(10,instruction_1);
//       mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
 
//       tensor_1 = tensorBuilder.setTensorLabel(8).setBaseAddress(250).setSize(45).build();
//       wideMap.put(8,tensor_1);
//       mtensorLabeltoTensorAllocationWide.set(validation,wideMap);

//       mValidateTraceEvents.invoke(validation, start, end);
//     }
//     @Test
//     public void testMultipleTraceEvents() 
//     throws IllegalAccessException, InvocationTargetException, Exception {  
//       start = 0;
//       end = 3;
//       traceEvent_1 = traceBuilder.setAddress(32).setAccessType(NARROWREAD).setInstructionTag(10).build();
//       traceEvent_2 = traceBuilder.setAddress(250).setAccessType(WIDEWRITE).setInstructionTag(10).build();
//       traceEvent_3 = traceBuilder.setAddress(1000).setAccessType(WIDEWRITE).setInstructionTag(12).build();
//       traceEventList.add(traceEvent_1);
//       traceEventList.add(traceEvent_2);
//       traceEventList.add(traceEvent_3);
//       mtraceEvents.set(validation, traceEventList);
     
//       List<Boolean> mask = new ArrayList(); 
//         for (int i = 0; i < 16; i++){
//             mask.add(true);
//         }
//       instruction_1 = instructionBuilder.setLayer("1").setTag(10).addWideWrite(8).addNarrowRead(5).addAllMask(mask).build();
//       instruction_2 = instructionBuilder.setLayer("14").setTag(12).addWideWrite(12).addAllMask(mask).build();
//       instructionTagtoInstruction.put(10,instruction_1);
//       instructionTagtoInstruction.put(12,instruction_2);
//       mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
 
//       tensor_1 = tensorBuilder.setTensorLabel(5).setBaseAddress(24).setSize(32).build();
//       tensor_2 = tensorBuilder.setTensorLabel(8).setBaseAddress(250).setSize(100).build();
//       tensor_3 = tensorBuilder.setTensorLabel(12).setBaseAddress(956).setSize(45).build();
//       narrowMap.put(5, tensor_1);
//       wideMap.put(8,tensor_2);
//       wideMap.put(12,tensor_3);
//       mtensorLabeltoTensorAllocationNarrow.set(validation,narrowMap);
//       mtensorLabeltoTensorAllocationWide.set(validation,wideMap);

//       mValidateTraceEvents.invoke(validation, start, end);
//     }
//     @Test(expected = Exception.class)
//     public void testOutOfBoundTensorAllocation() 
//     throws IllegalAccessException, InvocationTargetException, Exception {  
//       start = 0;
//       end = 3;
//       traceEvent_1 = traceBuilder.setAddress(32).setAccessType(NARROWREAD).setInstructionTag(10).build();
//       traceEvent_2 = traceBuilder.setAddress(250).setAccessType(WIDEWRITE).setInstructionTag(10).build();
//       traceEvent_3 = traceBuilder.setAddress(1000).setAccessType(WIDEWRITE).setInstructionTag(12).build();
//       traceEventList.add(traceEvent_1);
//       traceEventList.add(traceEvent_2);
//       traceEventList.add(traceEvent_3);
//       mtraceEvents.set(validation, traceEventList);
     
//       List<Boolean> mask = new ArrayList(); 
//         for (int i = 0; i < 16; i++){
//             mask.add(true);
//         }
//       instruction_1 = instructionBuilder.setLayer("1").setName("first").setTag(10).addWideWrite(8).addNarrowRead(5).addAllMask(mask).build();
//       instruction_2 = instructionBuilder.setLayer("14").setName("second").setTag(12).addWideWrite(12).addAllMask(mask).build();
//       instructionTagtoInstruction.put(10,instruction_1);
//       instructionTagtoInstruction.put(12,instruction_2);
//       mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
 
//       tensor_1 = tensorBuilder.setTensorLabel(5).setBaseAddress(24).setSize(32).build();
//       tensor_2 = tensorBuilder.setTensorLabel(8).setBaseAddress(250).setSize(100).build();
//       tensor_3 = tensorBuilder.setTensorLabel(12).setBaseAddress(955).setSize(45).build();
//       narrowMap.put(5, tensor_1);
//       wideMap.put(8,tensor_2);
//       wideMap.put(12,tensor_3);
//       mtensorLabeltoTensorAllocationNarrow.set(validation,narrowMap);
//       mtensorLabeltoTensorAllocationWide.set(validation,wideMap);

//       mValidateTraceEvents.invoke(validation, start, end);
//     }
//     @Test(expected = Exception.class)
//     public void testInvalidAccessType() 
//     throws IllegalAccessException, InvocationTargetException, Exception {  
//       start = 0;
//       end = 2;
//       traceEvent_1 = traceBuilder.setAddress(32).setAccessType(NARROWREAD).setInstructionTag(10).build();
//       traceEvent_2 = traceBuilder.setAddress(250).setAccessType(WIDEWRITE).setInstructionTag(10).build();
//       traceEventList.add(traceEvent_1);
//       traceEventList.add(traceEvent_2);
//       mtraceEvents.set(validation, traceEventList);
     
//       List<Boolean> mask = new ArrayList(); 
//         for (int i = 0; i < 16; i++){
//             mask.add(true);
//         }
//       instruction_1 = instructionBuilder.setLayer("1").setName("first").setTag(10).addAllMask(mask).build();
//       instructionTagtoInstruction.put(10,instruction_1);
//       mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
 
//       tensor_1 = tensorBuilder.setTensorLabel(5).setBaseAddress(24).setSize(32).build();
//       tensor_2 = tensorBuilder.setTensorLabel(8).setBaseAddress(250).setSize(100).build();
//       narrowMap.put(5, tensor_1);
//       wideMap.put(8,tensor_2);
//       mtensorLabeltoTensorAllocationNarrow.set(validation,narrowMap);
//       mtensorLabeltoTensorAllocationWide.set(validation,wideMap);

//       mValidateTraceEvents.invoke(validation, start, end);
//     }
//     @Test(expected = Exception.class)
//     public void testEmptyTable() 
//     throws IllegalAccessException, InvocationTargetException, Exception {  
//       start = 0;
//       end = 2;
//       traceEvent_1 = traceBuilder.setAddress(32).setAccessType(NARROWREAD).setInstructionTag(10).build();
//       traceEventList.add(traceEvent_1);
//       mtraceEvents.set(validation, traceEventList);
     
//       List<Boolean> mask = new ArrayList(); 
//         for (int i = 0; i < 16; i++){
//             mask.add(true);
//         }
//       instruction_1 = instructionBuilder.setLayer("1").setName("first").setTag(10).addNarrowRead(8).addAllMask(mask).build();
//       instructionTagtoInstruction.put(10,instruction_1);
//       mInstructionTagtoInstruction.set(validation, instructionTagtoInstruction);
 
//       tensor_1 = tensorBuilder.setTensorLabel(5).setBaseAddress(24).setSize(32).build();
//       narrowMap.put(5, tensor_1);

//       mValidateTraceEvents.invoke(validation, start, end);
//     }
//   }
// }