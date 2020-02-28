package ui;

import primitive.*;
import renderer.JacobiStocClass;
import renderer.Utilities;
import renderer.Renderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

import static primitive.ModelerProperties.PREVIEW_ONLY;
import static primitive.ModelerProperties.START_RENDERING;
import static ui.RenderAction.*;

public class Preview {
  public Preview(boolean isModeler) {
    // Interfaccia per mostrare l'immagine effettiva su schermo,
    boolean[] isRunning = {false};

    JDialog frame = new JDialog(InterfaceInitialiser.mainFrame, "Anteprima", true);
    frame.setMinimumSize(new Dimension(width, height));
    frame.setLocationRelativeTo(InterfaceInitialiser.editPanel);
    frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    JPanel imagePanel = new JPanel(null) {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int i = 0; i < height; i++) {
          for (int j = 0; j < width; j++) {
            g.setColor(image[j + i* width].toColor());
            g.drawLine(j, i, j, i);
          }
        }
      }
    };

    if (isModeler) {
      JButton changePositionButton = new JButton("Cambia posizioni");
      changePositionButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          JDialog dialog = new JDialog(frame, "Cambia posizione", true);
          dialog.setMinimumSize(new Dimension(400, 500));
          dialog.setLocationRelativeTo(frame);
          dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
          JList<Obj> list = new JList<>(getEditableObjects());
          list.setModel(new DefaultListModel<>());

          Obj[] o = getEditableObjects();
          for (int i = 0; i < o.length; i++) {
            ((DefaultListModel<Obj>) list.getModel()).add(i, o[i]);
          }

          list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              super.mouseClicked(e);

              if (e.getClickCount() == 2) {
                JDialog properties = new JDialog(dialog, list.getSelectedValue().toString(), true);
                properties.setLayout(new GridLayout(0, 1));
                JPanel panel = new JPanel();
                JSlider xSlider = new JSlider(-100, 100);
                xSlider.createStandardLabels(1);
                JSlider ySlider = new JSlider(-100, 100);
                ySlider.createStandardLabels(1);
                JSlider zSlider = new JSlider(-100, 100);
                JSlider rotatePhiSlider = new JSlider(0, 100, 0);
                JCheckBox[] checkBoxes = new JCheckBox[3];
                JButton abortButton = new JButton("Annulla");
                JButton doneButton = new JButton("Accetta modifiche");

                doneButton.addActionListener(new ActionListener() {
                  @Override
                  public void actionPerformed(ActionEvent e) {
                    Point3D direction = new Point3D(
                        xSlider.getValue()/ (double) 10,
                        ySlider.getValue()/ (double) 10,
                        zSlider.getValue()/ (double) 10);

                    for (Obj globalObject : globalObjects) {
                      if (globalObject.equals(list.getSelectedValue())) {
                        globalObject.setNewPosition(direction);

                        if (globalObject.t != null) {
                          Point3D axis = (new Point3D(
                              checkBoxes[0].isSelected() ? 1 : 0,
                              checkBoxes[1].isSelected() ? 1 : 0,
                              checkBoxes[2].isSelected() ? 1 : 0))
                              .getNormalizedPoint();
                          globalObject.rotateTriangleOnly(
                              axis,
                              (rotatePhiSlider.getValue()*(2* Utilities.MATH_PI))/ (double) 100);
                        }
                      }
                    }

                    RenderAction.initialiseMeshes(PREVIEW_ONLY);
                    JacobiStocClass.jacobiStoc(objects.size());
                    Renderer.calculateThreadedRadiance(cam);

                    frame.repaint();
                    properties.dispose();

                    ((DefaultListModel<Obj>) list.getModel()).removeAllElements();

                    Obj[] o = RenderAction.getEditableObjects();

                    for (int i = 0; i < o.length; i++) {
                      ((DefaultListModel<Obj>) list.getModel()).add(i, o[i]);
                    }
                  }
                });

                panel.add(new JLabel("Posizione su X: "));
                panel.add(xSlider);
                properties.add(panel);

                panel = new JPanel();
                panel.add(new JLabel("Posizione su Y"));
                panel.add(ySlider);
                properties.add(panel);

                panel = new JPanel();
                panel.add(new JLabel("Posizione su Z"));
                panel.add(zSlider);
                properties.add(panel);

                if (list.getSelectedValue().t != null) {
                  panel = new JPanel();

                  for (int i = 0; i < checkBoxes.length; i++) {
                    String axis = i == 0 ? "X" : i == 1 ? "Y" : "Z";
                    checkBoxes[i] = new JCheckBox("Asse " + axis);
                    panel.add(checkBoxes[i]);
                  }
                  properties.add(panel);

                  panel = new JPanel();
                  panel.add(new JLabel("Rotazione sulla latitudine"));

                  Hashtable<Integer, JLabel> table = new Hashtable<>();
                  table.put(0, new JLabel("0"));
                  table.put(25, new JLabel("PI/2"));
                  table.put(50, new JLabel("PI"));
                  table.put(75, new JLabel("3PI/2"));
                  table.put(100, new JLabel("2PI"));
                  rotatePhiSlider.setLabelTable(table);
                  rotatePhiSlider.setPaintLabels(true);

                  panel.add(rotatePhiSlider);

                  properties.add(panel);
                }

                panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                panel.add(abortButton);
                panel.add(doneButton);
                properties.add(panel);

                properties.setMinimumSize(new Dimension(300, 400));
                properties.setLocationRelativeTo(dialog);
                properties.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                properties.setVisible(true);
              }
            }
          });

          dialog.add(list);

          dialog.setVisible(true);
        }
      });

      JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      JButton insert = new JButton("Aggiungi sfere");
      insert.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          new Modeler(frame);
        }
      });

      JButton button = new JButton("Avvia render");
      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          isRunning[0] = true;

          frame.dispose();

          new Thread(new Runnable() {
            @Override
            public void run() {
              new RenderAction(START_RENDERING);
            }
          }).start();
        }
      });

      bottomPanel.add(changePositionButton);
      bottomPanel.add(insert);
      bottomPanel.add(button);
      frame.add(bottomPanel, BorderLayout.PAGE_END);
    }

    frame.add(imagePanel);

    frame.setVisible(true);

    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        super.windowClosed(e);

        if (isRunning[0]) {
          frame.dispose();
        } else {
          System.exit(0);
        }
      }
    });
  }
}
