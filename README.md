# Cash13SSE
This is a prototype implementaiton of Cash 13 paper [1]. It is implemented by using java with package JPBC [2].

I have impleneted the three SSE schemes that proposed in [1]: Single Keyword Search (SKS), Basic Cross-Tags Protocol (BXT) and Oblivious Cross-Tags Protocol (OXT). 

In the src folder, there are several packages which implement the aforementioned protocols: sks is the SKS, bxt is the BXT and oxt is the OXT. util is the package that used to store some basic tools that used in this project. Moreover, I have implemented the Hidden-Vector Cross-Tags Search (HXT) which has been propsed in [3]. The hve and bloomfilter are the Hidden Vector Encryption and bloom filter, respectively, which have been used in [3]. 

# Usage

Import this project to eclipse/myeclipse directly. Furthermore, include jpbc-api-2.0.0.jar and jpbc-plaf-2.0.0.jar that stored in lib folder (JPBC libraries). Now, you are ready to run all the schemes that stored the src folder.

* SKS: src/sks/SKSProtocol.java
* BXT: src/bxt/BXTProtocol.java
* OXT: src/oxt/OXTProtocol.java
* HXT: src/hxt/HXTProtocol.java

# Contact Us
* Cong Zuo. cong.zuo1@monash.edu

# References

[1] Cash, David, Stanislaw Jarecki, Charanjit Jutla, Hugo Krawczyk, Marcel-Cătălin Roşu, and Michael Steiner. "Highly-scalable searchable symmetric encryption with support for boolean queries." In Advances in cryptology–CRYPTO 2013, pp. 353-373. Springer, Berlin, Heidelberg, 2013.

[2] Java Pairing Based Cryptography. http://gas.dia.unisa.it/projects/jpbc/#.W4TkVZMzai4

[3] Shangqi Lai, Sikhar Patranabis, Amin Sakzad, Joseph K. Liu, Debdeep Mukhopadhyay, Ron Steinfeld, Shifeng Sun, Dongxi Liu, Cong Zuo. "Result Pattern Hiding Searchable Encryption for Conjunctive Queries." In CCS 2018, to appear.
