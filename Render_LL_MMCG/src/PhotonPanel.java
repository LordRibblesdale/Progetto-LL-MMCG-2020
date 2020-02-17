import javax.swing.*;
import java.awt.*;

public class PhotonPanel extends JPanel {
  // Sample per la massima ricorsività del photon mapping
  private JSpinner photonNum;

  // Sample per la massima ricorsività del photon mapping
  private JSpinner causticNum;

  // Sample per la massima ricorsività del photon mapping
  private JSpinner aoCaustic;

  //TODO add here
  private JSpinner projectResolution;

  //distanza al quadrato disco di ricerca dei fotoni
  private JSpinner photonSearchDisc;

  //distanza al quadrato disco di ricerca dei fotoni nell caustiche
  private JSpinner causticSearchDisc;

  //numero di fotoni da ricercare
  private JSpinner photonSearchNum;
  private JSpinner causticSearchNum;

  PhotonPanel() {
    super(new GridLayout(0, 1));

    photonNum = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 1));
    causticNum = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 1));
    aoCaustic = new JSpinner(new SpinnerNumberModel(150, 1, 10000, 1));
    projectResolution = new JSpinner(new SpinnerNumberModel(300, 1, 10000, 1));
    photonSearchNum = new JSpinner(new SpinnerNumberModel(80, 1, 10000, 1));
    causticSearchNum = new JSpinner(new SpinnerNumberModel(1500, 1, 10000, 1));
    photonSearchDisc = new JSpinner(new SpinnerNumberModel(1000, 1, 10000, 1));
    causticSearchDisc = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 1));

    //TODO add caustic process checkbox

    JPanel panel = new JPanel();
    panel.add(new JLabel("Numero di fotoni generici"));
    panel.add(photonNum);
    add(panel);

    panel = new JPanel();
    panel.add(new JLabel("Numero di fotoni per le caustiche"));
    panel.add(causticNum);
    add(panel);

    panel = new JPanel();
    //TODO: fix text here
    panel.add(new JLabel("Numero di fotoni per le caustiche indirette"));
    panel.add(aoCaustic);
    add(panel);

    panel = new JPanel();
    panel.add(new JLabel("Risoluzione per la mappa di proiezione fotoni"));
    panel.add(projectResolution);
    add(panel);

    panel = new JPanel();
    panel.add(new JLabel("Numero di fotoni da ricercare"));
    panel.add(photonSearchNum);
    add(panel);

    panel = new JPanel();
    panel.add(new JLabel("Numero di caustiche da ricercare"));
    panel.add(causticSearchNum);
    add(panel);

    panel = new JPanel();
    panel.add(new JLabel("Grandezza del disco di ricerca fotoni"));
    panel.add(photonSearchDisc);
    add(panel);

    panel = new JPanel();
    panel.add(new JLabel("Grandezza del disco di ricerca caustiche"));
    panel.add(causticSearchDisc);
    add(panel);
  }

  int getPhotonNum() {
    return (Integer) photonNum.getValue();
  }

  int getCausticNum() {
    return (Integer) causticNum.getValue();
  }

  int getAOCaustic() {
    return (Integer) aoCaustic.getValue();
  }

  int getProjectionResolution() {
    return (Integer) projectResolution.getValue();
  }

  int getPhotonSearchNum() {
    return (Integer) photonSearchNum.getValue();
  }

  int getCausticSearchNum() {
    return (Integer) causticSearchNum.getValue();
  }

  int getPhotonSearchDisc() {
    return (Integer) photonSearchDisc.getValue();
  }

  int getCausticSearchDisc() {
    return (Integer) causticSearchDisc.getValue();
  }

  @Override
  public void setEnabled(boolean isEnabled) {
    super.setEnabled(isEnabled);

    photonNum.setEnabled(isEnabled);
    causticNum.setEnabled(isEnabled);
    aoCaustic.setEnabled(isEnabled);
    projectResolution.setEnabled(isEnabled);
    photonSearchNum.setEnabled(isEnabled);
    causticSearchNum.setEnabled(isEnabled);
    photonSearchDisc.setEnabled(isEnabled);
    causticSearchDisc.setEnabled(isEnabled);
  }
}
