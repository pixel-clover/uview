package com.uview.gui;

import com.uview.gui.tree.TreeEntry;
import com.uview.model.UnityAsset;
import java.io.File;
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
              String pathForParentLookup =
                  path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

              File pathFile = new File(pathForParentLookup);
              String parentPath = pathFile.getParent();
              if (parentPath == null) {
                parentPath = "";
              }

              DefaultMutableTreeNode parentNode = findOrCreateParent(nodeMap, parentPath);

              if (asset.isDirectory()) {
                if (!nodeMap.containsKey(pathForParentLookup)) {
                  DefaultMutableTreeNode dirNode =
                      new DefaultMutableTreeNode(new TreeEntry.DirectoryEntry(path));
                  parentNode.add(dirNode);
                  nodeMap.put(pathForParentLookup, dirNode);
                }
              } else {
                DefaultMutableTreeNode assetNode =
                    new DefaultMutableTreeNode(new TreeEntry.AssetEntry(asset));
                assetNode.setAllowsChildren(false);
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
      return nodeMap.get("");
    }

    File pathFile = new File(path);
    String parentPath = pathFile.getParent();
    if (parentPath == null) {
      parentPath = "";
    }

    DefaultMutableTreeNode parentNode = findOrCreateParent(nodeMap, parentPath);
    DefaultMutableTreeNode newNode =
        new DefaultMutableTreeNode(new TreeEntry.DirectoryEntry(path + "/"));
    parentNode.add(newNode);
    nodeMap.put(path, newNode);
    return newNode;
  }
}
