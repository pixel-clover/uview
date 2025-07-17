package com.uview.gui;

import com.uview.model.UnityAsset;
import java.awt.Component;
import java.io.File;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/** A custom JTree cell renderer to display icons based on file type. */
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

    if (value instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
      Object userObject = node.getUserObject();

      if (userObject instanceof UnityAsset) {
        // It's a file asset
        UnityAsset asset = (UnityAsset) userObject;
        String filename = new File(asset.getAssetPath()).getName();
        setText(filename);
        setIcon(IconManager.getIconForFile(filename));
      } else if (userObject instanceof String) {
        // It's a directory or the root node
        String path = (String) userObject;
        String dirName = new File(path).getName();
        if (dirName.isEmpty() && path.length() > 1) {
          dirName = path.substring(0, path.length() - 1);
        }
        setText(dirName);
        setIcon(IconManager.getFolderIcon());

        // Handle the root node's text specifically
        if (node.getParent() == null) {
          setText((String) userObject);
        }
      }
    }
    return this;
  }
}
