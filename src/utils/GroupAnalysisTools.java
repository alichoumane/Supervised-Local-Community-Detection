package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class GroupAnalysisTools {
	

	public static ArrayList<ArrayList<String>> connectedParts(ArrayList<String> core, Graph<String> graph) {
		ArrayList<String> ungrouped = new ArrayList<String>(core);

		ArrayList<ArrayList<String>> parts = new ArrayList<ArrayList<String>>();
		while (ungrouped.isEmpty() == false) {
			String node = ungrouped.get(0);
			ArrayList<String> connectedPart = new ArrayList<String>();
			connectedPartsRecur(ungrouped, connectedPart, node, graph);

			parts.add(connectedPart);
		}

		return parts;
	}

	private static void connectedPartsRecur(ArrayList<String> ungrouped, ArrayList<String> connectedPart, String node,
			Graph<String> graph) {

		connectedPart.add(node);
		ungrouped.remove(node);
		ArrayList<String> successors = graph.getSuccessors(node);
		for (String s : successors) {
			if (ungrouped.contains(s)) {
				connectedPartsRecur(ungrouped, connectedPart, s, graph);
			}
		}
	}
        
	public static double groupsSim(ArrayList<ArrayList<String>> groups1,ArrayList<ArrayList<String>> groups2){
		double sim1=0;
		int sum1=0;
		for(ArrayList<String> g: groups1){
			double d=0;
			for(ArrayList<String> g0: groups2){
				d=Math.max(d, groupSim(g,g0));
			}
			
			sim1+=d*g.size();
			sum1+=g.size();
		}
		sim1=(sim1/sum1);
		
		double sim2=0;
		int sum2=0;
		for(ArrayList<String> g: groups2){
			double d=0;
			for(ArrayList<String> g0: groups1){
				d=Math.max(d, groupSim(g,g0));
			}
			
			sim2+=d*g.size();
			sum2+=g.size();
		}
		sim2=(sim2/sum2);
		
		return Math.sqrt(sim1*sim2);
	}
	
	public static double groupSim(ArrayList<String> group1,ArrayList<String> group2){
		int n=0;
		for(String e:group2){
			if(group1.contains(e)) n++;
		}
		
		return ((double)n)/(group1.size()+group2.size()-n);
	}
	
	public static double groupSim(ArrayList<String> group1,ArrayList<String> group2, int denominator){
		int n=0;
		for(String e:group2){
			if(group1.contains(e)) n++;
		}
		
		return ((double)n)/denominator;
	}
}
