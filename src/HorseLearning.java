import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class HorseLearning {
	
	static double log2(double a){
		return (Math.log(a) / Math.log(2));
	}
	
	//this is the data class which holds the tree itself
	//the training data is inputed via the constructor which then initializes the tree
	//the decision tree can be tested / used via the test method
	static private class Choice {
		boolean endPoint;
		boolean healthy;
		List<Horse> data;
		Choice left;
		Choice right;
		int attribute;
		double threshold;
		int level;
		
		Choice(List<Horse> data, int lvl){
			//want to test is all values have same health
			level = lvl;
			boolean allSame = true;
			healthy = data.get(0).healthy;
			
			for (ListIterator<Horse> iter = data.listIterator(); iter.hasNext(); ) 
			    if (iter.next().healthy != healthy)
			    	allSame = false;
			
			if (allSame){
				endPoint = true;
				
			} else {
				//we will need to make a branch in our tree
				double bestEntropy = Double.MIN_VALUE;
				
				//loop through attributes
				for (int attri = 0; attri < 16; attri++){
					//loop through thresholds
					ListIterator<Horse> iter = data.listIterator();
					double a = iter.next().attributes[attri];
					double b;
					for (; iter.hasNext(); ) {
						b = iter.next().attributes[attri];
					    double thresh = (a + b)/2;
					    a=b;
					    
					    double p = 0;
					    for (ListIterator<Horse> j = data.listIterator(); j.hasNext(); )
					    	if (j.next().attributes[attri] < thresh)
					    		p++;
					    p = p / data.size();
					    
						double entropy = -1.0 * p * log2(p) - ((1.0 - p) * log2(1.0 - p));
						
						if (entropy > bestEntropy ){
							bestEntropy = entropy;
							attribute = attri;
							threshold = thresh;
						}
					}
				}
				//have max entropy, set values
				List<Horse> leftData = new LinkedList<Horse>();
				List<Horse> rightData = new LinkedList<Horse>();
				
				for (ListIterator<Horse> j = data.listIterator(); j.hasNext(); ){
					Horse h = j.next();
			    	if (h.attributes[attribute] < threshold){
			    		leftData.add(h);
			    	} else {
			    		rightData.add(h);
			    	}
				}
				
				left = new Choice(leftData, level + 1);
				right = new Choice(rightData, level + 1);
			}
		}//constructor
		
		boolean test(Horse horse){
			if (endPoint)
				return healthy;
			if (horse.attributes[attribute] <= threshold)
				return left.test(horse);
			return right.test(horse);
		}//test
		
		void print(){
			if (endPoint){
				System.out.println("level:" + level + ", Endpoint:" + endPoint + ", Health:" + healthy);
			} else {
				System.out.println("level:" + level + ", Attri:" + (attribute+1) + ", "+ "Thresh:" + threshold);
				left.print();
				right.print();
			}
			
		}
	}
	static private class Horse{
		double[] attributes;
		boolean healthy;
		
		public Horse(String input, boolean training){
			attributes = new double[16];
			
			String[] in=input.split(",");
			for (int i = 0; i < 16; i++)
				attributes[i] = Double.parseDouble(in[i]);
			healthy = false;
			if (in[16].compareTo("healthy.") == 0)
				healthy = true;
		}
		void printHorse() {
			for (int i = 0; i < 16; i ++)
				System.out.print(attributes[i] + ", ");
			System.out.println(healthy);
		}
	}//Horse
	
	public static void main(String[] args) {
		List<Horse> trainingData = new LinkedList<Horse>();
		List<Horse> testingData = new LinkedList<Horse>();
		
		try (BufferedReader trainingFile = new BufferedReader(new FileReader("HorseTrain.txt"))) {
		    String line;
		    while ((line = trainingFile.readLine()) != null) {
		       // process the line.
		    	Horse horse = new Horse(line, false);
		    	trainingData.add(horse);
		    }
		    trainingFile.close();
		    BufferedReader testFile = new BufferedReader(new FileReader("HorseTest.txt"));
		    while ((line = testFile.readLine()) != null) {
		       // process the line.
		    	Horse horse = new Horse(line, false);
		    	testingData.add(horse);
		    }
		    testFile.close();
		} catch (FileNotFoundException e) {
			System.out.println("Training File not found");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//make decision tree
		Choice tree = new Choice(trainingData, 0);
		
		int correct = 0;
		
		for (ListIterator<Horse> j = testingData.listIterator(); j.hasNext(); ){
			Horse h = j.next();
			boolean guess = tree.test(h);
			if (guess == h.healthy) {
				//System.out.println("Correct!");
				correct++;
			} else {
				//System.out.println("Wrong >.<");
			}
		}
		System.out.println("I got " + correct + "/" + testingData.size() + " correct! (testData)");
		correct = 0;
		
		for (ListIterator<Horse> j = trainingData.listIterator(); j.hasNext(); ){
			Horse h = j.next();
			boolean guess = tree.test(h);
			if (guess == h.healthy) {
				//System.out.println("Correct!");
				correct++;
			} else {
				//System.out.println("Wrong >.<");
			}
		}
		System.out.println("I got " + correct + "/" + trainingData.size() + " correct! (trainingData)");
		
		//tree.print();
	}
}
