import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;


public class beamsearch_maxent {
	
	private static String testData;
	private static String boundary;
	private static String model;
	private static String output;
	private static int beamSize;
	private static int topN;
	private static int topK;
	
	private static MaxEnt me;
	
	private static int totalIns = 0;
	private static int rightIns = 0;
	
	/*
	private static BNode root;
	private static Queue<BNode> curLevel;
	*/
	public static void main(String[] args) throws IOException {
		//c.d("start time: "+c.getCurTime());
		//<debug>
		/*
		args = new String[7];
		args[0] = "sec19_21.txt";
		args[1] = "sec19_21.boundary";
		args[2] = "m1.txt";
		args[3] = "output.txt";
		args[4] = "0";
		args[5] = "1";
		args[6] = "1";
		*/
		//</debug>
		
		
		
		//<get input>
		testData = args[0];
		boundary = args[1];
		model = args[2];
		output = args[3];
		beamSize = Integer.parseInt(args[4]);
		topN = Integer.parseInt(args[5]);
		topK = Integer.parseInt(args[6]);
		//</get input>
		
		//<processing>
		buildMLModel();
		BufferedReader getTest = c.readFileByLine(testData);
		BufferedReader getBound = c.readFileByLine(boundary);
		c.outPath = output;
		String ins, bound;
		c.o("");
		c.o("");
		c.o("%%%%% test data:");
		while((bound = getBound.readLine()) != null) {  //getting one sentence from all sentence
			int len = Integer.parseInt(bound);
			int curI = 1;
			
			
			String prevTag = "BOS";
			String prev2Tag = "BOS";
			
			BNode root = new BNode("BOS", 0);
			Queue<BNode> processing = new LinkedList<BNode>();
			processing.add(root);
			Stack<String> testDataInfo = new Stack<String>();
			while(curI <= len) {  // getting one instance from one sentence
				if((ins = getTest.readLine()) == null) {
					break;
				}
				//c.d("current instance: "+ins);
				
				
				String[] info = ins.trim().split(" ");
				if(info.length < 3) {
					continue;
				}
				String goldLabel = info[1];
				testDataInfo.push(info[0]+" "+goldLabel);
				Map<String, Double> features = new HashMap<String,Double>();
				String feat = "";
				double val = 0.0;
				for(int i = 2; i < info.length; i++) {
					if(i % 2 == 0) {  // feat name
						feat = info[i];
					} else {  // feat val
						val = Double.parseDouble(info[i]);
						
						features.put(feat, val);
						feat = "";
						val = 0.0;
					}
				}
				//<previous tag and previous 2 tags>
				features.put("prevTwoTags="+prev2Tag+"+"+prevTag, 1.0);
				features.put("prevT="+prevTag, 1.0);
				//<previous tag and previous 2 tags>
				Map<String, Double> rating = getRating(features);
				//<beam build level>
				//Queue<BNode> tempNewLevel = new LinkedList<BNode>();
				//BNode prev = processing.peek();
				//BNode cur = processing.remove();
				//<add all nodes>
				Queue<Double> logProbs = new PriorityQueue<Double>();
				while(!processing.isEmpty()) {
					BNode cur = processing.remove();
					for(String label : rating.keySet()) {
						BNode newNode = new BNode(label, cur.level+1);
						newNode.logProb = cur.logProb + c.log(rating.get(label), Math.E);
						newNode.parent = cur;
						//tempNewLevel.add(newNode);
						logProbs.add(newNode.logProb);  // use log prob instead of prob
						if(logProbs.size() > topK) {
							logProbs.remove();
						}
						processing.add(newNode);
					}
					
					//BNode prev = cur;
					if(cur.level != processing.peek().level) {
						break;
					}
					//cur = processing.remove();
				}
				//<add all nodes>
				//<get max min prob>
				double minLogProb = 0.0;
				double maxLogProb = 0.0;
				int logProbsSize = logProbs.size();
				while(!logProbs.isEmpty()) {
					/*
					if(logProbs.size() == logProbsSize) {
						minLogProb = logProbs.remove();
					} else if(logProbs.size() == 1) {
						maxLogProb = logProbs.remove();
					} else {
						logProbs.remove();
					}
					*/
					int sizeBefore = logProbs.size();
					double curProb = logProbs.remove();
					if(sizeBefore == 1) {
						maxLogProb = curProb;
					}
					if(sizeBefore == logProbsSize) {
						minLogProb = curProb;
					}
				}
				//</get max min prob>
				//<delete meaningless nodes>
				Iterator<BNode> itr = processing.iterator();
				
				while(itr.hasNext()) {
					BNode cur = itr.next();
					if(!(cur.logProb >= minLogProb && cur.logProb + beamSize >= maxLogProb)) {
						cur.parent = null;
						itr.remove();
					}
				}
				//</delete meaningless nodes>
				//</beam build level>
				
				prev2Tag = prevTag;
				prevTag = goldLabel;
				curI++;
			}
			//<output the beam>
			//<debug>
			/*
			if(processing.isEmpty()) {
				System.err.println("processing is empty1");
				System.exit(0);
			}
			*/
			//</debug>
			double maxLogProb = Double.NEGATIVE_INFINITY;
			BNode best = null;
			Iterator<BNode> itr = processing.iterator();
			while(itr.hasNext()) {
				BNode cur = itr.next();
				if(cur.logProb > maxLogProb) {
					best = cur;
					maxLogProb = cur.logProb;
				}
			}
			
			Stack<String> reverse = new Stack<String>();
			while(best.parent != null) {
				totalIns++;
				String curTestData = testDataInfo.pop();
				double curProb = Math.pow(Math.E, best.logProb - best.parent.logProb);
				reverse.push(curTestData+" "+best.tag+" "+curProb);
				if(curTestData.split(" ")[1].equals(best.tag)) {
					rightIns++;
				}
				best = best.parent;
			}
			while(!reverse.isEmpty()) {
				c.o(reverse.pop());
			}
			//</output the beam>
		}
		
		
		
		
		
		getTest.close();
		getBound.close();
		//<processing>
		//c.d("end time: "+c.getCurTime());
		c.d("accuracy: "+((double) rightIns/totalIns));
	}
	
	
	
	/**
	 * build the ML model using the file referred by "model"
	 *  for each instance, only give rates to topN classes 
	 * @throws IOException 
	 */
	private static void buildMLModel() throws IOException {
		//TODO: use model file to build the model
		me = new MaxEnt(model);
	}
	
	/**
	 * 
	 * @param features: the features of a feature vector, map from feature name to feature value
	 * @return: return the topN classes with rating
	 */
	private static Map<String, Double> getRating(Map<String, Double> features) {
		//TODO: use haotian's maxent
		return me.beamsearch_predict(topN, features);
	}
	
	
}
