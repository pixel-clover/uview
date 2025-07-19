package com.uview.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

public class ButtonTabComponent extends JPanel {

  private final JTabbedPane pane;

  public ButtonTabComponent(final JTabbedPane pane) {
    super(new FlowLayout(FlowLayout.LEFT, 0, 0));
    if (pane == null) {
      throw new NullPointerException("TabbedPane is null");
    }
    this.pane = pane;
    setOpaque(false);

    JLabel label =
        new JLabel() {
          public String getText() {
            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
              return pane.getTitleAt(i);
            }
            return null;
          }
        };

    add(label);
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    JButton button = new TabButton();
    add(button);
    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
  }

  private class TabButton extends JButton implements ActionListener {
    public TabButton() {
      int size = 17;
      setPreferredSize(new Dimension(size, size));
      setToolTipText("close this tab");
      setUI(new BasicButtonUI());
      setContentAreaFilled(false);
      setFocusable(false);
      setBorder(BorderFactory.createEtchedBorder());
      setBorderPainted(false);
      addMouseListener(
          new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
              setBorderPainted(true);
            }

            public void mouseExited(MouseEvent e) {
              setBorderPainted(false);
            }
          });
      setRolloverEnabled(true);
      addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
      int i = pane.indexOfTabComponent(ButtonTabComponent.this);
      if (i != -1) {
        Component topLevelAncestor = pane.getTopLevelAncestor();
        if (topLevelAncestor instanceof MainWindow) {
          MainWindow mainWindow = (MainWindow) topLevelAncestor;
          pane.setSelectedIndex(i);
          mainWindow.closePackage();
        } else {
          // Fallback just in case, but should not be reached in this app
          pane.remove(i);
        }
      }
    }

    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setStroke(new BasicStroke(2));
      g2.setColor(Color.BLACK);
      if (getModel().isRollover()) {
        g2.setColor(Color.RED);
      }
      int delta = 6;
      g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
      g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
      g2.dispose();
    }
  }
}
