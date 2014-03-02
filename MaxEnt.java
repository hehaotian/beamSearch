/*
author: Haotian He

created by 2014/02/13
*/


import java.io.*;
import java.util.*;
import java.lang.*;
import java.math.*;

public class MaxEnt {
   
   private Map<String, Map<String, Double>> model = new HashMap<String, Map<String, Double>>();
   private Map<String, Map<String, Integer>> test_matrix = new HashMap<String, Map<String, Integer>>();
   private Set<String> classLabs = new TreeSet<String>();
   private Set<String> feature_counts = new TreeSet<String>();
   private Map<String, Double> default_weights = new HashMap<String, Double>();
   
   public MaxEnt(String model_path) throws IOException {
      this.model = get_model(model_path);
   }
   
   private Map<String, Map<String, Double>> get_model(String model_path) throws IOException {
      BufferedReader model_file = new BufferedReader(new FileReader(model_path));
      String line = "";
      String classLabel = "";
      while ((line = model_file.readLine()) != null) {
         if (line.contains("FEATURES FOR CLASS")) {
            classLabel = line.trim().split(" ")[3];
            classLabs.add(classLabel);
            continue;
         }
         if (line.contains("<default>")) {
            String[] word_weight = line.split(" ");
            String weight_string = word_weight[2];
            double weight = Double.parseDouble(weight_string); 
            default_weights.put(classLabel, weight);
            continue;           
         }
         String[] word_weight = line.split(" ");
         String word = word_weight[1];
         String weight_string = word_weight[2];
         double weight = Double.parseDouble(weight_string);
         if (!model.containsKey(classLabel)) {
            model.put(classLabel, new HashMap<String, Double>());
         }
         model.get(classLabel).put(word, weight);
         feature_counts.add(word);
      }
      return model;
   }

   public Map<String, Double> beamsearch_predict(int topN, Map<String, Double> feature_vector) {
         
      Map<String, Double> temp_pred_probs = new HashMap<String, Double>();
      
      double normal_z = 0.0;
      Iterator itr1 = classLabs.iterator();
      while (itr1.hasNext()) {
         String label = "" + itr1.next();
         double sum = default_weights.get(label);
      
         for (String word : feature_vector.keySet()) {
            sum += model.get(label).get(word);
         }
         double result = Math.exp(sum);
         temp_pred_probs.put(label, result);
         normal_z += result;
      }
         
      Map<String, Double> final_pred_probs = new HashMap<String, Double>();
         
      Iterator itr2 = classLabs.iterator();
      while (itr2.hasNext()) {
         String label = "" + itr2.next();
         final_pred_probs.put(label, temp_pred_probs.get(label) / normal_z);
      }

      Stack<String> res = c.getWordDecendOrder(final_pred_probs);
      Map<String, Double> ret = new HashMap<String, Double>();
      for(int i = 0; i < topN; i++) {
         String[] info = res.pop().split(" ");
         ret.put(info[0], Double.parseDouble(info[1]));
      }
      return ret;

   }
}