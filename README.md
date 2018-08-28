# SSE-Schemes
This is a prototype implementaiton of serveal SSE schemes that proposed in Cash13 paper [1] and Lai18 paper [2]. They are implemented by using java programming language with package JPBC [3].

I have impleneted the three SSE schemes that proposed in [1]: Single Keyword Search (SKS), Basic Cross-Tags Protocol (BXT), Oblivious Cross-Tags Protocol (OXT). And one SSE scheme that proposed in [2]: Hidden Cross-Tags Protocol (HXT). 

In the src folder, there are several packages which implement the aforementioned protocols: sks is the SKS, bxt is the BXT, oxt is the OXT and hxt is the HXT. util is the package that used to store some basic tools that used in this project. The hve and bloomfilter are the Hidden Vector Encryption and bloom filter, respectively, which have been used in [2].

# Usage

Import this project to eclipse/myeclipse directly. Furthermore, include jpbc-api-2.0.0.jar and jpbc-plaf-2.0.0.jar that stored in lib folder (JPBC libraries). Now, you are ready to run all the schemes that stored the src folder.

* SKS: src/sks/SKSProtocol.java
* BXT: src/bxt/BXTProtocol.java
* OXT: src/oxt/OXTProtocol.java
* HXT: src/hxt/HXTProtocol.java

# Contact Us

If you have any question, please feel free to contact us.

* Cong Zuo: cong.zuo1@monash.edu

# References

[1] Cash, David, Stanislaw Jarecki, Charanjit Jutla, Hugo Krawczyk, Marcel-Cătălin Roşu, and Michael Steiner. "Highly-scalable searchable symmetric encryption with support for boolean queries." In Advances in cryptology–CRYPTO 2013, pp. 353-373. Springer, Berlin, Heidelberg, 2013.

[2] Shangqi Lai, Sikhar Patranabis, Amin Sakzad, Joseph K. Liu, Debdeep Mukhopadhyay, Ron Steinfeld, Shifeng Sun, Dongxi Liu, Cong Zuo. "Result Pattern Hiding Searchable Encryption for Conjunctive Queries." In CCS 2018, to appear.

[3] Java Pairing Based Cryptography. http://gas.dia.unisa.it/projects/jpbc/#.W4TkVZMzai4
