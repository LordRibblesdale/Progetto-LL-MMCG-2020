import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class EditPanel extends JPanel {
  private JButton renderButton;

  private JPanel bottomPanel;
  private JTabbedPane tabs = null;
  private JComboBox<String> materials;
  private JComboBox<String> positions;

  JacobiPanel jacobiPanel;
  FinalGatheringPanel finalGatheringPanel;
  PhotonPanel photonPanel;

  EditPanel() {
    super();
    setLayout(new BorderLayout());

    bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottomPanel.add(new JLabel("Materiali: "));
    bottomPanel.add(new JSeparator(SwingConstants.VERTICAL));
    bottomPanel.add(materials = new JComboBox<>(Properties.MATERIALS));
    bottomPanel.add(new JSeparator(SwingConstants.VERTICAL));
    bottomPanel.add(new JLabel("Posizione sfere: "));
    bottomPanel.add(new JSeparator(SwingConstants.VERTICAL));
    bottomPanel.add(positions = new JComboBox<>(Properties.POSITIONS));
    bottomPanel.add(new JSeparator(SwingConstants.VERTICAL));
    bottomPanel.add(renderButton = new JButton("Render!"));

    initialise();

    renderButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new Thread(new Runnable() {
          @Override
          public void run() {
            new RenderAction();
          }
        }).start();
      }
    });

    add(bottomPanel, BorderLayout.PAGE_END);
  }

  void initialise() {
    tabs = new JTabbedPane();

    tabs.add(jacobiPanel = new JacobiPanel(), "Jacobi");
    tabs.add(finalGatheringPanel = new FinalGatheringPanel(), "Final Gathering");
    tabs.add(photonPanel = new PhotonPanel(), "Photon Mapping");

    add(tabs);
  }

  int getMethod() {
    return tabs.getSelectedIndex();
  }

  String getMaterial() {
    return materials.getItemAt(materials.getSelectedIndex());
  }

  String getPosition() {
    return positions.getItemAt(materials.getSelectedIndex());
  }

  void disableUI() {
    this.setEnabled(false);

    jacobiPanel.setEnabled(false);
    finalGatheringPanel.setEnabled(false);
    photonPanel.setEnabled(false);
  }

  @Override
  public void setEnabled(boolean isEnabled) {
    super.setEnabled(isEnabled);

    materials.setEnabled(isEnabled);
    positions.setEnabled(isEnabled);
    tabs.setEnabled(isEnabled);
    renderButton.setEnabled(isEnabled);
  }
}
