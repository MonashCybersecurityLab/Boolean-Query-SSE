# Boolean Query Schemes
This repository includes a set of prototypes for several boolean query SSE schemes. 

In particular, we implement three SSE schemes proposed in [1]: Single Keyword Search (SKS), Basic Cross-Tags Protocol (BXT), Oblivious Cross-Tags Protocol (OXT). Moreover, we implement a variant of OXT based on public-key based hidden vector encryption, named Hidden Cross-Tags Protocol (HXT). 

# Usage

Import this project to eclipse/myeclipse; include jpbc-api-2.0.0.jar and jpbc-plaf-2.0.0.jar stored in the lib folder (JPBC library). Then you are ready to run all the schemes in the src folder.

* SKS: src/sks/SKSProtocol.java
* BXT: src/bxt/BXTProtocol.java
* OXT: src/oxt/OXTProtocol.java
* HXT: src/hxt/HXTProtocol.java

# Contact Us

If you have any question, please feel free to contact us.

* Cong Zuo: cong.zuo1@monash.edu
* Shangqi Lai: shangqi.lai@monash.edu

# References

[1] Cash, David, Stanislaw Jarecki, Charanjit Jutla, Hugo Krawczyk, Marcel-Cătălin Roşu, and Michael Steiner. "Highly-scalable searchable symmetric encryption with support for boolean queries." In CRYPTO, pp. 353-373. Springer, Berlin, Heidelberg, 2013.



