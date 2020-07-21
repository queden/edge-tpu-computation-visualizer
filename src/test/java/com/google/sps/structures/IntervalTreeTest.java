// The MIT License (MIT)

// Copyright (c) 2016 Mason M Lai

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.google.sps.structures;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IntervalTreeTest {

    private IntervalTree<Impl> emptyTree;                 // an empty tree
    
    private IntervalTree<Impl> singletonTree;             // a tree with one node:
    private Impl singletonValue = new Impl(0, 10);        // [0, 10)
    private Impl copyOfSingletonValue = new Impl(singletonValue);

    private int randomUpperBound = 3000;
    private int numRandomIntervals = 5000;
    
    private IntervalTree<Impl> gappedTree;  // A tree with a dead-zone in the
    private int gappedUpperBound = 3000;    // middle to test overlap methods
    private int gappedLowerBound = 4000;
    private int numGappedIntervals = 2500;  // in each section
    private Set<Impl> gappedIntervals;
    
    
    // Private debugging methods.
    private Method mIsBST;
    private Method mHasValidRedColoring;
    private Method mIsBalanced;
    private Method mHasConsistentMaxEnds;

    @Before
    public void setup() throws NoSuchMethodException, SecurityException,
    NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        /////////////////////////////////////////////////////////
        // Make private methods accessible for easier testing. //
        /////////////////////////////////////////////////////////
        
        mIsBST = IntervalTree.class.getDeclaredMethod("isBST");
        mIsBST.setAccessible(true);
        
        mIsBalanced = IntervalTree.class.getDeclaredMethod("isBalanced");
        mIsBalanced.setAccessible(true);
        
        mHasValidRedColoring = IntervalTree.class.getDeclaredMethod("hasValidRedColoring");
        mHasValidRedColoring.setAccessible(true);
        
        mHasConsistentMaxEnds = IntervalTree.class.getDeclaredMethod("hasConsistentMaxEnds");
        mHasConsistentMaxEnds.setAccessible(true);
        
        emptyTree = new IntervalTree<Impl>();
        singletonTree = new IntervalTree<Impl>(singletonValue);
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    //////////////////////
    // Empty tree tests //
    //////////////////////
    
    @Test
    public void testEmptyTreeIsEmpty() {
        assertThat(emptyTree.isEmpty(), is(true));
    }
    
    @Test
    public void testEmptyTreeSize() {
        assertThat(emptyTree.size(), is(0));
    }
    
    @Test
    public void testEmptyTreeContains() {
        assertThat(emptyTree.contains(new Impl(1, 5)), is(false));
    }
    
    @Test
    public void testEmptyTreeMinimum() {
        assertThat(emptyTree.minimum().isPresent(), is(false));
    }
    
    @Test
    public void testEmptyTreeMaximum() {
        assertThat(emptyTree.maximum().isPresent(), is(false));
    }
    
    @Test
    public void testEmptyTreeSuccessor() {
        assertThat(emptyTree.successor(new Impl(1, 2)).isPresent(), is(false));
    }
    
    @Test
    public void testEmptyTreePredecessor() {
        assertThat(emptyTree.predecessor(new Impl(1, 2)).isPresent(), is(false));
    }
    
    @Test
    public void testEmptyTreeIteratorHasNext() {
        assertThat(emptyTree.iterator().hasNext(), is(false));
    }
    
    @Test
    public void testEmptyTreeIteratorNext() {
        thrown.expect(NoSuchElementException.class);
        thrown.expectMessage("Interval tree has no more elements.");
        emptyTree.iterator().next();
    }
    
    @Test
    public void testEmptyTreeOverlaps() {
        assertThat(emptyTree.overlaps(new Impl(1, 10)), is(false));
    }
    
    @Test
    public void testEmptyTreeOverlappersHasNext() {
        assertThat(emptyTree.overlappers(new Impl(1, 3)).hasNext(), is(false));
    }
    
    @Test
    public void testEmptyTreeOverlappersNext() {
        thrown.expect(NoSuchElementException.class);
        thrown.expectMessage("Interval tree has no more overlapping elements.");
        emptyTree.overlappers(new Impl(1, 3)).next();
    }
    
    @Test
    public void testEmptyTreeNumOverlappers() {
        assertThat(emptyTree.numOverlappers(new Impl(1, 3)), is(0));
    }
    
    @Test
    public void testEmptyTreeIsValidBST() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBST.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeIsBalanced() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBalanced.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeHasValidRedColoring() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        assertThat(mHasValidRedColoring.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeConsistentMaxEnds() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        assertThat(mHasConsistentMaxEnds.invoke(emptyTree), is(true));
    }

    @Test
    public void testEmptyTreeDelete() {
        assertThat(emptyTree.delete(new Impl(1, 2)), is(false));
    }
    
    @Test
    public void testEmptyTreeSizeAfterDelete() {
        emptyTree.delete(new Impl(1, 2));
        assertThat(emptyTree.size(), is(0));
    }
    
    @Test
    public void testEmptyTreeIsEmptyAfterDelete() {
        emptyTree.delete(new Impl(1, 2));
        assertThat(emptyTree.isEmpty(), is(true));
    }
    
    @Test
    public void testEmptyTreeDeleteMin() {
        assertThat(emptyTree.deleteMin(), is(false));
    }
    
    @Test
    public void testEmptyTreeSizeAfterDeleteMin() {
        emptyTree.deleteMin();
        assertThat(emptyTree.size(), is(0));
    }
    
    @Test
    public void testEmptyTreeIsEmptyAfterDeleteMin() {
        emptyTree.deleteMin();
        assertThat(emptyTree.isEmpty(), is(true));
    }
    
    @Test
    public void testEmptyTreeDeleteMax() {
        assertThat(emptyTree.deleteMax(), is(false));
    }
    
    @Test
    public void testEmptyTreeSizeAfterDeleteMax() {
        emptyTree.deleteMax();
        assertThat(emptyTree.size(), is(0));
    }
    
    @Test
    public void testEmptyTreeIsEmptyAfterDeleteMax() {
        emptyTree.deleteMax();
        assertThat(emptyTree.isEmpty(), is(true));
    }
    
    @Test
    public void testEmptyTreeDeleteOverlappers() {
        emptyTree.deleteOverlappers(new Impl(1, 2));
    }
    
    @Test
    public void testEmptyTreeSizeAfterDeleteOverlappers() {
        emptyTree.deleteOverlappers(new Impl(1, 2));
        assertThat(emptyTree.size(), is(0));
    }
    
    @Test
    public void testEmptyTreeIsEmptyAfterDeleteOverlappers() {
        emptyTree.deleteOverlappers(new Impl(1, 2));
        assertThat(emptyTree.isEmpty(), is(true));
    }
    
    @Test
    public void testEmptyTreeIsValidBSTAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        emptyTree.delete(new Impl(1, 3));
        assertThat(mIsBST.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeIsBalancedAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        emptyTree.delete(new Impl(1, 3));
        assertThat(mIsBalanced.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeHasValidRedColoringAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        emptyTree.delete(new Impl(1, 3));
        assertThat(mHasValidRedColoring.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeConsistentMaxEndsAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        emptyTree.delete(new Impl(1, 3));
        assertThat(mHasConsistentMaxEnds.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeInsertion() {
        assertThat(emptyTree.insert(new Impl(1, 3)), is(true));
    }
    
    @Test
    public void testEmptyTreeSizeAfterInsertion() {
        emptyTree.insert(new Impl(1, 2));
        assertThat(emptyTree.size(), is(1));
    }
    
    @Test
    public void testEmptyTreeIsEmptyAfterInsertion() {
        emptyTree.insert(new Impl(1, 2));
        assertThat(emptyTree.isEmpty(), is(false));
    }
    
    @Test
    public void testEmptyTreeIsValidBSTAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        emptyTree.insert(new Impl(1, 3));
        assertThat(mIsBST.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeIsBalancedAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        emptyTree.insert(new Impl(1, 3));
        assertThat(mIsBalanced.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeHasValidRedColoringAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        emptyTree.insert(new Impl(1, 3));
        assertThat(mHasValidRedColoring.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeHasConsistentMaxEndsAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        emptyTree.insert(new Impl(1, 3));
        assertThat(mHasConsistentMaxEnds.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeIsValidBSTAfterRepeatedInsertions() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Random rand = new Random();
        for (int i = 0; i < numRandomIntervals; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(randomUpperBound);
                s = rand.nextInt(randomUpperBound);
            }
            
            emptyTree.insert(new Impl(r, s));
            assertThat(mIsBST.invoke(emptyTree), is(true));
        }
    }
    
    @Test
    public void testEmptyTreeIsBalancedAfterRepeatedInsertions() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Random rand = new Random();
        for (int i = 0; i < numRandomIntervals; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(randomUpperBound);
                s = rand.nextInt(randomUpperBound);
            }
            
            emptyTree.insert(new Impl(r, s));
            assertThat(mIsBalanced.invoke(emptyTree), is(true));
        }
    }
    
    @Test
    public void testEmptyTreeHasValidRedColoringAfterRepeatedInsertions() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Random rand = new Random();
        for (int i = 0; i < numRandomIntervals; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(randomUpperBound);
                s = rand.nextInt(randomUpperBound);
            }
            
            emptyTree.insert(new Impl(r, s));
            assertThat(mHasValidRedColoring.invoke(emptyTree), is(true));
        }
    }
    
    @Test
    public void testEmptyTreeHasConsistentMaxEndsAfterRepeatedInsertions() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Random rand = new Random();
        for (int i = 0; i < numRandomIntervals; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(randomUpperBound);
                s = rand.nextInt(randomUpperBound);
            }
            
            emptyTree.insert(new Impl(r, s));
            assertThat(mHasConsistentMaxEnds.invoke(emptyTree), is(true));
        }
    }
    
    //////////////////////////
    // Singleton tree tests //
    //////////////////////////
    
    @Test
    public void testSingletonTreeIsEmpty() {
        assertThat(singletonTree.isEmpty(), is(false));
    }
    
    @Test
    public void testSingletonTreeSize() {
        assertThat(singletonTree.size(), is(1));
    }
    
    @Test
    public void testSingletonTreeContainsPositive() {
        assertThat(singletonTree.contains(copyOfSingletonValue), is(true));
    }
    
    @Test
    public void testSingletonTreeContainsNegative() {
        assertThat(singletonTree.contains(new Impl(1, 9)), is(false));
    }
    
    @Test
    public void testSingletonTreeMinimum() {
        assertThat(singletonTree.minimum()
                                .orElseThrow(() -> new IllegalStateException()),
                is(copyOfSingletonValue));
    }
    
    @Test
    public void testSingletonTreeMaximum() {
        assertThat(singletonTree.maximum()
                                .orElseThrow(() -> new IllegalStateException()),
                is(copyOfSingletonValue));
    }
    
    @Test
    public void testSingletonTreeSuccessor() {
        assertThat(singletonTree.successor(copyOfSingletonValue).isPresent(),
                is(false));
    }
    
    @Test
    public void testSingetonTreePredecessor() {
        assertThat(singletonTree.predecessor(copyOfSingletonValue).isPresent(),
                is(false));
    }
    
    @Test
    public void testSingletonTreeIteratorHasNext() {
        assertThat(singletonTree.iterator().hasNext(), is(true));
    }
    
    @Test
    public void testSingletonTreeIteratorNext() {
        assertThat(singletonTree.iterator().next(), is(copyOfSingletonValue));
    }
    
    @Test
    public void testSingletonTreeIteratorNextTwice() {
        thrown.expect(NoSuchElementException.class);
        thrown.expectMessage("Interval tree has no more elements.");
        Iterator<Impl> i = singletonTree.iterator();
        i.next();
        i.next();
    }
    
    @Test
    public void testSingletonTreeOverlapsPositive() {
        assertThat(singletonTree.overlaps(copyOfSingletonValue), is(true));
    }
    
    @Test
    public void testSingletonTreeOverlapsNegative() {
        assertThat(singletonTree.overlaps(new Impl(20, 22)), is(false));
    }
    
    @Test
    public void testSingletonTreeOverlappersHasNext() {
        assertThat(singletonTree.overlappers(new Impl(1, 3)).hasNext(), is(true));
    }

    @Test
    public void testSingletonTreeOverlappersNext() {
        assertThat(singletonTree.overlappers(new Impl(1, 3)).next(), is(copyOfSingletonValue));
    }
    
    @Test
    public void testSingletonTreeOverlappersNextTwice() {
        thrown.expect(NoSuchElementException.class);
        thrown.expectMessage("Interval tree has no more overlapping elements.");
        Iterator<Impl> i = singletonTree.overlappers(new Impl(1, 3));
        i.next();
        i.next();
    }
    
    @Test
    public void testSingletonTreeNumOverlappers() {
        assertThat(singletonTree.numOverlappers(new Impl(1, 3)), is(1));
    }
    
    @Test
    public void testSingletonTreeIsValidBST() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBST.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBalanced() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBalanced.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasValidRedColoring() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        assertThat(mHasValidRedColoring.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeConsistentMaxEnds() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        assertThat(mHasConsistentMaxEnds.invoke(singletonTree), is(true));
    }

    @Test
    public void testSingletonTreeDeletePositive() {
        assertThat(singletonTree.delete(copyOfSingletonValue), is(true));
    }
    
    @Test
    public void testSingletonTreeDeleteNegative() {
        assertThat(singletonTree.delete(new Impl(1, 5)), is(false));
    }
    
    @Test
    public void testSingletonTreeSizeAfterSuccessfulDeletion() {
        singletonTree.delete(copyOfSingletonValue);
        assertThat(singletonTree.size(), is(0));
    }
    
    @Test
    public void testSingletonTreeSizeAfterUnsuccessfulDeletion() {
        singletonTree.delete(new Impl(1, 9));
        assertThat(singletonTree.size(), is(1));
    }
    
    @Test
    public void testSingletonTreeIsEmptyAfterSuccessfulDeletion() {
        singletonTree.delete(copyOfSingletonValue);
        assertThat(singletonTree.isEmpty(), is(true));
    }
    
    @Test
    public void testSingletonTreeIsEmptyAfterUnsuccessfulDeletion() {
        singletonTree.delete(new Impl(1, 9));
        assertThat(singletonTree.isEmpty(), is(false));
    }
    
    @Test
    public void testSingletonTreeDeleteMin() {
        assertThat(singletonTree.deleteMin(), is(true));
    }
    
    @Test
    public void testSingletonTreeSizeAfterDeleteMin() {
        singletonTree.deleteMin();
        assertThat(singletonTree.size(), is(0));
    }
    
    @Test
    public void testSingletonTreeIsEmptyAfterDeleteMin() {
        singletonTree.deleteMin();
        assertThat(singletonTree.isEmpty(), is(true));
    }
    
    @Test
    public void testSingletonTreeDeleteMax() {
        assertThat(singletonTree.deleteMax(), is(true));
    }
    
    @Test
    public void testSingletonTreeSizeAfterDeleteMax() {
        singletonTree.deleteMax();
        assertThat(singletonTree.size(), is(0));
    }
    
    @Test
    public void testSingletonTreeIsEmptyAfterDeleteMax() {
        singletonTree.deleteMax();
        assertThat(singletonTree.isEmpty(), is(true));
    }
    
    @Test
    public void testSingletonTreeDeleteOverlappers() {
        assertThat(singletonTree.deleteOverlappers(new Impl(1, 5)), is(true));
    }
    
    @Test
    public void testSingletonTreeSizeAfterDeleteOverlappersPositive() {
        singletonTree.deleteOverlappers(new Impl(1, 5));
        assertThat(singletonTree.size(), is(0));
    }
    
    @Test
    public void testSingletonTreeSizeAfterDeleteOverlappersNegative() {
        singletonTree.deleteOverlappers(new Impl(20, 25));
        assertThat(singletonTree.size(), is(1));
    }

    @Test
    public void testSingletonTreeDoesContainAddress() {
        Impl impl = singletonTree.containsAddress(new Impl(5, 5));
        assertThat(impl, is(singletonValue));
    }
    
    @Test
    public void testSingletonTreeIsEmptyAfterDeleteOverlappers() {
        singletonTree.deleteOverlappers(new Impl(1, 5));
        assertThat(singletonTree.isEmpty(), is(true));
    }
    
    @Test
    public void testSingletonTreeIsNotEmptyAfterDeleteOverlappers() {
        singletonTree.deleteOverlappers(new Impl(20, 25));
        assertThat(singletonTree.isEmpty(), is(false));
    }
    
    @Test
    public void testSingletonTreeIsValidBSTAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.delete(copyOfSingletonValue);
        assertThat(mIsBST.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBalancedAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.delete(copyOfSingletonValue);
        assertThat(mIsBalanced.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasValidRedColoringAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.delete(copyOfSingletonValue);
        assertThat(mHasValidRedColoring.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeConsistentMaxEndsAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.delete(copyOfSingletonValue);
        assertThat(mHasConsistentMaxEnds.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeInsertion() {
        assertThat(singletonTree.insert(new Impl(1, 11)), is(true));
    }
    
    @Test
    public void testSingletonTreeRedundantInsertion() {
        assertThat(singletonTree.insert(copyOfSingletonValue), is(false));
    }
    
    @Test
    public void testSingletonTreeSizeAfterInsertion() {
        singletonTree.insert(new Impl(1, 2));
        assertThat(singletonTree.size(), is(2));
    }
    
    @Test
    public void testSingletonTreeSizeAfterRedundantInsertion() {
        singletonTree.insert(copyOfSingletonValue);
        assertThat(singletonTree.size(), is(1));
    }
    
    @Test
    public void testSingletonTreeIsNotEmptyAfterInsertion() {
        singletonTree.insert(new Impl(1, 2));
        assertThat(singletonTree.isEmpty(), is(false));
    }
    
    @Test
    public void testSingletonTreeIsValidBSTAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(new Impl(1, 3));
        assertThat(mIsBST.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsValidBSTAfterRedundantInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(copyOfSingletonValue);
        assertThat(mIsBST.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBalancedAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(new Impl(1, 3));
        assertThat(mIsBalanced.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBalancedAfterRedundantInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(copyOfSingletonValue);
        assertThat(mIsBalanced.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasValidRedColoringAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.insert(new Impl(1, 3));
        assertThat(mHasValidRedColoring.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasValidRedColoringAfterRedundantInsertion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.insert(copyOfSingletonValue);
        assertThat(mHasValidRedColoring.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeConsistentMaxEndsAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.insert(new Impl(1, 3));
        assertThat(mHasConsistentMaxEnds.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeConsistentMaxEndsAfterRedundantInsertion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.insert(copyOfSingletonValue);
        assertThat(mHasConsistentMaxEnds.invoke(singletonTree), is(true));
    }
    
    /**
     * Simple implementation of Interval for testing
     */
    private static class Impl implements Interval {

        private final int start;
        private final int end;
        
        public Impl(int start, int end) {
            this.start = start;
            this.end = end;
        }
        
        public Impl(Impl i) {
            this.start = i.start();
            this.end = i.end();
        }
        
        @Override
        public int start() {
            return start;
        }

        @Override
        public int end() {
            return end;
        }
        
        @Override
        public String toString() {
            return "start: " + start + " end: " + end;
        }
        
        @Override
        public boolean equals(Object other) {
            // No need for null check. The instanceof operator returns false if (other == null).
            if (!(other instanceof Impl)) {
                return false;
            }

            return start == ((Impl) other).start && end == ((Impl) other).end;
        }
        
        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + start;
            result = 31 * result + end;
            return result;
        }
    }
}