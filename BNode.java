public class BNode {
	public BNode parent;
	public String tag;
	public int level;
	public double logProb;
	
	public BNode(String tag, int level) {
		this.tag = tag;
		this.level = level;
		logProb = 0;
	}
	
	public BNode(String tag, int level, double prob) {
		this(tag, level);
		this.logProb = c.log(prob, Math.E);  //is the E right?
	}
}
