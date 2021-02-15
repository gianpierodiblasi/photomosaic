package it.unict.dmi.photomosaic;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class PhotomosaicFrame extends JFrame {

  private static final long serialVersionUID = 1L;
  private PhotomosaicPanel photomosaicPanel = new PhotomosaicPanel();

  @SuppressWarnings("CallToPrintStackTrace")
  public PhotomosaicFrame() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setTitle("Photomosaic Creator");
    this.setSize(new Dimension(800, 700));
    this.getContentPane().add(photomosaicPanel, BorderLayout.CENTER);
  }

  //MAIN
  public static void main(String[] a) {
    try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
    }
    PhotomosaicFrame f = new PhotomosaicFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(true);
    f.setExtendedState(JFrame.MAXIMIZED_BOTH);
  }
  //END MAIN
}
