package it.unict.dmi.photomosaic;

import it.unict.dmi.antipole.AntipoleTree;
import it.unict.dmi.antipole.ElementList;
import it.unict.dmi.quatree.QuadTree;
import it.unict.dmi.quatree.QuadTreeCreator;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

public class PhotomosaicAlgorithm {

  private BufferedImage image;
  private int wIm;
  private int hIm;

  private int wTileMin;
  private int hTileMin;
  private int wTileMax;
  private int hTileMax;

  private BufferedImage photomosaic;
  private AntipoleTree imagesATF;

  private JProgressBar bar;
  private JTextArea text;
  private int experimentCount;

  public final static int CLASSIC = 0;
  public final static int QUADTREE = 1;
  public final static int FRACTAL = 2;

  public void setImage(BufferedImage image) {
    this.image = image;
  }

  public BufferedImage getImage() {
    return image;
  }

  public BufferedImage getPhotomosaic() {
    return photomosaic;
  }

  public void setProgressBar(JProgressBar bar) {
    this.bar = bar;
  }

  public void setTextArea(JTextArea text) {
    this.text = text;
  }

  public void evaluate(Dimension tileSizeMin, Dimension tileSizeMax, boolean doAntipole, File dir, double sigma, int minDistance_variance, int type) {
    wTileMin = tileSizeMin.width;
    hTileMin = tileSizeMin.height;
    wTileMax = tileSizeMax.width;
    hTileMax = tileSizeMax.height;

    wIm = image.getWidth();
    hIm = image.getHeight();
    long startAntipole = System.currentTimeMillis();
    if (doAntipole && type != FRACTAL) {
      if (bar != null) {
        bar.setString("Reading Database...");
      }
      this.createAntipoleTree(dir, sigma);
      System.gc();
    }
    long stopAntipole = System.currentTimeMillis();

    //START ELABORATION
    long start = System.currentTimeMillis();
    //Photomosaic
    switch (type) {
      case CLASSIC:
        this.photomosaic(minDistance_variance);
        break;
      case QUADTREE:
        this.qtPhotomosaic(minDistance_variance);
        break;
      case FRACTAL:
        this.fqtPhotomosaic(minDistance_variance, sigma);
        break;
    }
    bar.setValue(0);
    long stop = System.currentTimeMillis();
    //STOP ELABORATION

    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMinimumFractionDigits(3);
    format.setMaximumFractionDigits(3);
    experimentCount++;
    if (text != null) {
      text.insert("--------------------------------\n", 0);
      text.insert("\tTotal Elapsed Time                       = " + format.format((stopAntipole - startAntipole + stop - start) / 1000.0) + " seconds\n", 0);
      text.insert("\tElapsed Time for Antipole Clustering     = " + format.format((stopAntipole - startAntipole) / 1000.0) + " seconds\n", 0);
      text.insert("\tElapsed Time for Photomosaic Creation    = " + format.format((stop - start) / 1000.0) + " seconds\n", 0);
      text.insert("Results\n", 0);
      if (type == CLASSIC) {
        text.insert("\tHeight                                   = " + wTileMin + "\n", 0);
        text.insert("\tWidth                                    = " + hTileMin + "\n", 0);
      } else if (type == QUADTREE) {
        text.insert("\tHeight Min                               = " + wTileMin + "\n", 0);
        text.insert("\tWidth Min                                = " + hTileMin + "\n", 0);
        text.insert("\tHeight Max                               = " + wTileMax + "\n", 0);
        text.insert("\tWidth Max                                = " + hTileMax + "\n", 0);
      }
      text.insert("Data\n", 0);
      text.insert("*** Experiment N. " + experimentCount + " ***\n", 0);
    }
  }

  private void createAntipoleTree(File dir, double sigma) {
    File[] file = dir.listFiles();
    ArrayList<Tile> list = new ArrayList<>();
    for (int i = 0; i < file.length; i++) {
      if (bar != null) {
        bar.setValue(100 * i / file.length);
      }
      BufferedImage buffer = null;
      try {
        buffer = ImageIO.read(file[i]);
      } catch (IOException io) {
      }
      if (buffer != null) {
        list.add(new Tile(buffer, file[i]));
      }
    }
    Tile[] tiles = new Tile[list.size()];
    list.toArray(tiles);
    imagesATF = new AntipoleTree(tiles, sigma);
  }

  private void photomosaic(int minDistance) {
    if (bar != null) {
      bar.setString("Classic Photomosaic...");
    }
    photomosaic = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = photomosaic.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int wTileNumber = wIm / wTileMin + 1;
    int hTileNumber = hIm / hTileMin + 1;
    Tile[] nearest = new Tile[wTileNumber * hTileNumber];
    int c = 0;
    for (int y = 0; y < hIm; y += hTileMin) {
      for (int x = 0; x < wIm; x += wTileMin) {
        int ww = x + wTileMin > wIm ? wIm - x : wTileMin;
        int hh = y + hTileMin > hIm ? hIm - y : hTileMin;
        Tile tile = new Tile(image.getSubimage(x, y, ww, hh));
        int n = 0;
        boolean ok = true;
        ElementList cluster = imagesATF.nearestElementSearch(tile, true);
        nearest[c] = (Tile) cluster.get(n).getKey();

        int size = cluster.size();
        while (n < size) {
          for (int xx = -minDistance; xx <= minDistance && ok; xx++) {
            for (int yy = -minDistance; yy <= minDistance && ok; yy++) {
              if (xx != 0 || yy != 0) {
                int cc = c + yy * wTileNumber + xx;
                if (0 <= cc && cc < nearest.length && nearest[c].equals(nearest[cc])) {
                  ok = false;
                }
              }
            }
          }
          if (!ok) {
            n++;
            if (n < size) {
              nearest[c] = (Tile) cluster.get(n).getKey();
              ok = true;
            } else {
              nearest[c] = (Tile) (cluster.get((int) (n * Math.random()))).getKey();
            }
          } else {
            n = size;
          }
        }

        BufferedImage buffer = null;
        try {
          buffer = ImageIO.read(nearest[c].getPath());
        } catch (IOException e) {
        }
        if (buffer != null) {
          g2.drawImage(buffer, x, y, wTileMin, hTileMin, null);
        }
        if (bar != null && c % 5 == 0) {
          bar.setValue(100 * c / nearest.length);
        }
        c++;
      }
    }
  }

  private void qtPhotomosaic(int variance) {
    photomosaic = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = photomosaic.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (bar != null) {
      bar.setString("QuadTree Creation...");
      bar.setIndeterminate(true);
    }
    QuadTree quadTree = QuadTreeCreator.evaluate(image, variance, wTileMin, hTileMin, wTileMax, hTileMax);

    if (bar != null) {
      bar.setString("QuadTree Photomosaic...");
    }
    recursiveQTPhotomosaic(quadTree, g2);

    if (bar != null) {
      bar.setIndeterminate(false);
    }
  }

  private void recursiveQTPhotomosaic(QuadTree quadTree, Graphics2D g2) {
    QuadTree[] child = quadTree.getChild();
    if (child[0] == null) {
      BufferedImage imm = image.getSubimage(quadTree.x, quadTree.y, quadTree.width, quadTree.height);
      Tile tile = new Tile(imm);

      ElementList cluster = imagesATF.nearestElementSearch(tile, true);
      Tile nearest = (Tile) cluster.get(0).getKey();

      BufferedImage buffer = null;
      try {
        buffer = ImageIO.read(nearest.getPath());
      } catch (IOException e) {
      }
      if (buffer != null) {
        g2.drawImage(buffer, quadTree.x, quadTree.y, quadTree.width, quadTree.height, null);
      }
    } else {
      recursiveQTPhotomosaic(child[0], g2);
      recursiveQTPhotomosaic(child[1], g2);
      recursiveQTPhotomosaic(child[2], g2);
      recursiveQTPhotomosaic(child[3], g2);
    }
  }

  private void fqtPhotomosaic(int variance, double sigma) {
    photomosaic = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = photomosaic.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (bar != null) {
      bar.setString("QuadTree Creation...");
      bar.setIndeterminate(true);
    }
    QuadTree quadTree = QuadTreeCreator.evaluate(image, variance, wTileMin, hTileMin, wTileMax, hTileMax);

    if (bar != null) {
      bar.setString("FractalQuadTree Photomosaic...");
    }
    recursiveFQTPhotomosaic(quadTree, g2, sigma, new ArrayList<>());

    if (bar != null) {
      bar.setIndeterminate(false);
    }
  }

  private void recursiveFQTPhotomosaic(QuadTree quadTree, Graphics2D g2, double sigma, ArrayList<Tile> list) {
    QuadTree[] child = quadTree.getChild();
    if (child[0] == null) {
      BufferedImage imm = image.getSubimage(quadTree.x, quadTree.y, quadTree.width, quadTree.height);
      Tile tile = new Tile(imm);

      Tile[] tiles = new Tile[list.size()];
      list.toArray(tiles);
      imagesATF = new AntipoleTree(tiles, sigma);

      ElementList cluster = imagesATF.nearestElementSearch(tile, true);
      Tile nearest = (Tile) cluster.get(0).getKey();

      Rectangle rectNearest = nearest.getBound();
      BufferedImage buffer = image.getSubimage(rectNearest.x, rectNearest.y, rectNearest.width, rectNearest.height);
      g2.drawImage(buffer, quadTree.x, quadTree.y, quadTree.width, quadTree.height, null);
    } else {
      BufferedImage imm = image.getSubimage(quadTree.x, quadTree.y, quadTree.width, quadTree.height);
      Tile tile = new Tile(imm, quadTree);
      list.add(tile);

      recursiveFQTPhotomosaic(child[0], g2, sigma, list);
      recursiveFQTPhotomosaic(child[1], g2, sigma, list);
      recursiveFQTPhotomosaic(child[2], g2, sigma, list);
      recursiveFQTPhotomosaic(child[3], g2, sigma, list);

      list.remove(list.size() - 1);
    }
  }
}
