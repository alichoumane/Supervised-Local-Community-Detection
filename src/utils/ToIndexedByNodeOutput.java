package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import helpers.FileWritingHelper;
import helpers.PathsHelper;

/**
 * Used to convert output of Palla Clique Percolation output to an output indexed by node (each node along with its communities)
 * The output of this class is readable by Gephi for visualisation.
 * @author Ali Harkous
 *
 */
public class ToIndexedByNodeOutput {
    
        public static String groupIndexedInput = "D:\\Datasets\\Amazon_U\\LocalCommunities\\with_resolution2.2\\used-com-amazon.top5000.cmty.txt";
	public static String nodeIndexedOutput = "D:\\Datasets\\Amazon_U\\LocalCommunities\\with_resolution2.2\\byNode-used-com-amazon.top5000.cmty.txt";
       
	//public static String groupIndexedInput = PathsHelper.currentSubDirectory+"/com-testset-dblp.top3674.cmty.txt";
	//public static String nodeIndexedOutput = PathsHelper.currentSubDirectory+"/"+PathsHelper.trueCommunitiesFileName;
	
	public static void main(String[] args) {
//		String[] graphsNames = new String[] {"zachary","polbooks","santafe","dolphins"};
//		for(String name:graphsNames) {
//			for(int k=3;k<7;k++) {
//				convert(name,k);
//			}
//		}
		convert(groupIndexedInput, nodeIndexedOutput);
	}
	
	@SuppressWarnings("unused")
	private static void convert(String graphName, int k) {
		String groupIndexedInput1 = groupIndexedInput.replace("[name]", graphName);
		String nodeIndexedOutput1 = nodeIndexedOutput.replace("[name]", graphName);
		groupIndexedInput1 = groupIndexedInput1.replace("[n]", k+"");
		nodeIndexedOutput1 = nodeIndexedOutput1.replace("[n]", k+"");
		
		File f = new File(groupIndexedInput1);
		if(!f.exists()) {
			return ;
		}
		System.out.println("saving result to: "+nodeIndexedOutput1);
		convert(groupIndexedInput1, nodeIndexedOutput1);
		System.out.println("converted "+graphName+" k="+k);
	}
	
	private static void convert(String groupIndexedInput, String nodeIndexedOutput) {
		HashMap<Integer, ArrayList<String>> groups = loadGroups(groupIndexedInput, "\t");
		writeGroups(nodeIndexedOutput, groups);
	}
	
	public static HashMap<Integer, ArrayList<String>> loadGroups(String groupIndexedInput, String delimeter){
		HashMap<Integer, ArrayList<String>> groups = new HashMap<>();
		System.out.println("loading classes from file "+groupIndexedInput);
		try {
			Scanner scanner = new Scanner(new FileReader(groupIndexedInput));
			int groupId = 0;
			while(scanner.hasNext()){
				String rawLine = scanner.nextLine();
				if(rawLine.startsWith("#")/* || !rawLine.contains(":")*/) {
					continue;
				}
				//String[] line = rawLine.split(": ");
				//String groupId = line[0];
				String[] nodes = rawLine.split(delimeter);
				ArrayList<String> members = new ArrayList<>();
				for(String node:nodes) {
					members.add(node);
				}
				groups.put(groupId, members);
				++groupId;
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return groups;
	}
	
	public static void writeGroups(String nodeIndexedOutput, HashMap<Integer, ArrayList<String>> groups) {
		FileWritingHelper gen = new FileWritingHelper(null) {};
		gen.writeResults(groups, null, nodeIndexedOutput, "communities", true);
	}

}
