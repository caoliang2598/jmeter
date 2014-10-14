package sampler;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.ByteString;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import com.spotify.hermes.Hermes;
import com.spotify.hermes.ext.userinfo.UserInfoBuilder;
import com.spotify.hermes.message.Message;
import com.spotify.hermes.message.MessageBuilder;
import com.spotify.hermes.message.MessageBuilderFactory;
import com.spotify.hermes.message.ProtocolVersion;
import com.spotify.hermes.service.Client;
import static com.spotify.hermes.message.Command.REQUEST;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.spotify.hermes.Hermes.clientConfig;

public class HermesSampler  extends AbstractSampler implements Interruptible, ThreadListener,TestStateListener {
  public static final Logger log = LoggingManager.getLoggerForClass();
  public static final String VERSION="HermesSampler.version";
  public static final String ENDPOINT="HermesSampler.endpoint";
  public static final String USERNAME="HermesSampler.username";
  public static final String USERID="HermesSampler.userid";
  public static final String COUNTRY="HermesSampler.country";
  public static final String EMPLOYEE="HermesSampler.employee";
  public static final String SITE="HermesSampler.site";
  public static final String REQ_TIMEOUT="HermesSampler.reqtimeout";
  public static final String RES_TIMEOUT="HermesSampler.restiemout";
  public static final String PORT="HermesSampler.port";
  public static final String ENCODING = "HermesSampler.encoding";
  public static final String URI = "HermesSampler.uri";
  public static final String METHOD = "HermesSampler.method";
  public static final String ARGUMENTS = "HermesSampler.args";
  public static final String CATALOG = "HermesSampler.catalog";
  public static Client client=null;
  private SampleResult res;
  public static ConcurrentHashMap<String,Client> threadClientList=new ConcurrentHashMap<String,Client>();
  private String service;
  private java.net.URI parseUri;
  private String endpoint;
  private boolean firstSample=false;

  public HermesSampler() {

  }

  @Override
  public void threadStarted() {
    log.debug("Thread Started"); //$NON-NLS-1$
    log.debug(this.getPropertyAsString(URI));
    log.debug(this.getPropertyAsString(VERSION));
    log.debug(this.getPropertyAsString(USERID));
    log.debug(this.getPropertyAsString(USERNAME));
    log.debug(this.getPropertyAsString(COUNTRY));
    log.debug(this.getPropertyAsString(SITE));
    log.debug(this.getPropertyAsString(ENCODING));
    log.debug(this.getPropertyAsString(METHOD));
    log.debug(this.getPropertyAsString(CATALOG));
    log.debug(this.getPropertyAsString(PORT));
    firstSample = true;
  }

  @Override
  public void threadFinished() {
    log.debug("Thread finished"); //$NON-NLS-1$
    firstSample = false;
    teardownTest();
  }

  /**
   * Versions of JMeter after 2.3.2 invoke this method when the thread starts.
   */
  void setupTest() throws URISyntaxException {
    log.info("setup test for" + this.getThreadName());
    parseUri = new URI(this.getPropertyAsString(URI));
    service = parseUri.getAuthority();
    endpoint = "srv://" + service + "." + this.getPropertyAsString(SITE);
    threadClientList.putIfAbsent(service,Hermes.newClient(clientConfig(),endpoint));
  }

  /**
   * Versions of JMeter after 2.3.2 invoke this method when the thread ends.
   */
  void teardownTest(){
      log.info("close the client");
      Client client = threadClientList.get(this.getThreadName());
      client.close();
      threadClientList.remove(this.getThreadName());
  }

  @Override
  public SampleResult sample(Entry e) {

    if(firstSample) {
      try {
        setupTest();
      } catch (URISyntaxException e1) {
        e1.printStackTrace();
      }
    }
    log.info("make the hermes call");
    res = new SampleResult ();
    final MessageBuilder messageBuilder;
    final Message message;
    final UserInfoBuilder userInfoBuilder;
      messageBuilder = MessageBuilderFactory.newBuilder(parseUri,REQUEST,ProtocolVersion.valueOf(this.getPropertyAsString(VERSION)))
          .setMethod(this.getPropertyAsString(METHOD))
          .setTtlMillis(Long.parseLong(this.getPropertyAsString(RES_TIMEOUT)));
      userInfoBuilder = UserInfoBuilder.newInstance();
      userInfoBuilder.setCatalogueString(this.getPropertyAsString(CATALOG));
      userInfoBuilder.setUserId(this.getPropertyAsString(USERID));
      userInfoBuilder.setUsername(this.getPropertyAsString(USERNAME));
      userInfoBuilder.setCountry(this.getPropertyAsString(COUNTRY));
      userInfoBuilder.setEmployeeFlag(Boolean.parseBoolean(this.getPropertyAsString(EMPLOYEE)));
      messageBuilder.addExtension(userInfoBuilder.build());
      message=messageBuilder.build();
      res.setUserName(this.getPropertyAsString(USERNAME));
      res.setUri(this.getPropertyAsString(URI));
      res.setUserId(this.getPropertyAsString(USERID));
      res.setCountry(this.getPropertyAsString(COUNTRY));
      res.setCatalog(this.getPropertyAsString(CATALOG));
      res.setSite(this.getPropertyAsString(SITE));
      res.sampleStart();
      ListenableFuture<Message> msg = threadClientList.get(this.getThreadName()).send(message);
      Message respond = null;
    try {
      respond = Uninterruptibles.getUninterruptibly(msg);
      //respond = msg.get(this.getPropertyAsInt(RES_TIMEOUT), TimeUnit.MILLISECONDS);
      res.latencyEnd();
    } catch (ExecutionException e1) {
      e1.printStackTrace();
      res.setSuccessful(false);
      res.latencyEnd();
      e1.printStackTrace();
      return res;
    }
    res.setContentType(respond.getContentType());
    List<ByteString> payloads = respond.getPayloads();
    int payloadSize=0;
    String paylaod="";
    for (ByteString frame : payloads) {
      log.info("payload");
      paylaod+=frame.toStringUtf8();
      payloadSize+=frame.size();
    }
    res.setResponseMessage(paylaod);
    res.setBytes(paylaod.length());
    res.setHeadersSize(paylaod.length());
    res.setBodySize(paylaod.length());
    res.setResponseCode(respond.getStatusCodeInteger().toString());
    res.setSuccessful(true);
    res.sampleEnd();
    return res;
}

  @Override
  public boolean interrupt() {
    //teardownTest();
    return true;
  }


  @Override
  public void testStarted() {
    log.info("test started");
  }

  @Override
  public void testStarted(String host) {

  }

  @Override
  public void testEnded() {
    log.info("test ended");
  }

  @Override
  public void testEnded(String host) {

  }
}
