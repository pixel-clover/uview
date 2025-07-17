package com.uview.gui;

import com.uview.model.UnityAsset;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;

/** A utility class to build a JTree model from a collection of Unity assets. */
public final class TreeModelBuilder {

  private TreeModelBuilder() {}

  /**
   * Builds a tree structure for display in a JTree.
   *
   * @param assets The collection of assets to build the tree from.
   * @param rootName The name to display for the root node.
   * @return The root node of the constructed tree.
   */
  public static DefaultMutableTreeNode build(Collection<UnityAsset> assets, String rootName) {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootName);
    if (assets.isEmpty()) {
      return root;
    }

    Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();
    nodeMap.put("", root);

    assets.stream()
        .sorted((a1, a2) -> a1.getAssetPath().compareTo(a2.getAssetPath()))
        .forEach(
            asset -> {
              // It's a file, so its user object will be the asset itself
              if (!asset.isDirectory()) {
                File pathFile = new File(asset.getAssetPath());
                String parentPath = pathFile.getParent();
                if (parentPath == null) {
                  parentPath = "";
                }
                DefaultMutableTreeNode parentNode = findOrCreateParent(nodeMap, parentPath);
                DefaultMutableTreeNode assetNode = new DefaultMutableTreeNode(asset);
                parentNode.add(assetNode);
              }
            });

    return root;
  }

  private static DefaultMutableTreeNode findOrCreateParent(
      Map<String, DefaultMutableTreeNode> nodeMap, String path) {
    if (nodeMap.containsKey(path)) {
      return nodeMap.get(path);
    }
    if (path.isEmpty()) {
      // Should be caught by the initial check, but as a safeguard.
      return nodeMap.get("");
    }

    File pathFile = new File(path);
    String parentPath = pathFile.getParent();
    if (parentPath == null) {
      parentPath = "";
    }

    DefaultMutableTreeNode parentNode = findOrCreateParent(nodeMap, parentPath);
    // User object for directories is the path string
    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(path + "/");
    parentNode.add(newNode);
    nodeMap.put(path, newNode);
    return newNode;
  }
}
