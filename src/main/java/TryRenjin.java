import javax.script.*;
import org.renjin.script.*;

// ... add additional imports here ...

public class TryRenjin {
  public static void main(String[] args) throws Exception {
    // create a script engine manager:
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    // create a Renjin engine:
    ScriptEngine engine = factory.getScriptEngine();

    engine.eval("df <- data.frame(x=1:20, y=(1:20)+rnorm(n=10))");
    engine.eval("print(df)");
    engine.eval("print(lm(y ~ x, df))");
  }
}