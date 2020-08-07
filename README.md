# Computation Vizualizer for the Edge TPU

## Summary

The Edge TPU Computation Visualizer is an internal debugging tool designed for the Edge TPU Team to find memory access dependency errors due to bugs in the chip's compiler. Currently, users can upload a log of a software simulation, and our tool will validate whether or not the compiler executed the instructions given to it correctly. We have a web interface where users can upload the file, and then we simulate the simulation and any errors that occur.

For a detailed design doc:
https://docs.google.com/document/d/1XzgKLn5qp4aP58RxTXuekrX1jXuSs58QO2LfIH2tCFs/edit?usp=sharing

## Working with the Project

To generate the protobuf, cd into the root of the repository and run:
```
protoc --java_out=src/main/java src/main/java/com/google/sps/proto/memaccess_checker_data.proto
protoc --java_out=src/test/java src/main/java/com/google/sps/proto/memaccess_checker_data.proto
```

Then, to run on a local server, run: 
```
mvn package appengine:run
```
and to deploy (to a valid Google Cloud Appengine project), run:
```
mvn package appengine:deploy
```

## Project Components

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
