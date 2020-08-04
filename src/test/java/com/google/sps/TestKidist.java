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
import com.google.sps.structures.Delta;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

@RunWith(TestKidist.class)
@Suite.SuiteClasses({
    TestKidist.TestWriteValidation.class,
    TestKidist.TestValidateTraceEvents.class})
public final class TestKidist extends Suite {

    public TestKidist(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
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

    //   int[][] newNarrow = new int[16][128 * 1024];
    //   int[][] newWide = new int[16][256 * 1024];
    //   newNarrow[0][24] = 5;
    //   newWide[0][250] = 8;
    // //   newWidep=[0][956] = 12;
      
    //   mNarrow.set(validation, newNarrow);
    //   mWide.set(validation, newWide);

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