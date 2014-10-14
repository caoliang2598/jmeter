package config.gui;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;

import java.awt.*;
import java.awt.peer.ButtonPeer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sampler.HermesSampler;

/**
 * Created by caoliang2598 on 02/10/14.
 * panel which is used to config hermes header
 * - site
 * - catalog
 * - userName
 * - userId
 * - country
 */
public class HermesHeaderPanel extends JPanel implements ChangeListener {
  private static final long serialVersion=240L;
  /** Field allowing the user to enter a username. */
  private final JTextField username = new JTextField(15);

  /** Field allowing the user to enter a  catalog*/
  private final JTextField catalog= new JTextField(15);

  /** Field allowing the user to enter a  country*/
  private final JTextField country= new JTextField(15);

  /** Field allowing the user to enter a  userid*/
  private final JTextField userid= new JTextField(15);

  /** Field allowing the user to enter a  employee flag*/
  private final JTextField employee= new JTextField(15);


  @Override
  public void stateChanged(ChangeEvent e) {

  }

  public void modifyTestElement(TestElement element){
    element.setProperty(HermesSampler.USERID,userid.getText());
    element.setProperty(HermesSampler.USERNAME,username.getText());
    element.setProperty(HermesSampler.COUNTRY, country.getText());
    element.setProperty(HermesSampler.CATALOG, catalog.getText());
    element.setProperty(HermesSampler.EMPLOYEE, employee.getText());
  }

  public void configure(TestElement element) {
    username.setText(element.getPropertyAsString(HermesSampler.USERNAME));
    userid.setText(element.getPropertyAsString(HermesSampler.USERID));
    country.setText(element.getPropertyAsString(HermesSampler.COUNTRY));
    employee.setText(element.getPropertyAsString(HermesSampler.EMPLOYEE));
    catalog.setText(element.getPropertyAsString(HermesSampler.CATALOG));
  }

  public HermesHeaderPanel(){
    init();
  }

  public TestElement createTestElement(){
    ConfigTestElement configTestElement=new ConfigTestElement();
    modifyTestElement(configTestElement);
    configTestElement.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
    configTestElement.setProperty(TestElement.TEST_CLASS,configTestElement.getClass().getName());
    return configTestElement;
  }

  /**
   * Initialize the components and layout of this component.
   */
  private void init() {
    setLayout(new BorderLayout(0, 5));
    JPanel northPan=new JPanel(new GridBagLayout());
    this.setLayout(new BorderLayout());
    this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                        "HermesConfigGui User Header Configuration"));
    GridBagConstraints c = new GridBagConstraints();
    c.gridx=0;
    c.gridy=0;
    northPan.add(createUserPanel(),c);
    c = new GridBagConstraints();
    c.gridx=1;
    c.gridy=0;
    northPan.add(createUserIdPanel(),c);
    c = new GridBagConstraints();
    c.gridx=2;
    c.gridy=0;
    northPan.add(createUserPanel(),c);
    c = new GridBagConstraints();
    c.gridx=3;
    c.gridy=0;
    northPan.add(createCountryPanel());
    c = new GridBagConstraints();
    c.gridx=4;
    c.gridy=0;
    northPan.add(createCatalogPanel(), c);
    this.add(northPan, BorderLayout.CENTER);
  }

  private JPanel createUserPanel() {
    JPanel user=new JPanel();
    user.setLayout(new BorderLayout(5,0));
    JLabel userLabel = new JLabel("User Name");
    user.add(userLabel,BorderLayout.WEST);
    user.add(this.username,BorderLayout.CENTER);
    return user;
  }

  private JPanel createUserIdPanel() {
    JPanel userId=new JPanel();
    userId.setLayout(new BorderLayout(5,0));
    JLabel userIdLabel = new JLabel("User Id");
    userId.add(userIdLabel,BorderLayout.WEST);
    userId.add(this.userid,BorderLayout.CENTER);
    return userId;
  }

  private JPanel createCountryPanel() {
    JPanel countryContainer=new JPanel();
    countryContainer.setLayout(new BorderLayout(5,0));
    JLabel countryLabel = new JLabel("Country");
    countryContainer.add(countryLabel,BorderLayout.WEST);
    countryContainer.add(this.country,BorderLayout.CENTER);
    return countryContainer;
  }

  private JPanel createCatalogPanel() {
    JPanel catalogContainer=new JPanel();
    catalogContainer.setLayout(new BorderLayout(5,0));
    JLabel catalogLabel = new JLabel("Catalog");
    catalogContainer.add(catalogLabel,BorderLayout.WEST);
    catalogContainer.add(this.catalog,BorderLayout.CENTER);
    return catalogContainer;
  }

}
