package learning;

import java.util.ArrayList;
import java.util.HashMap;

import utils.Graph;

public class TrueCommunitiesFeatures {
	private double resolution = 0.4;
	// LFR res = 1.0
	// DBLP res = 0.4
        // Amazon res = 1.0
        // LiveJournal res = 0.4

	private Graph<String> graph;
	private ArrayList<String> group;
	private Graph<String> subGraph;

	private ArrayList<String> groupNeighborhood;

	private HashMap<String, ArrayList<String>> internalSuccessors;
	private HashMap<String, ArrayList<String>> allSuccessors;

	private HashMap<String, Double>[] memberships_usingNodes;
	private double[] avgMembership_usingNodesIn;
	private double[] maxMembership_usingNodesOut;

	private HashMap<String, Double> internalNEs;
	private double avgInternalNEIn;
	private double maxInternalNEOut;

	// private HashMap<String, Double> internalDensities;
	// private double avgInternalDensityIn;
	// private double maxInternalDensityOut;

	private HashMap<String, Double> externalDensities;
	private double avgExternalDensityIn;
	private double maxExternalDensityOut;

	// private HashMap<String, Double> newMeasureValues;
	// private double avgOfNewMeasureIn;
	// private double maxOfNewMeasureOut;

	// identifying recored //////////////////////////////

	public TrueCommunitiesFeatures(TrueCommunitiesFeatures oldInstance, ArrayList<String> group) {
		this(oldInstance,group, 1.0);
	}
        
        public TrueCommunitiesFeatures(TrueCommunitiesFeatures oldInstance, ArrayList<String> group, double resolution) {
		this.graph = oldInstance.graph;
		this.group = group;
                this.resolution = resolution;

		init();
	}

	@SuppressWarnings("unchecked")
	public TrueCommunitiesFeatures(Graph<String> graph, double resolution) {
		this.graph = (Graph<String>) graph.clone();
                this.resolution = resolution;
	}

	@SuppressWarnings("unchecked")
	private void init() {
		groupNeighborhood = neighborhood(group, null, graph);
		// groupNeighborhood = new ArrayList<>(group);

		allSuccessors = new HashMap<>();
		internalSuccessors = new HashMap<>();
		for (String n : groupNeighborhood) {
			ArrayList<String> successors = new ArrayList<String>(graph.getSuccessors(n));
			successors.remove(n);

			ArrayList<String> internalSuccessors0 = new ArrayList<String>(successors);
			internalSuccessors0.retainAll(group);

			allSuccessors.put(n, successors);
			internalSuccessors.put(n, internalSuccessors0);
		}

		// avgInternalDensityIn = 0d;
		// maxInternalDensityOut = 0d;
		// internalDensities = new HashMap<>();
		// for (String n : groupNeighborhood) {
		// double internalDensity = (internalSuccessors.get(n).size()
		// + numberOfEdges(internalSuccessors.get(n), graph)) / (double)
		// internalSuccessors.get(n).size();
		// internalDensities.put(n, internalDensity);
		//
		// if (group.contains(n)) {
		// avgInternalDensityIn += internalDensity;
		// } else {
		// maxInternalDensityOut = Math.max(maxInternalDensityOut,
		// internalDensity);
		// }
		// }
		// avgInternalDensityIn /= (double) (group.size());

		avgExternalDensityIn = 0d;
		maxExternalDensityOut = 0d;
		externalDensities = new HashMap<>();
		for (String n : groupNeighborhood) {
			ArrayList<String> group0 = new ArrayList<>(group);
			group0.remove(n);

			double externalDensity = (numberOfEdges(allSuccessors.get(n), group0, graph))
					/ Math.sqrt(allSuccessors.get(n).size() * group0.size());
			externalDensities.put(n, externalDensity);

			if (group.contains(n)) {
				avgExternalDensityIn += externalDensity;
			} else {
				maxExternalDensityOut = Math.max(maxExternalDensityOut, externalDensity);
			}
		}
		avgExternalDensityIn /= (double) (group.size());

		avgInternalNEIn = 0d;
		maxInternalNEOut = 0d;
		internalNEs = new HashMap<>();
		for (String n : groupNeighborhood) {
			double interNE = (internalSuccessors.get(n).size() + numberOfEdges(internalSuccessors.get(n), graph))
					/ (double) (allSuccessors.get(n).size() + numberOfEdges(allSuccessors.get(n), graph));
			internalNEs.put(n, interNE);

			if (group.contains(n)) {
				avgInternalNEIn += interNE;
			} else {
				maxInternalNEOut = Math.max(maxInternalNEOut, interNE);
			}
		}
		avgInternalNEIn /= (double) (group.size());

		ArrayList<String> groupNeighbors = new ArrayList<>(groupNeighborhood);
		groupNeighbors.removeAll(group);

		// subGraph = Graph.keepOnly(groupNeighborhood, graph);
		subGraph = graph.reconstructFrom(groupNeighborhood);
		for (String n1 : groupNeighbors) {
			for (String n2 : groupNeighbors) {
				subGraph.removeEdge(n1, n2, false);
			}
		}
		subGraph.setWeights(new HashMap<>());

		Graph<String>[] graphs = new Graph[2];
		graphs[1] = (Graph<String>) subGraph.clone();

		memberships_usingNodes = new HashMap[2];

		avgMembership_usingNodesIn = new double[2];
		maxMembership_usingNodesOut = new double[2];
		for (int i = 1; i < graphs.length; i++) {
			memberships_usingNodes[i] = getMemberships_usingNodes(groupNeighborhood, group, graphs[i]);

			avgMembership_usingNodesIn[i] = 0d;
			maxMembership_usingNodesOut[i] = 0d;
			for (String n : groupNeighborhood) {

				if (group.contains(n)) {
					avgMembership_usingNodesIn[i] += memberships_usingNodes[i].get(n);
				} else {
					maxMembership_usingNodesOut[i] = Math.max(maxMembership_usingNodesOut[i],
							memberships_usingNodes[i].get(n));
				}
			}
			avgMembership_usingNodesIn[i] /= (double) (group.size());
		}

		// avgOfNewMeasureIn = 0d;
		// maxOfNewMeasureOut = 0d;
		// newMeasureValues = new HashMap<>();
		// for (String n : groupNeighborhood) {
		// ArrayList<String> nieborhoodInternalSuccessors = new
		// ArrayList<>(groupNeighborhood);
		// nieborhoodInternalSuccessors.retainAll(graph.getSuccessors(n));
		// double newMeasureValue = nieborhoodInternalSuccessors.size()
		// / (double) Math.sqrt(groupNeighborhood.size() *
		// graph.getSuccessors(n).size());
		//
		// newMeasureValues.put(n, newMeasureValue);
		//
		// if (group.contains(n)) {
		// avgOfNewMeasureIn += newMeasureValue;
		// } else {
		// maxOfNewMeasureOut = Math.max(maxOfNewMeasureOut, newMeasureValue);
		// }
		// }
		// avgOfNewMeasureIn /= (double) (group.size());
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> getGroupNeighborhood() {
		return (ArrayList<String>) groupNeighborhood.clone();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> getGroupNeighbors() {
		ArrayList<String> neighbors = (ArrayList<String>) groupNeighborhood.clone();
		neighbors.removeAll(group);

		return neighbors;
	}

	public Graph<String> getGraph() {
		return graph;
	}

	// Utilities ////////////////////////////////////////////

	private static HashMap<String, Double> getMemberships_usingNodes(ArrayList<String> nodes, ArrayList<String> group,
			Graph<String> graph) {
		HashMap<String, Double> memberships = new HashMap<>();

		for (String n : nodes) {
			memberships.put(n, 1d);
		}

		for (int i = 0; i < 4; i++) {
			refreshMemberships_usingNodes(memberships, group, graph);
		}

		return memberships;
	}

	private static void refreshMemberships_usingNodes(HashMap<String, Double> memberships, ArrayList<String> group,
			Graph<String> graph) {
		@SuppressWarnings("unchecked")
		HashMap<String, Double> memberships0 = (HashMap<String, Double>) memberships.clone();

		double totalSum = 0d;
		for (String n : group) {
			totalSum += memberships0.get(n);
		}

		for (String n : memberships0.keySet()) {

			double sum = 0d;
			for (String n0 : group) {
				sum += graph.getWeight(n, n0) * memberships0.get(n0);
			}

			memberships.put(n, sum / totalSum);
		}
	}

	private static double numberOfEdges(ArrayList<String> group1, ArrayList<String> group2, Graph<String> graph) {
		double result = 0d;

		for (String n1 : group1) {
			for (String n2 : group2) {
				result += (graph.getSuccessors(n1).contains(n2)) ? 1.0 : 0.0;
			}
		}

		return result / 2.0; // divide by 2 for undirected graphs
	}

	public static double numberOfEdges(ArrayList<String> group, Graph<String> graph) {
		return numberOfEdges(group, group, graph);
	}

	/**
	 * calculates the number of edges related to group, i.e. edges that have at
	 * least on terminal inside the given group
	 * 
	 * @param group
	 * @param graph
	 * @return
	 */
	public static int numberOfAllEdges(ArrayList<String> group, Graph<String> graph) {
		int result = 0;
		// get neighbourhood of 'group'
		ArrayList<String> neighborhood = neighborhood(group, null, graph);
		// get graph of neighbourhood (contains edges that are only inside
		// neighbourhood)
		Graph<String> g = graph.reconstructFrom(neighborhood);

		for (String node : neighborhood) {
			// get number of successors of this node
			int countSuccessors = g.getSuccessors(node).size();
			// remove this node from graph to avoid counting same edge twice
			g.removeNode(node);
			result += countSuccessors;
		}
		return result;

		// or simply use g.getNumberEdges(true);

	}

	public static ArrayList<String> neighborhood(ArrayList<String> group, ArrayList<String> mask, Graph<String> graph) {
		ArrayList<String> neighborhood = new ArrayList<String>(group);

		for (String n : group) {
			for (String n0 : graph.getSuccessors(n)) {
				if (!neighborhood.contains(n0) && (mask == null || (mask != null && mask.contains(n0)))) {
					neighborhood.add(n0);
				}
			}
		}

		return neighborhood;
	}

	//// measures /////////////////////////////////////

	public double M1(String node) {
		return internalNEs.get(node);
	}

	// public double M2(String node) {
	// return internalDensities.get(node);
	// }

	public double M3(String node) {
		return externalDensities.get(node);
	}

	public double M4(String node) {
		return memberships_usingNodes[1].get(node);
	}

	// public double M5(String node) {
	// return newMeasureValues.get(node);
	// }

	//// features /////////////////////////////////////

	private double calculateF1(String node) { // Measure M1 avg
		return Math.min(1d, resolution * internalNEs.get(node) / avgInternalNEIn);
	}

	private double calculateF2(String node) { // Measure M3 with avg
		return Math.min(1d, resolution * externalDensities.get(node) / avgExternalDensityIn);
	}

	private double calculateF3(String node) { // Measure M4 with avg
		return Math.min(1d, resolution * memberships_usingNodes[1].get(node) / avgMembership_usingNodesIn[1]);
	}

	//// general feature ////////////////////////////

	public Double calculateF(int fi, String node) {
		switch (fi) {
		case 1:
			return 2 * calculateF1(node) - 1;
		case 2:
			return 2 * calculateF2(node) - 1;
		case 3:
			return 2 * calculateF3(node) - 1;
		}

		return null;
	}

	public double[] calculateFeaturesForExpandingModel(String node) {
		// if (Math.sqrt(avgInternalNEIn * avgMembership_usingNodesIn[1]) < 1d /
		// 8 + 1d / 16 - 1d / 32) {
		// return new double[] { -1, -1, -1, 0 };
		// }
		return new double[] { calculateF(1, node), calculateF(2, node), calculateF(3, node), 0d };
	}
}
