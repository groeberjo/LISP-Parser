# Introduction
This project was created as exercise to the corresponding course 'Debugging and Fuzzing' held by Prof. Dr. Armin Biere at the University of Freiburg.

The project aimed to develop a LISP parser and to thoroughly evaluate it using testing and analysis methods introduced in the lecture, such as profiling, fuzzing, delta debugging, code coverage measurement, and dynamic code analysis, in order to ensure both correctness and efficiency.

This repository includes the resulting LISP parser and Fuzzer of the exercises. It contains my personal implementation created as part of coursework. No official solution, course materials, or assignment text are included. 

In the section below there is a detailed explanation on how to build and run the respective programs.


# Build Instructions for Linux cli

## Build and Run Fuzzer
- Open a terminal in the top level directory.
- Compile all files with: `javac -cp ".:libs/*" src/*.java`.
- We can call the Fuzzer via `java -cp ".:libs/*:src" Fuzzer2000 <seed> <mode>`.
- Here seed is the seed for the randomness and mode can either be 0 or 1 to produce invalid or valid input.
- For example we produce valid input with seed 12345 with: `java -cp ".:libs/*:src" Fuzzer2000 12345 1`.
- This creates a file `fuzz.smt2` in the top level directory with 2000 lines of input for the lisp parser.

## Build and Run Lisp Parser (Main)
- Open a terminal in the top level directory.
- Compile all files with: `javac -cp ".:libs/*" src/*.java`.
- We can run the Lisp parser with: `java -cp ".:libs/*:src" Main <your_file>`.
- `<your_file>` here can be any suitable smt2 file, e.g. the `fuzz.smt2` file we just generated with the fuzzer.
- Or we can test the file `inp.smt2` via: `java -cp ".:libs/*:src" Main inp.smt2`.