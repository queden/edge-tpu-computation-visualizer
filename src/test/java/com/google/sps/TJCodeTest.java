package com.google.sps;

import static org.junit.Assert.*;

import com.google.sps.exceptions.*;
import com.google.sps.proto.SimulationTraceProto.*;
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

    public TJCodeTest(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
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

        private final TraceEntry TRACE_FOUR = 
            TraceEntry.newBuilder()
                .setInstructionTag(4)
                .setAddress(21)
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
