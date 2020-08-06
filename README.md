# Computation Vizualizer for the Edge TPU
To generate the protobuf, cd into the root of the repository and run:
```
protoc --java_out=src/main/java src/main/java/com/google/sps/proto/memaccess_checker_data.proto
protoc --java_out=src/test/java src/main/java/com/google/sps/proto/memaccess_checker_data.proto
```

Project components:
  - Algorithm:
    - Validation algorithm code
      ```
      src/main/java/com/google/sps/Validation.java
      ```
    - Protobuf used by the algorithm
      ```
      src/main/java/com/google/sps/proto
      ```
    - Custom exceptions thrown by the algorithm
      ```
      src/main/java/com/google/sps/exceptions
      ```

    - Structures:
      - Pair object used for grouping layer and tensor (around line 760 and below)
        ```
        src/main/java/com/google/sps/Validation.java
        ```

      - Interval tree code used when validating:
        - Nodes (around line 815 and below)
          ```
          src/main/java/com/google/sps/Validation.java
          ```
        - Tree
          ```
          src/main/java/com/google/sps/structures/Interval.java
          src/main/java/com/google/sps/structures/IntervalTree.java
          ```

      - Non-custom:
        - List:
          - Tensor allocations across layers, two for narrow and wide
          - Total collection of instructions
          - Total collection of traceEvents

        - Hashtable:
          - Layer to tensor Pair object to TensorAllocation
        - Map:
          - Relation of all instruction tags to their instruction

        - 2-dimensional array:
          - Representation of narrow and wide memory states, one for each

    - Methods:
      - preProcess
      - process
      - getTileUnion
      - getLayerToInstructionTable
      - relateInstructionTagtoInstructionTable
      - validateTraceEvents
      - getTraceTensor
      - getTensor
      - writeValidation
      - readValidation

    - Processing results:
      - Preprocessing information
        ```
        src/main/java/com/google/sps/results/PreProcessResults.java
        ```
      - Post-processing information for different chunks of events
        ```
        src/main/java/com/google/sps/results/ProcessResults
        ```

  - Visualizer:
    - Code (around line 550 and below)
      ```
      src/main/webapp/script.js
      ```
    - Change in memory state after post-processing
      ```
      src/main/java/com/google/sps/structures/Delta.java
      ```

  - Website code:
    - JSON processing and UI updates
      ```
      src/main/webapp/script.js
      ```

    - Server communications:
      - Main page
        ```
        src/main/java/com/google/sps/servlets/VisualizerServlet.java
        ```
      - Visualizer window
        ```
        src/main/java/com/google/sps/servlets/ReportServlet.java
        ```

    - UI skeleton:
      - Main page
        ```
        src/main/webapp/index.html
        ```
      - Visualizer window
        ```
        src/main/webapp/report.html
        ```

    - Styling
      ```
      src/main/webapp/style.css
      ```

Possible errors caught by the algorithm:
  - Lack of mask(s) assigned to a given instruction and trace event
  - Lack of tensor assigned to a base address in narrow/wide memory
  - Attempting to read from a memory location that has not yet been allocated
  - Attempting to perform a memory access operation from an instruction that does not contain its trace event
  - A layer does not have the same number of tiles as expected by the proto
  - A complete lack of provided trace events
  - A non-existent instruction corresponding to an existing trace event
  - A present trace event with an access type that is not narrow/wide read/write
  - A lack of a tensor associated to an instruction
  - An empty narrow/wide memory allocation table
  - Attempting to read/write from/to a memory location on a tile that is 
    in the operating trace event but not its corresponding instruction
