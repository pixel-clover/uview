package com.uview.gui;

import com.uview.gui.tree.TreeEntry;
import java.awt.Component;
import javax.swing.JTree;
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

        // New logic: If the node is a branch (i.e., has children), always use the folder icon.
        if (!leaf) {
          setIcon(IconManager.getFolderIcon());
        } else if (entry instanceof TreeEntry.AssetEntry) {
          setIcon(IconManager.getIconForFile(entry.getDisplayName()));
        } else {
          // This case now only applies to empty directories.
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
