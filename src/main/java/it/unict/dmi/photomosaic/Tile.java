package it.unict.dmi.photomosaic;

import it.unict.dmi.antipole.Element;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

public class Tile implements Element {

  private static final long serialVersionUID = 1L;
  private final int[] medianRGB = new int[27];
  private File path;
  private Rectangle rectangle;

  public Tile(BufferedImage buffer, File f) {
    this(buffer);
    path = f;
  }

  public Tile(BufferedImage buffer, Rectangle rect) {
    this(buffer);
    rectangle = rect;
  }

  public Tile(BufferedImage buffer) {
    int w = buffer.getWidth();
    int h = buffer.getHeight();

    int countX = 1;
    int countY;
    int ww = w / 3;
    int hh = h / 3;
    int[] counter = new int[9];
    for (int x = 0; x < w; x++) {
      countY = 1;
      for (int y = 0; y < h; y++) {
        if (x >= countX * ww && countX < 3) {
          countX++;
        }
        if (y >= countY * hh && countY < 3) {
          countY++;
        }
        int c = 3 * ((countY - 1) * 3 + countX - 1);

        int color = buffer.getRGB(x, y);
        medianRGB[c] += (color >> 16) & 0xFF;
        medianRGB[c + 1] += (color >> 8) & 0xFF;
        medianRGB[c + 2] += color & 0xFF;
        counter[c / 3]++;
      }
    }
    for (int i = 0; i < medianRGB.length; i++) {
      if (counter[i / 3] != 0) {
        medianRGB[i] /= counter[i / 3];
      }
    }
  }

  @Override
  public double distance(Element e) {
    Tile t = (Tile) e;
    double distance = 0;
    for (int i = 0; i < medianRGB.length; i += 3) {
      distance += Math.sqrt((medianRGB[i] - t.medianRGB[i]) * (medianRGB[i] - t.medianRGB[i])
              + (medianRGB[i + 1] - t.medianRGB[i + 1]) * (medianRGB[i + 1] - t.medianRGB[i + 1])
              + (medianRGB[i + 2] - t.medianRGB[i + 2]) * (medianRGB[i + 2] - t.medianRGB[i + 2]));
    }
    return distance;
  }

  public File getPath() {
    return path;
  }

  public Rectangle getBound() {
    return rectangle;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Tile ? path.equals(((Tile) obj).getPath()) : false;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 79 * hash + Objects.hashCode(this.path);
    return hash;
  }
}
