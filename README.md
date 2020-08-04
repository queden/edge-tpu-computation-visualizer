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
    - Interval tree code used when validating:
      - Nodes (around line 700 and below)
        ```
        src/main/java/com/google/sps/Validation.java
        ```
      - Tree
        ```
        src/main/java/com/google/sps/structures/Interval.java
        src/main/java/com/google/sps/structures/IntervalTree.java
        ```
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
    - Code (around line 500 and below)
      ```
      src/main/webapp/script.js
      ```
    - Change in memory state after post-processing
      ```
      src/main/java/com/google/sps/structures/Delta.java
      ```
  - Website:
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
