# Computation Vizualizer for the Edge TPU
To generate the protobuf, cd into the root of the repository and run 
```
protoc --java_out=src/main/java src/main/java/com/google/sps/proto/simulation.proto
protoc --java_out=src/main/java test/main/java/com/google/sps/simulation.proto
```
