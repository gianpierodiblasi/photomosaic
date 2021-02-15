package it.unict.dmi.photomosaic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class SettingPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private JSpinner wTileMin = new JSpinner();
  private JSpinner hTileMin = new JSpinner();
  private JSpinner wTileMax = new JSpinner();
  private JSpinner hTileMax = new JSpinner();
  private JSpinner minDistance_Variance = new JSpinner();
  private SpinnerNumberModel modelWmin = new SpinnerNumberModel(10, 5, 100, 1);
  private SpinnerNumberModel modelHmin = new SpinnerNumberModel(10, 5, 100, 1);
  private SpinnerNumberModel modelWmax = new SpinnerNumberModel(300, 5, 500, 1);
  private SpinnerNumberModel modelHmax = new SpinnerNumberModel(300, 5, 500, 1);
  private SpinnerNumberModel modelMinDistance_Variance = new SpinnerNumberModel(5, 0, 20, 1);
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private Border border1;
  private TitledBorder titledBorder1;
  private JLabel jLabel3 = new JLabel();
  private JPanel jPanel9 = new JPanel();
  private GridLayout gridLayout7 = new GridLayout();
  private JPanel jPanel10 = new JPanel();
  private GridLayout gridLayout2 = new GridLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private JRadioButton classic = new JRadioButton();
  private JRadioButton quadtree = new JRadioButton();
  private JRadioButton fractalquadtree = new JRadioButton();
  private GridLayout gridLayout1 = new GridLayout();
  private ButtonGroup buttonGroup1 = new ButtonGroup();
  private JLabel jLabel4 = new JLabel();
  private JLabel jLabel5 = new JLabel();

  @SuppressWarnings("CallToPrintStackTrace")
  public SettingPanel() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Dimension getTileSizeMin() {
    return new Dimension(((Number) wTileMin.getValue()).intValue(), ((Number) hTileMin.getValue()).intValue());
  }

  public Dimension getTileSizeMax() {
    return new Dimension(((Number) wTileMax.getValue()).intValue(), ((Number) hTileMax.getValue()).intValue());
  }

  public int getMinDistance_Variance() {
    return ((Number) minDistance_Variance.getValue()).intValue();
  }

  public int getType() {
    return classic.isSelected() ? PhotomosaicAlgorithm.CLASSIC
            : quadtree.isSelected() ? PhotomosaicAlgorithm.QUADTREE
            : fractalquadtree.isSelected() ? PhotomosaicAlgorithm.FRACTAL : -1;
  }

  public void setComponentsEnabled(boolean b) {
    boolean bb = classic.isSelected();
    classic.setEnabled(b);
    quadtree.setEnabled(b);
    fractalquadtree.setEnabled(b);
    wTileMin.setEnabled(b);
    hTileMin.setEnabled(b);
    wTileMax.setEnabled(b && !bb);
    hTileMax.setEnabled(b && !bb);
    minDistance_Variance.setEnabled(b);
  }

  private void jbInit() throws Exception {
    border1 = BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151));
    titledBorder1 = new TitledBorder(border1, "Settings");
    wTileMin.setModel(modelWmin);
    hTileMin.setModel(modelHmin);
    wTileMax.setModel(modelWmax);
    wTileMax.setEnabled(false);
    hTileMax.setModel(modelHmax);
    hTileMax.setEnabled(false);
    jLabel1.setBorder(BorderFactory.createEtchedBorder());
    jLabel1.setText("Width");
    jLabel2.setBorder(BorderFactory.createEtchedBorder());
    jLabel2.setText("Height");
    this.setBorder(titledBorder1);
    jLabel3.setBorder(BorderFactory.createEtchedBorder());
    jLabel3.setText("Min. Distance");
    jPanel9.setLayout(gridLayout7);
    gridLayout7.setRows(5);
    jPanel10.setLayout(gridLayout2);
    gridLayout2.setRows(5);
    minDistance_Variance.setModel(modelMinDistance_Variance);
    this.setLayout(borderLayout1);
    classic.setSelected(true);
    classic.setText("Classic");
    quadtree.setText("QuadTree");
    fractalquadtree.setText("FractalQuadTree");
    jPanel1.setLayout(gridLayout1);
    gridLayout1.setRows(3);
    jLabel4.setText("Height Max");
    jLabel4.setBorder(BorderFactory.createEtchedBorder());
    jLabel5.setText("Width Max");
    jLabel5.setBorder(BorderFactory.createEtchedBorder());
    jPanel10.add(wTileMin, null);
    jPanel10.add(hTileMin, null);
    jPanel10.add(wTileMax, null);
    jPanel10.add(hTileMax, null);
    jPanel10.add(minDistance_Variance, null);
    this.add(jPanel1, BorderLayout.NORTH);
    jPanel1.add(classic, null);
    jPanel1.add(quadtree, null);
    jPanel1.add(fractalquadtree, null);
    this.add(jPanel9, BorderLayout.WEST);
    this.add(jPanel10, BorderLayout.CENTER);
    jPanel9.add(jLabel1, null);
    jPanel9.add(jLabel2, null);
    jPanel9.add(jLabel5, null);
    jPanel9.add(jLabel4, null);
    jPanel9.add(jLabel3, null);
    buttonGroup1.add(classic);
    buttonGroup1.add(quadtree);
    buttonGroup1.add(fractalquadtree);
    Listener listener = new Listener();
    classic.addActionListener(listener);
    quadtree.addActionListener(listener);
    fractalquadtree.addActionListener(listener);
  }

  private class Listener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source == classic) {
        jLabel1.setText("Width");
        jLabel2.setText("Height");
        wTileMax.setEnabled(false);
        hTileMax.setEnabled(false);
        jLabel3.setText("Min Distance");
        modelMinDistance_Variance.setMinimum(0);
        modelMinDistance_Variance.setMaximum(20);
        modelMinDistance_Variance.setValue(5);
      } else if (source == quadtree || source == fractalquadtree) {
        jLabel1.setText("Width Min");
        jLabel2.setText("Height Min");
        wTileMax.setEnabled(true);
        hTileMax.setEnabled(true);
        jLabel3.setText("Max Variance");
        modelMinDistance_Variance.setMinimum(10);
        modelMinDistance_Variance.setMaximum(30);
        modelMinDistance_Variance.setValue(15);
      }
    }
  }
}
