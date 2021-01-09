# Supervised-Local-Community-Detection

This program implements the algorithm "Supervised local community detection algorithm".


Article: Choumane, A. and Al-Akhrass, A. (2021) "Supervised local community detection algorithm", Int. J. Data Science (to appear).


IMPORTANT NOTE: the file NeuralNetwork.7z should be uncompressed using your favorite software. It contains the Neural Network Model NeuralNetwork.model (binary file).


#############
Compile
#############

To compile the source code again, execute ./compile.sh under Linux. Make sure you have JDK 8 installed.

#############
Run
#############

The minimum command to run the algorithm is:

	java -jar LocalCommunity.jar -f network.dat -m NeuralNetwork.model -r 1.0

where:

	network.dat is an undirected, unweighted network, one edge per line.
	
	NeuralNetwork.model is a binary file containing the pre-trained Neural Network classifier (cf. the article for more details).
	
	1.0 is the default value to assign to the resolution parameter. Lower than 1.0 to get smaller communities and higher than 1.0 to get bigger ones.


Contact us for any question: ali.choumane@ul.edu.lb
