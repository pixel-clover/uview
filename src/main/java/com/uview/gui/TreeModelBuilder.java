package com.uview.gui;

import com.uview.gui.tree.TreeEntry;
import com.uview.model.UnityAsset;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;

public final class TreeModelBuilder {

  private TreeModelBuilder() {}

  public static DefaultMutableTreeNode build(Collection<UnityAsset> assets, String rootName) {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootName);
    if (assets.isEmpty()) {
      return root;
    }

    Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();
    nodeMap.put("", root);

    assets.stream()
        .sorted(Comparator.comparing(UnityAsset::assetPath))
        .forEach(
            asset -> {
              String path = asset.assetPath();
              String normalizedPath =
                  path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
              String[] components = normalizedPath.split("/");

              DefaultMutableTreeNode parentNode = root;
              StringBuilder currentPath = new StringBuilder();

              int limit = asset.isDirectory() ? components.length : components.length - 1;

              for (int i = 0; i < limit; i++) {
                String part = components[i];
                if (i > 0) {
                  currentPath.append('/');
                }
                currentPath.append(part);
                String pathKey = currentPath.toString();
                DefaultMutableTreeNode currentNode = nodeMap.get(pathKey);
                if (currentNode == null) {
                  currentNode =
                      new DefaultMutableTreeNode(new TreeEntry.DirectoryEntry(pathKey + "/"));
                  parentNode.add(currentNode);
                  nodeMap.put(pathKey, currentNode);
                }
                parentNode = currentNode;
              }

              if (!asset.isDirectory()) {
                DefaultMutableTreeNode assetNode =
                    new DefaultMutableTreeNode(new TreeEntry.AssetEntry(asset));
                assetNode.setAllowsChildren(false);
                parentNode.add(assetNode);
              }
            });

    return root;
  }
}
