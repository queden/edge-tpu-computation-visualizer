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

/**
 * Closed-open, [), interval on the integer number line. 
 */
public interface Interval extends Comparable<Interval> {

    /**
     * Returns the starting point of this.
     */
    int start();

    /**
     * Returns the ending point of this.
     * <p>
     * The interval does not include this point.
     */
    int end();

    /**
     * Returns the length of this.
     */
    default int length() {
        return end() - start();
    }

    /**
     * Returns if this interval is adjacent to the specified interval.
     * <p>
     * Two intervals are adjacent if either one ends where the other starts.
     * @param interval - the interval to compare this one to
     * @return if this interval is adjacent to the specified interval.
     */
    default boolean isAdjacent(Interval other) {
        return start() == other.end() || end() == other.start();
    }
    
    default boolean overlaps(Interval o) {
        return end() >= o.start() && o.end() >= start();
    }
    
    default int compareTo(Interval o) {
        if (start() > o.start()) {
            return 1;
        } else if (start() < o.start()) {
            return -1;
        } else if (end() > o.end()) {
            return 1;
        } else if (end() < o.end()) {
            return -1;
        } else {
            return 0;
        }
    }
}