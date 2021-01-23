# Supervised-Local-Community-Detection

This program implements the algorithm "Supervised local community detection algorithm".


Article: Choumane, A. and Al-Akhrass, A. (2020) "Supervised local community detection algorithm", Int. J. Data Science, Vol. 5, No. 3, pp.247–261.



#############
Compile
#############

To compile the source code again, execute ./compile.sh under Linux. Make sure you have JDK 8 installed.

#############
Run
#############

The minimum command to run the algorithm is:

	java -jar LocalCommunity.jar -f network.dat -m NeuralNetwork.bin -r 1.0

where:

	network.dat is an undirected, unweighted network, one edge per line.
	
	NeuralNetwork.bin is a binary file containing the pre-trained Neural Network classifier (cf. the article for more details).
	
	1.0 is the default value to assign to the resolution parameter. Lower than 1.0 to get smaller communities and higher than 1.0 to get bigger ones.


Contact us for any question: ali.choumane@ul.edu.lb
