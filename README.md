# SVCEval
a Scalable Video Coding (SVC) evaluation framework for transmitting SVC in Qualnet simulators


To evaluate the H.264/SVC transmission over different kinds of networks, especially ad hoc networks, we proposed a evaluation framework SVCEval . It is based on the SVC reference software JSVM and make use of the Qualnet simulator for the network simulation.

The SVCEval is in Java. The source code is at \SVCEval_FECSim\src. An introduction of the framework is available at P156 9.1.2 SVCEval Evaluation Framework of MP-OLSR_thesis.pdf. The details of parameters for QualnetTraceParser is at P183 Appendix D of MP-OLSR_thesis.pdf.

NOTE:

A sample of the configuration is presented in P161 of MP-OLSR_thesis.pdf. Please pay attention to the parameter SliceMode and SliceArgument, which defines the size of the packet.
In the bitstream trace, there are some 9-byte packets that are commonly not useful for the decoding. So it’s better to remove those packets during the video transmission simulation. Check P150 Section 8.2.2 Packet priority in H.264/SVC of MP-OLSR_thesis.pdf for more info.
In the current simulation, an old version of JSVM (9.8) is used. This is at the moment, the error concealment function are removed in the new version. If the user wants to use the latest version, please make sure that the error concealment function are included.
In JSVM 9.8, there are still some bugs exist. So for the last several frames of the decoding, there are probably some errors occur. Just ignore them.
