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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.sps.Proto;
/** */
@RunWith(JUnit4.class)
public final class ValidationTest { 
    private static int[] testMemory = new int[128 * 1024];
    private static SimulationTraceProto.TensorAllocation.Builder allocationBuilder = SimulationTraceProto.TensorAllocation.newBuilder();


    private static ArrayList<TensorAllocation> testAllocation = Arrays.asList(new TensorAllocation[]{
        allocationBuilder.setLabel(1).setStartAddress(8).setSize(1000).build(), 
        allocationBuilder.setLabel(2).setStartAddress(95678).setSize(216).build()});

    for (int i = 8; i < 1009; i++) {
        testMemory[i] = 1;
    }

    for (int i = 95678; i < 95895; i++) {
        testMemory[i] = 2;
    }

    @Test
    public void testBuildAllocationMemory() {
        assertArrayEquals(testMemory, Validation.getAllocationArray(testAllocation));
    }
}