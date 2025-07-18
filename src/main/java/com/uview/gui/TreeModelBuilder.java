package com.uview.gui;

import com.uview.gui.tree.TreeEntry;
import com.uview.model.UnityAsset;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.tree.DefaultMutableTreeNode;

public final class TreeModelBuilder {

  private TreeModelBuilder() {}

  public static DefaultMutableTreeNode build(Collection<UnityAsset> assets) {
    // A temporary root to build the full package hierarchy under.
    DefaultMutableTreeNode masterRoot = new DefaultMutableTreeNode("master-root");
    if (assets == null || assets.isEmpty()) {
      return new DefaultMutableTreeNode("Assets");
    }

    Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();
    nodeMap.put("", masterRoot);

    for (UnityAsset asset : assets) {
      String path = asset.assetPath().replace('\\', '/');
      String normalizedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

      // Ensure the parent hierarchy for this asset exists.
      String parentPathStr = new File(normalizedPath).getParent();
      DefaultMutableTreeNode parentNode = getOrCreatePath(nodeMap, masterRoot, parentPathStr);

      // Now that the parent is guaranteed to exist, create the node for the current asset.
      if (!nodeMap.containsKey(normalizedPath)) {
        TreeEntry entry =
            asset.isDirectory()
                ? new TreeEntry.DirectoryEntry(path)
                : new TreeEntry.AssetEntry(asset);
        DefaultMutableTreeNode assetNode = new DefaultMutableTreeNode(entry);
        parentNode.add(assetNode);
        nodeMap.put(normalizedPath, assetNode);
      }
    }

    // Find the "Assets" node, which now contains the correct full hierarchy.
    DefaultMutableTreeNode assetsNode = nodeMap.get("Assets");
    return Objects.requireNonNullElseGet(assetsNode, () -> new DefaultMutableTreeNode("Assets"));
  }

  private static DefaultMutableTreeNode getOrCreatePath(
      Map<String, DefaultMutableTreeNode> nodeMap, DefaultMutableTreeNode root, String path) {
    if (path == null) {
      return root;
    }
    // Normalize path for lookup
    String normalizedPath = path.replace('\\', '/');
    if (nodeMap.containsKey(normalizedPath)) {
      return nodeMap.get(normalizedPath);
    }

    // If the path doesn't exist, create it by first creating its parent.
    String parentPathStr = new File(normalizedPath).getParent();
    DefaultMutableTreeNode parentNode = getOrCreatePath(nodeMap, root, parentPathStr);

    // Now, create the node for the current path.
    DefaultMutableTreeNode newNode =
        new DefaultMutableTreeNode(new TreeEntry.DirectoryEntry(normalizedPath + "/"));
    parentNode.add(newNode);
    nodeMap.put(normalizedPath, newNode);

    return newNode;
  }
}
