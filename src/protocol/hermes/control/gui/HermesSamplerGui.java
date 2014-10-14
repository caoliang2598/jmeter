package control.gui;

import java.awt.*;


import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import config.gui.HermesConfigGui;
import sampler.HermesSampler;

public class HermesSamplerGui extends AbstractSamplerGui{
  private HermesConfigGui hermesConfigGui;


  public HermesSamplerGui() {
    super();
    init();
  }

  @Override
  public String getLabelResource() {
    // TODO Auto-generated method stub
    return "hermes_sampler_label";
  }

  @Override
  public TestElement createTestElement() {
    HermesSampler sampler= new HermesSampler();
    //sampler.addTestElement(hermesConfigGui.createTestElement());
    modifyTestElement(sampler);
    return sampler;
  }

  @Override
  public void modifyTestElement(TestElement element) {
    element.clear();
    hermesConfigGui.modifyTestElement(element);
    this.configureTestElement(element);
  }
  
  private void init() {
    setLayout(new BorderLayout(0, 5));
    setBorder(makeBorder());
    add(makeTitlePanel(), BorderLayout.NORTH);
    VerticalPanel mainPanel = new VerticalPanel();
    mainPanel.add(hermesConfigGui=new HermesConfigGui(false));
    add(mainPanel, BorderLayout.CENTER);
}

}
