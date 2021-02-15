package it.unict.dmi.photomosaic;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

public class PhotomosaicPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private JButton open = new JButton();
  private JButton openDB = new JButton();
  private JButton create = new JButton();
  private JButton save = new JButton();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private JLabel image = new JLabel();
  private JLabel photomosaic = new JLabel();
  private JFileChooser openChooser = new JFileChooser();
  private JFileChooser openChooserDB = new JFileChooser();
  private JFileChooser saveChooser = new JFileChooser();
  private JProgressBar bar = new JProgressBar();
  private JPanel jPanel4 = new JPanel();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JDialog dialog = new JDialog();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JLabel preview = new JLabel();
  private JPanel jPanel2 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private JSplitPane jSplitPane1 = new JSplitPane();
  private JScrollPane jScrollPane3 = new JScrollPane();
  private JTextArea messages = new JTextArea();
  private JPanel jPanel11 = new JPanel();
  private JPanel jPanel12 = new JPanel();
  private BorderLayout borderLayout5 = new BorderLayout();
  private GridLayout gridLayout8 = new GridLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private SettingPanel settingPanel = new SettingPanel();

  private PhotomosaicAlgorithm pa = new PhotomosaicAlgorithm();
  private int dim = 120;
  private File file;
  private boolean doAntipole = true;
  private final static boolean MAC_OS_X = System.getProperty("os.name").toLowerCase().startsWith("mac os x");

  @SuppressWarnings("CallToPrintStackTrace")
  public PhotomosaicPanel() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
    pa.setProgressBar(bar);
    pa.setTextArea(messages);
  }

  private ImageIcon createIcon(BufferedImage im) {
    BufferedImage icon = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
    int w = im.getWidth();
    int h = im.getHeight();
    double scale = Math.min((double) dim / w, (double) dim / h);
    Graphics2D g2 = icon.createGraphics();
    g2.scale(scale, scale);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2.drawImage(im, 0, 0, null);
    g2.dispose();
    return new ImageIcon(icon);
  }

  @SuppressWarnings("UnusedAssignment")
  private void open() {
    openChooser.setSelectedFile(null);
    if (openChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      try {
        FileImageInputStream fImm = new FileImageInputStream(openChooser.getSelectedFile());
        Iterator<ImageReader> iter = ImageIO.getImageReaders(fImm);
        if (iter.hasNext()) {
          ImageReader reader = iter.next();
          reader.setInput(fImm);
          int w = reader.getWidth(0);
          int h = reader.getHeight(0);
          if (w * h <= 1960000) {
            BufferedImage im = reader.read(0);
            if (PhotomosaicPanel.MAC_OS_X) {
              int[] data = new int[w * h];
              im.getRGB(0, 0, w, h, data, 0, w);
              im = null;
              System.gc();
              im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
              im.setRGB(0, 0, w, h, data, 0, w);
            }

            pa.setImage(im);
            image.setIcon(this.createIcon(im));
            image.setCursor(new Cursor(Cursor.HAND_CURSOR));
            image.setToolTipText("Click to enlarge");
            if (file != null) {
              create.setEnabled(true);
            }
          } else {
            JOptionPane.showMessageDialog(this, "It is not possible to open the file\nThe image size is greater than 1960000 pixel", "Error", JOptionPane.ERROR_MESSAGE);
          }
          reader.setInput(null);
        }
      } catch (HeadlessException | IOException ex) {
        JOptionPane.showMessageDialog(this, "It is not possible to open the file", "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void openDB() {
    if (openChooserDB.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      doAntipole = true;
      file = openChooserDB.getSelectedFile();
      if (pa.getImage() != null) {
        create.setEnabled(true);
      }
    }
  }

  private void save(BufferedImage image) {
    saveChooser.setSelectedFile(null);
    int res2 = JOptionPane.NO_OPTION;
    File f = null;
    while (res2 == JOptionPane.NO_OPTION) {
      int res = saveChooser.showSaveDialog(this);
      if (res == JFileChooser.APPROVE_OPTION) {
        f = saveChooser.getSelectedFile();
        if (f.exists()) {
          res2 = JOptionPane.showConfirmDialog(this, "The file already exists, overwrite?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        } else {
          res2 = JOptionPane.YES_OPTION;
        }
      } else {
        res2 = JOptionPane.CANCEL_OPTION;
      }
    }
    try {
      if (res2 == JOptionPane.YES_OPTION) {
        ImageIO.write(image, saveChooser.getFileFilter().toString(), f);
      }
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this, "It's not possible to save the file", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void setComponentsEnabled(boolean b) {
    open.setEnabled(b);
    openDB.setEnabled(b);
    create.setEnabled(b);
    save.setEnabled(b);
    settingPanel.setComponentsEnabled(b);
  }

  private void jbInit() throws Exception {
    openChooser.setFileFilter(new OpenImageFilter());
    openChooser.setAcceptAllFileFilterUsed(false);
    openChooser.setCurrentDirectory(new File("/Users/giampo76/images"));
    openChooserDB.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    openChooserDB.setCurrentDirectory(new File("/Users/giampo76/Temporanea/Tiles x Photomosaic"));
    saveChooser.setFileFilter(new SaveImageFilter());
    saveChooser.setAcceptAllFileFilterUsed(false);
    saveChooser.setCurrentDirectory(new File("/Users/giampo76/images"));
    open.setFocusPainted(false);
    open.setMargin(new Insets(0, 0, 0, 0));
    open.setToolTipText("Open");
    open.setIcon(new ImageIcon(ImageIO.read(PhotomosaicPanel.class.getClassLoader().getResourceAsStream("open.gif"))));
    openDB.setMargin(new Insets(0, 0, 0, 0));
    openDB.setToolTipText("Open Database");
    openDB.setIcon(new ImageIcon(ImageIO.read(PhotomosaicPanel.class.getClassLoader().getResourceAsStream("open.gif"))));
    create.setEnabled(false);
    create.setFocusPainted(false);
    create.setMargin(new Insets(0, 0, 0, 0));
    create.setToolTipText("Create");
    create.setIcon(new ImageIcon(ImageIO.read(PhotomosaicPanel.class.getClassLoader().getResourceAsStream("start.gif"))));
    save.setEnabled(false);
    save.setFocusPainted(false);
    save.setMargin(new Insets(0, 0, 0, 0));
    save.setToolTipText("Save");
    save.setIcon(new ImageIcon(ImageIO.read(PhotomosaicPanel.class.getClassLoader().getResourceAsStream("save.gif"))));
    image.setBorder(BorderFactory.createEtchedBorder());
    image.setPreferredSize(new Dimension(dim, dim));
    jPanel4.setLayout(borderLayout3);
    dialog.setSize(500, 500);
    dialog.setTitle("Photomosaic Creator");
    jPanel2.setLayout(gridLayout1);
    gridLayout1.setRows(2);
    bar.setString("");
    bar.setStringPainted(true);
    jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
    jSplitPane1.setLastDividerLocation(360);
    jSplitPane1.setOneTouchExpandable(true);
    messages.setFont(new java.awt.Font("Monospaced", 0, 12));
    messages.setEditable(false);
    jPanel11.setLayout(borderLayout5);
    jPanel12.setLayout(gridLayout8);
    this.setLayout(borderLayout1);
    jPanel12.add(open, null);
    jPanel12.add(openDB, null);
    jPanel12.add(create, null);
    jPanel12.add(save, null);
    jPanel2.add(image, null);
    jPanel4.add(jPanel2, BorderLayout.SOUTH);
    jPanel4.add(settingPanel, BorderLayout.NORTH);
    this.add(jPanel4, BorderLayout.WEST);
    this.add(jSplitPane1, BorderLayout.CENTER);
    jSplitPane1.add(jScrollPane2, JSplitPane.LEFT);
    jSplitPane1.add(jScrollPane3, JSplitPane.RIGHT);
    jScrollPane3.getViewport().add(messages, null);
    jScrollPane2.getViewport().add(photomosaic, null);
    this.add(jPanel11, BorderLayout.NORTH);
    jPanel11.add(jPanel12, BorderLayout.WEST);
    jPanel11.add(bar, BorderLayout.CENTER);
    dialog.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(preview, null);
    Listener listener = new Listener();
    open.addActionListener(listener);
    openDB.addActionListener(listener);
    create.addActionListener(listener);
    save.addActionListener(listener);
    image.addMouseListener(listener);
    jSplitPane1.setDividerLocation(340);
  }

  private class Listener extends MouseAdapter implements ActionListener {

    @Override
    public void mousePressed(MouseEvent e) {
      Image im = pa.getImage();
      if (im != null) {
        preview.setIcon(new ImageIcon(im));
        dialog.setVisible(true);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source == open) {
        open();
      } else if (source == openDB) {
        openDB();
      } else if (source == create) {
        (new Thread() {
          @Override
          public void run() {
            setComponentsEnabled(false);
            pa.evaluate(settingPanel.getTileSizeMin(), settingPanel.getTileSizeMax(), doAntipole, file, 50, settingPanel.getMinDistance_Variance(), settingPanel.getType());
            photomosaic.setIcon(new ImageIcon(pa.getPhotomosaic()));
            doAntipole = false;
            setComponentsEnabled(true);
          }
        }).start();
      } else if (source == save) {
        save(pa.getPhotomosaic());
      }
    }
  }

  private class OpenImageFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
      String str = f.getName().toLowerCase();
      return str.endsWith(".gif") || str.endsWith(".jpg") || str.endsWith(".jpeg")
              || str.endsWith(".png") || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return "Image File";
    }
  }

  private class SaveImageFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
      String str = f.getName().toLowerCase();
      return str.endsWith(".png") || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return "PNG";
    }

    @Override
    public String toString() {
      return "png";
    }
  }
}
