package config.gui;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sampler.HermesSampler;

/**
 * Created by caoliang2598 on 03/10/14.
 */
public class HermesRequestPanel extends JPanel implements ChangeListener {
  private static final long serialVersionUID = 240L;

  private static final int TAB_PARAMETERS = 0;

  private static final int TAB_RAW_BODY = 1;

  private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);

  private HTTPArgumentsPanel argsPanel;

  private JTextField site;

  private JTextField port;

  private JTextField connectTimeOut;

  private JTextField responseTimeOut;

  private JTextField contentEncoding;

  private JTextField uri;

  private JLabeledChoice method;

  private JLabeledChoice hermesVersion;



  // Body data
  private JSyntaxTextArea postBodyContent;

  // Tabbed pane that contains parameters and raw body
  private ValidationTabbedPane postContentTabbedPane;
  private String[] versions = {"V1", "V2"};

  /**
   * constructor
   */
  public HermesRequestPanel() {
    init();
  }

  public void clear() {
    site.setText(""); // $NON-NLS-1$
    method.setText(HTTPSamplerBase.DEFAULT_METHOD);

    hermesVersion.setText(""); // $NON-NLS-1$
    uri.setText(""); // $NON-NLS-1$
    port.setText(""); // $NON-NLS-1$
    connectTimeOut.setText(""); // $NON-NLS-1$
    responseTimeOut.setText(""); // $NON-NLS-1$
    contentEncoding.setText(""); // $NON-NLS-1$
    argsPanel.clear();
    postBodyContent.setInitialText("");// $NON-NLS-1$
    postContentTabbedPane.setSelectedIndex(TAB_PARAMETERS, false);
  }

  public TestElement createTestElement() {
    ConfigTestElement element = new ConfigTestElement();

    element.setName(this.getName());
    element.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
    element.setProperty(TestElement.TEST_CLASS, element.getClass().getName());
    modifyTestElement(element);
    return element;
  }

  /**
   * Save the GUI values in the sampler.
   *
   * @param element
   */
  public void modifyTestElement(TestElement element) {
    boolean useRaw = postContentTabbedPane.getSelectedIndex()==TAB_RAW_BODY;
    Arguments args;
    if(useRaw) {
      args = new Arguments();
      String text = postBodyContent.getText();
            /*
             * Textfield uses \n (LF) to delimit lines; we need to send CRLF.
             * Rather than change the way that arguments are processed by the
             * samplers for raw data, it is easier to fix the data.
             * On retrival, CRLF is converted back to LF for storage in the text field.
             * See
             */
      HTTPArgument arg = new HTTPArgument("", text.replaceAll("\n","\r\n"), false);
      arg.setAlwaysEncoded(false);
      args.addArgument(arg);
    } else {
      args = (Arguments) argsPanel.createTestElement();
      HTTPArgument.convertArgumentsToHTTP(args);
    }
    //element.setProperty(HTTPSamplerBase.POST_BODY_RAW, useRaw, HermesSampler.ARGUMENTS, args));
    element.setProperty(HermesSampler.SITE, site.getText());
    element.setProperty(HermesSampler.PORT, port.getText());
    element.setProperty(HermesSampler.REQ_TIMEOUT, connectTimeOut.getText());
    element.setProperty(HermesSampler.RES_TIMEOUT, responseTimeOut.getText());
    element.setProperty(HermesSampler.ENCODING, contentEncoding.getText());
    element.setProperty(HermesSampler.URI, uri.getText());

    element.setProperty(HermesSampler.METHOD, method.getText());
    element.setProperty(HermesSampler.VERSION, hermesVersion.getText(),"");
  }

  // FIXME FACTOR WITH HTTPHC4Impl, HTTPHC3Impl
  // Just append all the parameter values, and use that as the post body
  /**
   * Compute body data from arguments
   * @param arguments {@link Arguments}
   * @return {@link String}
   */
  private static final String computePostBody(Arguments arguments) {
    return computePostBody(arguments, false);
  }

  /**
   * Compute body data from arguments
   * @param arguments {@link Arguments}
   * @param crlfToLF whether to convert CRLF to LF
   * @return {@link String}
   */
  private static final String computePostBody(Arguments arguments, boolean crlfToLF) {
    StringBuilder postBody = new StringBuilder();
    PropertyIterator args = arguments.iterator();
    while (args.hasNext()) {
      HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
      String value = arg.getValue();
      if (crlfToLF) {
        value=value.replaceAll("\r\n", "\n"); // See modifyTestElement
      }
      postBody.append(value);
    }
    return postBody.toString();
  }

  /**
   * Set the text, etc. in the UI.
   *
   * @param el
   *            contains the data to be displayed
   */
  public void configure(TestElement el) {
    setName(el.getName());
    Arguments arguments = (Arguments) el.getProperty(HermesSampler.ARGUMENTS).getObjectValue();

    boolean useRaw = el.getPropertyAsBoolean(HTTPSamplerBase.POST_BODY_RAW, HTTPSamplerBase.POST_BODY_RAW_DEFAULT);
    if(useRaw) {
      String postBody = computePostBody(arguments, true); // Convert CRLF to CR, see modifyTestElement
      postBodyContent.setInitialText(postBody);
      postBodyContent.setCaretPosition(0);
      postContentTabbedPane.setSelectedIndex(TAB_RAW_BODY, false);
    } else {
      argsPanel.configure(arguments);
      postContentTabbedPane.setSelectedIndex(TAB_PARAMETERS, false);
    }

    site.setText(el.getPropertyAsString(HermesSampler.SITE));

    String portString = el.getPropertyAsString(HermesSampler.PORT);


    port.setText(portString);

    connectTimeOut.setText(el.getPropertyAsString(HermesSampler.REQ_TIMEOUT));
    responseTimeOut.setText(el.getPropertyAsString(HermesSampler.RES_TIMEOUT));
    contentEncoding.setText(el.getPropertyAsString(HermesSampler.ENCODING));
    uri.setText(el.getPropertyAsString(HermesSampler.URI));

    method.setText(el.getPropertyAsString(HermesSampler.METHOD));

    hermesVersion.setText(el.getPropertyAsString(HermesSampler.VERSION));
  }

  private void init() {// called from ctor, so must not be overridable
    this.setLayout(new BorderLayout());

    // WEB REQUEST PANEL
    JPanel hermesRequestPanel = new JPanel();
    hermesRequestPanel.setLayout(new BorderLayout());
    hermesRequestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                               JMeterUtils.getResString("hermes_request"))); //
                                                               // $NON-NLS-1$

    JPanel northPanel = new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
    northPanel.add(getProtocolAndMethodPanel());
    northPanel.add(getPathPanel());

    hermesRequestPanel.add(northPanel, BorderLayout.NORTH);
    hermesRequestPanel.add(getParameterPanel(), BorderLayout.CENTER);

    this.add(getWebServerTimeoutPanel(), BorderLayout.NORTH);
    this.add(hermesRequestPanel, BorderLayout.CENTER);
  }

  /**
   * Create a panel containing the webserver (site+port) and timeouts (connect+request).
   *
   * @return the panel
   */
  protected final JPanel getWebServerTimeoutPanel() {
    // WEB SERVER PANEL
    JPanel webServerPanel = new HorizontalPanel();
    webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                              JMeterUtils.getResString("hermes_server"))); //
                                                              // $NON-NLS-1$
    final JPanel domainPanel = getDomainPanel();
    final JPanel portPanel = getPortPanel();
    webServerPanel.add(domainPanel, BorderLayout.CENTER);
    webServerPanel.add(portPanel, BorderLayout.EAST);

    JPanel timeOut = new HorizontalPanel();
    timeOut.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                       JMeterUtils.getResString("web_server_timeout_title"))); // $NON-NLS-1$
    final JPanel connPanel = getConnectTimeOutPanel();
    final JPanel reqPanel = getResponseTimeOutPanel();
    timeOut.add(connPanel);
    timeOut.add(reqPanel);

    JPanel webServerTimeoutPanel = new VerticalPanel();
    webServerTimeoutPanel.add(webServerPanel, BorderLayout.CENTER);
    webServerTimeoutPanel.add(timeOut, BorderLayout.EAST);

    JPanel bigPanel = new VerticalPanel();
    bigPanel.add(webServerTimeoutPanel);
    return bigPanel;
  }


  private JPanel getPortPanel() {
    port = new JTextField(10);

    JLabel label = new JLabel(JMeterUtils.getResString("web_server_port")); // $NON-NLS-1$
    label.setLabelFor(port);

    JPanel panel = new JPanel(new BorderLayout(5, 0));
    panel.add(label, BorderLayout.WEST);
    panel.add(port, BorderLayout.CENTER);

    return panel;
  }



  private JPanel getConnectTimeOutPanel() {
    connectTimeOut = new JTextField(10);

    JLabel label = new JLabel(JMeterUtils.getResString("web_server_timeout_connect")); // $NON-NLS-1$
    label.setLabelFor(connectTimeOut);

    JPanel panel = new JPanel(new BorderLayout(5, 0));
    panel.add(label, BorderLayout.WEST);
    panel.add(connectTimeOut, BorderLayout.CENTER);

    return panel;
  }

  private JPanel getResponseTimeOutPanel() {
    responseTimeOut = new JTextField(10);

    JLabel label = new JLabel(JMeterUtils.getResString("web_server_timeout_response")); // $NON-NLS-1$
    label.setLabelFor(responseTimeOut);

    JPanel panel = new JPanel(new BorderLayout(5, 0));
    panel.add(label, BorderLayout.WEST);
    panel.add(responseTimeOut, BorderLayout.CENTER);

    return panel;
  }

  private JPanel getDomainPanel() {
    site = new JTextField(20);

    JLabel label = new JLabel(JMeterUtils.getResString("hermes_site")); // $NON-NLS-1$
    label.setLabelFor(site);

    JPanel panel = new JPanel(new BorderLayout(5, 0));
    panel.add(label, BorderLayout.WEST);
    panel.add(site, BorderLayout.CENTER);
    return panel;
  }



  /**
   * This method defines the Panel for the HTTP uri, 'Follow Redirects'
   * 'Use KeepAlive', and 'Use multipart for HTTP POST' elements.
   *
   * @return JPanel The Panel for the uri, 'Follow Redirects' and 'Use
   *         KeepAlive' elements.
   */
  protected Component getPathPanel() {
    uri = new JTextField(15);

    JLabel label = new JLabel(JMeterUtils.getResString("hermes_uri")); //$NON-NLS-1$
    label.setLabelFor(uri);


    JPanel pathPanel = new HorizontalPanel();
    pathPanel.add(label);
    pathPanel.add(uri);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(pathPanel);
      JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      optionPanel.setFont(FONT_SMALL); // all sub-components with setFont(null) inherit this font
      optionPanel.setMinimumSize(optionPanel.getPreferredSize());
      panel.add(optionPanel);

    return panel;
  }

  protected JPanel getProtocolAndMethodPanel() {

    hermesVersion = new JLabeledChoice(JMeterUtils.getResString("hermes_version"), // $NON-NLS-1$
                                       versions);

    // CONTENT_ENCODING
    contentEncoding = new JTextField(10);
    JLabel contentEncodingLabel = new JLabel(JMeterUtils.getResString("content_encoding")); // $NON-NLS-1$
    contentEncodingLabel.setLabelFor(contentEncoding);

    method = new JLabeledChoice(JMeterUtils.getResString("method"), // $NON-NLS-1$
                                  HTTPSamplerBase.getValidMethodsAsArray());

    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    panel.add(hermesVersion);
    panel.add(Box.createHorizontalStrut(5));
    panel.add(method);
    panel.setMinimumSize(panel.getPreferredSize());
    panel.add(Box.createHorizontalStrut(5));

    panel.add(contentEncodingLabel);
    panel.add(contentEncoding);
    panel.setMinimumSize(panel.getPreferredSize());
    return panel;
  }

  protected JTabbedPane getParameterPanel() {
    postContentTabbedPane = new ValidationTabbedPane();
    argsPanel = new HTTPArgumentsPanel();
    postContentTabbedPane.add(JMeterUtils.getResString("post_as_parameters"), argsPanel);// $NON-NLS-1$
    postBodyContent = new JSyntaxTextArea(30, 50);// $NON-NLS-1$
    postContentTabbedPane.add(JMeterUtils.getResString("post_body"), new JTextScrollPane(postBodyContent));// $NON-NLS-1$
    return postContentTabbedPane;
  }

  /**
   *
   */
  class ValidationTabbedPane extends JTabbedPane{

    /**
     *
     */
    private static final long serialVersionUID = 7014311238367882880L;

    /* (non-Javadoc)
     * @see javax.swing.JTabbedPane#setSelectedIndex(int)
     */
    @Override
    public void setSelectedIndex(int index) {
      setSelectedIndex(index, true);
    }
    /**
     * Apply some check rules if check is true
     */
    public void setSelectedIndex(int index, boolean check) {
      int oldSelectedIndex = getSelectedIndex();
      if(!check || oldSelectedIndex==-1) {
        super.setSelectedIndex(index);
      }
      else if(index != this.getSelectedIndex())
      {
        if(noData(getSelectedIndex())) {
          // If there is no data, then switching between Parameters and Raw should be
          // allowed with no further user interaction.
          argsPanel.clear();
          postBodyContent.setInitialText("");
          super.setSelectedIndex(index);
        }
        else {
          if(oldSelectedIndex == TAB_RAW_BODY) {
            // If RAW data and Parameters match we allow switching
            if(postBodyContent.getText().equals(computePostBody((Arguments)argsPanel.createTestElement()).trim())) {
              super.setSelectedIndex(index);
            }
            else {
              // If there is data in the Raw panel, then the user should be
              // prevented from switching (that would be easy to track).
              JOptionPane.showConfirmDialog(this,
                                            JMeterUtils.getResString("web_cannot_switch_tab"), // $NON-NLS-1$
                                            JMeterUtils.getResString("warning"), // $NON-NLS-1$
                                            JOptionPane.DEFAULT_OPTION,
                                            JOptionPane.ERROR_MESSAGE);
              return;
            }
          }
          else {
            // If the Parameter data can be converted (i.e. no names), we
            // warn the user that the Parameter data will be lost.
            if(canConvertParameters()) {
              Object[] options = {
                  JMeterUtils.getResString("confirm"), // $NON-NLS-1$
                  JMeterUtils.getResString("cancel")}; // $NON-NLS-1$
              int n = JOptionPane.showOptionDialog(this,
                                                   JMeterUtils.getResString("web_parameters_lost_message"), // $NON-NLS-1$
                                                   JMeterUtils.getResString("warning"), // $NON-NLS-1$
                                                   JOptionPane.YES_NO_CANCEL_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE,
                                                   null,
                                                   options,
                                                   options[1]);
              if(n == JOptionPane.YES_OPTION) {
                convertParametersToRaw();
                super.setSelectedIndex(index);
              }
              else{
                return;
              }
            }
            else {
              // If the Parameter data cannot be converted to Raw, then the user should be
              // prevented from doing so raise an error dialog
              JOptionPane.showConfirmDialog(this,
                                            JMeterUtils.getResString("web_cannot_convert_parameters_to_raw"), // $NON-NLS-1$
                                            JMeterUtils.getResString("warning"), // $NON-NLS-1$
                                            JOptionPane.DEFAULT_OPTION,
                                            JOptionPane.ERROR_MESSAGE);
              return;
            }
          }
        }
      }
    }
  }
  // autoRedirects and followRedirects cannot both be selected
  @Override
  public void stateChanged(ChangeEvent e) {

  }


  /**
   * Convert Parameters to Raw Body
   */
  void convertParametersToRaw() {
    postBodyContent.setInitialText(computePostBody((Arguments)argsPanel.createTestElement()));
    postBodyContent.setCaretPosition(0);
  }

  /**
   *
   * @return true if no argument has a name
   */
  boolean canConvertParameters() {
    Arguments arguments = (Arguments)argsPanel.createTestElement();
    for (int i = 0; i < arguments.getArgumentCount(); i++) {
      if(!StringUtils.isEmpty(arguments.getArgument(i).getName())) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return true if neither Parameters tab nor Raw Body tab contain data
   */
  boolean noData(int oldSelectedIndex) {
    if(oldSelectedIndex == TAB_RAW_BODY) {
      return StringUtils.isEmpty(postBodyContent.getText().trim());
    }
    else {
      Arguments element = (Arguments)argsPanel.createTestElement();
      return StringUtils.isEmpty(computePostBody(element));
    }
  }
}
