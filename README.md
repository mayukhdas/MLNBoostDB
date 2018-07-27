---
Title: "Basic Parameters"
Author: Mayukh Das
Original author: Marcin Malec
excerpt: "Basic usage overview MLNBoostDB."
---

License: [![][license img]][license]

*Note that this is for learning a Boosted MLN with in-memory Relational Database integration (Malec et al. ILP 2016). This is an extension where wrapper ensures same command line argument structure as MLN-Boost. Most arguments are same as the original MLN-Boost(Khot et al. ICDM 2011) platform. Few that are different have been stated below.*

<b>Warning!!  Further note that this implementation <u>DOES NOT WORK with PRECOMPUTES or derived precidates</u> in the BK file. PLEASE REMOVE all precomputes and/or derived predicates that are not directly present in the evidence. Simple mode declarations should work perfectly.</b>

### Primary Runnable Binary

* `MLN-Boost-DB.jar`

*Download the whole repository for easy resolution of dependencies*


*If using souce code make sure to include all libraries in the `lib` folder in your build path*

### Simple Usage:

* `java -jar MLN-Boost-DB.jar [Args]`

### Arguments [Args]:

* `-l` : enable training (**l**earning).
* `-i` : enable testing (**i**nference).
* `-train <Training directory>` : Path to the training directory in predicate logic format.
* `-test <Testing directory>` : Path to the testing directory in predicate logic format format.
* `-model <Model directory>` : Path to the directory with the stored models [or where they will be stored].
* `-target <target predicates>` : Comma separated list of predicates to be learned/inferred.
* `-trees <Number of trees>` : Number of Boosting trees aka Num of clauses in MLN.
* `-aucJarPath <path to auc.jar>` : If this is not set, AUC values are not computed.

* `-mln` : Set this flag, if you want to learn MLNs instead of RDNs 
* `-mlnClauseLen` : If -mlnclause is set, set the length of the clauses learned during each gradient step. 

Additional arguments for Databse:
* `-dt <Database Type [hsqldb | H2]>` : Choice of in-memory database to be used. 2 options available <b>hsqldb</b> (preferred) OR <b>H2</b>. 

### Paper:

> Marcin Malec, Tushar Khot, James Nagy, Erik Blasch, and Sriraam Natarajan. Inductive Logic Programming meets Relational Databases: An Application to Statistical Relational Learning. In ILP 2016

> Tushar Khot, Sriraam Natarajan, Kristian Kersting, Jude Shavlik.Learning Markov Logic Networks via Functional Gradient Boosting. In ICDM 2011. 
---

### Sample Calls:

*Try to follow along with what each of these are doing:*

From the [Smokes-Friends-Cancer Dataset](Smokes-Friends-Cancer Dataset):

* `java -jar BoostSRL.jar -l -train ./Datasets/Toy-Cancer/train -model ./Datasets/Toy-Cancer/model -dt hsqldb -target cancer -i -test ./Datasets/Toy-Cancer/test -aucJarPath ./ -trees 20`


