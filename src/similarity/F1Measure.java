/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package similarity;

import helpers.PathsHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import utils.ToIndexedByCommunityOutput;

/**
 *
 * @author Ali
 */
public class F1Measure {
            public static double averageF1_slow(ArrayList<ArrayList<String>> predictedGroups,ArrayList<ArrayList<String>> trueGroups){
            double sum=0.0;
            int n = trueGroups.size();
            int m = predictedGroups.size();
            
            while(m > 0){
                
                int indexMaxPredicted = 0;
                int indexMaxTrue = 0;
                double maxF1=0;
                int j = 0;
                
                for(ArrayList<String> g: predictedGroups){
                    int i = 0;
                    for(ArrayList<String> g0: trueGroups){
                        double tempF1 = F1(g,g0);
                        if(tempF1 > maxF1)
                        {
                            maxF1 = tempF1;
                            indexMaxTrue = i;
                            indexMaxPredicted = j;
                        }
                        
                        i++;
                    }
                    
                    j++;
                  
                }
                 
                predictedGroups.remove(indexMaxPredicted);
                m--;
                trueGroups.remove(indexMaxTrue);
                System.out.println("m = " + m +  "  max F1 = " + maxF1);
                sum += maxF1;
            }
            
           
            
            return sum/n;
        }
            
        // use it if the number of predicted communities is different from the number of true communities (not all are predicted)
        public static double averageF1_1(ArrayList<ArrayList<String>> predictedGroups,ArrayList<ArrayList<String>> trueGroups){
            double sum=0.0;
            int n = predictedGroups.size();

            int j = 0;
            for(ArrayList<String> g: predictedGroups){
                int indexMaxTrue = 0;
                double maxF1=0;
                int i = 0;
                for(ArrayList<String> g0: trueGroups){
                    double tempF1 = F1(g,g0);
                    if(tempF1 > maxF1)
                    {
                        maxF1 = tempF1;
                        indexMaxTrue = i;
                    }

                    i++;
                }
                    
                trueGroups.remove(indexMaxTrue);
                System.out.println("max F1 = " + maxF1 + "\t index_predicted = "+ j + "\t index_true = " + indexMaxTrue);
                sum += maxF1;  
                
                j++;
            }

            return sum/n;
        }    
        
        // Use it if the number of predicted communities is identical to the number of true communities
        public static double averageF1(ArrayList<ArrayList<String>> predictedGroups,ArrayList<ArrayList<String>> trueGroups){
            double sum=0.0;
            int n = trueGroups.size();

            for(int i = 0 ; i < n; i++){
                double currentF1 = F1(predictedGroups.get(i),trueGroups.get(i));
                System.out.println("current F1 = " + currentF1);
                sum += currentF1; 
            }
         
            return sum/n;
        }
        
        public static double F1(ArrayList<String> group1, ArrayList<String> group2){
            double denominator = group1.size() + group2.size();
            
            HashSet<String> set = new HashSet<>(); 
            set.addAll(group1);
            
            set.retainAll(group2);
            
            double numerator = 2*set.size();
            
            return numerator/denominator;
	}
        
        public static void main(String[] args)
        {
            String path = "D:\\Datasets\\DBLP_U\\";
            HashMap<Integer, ArrayList<String>> trueGroups = ToIndexedByCommunityOutput.loadGroups(path+"\\byNode-com-dblp.top5000.cmty.txt" ,"\t",PathsHelper.overlappingGroupsDelimeter);
            HashMap<Integer, ArrayList<String>> predicted = ToIndexedByCommunityOutput.loadGroups(path+"\\predicted.csv","\t", PathsHelper.overlappingGroupsDelimeter);
            if(predicted.size()!=trueGroups.size()) {
                    System.out.println("WARNING: groups sets sizes are different, trueGroups:"+trueGroups.size()+", predicted:"+predicted.size());
            }
            ArrayList<ArrayList<String>> predictedList = new ArrayList<>(predicted.values());
            ArrayList<ArrayList<String>> trueGroupsList = new ArrayList<>(trueGroups.values());
            //System.out.println(tag+GroupAnalysisTools.groupsSim(groups1List, groups2List));
            System.out.println(F1Measure.averageF1(predictedList, trueGroupsList));
            
        }
}
