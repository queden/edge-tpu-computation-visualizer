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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.sps.proto.SimulationTraceProto;
import com.google.sps.proto.SimulationTraceProto.TensorAllocation;

/** */
@RunWith(JUnit4.class)
public final class ValidationTest { 
    private static int[] testMemory = new int[128 * 1024];
    private static SimulationTraceProto.TensorAllocation.Builder allocationBuilder = SimulationTraceProto.TensorAllocation.newBuilder();
    private Validation validation; 

    private static ArrayList<TensorAllocation> testAllocation = new ArrayList(Arrays.asList(new TensorAllocation[]{
        allocationBuilder.setLabel(1).setStartAddress(8).setSize(1000).build(), 
        allocationBuilder.setLabel(2).setStartAddress(95678).setSize(216).build()}));
    @Before
    public void setUp(){
        validation = new Validation();
        for (int i = 8; i < 1008; i++) {
            testMemory[i] = 1;
        }

        for (int i = 95678; i < 95894; i++) {
            testMemory[i] = 2;
        }
    }

    @Test
    public void testBuildAllocationMemory() {
        Assert.assertArrayEquals(testMemory, validation.getAllocationArray(testAllocation));
    }
    
}