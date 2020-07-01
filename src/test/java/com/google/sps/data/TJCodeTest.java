package com.google.sps.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Hashtable;
import java.util.List;
import com.google.sps.proto.SimulationTraceProto;
import com.google.sps.proto.SimulationTraceProto.Instruction;
import com.google.sps.proto.SimulationTraceProto.MemoryAccess;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

@RunWith(TJCodeTest.class)
@Suite.SuiteClasses({TJCodeTest.TestInstructionTagtoInstructionTable.class})
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

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // A single instruction with no masks or memory accesses
        public void testSingleInstruction() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList())
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with no masks or memory accesses
        public void testMultipleInstructions() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .addAllMask(Arrays.asList())
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(2)
                            .addAllMask(Arrays.asList())
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList())
                    .build());
            expected.put(
                1,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(1).addAllMask(Arrays.asList())
                    .build());
            expected.put(
                2,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .addAllMask(Arrays.asList())
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions who share the same tag with no masks or memory accesses
        public void testOverlapTags() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(2)
                            .addAllMask(Arrays.asList())
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(0)
                    .addAllMask(Arrays.asList())
                    .build());
            expected.put(
                2,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(2)
                    .addAllMask(Arrays.asList())
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // A single instruction with memory access and no masks
        public void testSingleInstructionWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .setNarrowRead(
                                SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList())
                    .setNarrowRead(
                        SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with memory accesses and no masks
        public void testMultipleInstructionsWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .setNarrowRead(
                                SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .addAllMask(Arrays.asList())
                            .setNarrowWrite(
                                SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(2)
                            .addAllMask(Arrays.asList())
                            .setWideWrite(
                                SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList())
                    .setNarrowRead(
                        SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                    .build());
            expected.put(
                1,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .addAllMask(Arrays.asList())
                    .setNarrowWrite(
                        SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                    .build());
            expected.put(
                2,
                SimulationTraceProto.Instruction.newBuilder()
                .setName("C")
                .setTag(2)
                .addAllMask(Arrays.asList())
                .setWideWrite(
                    SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions who share the same tag with memory accesses and no masks
        public void testOverlapTagsWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .setNarrowRead(
                                SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .addAllMask(Arrays.asList())
                            .setNarrowWrite(
                                SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .setWideWrite(
                                SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                .setName("C")
                .setTag(0)
                .addAllMask(Arrays.asList())
                .setWideWrite(
                    SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                .build());
            expected.put(
                1,
                SimulationTraceProto.Instruction.newBuilder()
                .setName("B")
                .setTag(1)
                .addAllMask(Arrays.asList())
                .setNarrowWrite(
                    SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList()))
                .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with no masks and with/without memory accesses
        public void testMultipleInstructionsMixedMemoryAccessTags() {
            testInstructions = new ArrayList<>(Arrays.asList(
                SimulationTraceProto.Instruction.newBuilder().setName("A").setTag(0)
                    .addAllMask(Arrays.asList())
                    .setNarrowRead(SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList())).build(),
                SimulationTraceProto.Instruction.newBuilder().setName("B")
                    .setTag(1)
                    .addAllMask(Arrays.asList()).build(),
                SimulationTraceProto.Instruction.newBuilder().setName("C")
                    .setTag(2)
                    .addAllMask(Arrays.asList())
                    .setWideWrite(SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList())).build()));

            expected.put(0, SimulationTraceProto.Instruction.newBuilder()
                .setName("A")
                .setTag(0)
                .addAllMask(Arrays.asList())
                .setNarrowRead(SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList())).build());
            expected.put(1, SimulationTraceProto.Instruction.newBuilder()
                .setName("B")
                .setTag(1)
                .addAllMask(Arrays.asList()).build());
            expected.put(2, SimulationTraceProto.Instruction.newBuilder()
                .setName("C")
                .setTag(2)
                .addAllMask(Arrays.asList())
                .setWideWrite(SimulationTraceProto.MemoryAccess.newBuilder().addAllCounter(Arrays.asList())).build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // A single instruction with a single mask and no memory accesses
        public void testSingleInstructionOneMask() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList(true))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList(true))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // A single instruction with multiple masks and no memory accesses
        public void testSingleInstructionMultipleMasks() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList(true, false, false))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList(true, false, false))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with a single mask and no memory accesses
        public void testMultipleInstructionsOneMask() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList(true))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .addAllMask(Arrays.asList(false))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(2)
                            .addAllMask(Arrays.asList(true))
                            .build()));

            expected.put(
                0,
                    SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList(true))
                    .build());
            expected.put(
                1,
                    SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .addAllMask(Arrays.asList(false))
                    .build());
            expected.put(
                2,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .addAllMask(Arrays.asList(true))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with multiple masks and no memory accesses
        public void testMultipleInstructionsMultipleMasks() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList(true, false, false))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .addAllMask(Arrays.asList(false, true, false))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(2)
                            .addAllMask(Arrays.asList(true, true, true))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList(true, false, false))
                    .build());
            expected.put(
                1,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .addAllMask(Arrays.asList(false, true, false))
                    .build());
            expected.put(
                2,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .addAllMask(Arrays.asList(true, true, true))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with no memory accesses and 0-3 masks
        public void testMultipleInstructionsMixedMasks() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .addAllMask(Arrays.asList(false, true, false))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(2)
                            .addAllMask(Arrays.asList(true, true))
                            .build()));

            expected.put(
                0,
                    SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList())
                    .build());
            expected.put(
                1,
                    SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .addAllMask(Arrays.asList(false, true, false))
                    .build());
            expected.put(
                2,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .addAllMask(Arrays.asList(true, true))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions who share the same tag with multiple masks and no memory accesses
        public void testOverlapTagsWithMasks() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList(true, false, false))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(2)
                            .addAllMask(Arrays.asList(false, true))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(0)
                            .addAllMask(Arrays.asList(true, true, true))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(0)
                    .addAllMask(Arrays.asList(true, true, true))
                    .build());
            expected.put(
                2,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(2)
                    .addAllMask(Arrays.asList(false, true))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions who share the same tag with 0-3 masks and no memory accesses
        public void testOverlapTagsMixedMasks() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList(true, false, false))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(2)
                            .addAllMask(Arrays.asList(false, true))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(0)
                            .addAllMask(Arrays.asList())
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(0).addAllMask(Arrays.asList())
                    .build());
            expected.put(
                2,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(2).addAllMask(Arrays.asList(false, true))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // A single instruction with a single mask and a memory access
        public void testSingleInstructionOneMaskWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList(false))
                            .setNarrowRead(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList(false))
                    .setNarrowRead(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // A single instruction with multiple masks and a memory access
        public void testSingleInstructionMultipleMasksWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .addAllMask(Arrays.asList(false, true, true))
                            .setNarrowRead(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .addAllMask(Arrays.asList(false, true, true))
                    .setNarrowRead(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with a single mask and memory accesses
        public void testMultipleInstructionsOneMaskWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .setNarrowWrite(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .setNarrowRead(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(false))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(2)
                            .setWideRead(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .setNarrowWrite(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true))
                    .build());
            expected.put(
                1,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .setNarrowRead(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(false))
                    .build());
            expected.put(
                2,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .setWideRead(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with a single mask and with/without a memory access
        public void testMultipleInstructionsOneMaskMixedMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .setNarrowWrite(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .addAllMask(Arrays.asList(false))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(2)
                            .setWideRead(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .setNarrowWrite(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true))
                    .build());
            expected.put(
                1,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .addAllMask(Arrays.asList(false))
                    .build());
            expected.put(
                2,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .setWideRead(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with multiple masks and memory accesses
        public void testMultipleInstructionsMultipleMasksWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .setNarrowWrite(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true, false, true))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .setNarrowRead(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(false, false))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(2)
                            .setWideRead(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true, false, true, false))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .setNarrowWrite(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true, false, true))
                    .build());
            expected.put(
                1,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .setNarrowRead(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(false, false))
                    .build());
            expected.put(
                2,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .setWideRead(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true, false, true, false))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions with multiple masks and with/without memory access
        public void testMultipleInstructionsMultipleMasksMixedMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .setNarrowWrite(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true, false, true))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .addAllMask(Arrays.asList(false, false))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(2)
                            .setWideRead(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true, false, true, false))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("A")
                    .setTag(0)
                    .setNarrowWrite(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true, false, true))
                    .build());
            expected.put(
                1,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .addAllMask(Arrays.asList(false, false))
                    .build());
            expected.put(
                2,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(2)
                    .setWideRead(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true, false, true, false))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions who share the same tag with multiple masks and memory accesses
        public void testOverlapTagsWithMasksWithMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .setNarrowWrite(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true, false, true))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .setNarrowRead(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(false, false))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(0)
                            .setWideRead(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true, false, true, false))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(0)
                    .setWideRead(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true, false, true, false))
                    .build());
            expected.put(
                1,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .setNarrowRead(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(false, false))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }

        @Test
        // Multiple instructions who share the same tag with multiple masks and with/without memory access
        public void testOverlapTagsWithMasksMixedMemoryAccess() {
            testInstructions =
                new ArrayList<>(
                    Arrays.asList(
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("A")
                            .setTag(0)
                            .setNarrowWrite(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true, false, true))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("B")
                            .setTag(1)
                            .addAllMask(Arrays.asList(false, false))
                            .build(),
                        SimulationTraceProto.Instruction.newBuilder()
                            .setName("C")
                            .setTag(0)
                            .setWideRead(
                                SimulationTraceProto.MemoryAccess.newBuilder()
                                    .addAllCounter(Arrays.asList()))
                            .addAllMask(Arrays.asList(true, false, true, false))
                            .build()));

            expected.put(
                0,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("C")
                    .setTag(0)
                    .setWideRead(
                        SimulationTraceProto.MemoryAccess.newBuilder()
                            .addAllCounter(Arrays.asList()))
                    .addAllMask(Arrays.asList(true, false, true, false))
                    .build());
            expected.put(
                1,
                SimulationTraceProto.Instruction.newBuilder()
                    .setName("B")
                    .setTag(1)
                    .addAllMask(Arrays.asList(false, false))
                    .build());

            assertEquals(expected, TJCode.relateIntructionTagtoInstructionTable(testInstructions));
        }
    }
}
