---
title: "Basic Parameters"
author: Mayukh Das
original author: Marcin Malec
excerpt: "Basic overview for commandline arguments used by MLNBoostDB. The Development version of Marcin's code on database integration of MLN-Boost. A wrapper ensures same ars structure as MLN-Boost."
---

*Note that this is for learning a Boosted MLN with in-memory Relational Database integration.* 

### Simple Usage:

* `java -jar MLN-Boost-DB.jar [Args]`

### Arguments [Args]:

* `-l` : enable training (**l**earning).
* `-i` : enable testing (**i**nference).
* `-noBoost` : disable Boosting (i.e., learns a single relational regression tree).
* `-train <Training directory>` : Path to the training directory in predicate logic format.
* `-test <Testing directory>` : Path to the testing directory in predicate logic format format.
* `-model <Model directory>` : Path to the directory with the stored models [or where they will be stored].
* `-target <target predicates>` : Comma separated list of predicates to be learned/inferred.
* `-trees <Number of trees>` : Number of Boosting trees aka Num of clauses in MLN.
* `-aucJarPath <path to auc.jar>` : If this is not set, AUC values are not computed.

* `-mln` : Set this flag, if you want to learn MLNs instead of RDNs 
* `-mlnClauseLen` : If -mlnclause is set, set the length of the clauses learned during each gradient step. 



Paper:

> Tushar Khot, Sriraam Natarajan, Kristian Kersting, Jude Shavlik.Learning Markov Logic Networks via Functional Gradient Boosting. In ICDM 2011. 

---

### Sample Calls:

*Try to follow along with what each of these are doing:*

From the [Smokes-Friends-Cancer Dataset](Smokes-Friends-Cancer Dataset):

* `java -jar BoostSRL.jar -l -train ./Datasets/Toy-Cancer/train -model ./Datasets/Toy-Cancer/model -dt hsqldb -target cancer -i -test ./Datasets/Toy-Cancer/test -aucJarPath ./ -trees 20`


