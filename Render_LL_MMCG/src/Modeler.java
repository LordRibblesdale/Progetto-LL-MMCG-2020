import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Modeler extends JDialog {
  class ImagePanel extends JPanel {
    ImagePanel() {
      super(null);

      repaint();  //TODO ?
    }

    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);

      for (int i = 0; i < RenderAction.w; i++) {
        for (int j = 0; j < RenderAction.h; j++) {
          g.setColor(pixelMatrix[i][j].toColor());
          g.drawLine(i, j, i, j);
        }
      }
    }
  }


  BufferEngine buffer;
  Point3D[][] pixelMatrix;

  JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
  JButton addSphere = new JButton("Aggiung sfere");
  JButton doRender = new JButton("Avvia Render");

  ImagePanel imagePanel = new ImagePanel();

  Modeler(JFrame frame) {
    super(frame, true);

    RenderAction.createApproxObjects();

    buffer = new BufferEngine(RenderAction.w, RenderAction.h, (int) RenderAction.distfilm);
    pixelMatrix = buffer.getPixelMatrix();

    buttonsPanel.add(addSphere);
    buttonsPanel.add(doRender);

    add(buttonsPanel, BorderLayout.PAGE_END);

    add(imagePanel);

    this.addKeyListener(new KeyListener() {
      Point3D v;
      /* le prossime istruzioni permettono di spostare il
      punto di visuale tramite i tasti di freccia e
      backspace/delete
      */

      @Override
      public void keyPressed(KeyEvent e) {
        v = new Point3D(0, 0, 0);
        if(e.getKeyCode() == KeyEvent.VK_UP) {
          v = new Point3D(0,-10,0);
        }

        if(e.getKeyCode() == KeyEvent.VK_DOWN) {
          v = new Point3D(0,10,0);
        }

        if(e.getKeyCode() == KeyEvent.VK_LEFT) {
          v = new Point3D(-10,0,0);
        }

        if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
          v = new Point3D(10,0,0);
        }

        if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
          v = new Point3D(0,0,-10);
        }

        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
          v = new Point3D(0,0,10);
        }

        RenderAction.cam.eye.add(v);

        Modeler.this.repaint();
      }

      @Override
      public void keyTyped(KeyEvent e) {}

      @Override
      public void keyReleased(KeyEvent e){}
    });


    //setPreferredSize(new Dimension(1080, 720));
    pack();
    //setResizable(false);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setVisible(true);
  }
}