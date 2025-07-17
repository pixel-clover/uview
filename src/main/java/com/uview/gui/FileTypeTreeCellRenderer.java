package com.uview.gui;

import com.uview.gui.tree.TreeEntry;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class FileTypeTreeCellRenderer extends DefaultTreeCellRenderer {

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree,
      Object value,
      boolean sel,
      boolean expanded,
      boolean leaf,
      int row,
      boolean hasFocus) {

    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    if (value instanceof DefaultMutableTreeNode node) {
      Object userObject = node.getUserObject();

      if (userObject instanceof TreeEntry entry) {
        setText(entry.getDisplayName());
        if (entry instanceof TreeEntry.AssetEntry) {
          setIcon(IconManager.getIconForFile(entry.getDisplayName()));
        } else {
          setIcon(IconManager.getFolderIcon());
        }
      } else if (userObject instanceof String) {
        setText((String) userObject);
        setIcon(IconManager.getFolderIcon());
      }
    }
    return this;
  }
}
