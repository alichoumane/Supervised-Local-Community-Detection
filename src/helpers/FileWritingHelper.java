package helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import utils.Graph;

/**
 * @author Ali Harkous
 * @version 1.14.30012019
 */
public abstract class FileWritingHelper {

	public static final CustomLogger logger = new CustomLogger("NConnexGenerator", Level.FINEST);
	
	protected static int countGroups=0;
	protected Graph<String> graph;
	protected ArrayList<String> groupedNodes;
	
	//ALGORITHM PARAMETERS
	public static boolean checkPredecessors = false;
	
	public FileWritingHelper(String graphFile, boolean directed) {
		graph = utils.Graph.loadFromFile(graphFile, directed);
	}
	
	public FileWritingHelper(Graph<String> graph) {
		this.graph = graph;
	}
	
	public void writeResults(HashMap<Integer, ArrayList<String>> groupsList, HashMap<Integer, String> groupsAttributes, String file,
			String attributeName, boolean mergeDuplicates) {
		if(mergeDuplicates) {
			//merging duplicates and assigning colors
			HashMap<String, String> nodesAttrs = new HashMap<>();
			for(Integer key:groupsList.keySet()) {
				for(String node:groupsList.get(key)) {
					if(!nodesAttrs.containsKey(node))nodesAttrs.put(node,key+"");
					else nodesAttrs.put(node,nodesAttrs.get(node)+" "+key);
				}
			}
			//writing results
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write("Id\t"+attributeName);
				writer.write("\n");
				for(String node:nodesAttrs.keySet()) {
					writer.write(node+"\t"+nodesAttrs.get(node));
					writer.write("\n");
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				if(groupsAttributes!=null)writer.write("Id\t"+attributeName+"\tN\n");else writer.write("Id\t"+attributeName+"\n");
				String N="0";
				for(Integer groupId:groupsList.keySet()) {
					N = (groupsAttributes!=null)?groupsAttributes.get(groupId):"0";
					for(String node: groupsList.get(groupId)) {
						if(groupsAttributes!=null) writer.write(node+"\t"+groupId+"\t"+N+"\n"); else writer.write(node+"\t"+groupId+"\n");
					}
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * writes results to a file
	 * @param attributes contains the classification for each node
	 * @param attributeName
	 * @param file
	 * @param edgeAttrs if true, the string keys are considered to be tuples delimited with a ','
	 */
	public void writeResults(HashMap<String, Double> attributes, String attributeName, String file, boolean edgeAttrs) {
		try {
			//write results with attributes
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			if(!edgeAttrs)writer.write("Id\t"+attributeName+"\n");else writer.write("Source\tTarget\t"+attributeName+"\n");
			writeResults(attributes, writer, edgeAttrs);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeResults(HashMap<String, Double> attributes, BufferedWriter writer, boolean edgeAttrs) {
		try {
			//write results with attributes
			for(String node:attributes.keySet()) {
				String edge = node;
				if(edgeAttrs) {
					node = node.replace(",", "\t");
				}
				writer.write(node+"\t"+attributes.get(edge)+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
