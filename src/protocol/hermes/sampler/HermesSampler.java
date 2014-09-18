package sampler;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.spotify.hermes.tools.Hurl;

public class HermesSampler  extends AbstractSampler implements Interruptible{
  private static final Logger log = LoggingManager.getLoggerForClass();
  @Override
  public SampleResult sample(Entry e) {
    log.info("make the hermes call");
    Hurl.main(new String[]{
         
      "--canonical","-username","rafiki_free",
      "--userid", "909", "--country", "SE",
      "--catalogue", "premium", "-s", "lon", 
      "hm://artist/v1/00FQb4jTyendYWaN8pK0wa/desktop?format=json"
    });
    return null;    
}

  @Override
  public boolean interrupt() {
    // TODO Auto-generated method stub
    return false;
  }

}
