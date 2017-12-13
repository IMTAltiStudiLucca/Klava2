# Klava2
A new Java implementation of KLAIM (a Kernel Language for Agents Interaction and Mobility)

## Project description
The tuple space paradigm, made popular by Linda, is a concurrent coordination and communication model for parallel and distributed systems. Tuple space is a repository of tuples, where a process can add, withdraw or read tuples by means of atomic operations. Tuples may contain different values, and processes can inspect the content of a tuple via pattern matching.

KLAIM (the Kernel Language for Agents Interaction and Mobility) is an extension of Linda supporting processes migration. The emphasis of KLAIM is on process mobility, which means that processes as any data can be moved from one locality to another and they can be executed in any localities. Klava is a Java implementation of KLAIM. Klaim supports multiple tuple spaces and operates with explicit localities where processes and tuples are allocated. In this way, several tuples can be grouped and stored in one locality. Moreover, all the operations on tuple spaces are parametric to localities. Emphasis is put also on access control which is important for mobile applications. For this reason, KLAIM introduces type system which allows checking whether a process can perform an operation at specific localities.

## Project structure

* DataGenerator. An auxiliary project with classes for the creation of test data.
* CaseStudies. The project includes a number of case studies that are used to compare several tuple space implementations (GigaSpaces, KLAIM, MozartSpace, Tupleware). Each case study is written as a skeleton: the code remains the same for all the
tuple space implementations and testing tuple space implementation should be passed as a parameter of the case study. There are following case studies:
  * Password search;
  * Sorting;
  * Ocean mode simulation;
  * Matrix multiplication (row-wise and block-wise).
  
  Packages stated from **app.skeleton** contain the code of skeletons for each case study, whereas packages started from **app.launcher** contain the code certain tuple space implementations are used and have to be used to start tests.
* KlavaReplication. The project contains classes for the introduction of replication in Klava.
* LocalTupleSpaceTesting. The project includes a number test to evaluate performances of several implementations of local tuple space based on hash tables, trees, the indexing.
* ReplicationCaseStudy. The project presents case studies designed for klaim nodes that supports the replication based on sharing groups. There are the following case studies:
  * Mobile Detectors;
  * Smart home.
* TSProxies. The project includes a number of proxies for different tuple space implementation. Each proxy implements interface ITupleSpace. 
* klaim_source. The project contains a current version of Klava's source code.

Also:
* docker. The folder contains files to build a docker image (based on Ubuntu 14.04) to run experiments for case studies Matrix multiplication and Sorting. To use it is necessary to put a corresponding JAR file to a folder "jar".

