package utils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;

import helpers.CustomLogger;
import helpers.PathsHelper;

public class ToIndexedByCommunityOutput {
        
	public static String nodeIndexedInput = "D:/Datasets/DBLP_U/predicted.csv";//PathsHelper.currentSubDirectory+"/predicted.csv";
	public static String groupIndexedOutput = "D:/Datasets/DBLP_U/byGroup-predicted.csv";//PathsHelper.currentSubDirectory+"/byGroup-predicted.csv";
	
	public static String inputDelimiter = "\t";//the delimiter separating the nodeId from its groups
	public static String inputDelimiterG = " ";//the delimiter separating the groups ids from each other
	public static String outputDelimiter = "\t";//the delimiter separating the nodes of one group on one line
	public static String outputDelimiterG = "\t";//the delimiter separating groupId from its nodes
	

	public static CustomLogger logger = new CustomLogger("ToIndexedByCommunityOutput", Level.FINE);

	public static void main(String[] args) {
		if(!loadArgs(args)) {
			//return;
		}

		System.out.println("start converting "+nodeIndexedInput+" to "+groupIndexedOutput);
		convert(nodeIndexedInput,groupIndexedOutput);
		//runOnAllLFRGraphs();
		
	}
	
	public static void runOnAllLFRGraphs() {
		/*System.out.println("start converting "+nodeIndexedInput+" to "+groupIndexedOutput);
		convert(nodeIndexedInput,groupIndexedOutput);*/
		for(int l=1;l<=3;l+=2) {
			double dl = l/10.0;
			for(int k=10;k<=50;k+=40) {
				for(int i=0;i<3;i++) {
					for(int j=2;j<=8;j+=2) {
						String n = nodeIndexedInput.replace("[i]", i+"");
						String g = groupIndexedOutput.replace("[i]", i+"");
						n = n.replace("[j]", j+"");
						g = g.replace("[j]", j+"");
						n = n.replace("[k]", k+"");
						g = g.replace("[k]", k+"");
						n = n.replace("[l]", dl+"");
						g = g.replace("[l]", dl+"");
						System.out.println("start converting "+n+" to "+g);
						//convert(n,g);
					}
				}
			}
		}
	}
	
	public static boolean loadArgs(String[] args) {
		ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));
		//if(args.length==0)return true;//run from IDE
		if(args.length==1 && args[0].equals("-h")) {
			//print help
			System.out.println("please provide the following args to run the program:");
			System.out.println("-nodeIndexedInput");
			System.out.println("-groupIndexedOutput");
			System.out.println("-inputDelimiter");
			System.out.println("-inputDelimiterG");
			System.out.println("-outputDelimiter");
			System.out.println("-outputDelimiterG");
			return false;
		}
		
		if(args.length>2) {
			int index = -1;
			
			index = argsList.indexOf("-nodeIndexedInput");
			if(index==-1)return loadArgs(new String[]{"-h"});
			nodeIndexedInput = argsList.get(index+1);
			
			index = argsList.indexOf("-groupIndexedOutput");
			if(index==-1)return loadArgs(new String[]{"-h"});
			groupIndexedOutput = argsList.get(index+1);
			
			index = argsList.indexOf("-inputDelimiter");
			if(index!=-1)inputDelimiter = argsList.get(index+1);
			index = argsList.indexOf("-inputDelimiterG");
			if(index!=-1)inputDelimiterG = argsList.get(index+1);
			index = argsList.indexOf("-outputDelimiter");
			if(index!=-1)outputDelimiter = argsList.get(index+1);
			index = argsList.indexOf("-outputDelimiterG");
			if(index!=-1)outputDelimiterG = argsList.get(index+1);
			
			return true;
		}
		
		return loadArgs(new String[]{"-h"});
	}
	public static void convert(String nodeIndexedInput, String groupIndexedOutput) {
		HashMap<Integer, ArrayList<String>> groups = loadGroups(nodeIndexedInput,inputDelimiter,inputDelimiterG);
		if(groups==null)return;//file doesn't exist
		writeGroups(groupIndexedOutput, groups, false);
	}
	
	/**
	 * Load groups with overlap
	 * @param nodeIndexedInput
	 * @param inputDelimiter the delimiter separating the nodeId from its groups
	 * @param inputDelimiterG the delimiter separating the groups ids from each other
	 * @return
	 */
	public static HashMap<Integer, ArrayList<String>> loadGroups(String nodeIndexedInput, String inputDelimiter,
			String inputDelimiterG){
		HashMap<Integer, ArrayList<String>> groups = new HashMap<>();
		logger.log(Level.FINER, "loading classes indexed by nodes from file "+nodeIndexedInput);
		try {
			Scanner scanner = new Scanner(new FileReader(nodeIndexedInput));
			while(scanner.hasNext()){
				String rawLine = scanner.nextLine();
				if(rawLine.startsWith("#") || rawLine.startsWith("Id")) {
					continue;
				}
				
				String[] groupsNames;
				String node;
				if(inputDelimiter.equals(inputDelimiterG)) {
					String[] lineParts = rawLine.split(inputDelimiter);
					ArrayList<String> groupsNamesList = new ArrayList<>(Arrays.asList(lineParts));
					node = lineParts[0];//first entry in the file
					groupsNamesList.remove(node);
					node = node.replace(":", "");//replace anything else that is expected to be not a part of node name
					groupsNames = groupsNamesList.toArray(new String[] {});
				}else {
					String[] line = rawLine.split(inputDelimiter);
					node = line[0];
					groupsNames = line[1].split(inputDelimiterG);
				}
				
				for(int i=0;i<groupsNames.length;i++) {
					int group = Integer.parseInt(groupsNames[i]);
					if(groups.containsKey(group)==false)groups.put(group, new ArrayList<>());
					if(!groups.get(group).contains(node))groups.get(group).add(node);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return groups;
	}
	
	/**
	 * loading sub groups when saved in normal output format i.e Id \t Community with overlap. The form in which the other
	 * loadSubGroups implementations can't load. This is basically the same as loadGroups but with different return type.
	 * @param nodeIndexedInput
	 * @param inputDelimiter
	 * @param inputDelimiterG
	 * @return
	 */
	public static HashMap<Integer, HashMap<Integer, ArrayList<String>>> loadSubGroups(String nodeIndexedInput, String inputDelimiter,
			String inputDelimiterG){
		HashMap<Integer, HashMap<Integer, ArrayList<String>>> groups = new HashMap<>();
		logger.log(Level.FINER, "loading classes indexed by nodes from file "+nodeIndexedInput);
		try {
			Scanner scanner = new Scanner(new FileReader(nodeIndexedInput));
			while(scanner.hasNext()){
				String rawLine = scanner.nextLine();
				if(rawLine.startsWith("#") || rawLine.startsWith("Id")) {
					continue;
				}
				if(inputDelimiter.equals(inputDelimiterG)) {
					String[] groupsNames = rawLine.split(inputDelimiter);
					String node = groupsNames[0];//first entry in the file
					node = node.replace(":", "");//replace anything else that is expected to be not a part of node name
					for(int i=1;i<groupsNames.length;i++) {
						int group = Integer.parseInt(groupsNames[i]);
						
						if(groups.containsKey(group)==false) {
							groups.put(group, new HashMap<>());
							ArrayList<String> community = new ArrayList<>();
							groups.get(group).put(0, community);
						}
						groups.get(group).get(0).add(node);
					}
				}else {
					String[] line = rawLine.split(inputDelimiter);
					String node = line[0];
					String[] groupsNames = line[1].split(inputDelimiterG);
					for(int i=0;i<groupsNames.length;i++) {
						int group = Integer.parseInt(groupsNames[i]);
						
						if(groups.containsKey(group)==false) {
							groups.put(group, new HashMap<>());
							ArrayList<String> community = new ArrayList<>();
							groups.get(group).put(0, community);
						}
						groups.get(group).get(0).add(node);
					}
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return groups;
	}
	
	/**
	 * load sub groups with default params. format: node,trueCommunityKey,subCommunityKey
	 * @param path
	 * @return
	 */
	public static HashMap<Integer, HashMap<Integer, ArrayList<String>>> loadSubGroups(String path){
		return loadSubGroups(path, ",");
	}
	/**
	 * loads sub groups from file of format: node,trueCommunityKey,subCommunityKey
	 * @param path
	 * @param inputDelimiter
	 * @return
	 */
	public static HashMap<Integer, HashMap<Integer, ArrayList<String>>> loadSubGroups(String path, String inputDelimiter){
		HashMap<Integer, HashMap<Integer, ArrayList<String>>> groups = new HashMap<>();
		logger.log(Level.FINER, "loading sub groups indexed by nodes from file "+nodeIndexedInput);
		try {
			Scanner scanner = new Scanner(new FileReader(path));
			while(scanner.hasNext()){
				String rawLine = scanner.nextLine();
				if(rawLine.startsWith("#") || rawLine.startsWith("Id")) {
					continue;
				}
				String[] parts = rawLine.split(inputDelimiter);
				String node = parts[0];//first entry in the file

				int trueCommnityKey = Integer.parseInt(parts[1]);
				int subGroupKey = 1;
				if(parts.length>=3) {
					subGroupKey = Integer.parseInt(parts[2]);
				}
				
				if(groups.containsKey(trueCommnityKey)==false)groups.put(trueCommnityKey, new HashMap<>());
				if(groups.get(trueCommnityKey).containsKey(subGroupKey)==false) {
					groups.get(trueCommnityKey).put(subGroupKey, new ArrayList<>());
				}
				groups.get(trueCommnityKey).get(subGroupKey).add(node);
				
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return groups;
	}
	
	/**
	 * write groups indexed by group id, i.e. groupId nodeId1 nodeId2 nodeId3...
	 * @param nodeIndexedOutput
	 * @param groups
	 */
	public static void writeGroups(String nodeIndexedOutput, HashMap<Integer, ArrayList<String>> groups, boolean writeGroupId) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(nodeIndexedOutput));
			//writer.write("communityId\tnodes\n");
			for(int group:groups.keySet()) {
				if(writeGroupId)writer.write(group+outputDelimiterG);
				for(int i=0; i<groups.get(group).size(); ++i) {
					String node = groups.get(group).get(i);
					writer.write(node);
					if(i<groups.get(group).size()-1) {
						writer.write(outputDelimiter);
					}
				}
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
