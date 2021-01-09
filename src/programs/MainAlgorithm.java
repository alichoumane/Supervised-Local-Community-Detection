package programs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;

import helpers.CustomLogger;
import helpers.FileWritingHelper;
import helpers.PathsHelper;
import helpers.TimeTracker;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;
import learning.DecisionMaker;
import learning.TrueCommunitiesFeatures;
import utils.*;

public class MainAlgorithm {

        private static DecisionMaker dm;
        private static String graphPath = "";
	private static String currentGraph = graphPath +"edges.csv";
        private static String currentSeed = "";
        private static String modelPath = "model-binary.model";
        private static double resolution = 1.0;
        
	public static void main(String[] args) {
		    
            if(!loadArgs(args)) {
		return;
            }
            
            testOnSingleSeed(currentGraph, currentSeed);
	}
        
        public static boolean loadArgs(String[] args) {
            ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));
            //if(args.length==0) return true;//run from IDE
            if(args.length==1 && args[0].equals("-h")) {
                    //print help
                    System.out.println("Please provide the following arguments to run the program:");
                    System.out.println("-f followed by the network file name (undirected, unweighted, one edge per line)");
                    System.out.println("-m followed by the Neural Network Model file name");
                    System.out.println("-r followed by the resolution value (1.0 for default behaviour)");
                    System.out.println("Check the README.txt file for more information.");
                    return false;
            }

            if(args.length == 6) {
                    int index = -1;

                    index = argsList.indexOf("-f");
                    if(index == -1) return loadArgs(new String[]{"-h"});
                    currentGraph = argsList.get(index+1);

                    File graphFile = new File(currentGraph);

                    if(!graphFile.exists()) 
                    {
                            System.out.println(currentGraph + " does not exist.");
                            return false;
                    }
                    
                    index = -1;
                    
                    index = argsList.indexOf("-m");
                    if(index == -1) return loadArgs(new String[]{"-h"});
                    modelPath = argsList.get(index+1);

                    File modelFile = new File(modelPath);

                    if(!modelFile.exists()) 
                    {
                            System.out.println(modelFile + " does not exist.");
                            return false;
                    }
                    
                    index = -1;
                    
                    index = argsList.indexOf("-r");
                    if(index == -1) return loadArgs(new String[]{"-h"});
                    resolution = Double.parseDouble(argsList.get(index+1));
                    

                return true;
            }

            return loadArgs(new String[]{"-h"});
        }
        
        public static void testOnSingleSeed(String graphPath, String seed)
        {
            if(dm==null) {
			dm = new DecisionMaker();

			File model = new File(modelPath);
			if(model.exists()==false) {
                            System.out.println("Error: Neural network model " +  modelPath + " not found!"+"\n");
                            return; 
				
			}else {
				System.out.println("Loading the neural network model: "+ modelPath);
				dm.initFromModel(modelPath, true);
			}
		}
		
		TrueCommunitiesFeatures featuresCalc;

		Graph<String> graph = Graph.loadFromFile(graphPath, false);
		featuresCalc = new TrueCommunitiesFeatures(graph, resolution);
                
                Scanner input = new Scanner(System.in);
                while(true)
                {
                    if(currentSeed.equals("")){
                        // seed not given, ask from standard input
                         System.out.println("\n-------------------------------------------------------------------------------");
                        System.out.println("To find a local community, enter seed node ID (Type x to terminate the program): ");
                        currentSeed = input.next();
                        
                        if(currentSeed.toLowerCase().equals("x")) break;
                    }
                    else{
                        System.out.println("Starting the expansion from seed " + currentSeed);
                        ArrayList<String> initialCores = null;
                        
                        try{
                            initialCores = getCore(graph, currentSeed);
                        }
                        catch(RuntimeException ex)
                        {
                            System.out.println("Seed " + currentSeed + " has no neighbours so it can't belong to any community!");
                            currentSeed = "";
                            continue;
                        }
                        ArrayList<String> core = classify(graph, dm, featuresCalc, initialCores, false);
                        System.out.println("Local community found with " + core.size() + " nodes including the given seed.");
                        System.out.println("Result saved to " + currentSeed+"_community.dat");

                        try {

                            PrintWriter writer = new PrintWriter(new FileWriter(currentSeed+"_community.dat"));
                            //writer.println("Id\t" + currentSeed + "_LocalCommunity_Id");
                            for(int i = 0; i < core.size(); i++)
                                writer.println(core.get(i));

                            writer.close();

                        } catch (IOException ex) {
                            Logger.getLogger(MainAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        currentSeed = "";
                        
                    }
                   
                 
                }
		
		
	
        }
	
	private static ArrayList<String> groupNeighbors = null;//temporary
	private static ArrayList<String> classify(
                Graph<String> graph, 
                DecisionMaker dm,
                TrueCommunitiesFeatures featuresCalc, 
                ArrayList<String> core, boolean oneNodePerIteration) {
            
		TimeTracker time = new TimeTracker();
            
                int nbAdded=0;
                time.restart();
                ArrayList<String> addedNewly = expandCommunity(core, graph, dm, featuresCalc, oneNodePerIteration);
                nbAdded = addedNewly.size();
                time.stop();

                /*
                double sim2 = GroupAnalysisTools.groupSim(core, trueCommunities.get(key), trueCommunities.get(key).size());
                double sim = GroupAnalysisTools.groupSim(core, trueCommunities.get(key));
                groupNeighbors.retainAll(trueCommunities.get(key));
                logger.log(Level.FINER, tag+","+key+","+dm.threshold+","+nbAdded+","+core.size()+","+sim+","+sim2+","+groupNeighbors.size()+","+time.toString()+"\n");
                */
		
		return core;
	}
	
	/**
	 * 
	 * @param core
	 * @param graph
	 * @param dm
	 * @return list of added nodes
	 */
	public static ArrayList<String> expandCommunity(ArrayList<String> core, Graph<String> graph, DecisionMaker dm,
			TrueCommunitiesFeatures featuresCalc,
			boolean oneNodePerIteration) {
		ArrayList<String> addedNewly = new ArrayList<>();
		TrueCommunitiesFeatures featuresCalcLocal = new TrueCommunitiesFeatures(featuresCalc, core, resolution);
		groupNeighbors = featuresCalcLocal.getGroupNeighborhood();
		if(oneNodePerIteration==false) {
			//start expansion
			LinkedList<String> toCheck = new LinkedList<>(groupNeighbors);
			ArrayList<String> checked = new ArrayList<>();
			//BufferedWriter writer = new BufferedWriter(new FileWriter(PathsHelper.root+"LFR/learn/log.csv",true));
			do {
				ArrayList<String> added = new ArrayList<>();
				while(toCheck.isEmpty()==false) {
					String node = toCheck.pop();
					double rawDecision = dm.rawDecision(node, featuresCalcLocal);
					if(!core.contains(node) && addedNewly.contains(node)==false && 
							added.contains(node)==false && dm.decide(rawDecision)==true) {
						added.add(node);
					}
					checked.add(node);
					
					//logging
					//double[] features = featuresCalcLocal.calculateFeaturesForExpandingModel(node);
					//writer.write(tag+",");
					//for(double d:features)writer.write(d+",");
					//writer.write(rawDecision+","+trueCommunity.contains(node)+"\n");
					//logger.log(Level.FINEST, tag+","+node+","+rawDecision+","+trueCommunity.contains(node)+"\n");
				}
				if(added.size()==0 /*|| addedNewly.size()>trueCommunity.size()*/)break;
				
				core.addAll(added);
				
				featuresCalcLocal = new TrueCommunitiesFeatures(featuresCalc, core, resolution);
				toCheck.addAll(featuresCalcLocal.getGroupNeighborhood());
				
				addedNewly.addAll(added);
				
				//TODO:remove (avoid overexpansion)
				/*if(addedNewly.size()>trueCommunity.size()*5) {
					core.clear();
					return core;
				}*/
			}while(true);
		}else {
			//start expansion
			LinkedList<String> toCheck = new LinkedList<>(groupNeighbors);
			ArrayList<String> checked = new ArrayList<>();
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(PathsHelper.root+"LFR/learn/log.csv",true));
				do {
					//ArrayList<String> added = new ArrayList<>();
					String nodeToAdd = "";
					double lastValue=-1;
					while(toCheck.isEmpty()==false) {
						String node = toCheck.pop();
						//if(checked.contains(node))continue;
						double rawDecision = dm.rawDecision(node, featuresCalcLocal);
						if(!core.contains(node) && addedNewly.contains(node)==false && 
								/*added.contains(node)==false && */dm.decide(rawDecision)==true) {
							//added.add(node);
							if(rawDecision>lastValue) {
								lastValue = rawDecision;
								nodeToAdd = node;
							}
						}
						checked.add(node);
						
						//logging
						/*double[] features = featuresCalcLocal.calculateFeaturesForExpandingModel(node);
						writer.write(tag+",");
						for(double d:features)writer.write(d+",");
						writer.write(rawDecision+","+trueCommunity.contains(node)+"\n");
						logger.log(Level.FINEST, tag+","+node+","+rawDecision+","+trueCommunity.contains(node)+"\n");*/
					}
					//if(added.size()==0 || added.size()>100)break;
					if(nodeToAdd.equals(""))break;
					//if(addedNewly.size()>trueCommunity.size())break;
					
					core.add(nodeToAdd);
					featuresCalcLocal = new TrueCommunitiesFeatures(featuresCalc, core, resolution);
					toCheck.addAll(featuresCalcLocal.getGroupNeighborhood());
					
					addedNewly.add(nodeToAdd);
				}while(true);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		return addedNewly;
	}
	
	/**
	 * Forms a core out of each seed by adding a neighbour of the seed thus forming a group
	 * @param graph
	 * @param trueCommunities
	 * @param coresPath
	 * @return
	 */
	public static ArrayList<String> getCore(Graph<String> graph, String currentSeed) {
               ArrayList<String> core = new ArrayList<>();
               core.add(currentSeed);
               
               ArrayList<String> neighbours = graph.getSuccessors(currentSeed);

                if(neighbours.size()==0) {
                        throw new RuntimeException("Seed " + currentSeed + " has no neighbours! \n");
                }

                int max = 0;
                String candidate = "";
                for(String n:neighbours) {
                        ArrayList<String> inter = graph.getSuccessors(currentSeed);
                        inter.retainAll(graph.getSuccessors(n));
                        if(inter.size()>max) {
                                max = inter.size();
                                candidate = n;
                        }
                }
                
                if(candidate.equals("")) {
                        //we didn't find any neighbour that have common neighbours with the seed
                        //logger.log(Level.WARNING, "Seed of community "+key+
                        //                " has no neighbour in common with its neighbours! adding random neighbour\n");
                        candidate = neighbours.get(0);
                }
                
                core.add(candidate);
		
		return core;
	}
	
	
	public static ArrayList<String> getSetOfNeighbors(ArrayList<String> group, Graph<String> graph){
		ArrayList<String> neighbors = new ArrayList<String>();
		for(String node:group) {
			for(String s:graph.getSuccessors(node)) {
				if(neighbors.contains(s)==false)neighbors.add(s);
			}
		}
		neighbors.removeAll(group);
		return neighbors;
	}

}
