import java.io.PrintStream;
import java.io.IOException;

public class maxent_classify {
   
   public static void main(String[] args) throws IOException {
      
      String test_path = args[0];
      String model_path = args[1];
      PrintStream res = new PrintStream(args[2]);
      
      MaxEnt me = new MaxEnt(model_path);
      me.predict(test_path, res);
      me.report();
   }
   
}