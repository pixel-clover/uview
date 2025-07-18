package com.uview.gui;

import com.uview.gui.tree.TreeEntry;
import com.uview.model.UnityAsset;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;

public final class TreeModelBuilder {

  private TreeModelBuilder() {}

  public static DefaultMutableTreeNode build(Collection<UnityAsset> assets, String rootName) {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootName);
    if (assets == null || assets.isEmpty()) {
      return root;
    }

    Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

    // Pass 1: Create a node for every asset and directory.
    for (UnityAsset asset : assets) {
      String path = asset.assetPath();
      String normalizedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

      TreeEntry entry =
          asset.isDirectory()
              ? new TreeEntry.DirectoryEntry(path)
              : new TreeEntry.AssetEntry(asset);
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry);
      nodeMap.put(normalizedPath, node);
    }

    // Pass 2: Wire up parent-child relationships.
    for (Map.Entry<String, DefaultMutableTreeNode> mapEntry : nodeMap.entrySet()) {
      String path = mapEntry.getKey();
      DefaultMutableTreeNode node = mapEntry.getValue();

      File pathFile = new File(path);
      String parentPath = pathFile.getParent();

      if (parentPath == null) {
        root.add(node);
      } else {
        DefaultMutableTreeNode parentNode = nodeMap.get(parentPath);
        if (parentNode != null) {
          parentNode.add(node);
        } else {
          // This case can happen if a parent directory is not explicitly listed
          // in the asset list. Add to root as a fallback.
          root.add(node);
        }
      }
    }
    return root;
  }
}
