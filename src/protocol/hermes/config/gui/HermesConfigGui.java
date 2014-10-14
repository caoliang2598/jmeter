package config.gui;

import java.awt.BorderLayout;

import javax.swing.JTextField;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;

import sampler.HermesSampler;

public class HermesConfigGui extends AbstractConfigGui{
  private HermesHeaderPanel header;
  private HermesRequestPanel request;


  /**
   * Boolean indicating whether or not this component should display its name.
   * If true, this is a standalone component. If false, this component is
   * intended to be used as a subpanel for another component.
   */
  private boolean displayName = false;




  public HermesConfigGui(){
    this(true);
  }

  public HermesConfigGui(boolean displayName) {
    super();
    this.displayName=displayName;
    init();
  }

  @Override
  public TestElement createTestElement() {
    ConfigTestElement configTestElement = new ConfigTestElement();
    modifyTestElement(configTestElement);
    return configTestElement;
  }

  @Override
  public String getLabelResource() {
    return "hermes_config_label";
  }

  @Override
  public void modifyTestElement(TestElement element) {
    header.modifyTestElement(element);
    request.modifyTestElement(element);
  }

  
  /**
   * Initialize the components and layout of this component.
   */
  private void init() {
      setLayout(new BorderLayout(0, 5));

      if (displayName) {
          setBorder(makeBorder());
          add(makeTitlePanel(), BorderLayout.NORTH);
      }


    setLayout(new BorderLayout(0, 5));
    setBorder(makeBorder());


    VerticalPanel mainPanel = new VerticalPanel();
    mainPanel.add(header=new HermesHeaderPanel());
    mainPanel.add(request=new HermesRequestPanel());

    add(mainPanel, BorderLayout.CENTER);
  }




}
