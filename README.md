Generic-Assembler
=================

An assembler is a program that reads in a source program written in an assembly language, and translates it to the corresponding machine language. A machine language specifies the internal representation of the instructions comprising a program (typically represented as hexadecimal strings, such as "62b1", while an assembly language allows the programmer to write the machine instructions in a readable symbolic form, such as "load R2,x[R2]".

There are many different computer architectures, each with its own machine (and assembly) language. Research in computer architecture involves experimenting with many variations in a machine language.

In order to support such research, it is useful to have an assembler generator. This is a program that reads in two inputs: (1) a specification of the computer architecture and assembly language, and (2) a source program written in that assembly language. The software then outputs the corresponding machine language result. A refinement on this is for the generator to take just one input (the specification of the architecture) and then to output a new computer program, which is the assembler.

The aim of this project is to design and implement an assembler generator.
