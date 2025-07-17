package com.uview.gui.tree;

import com.uview.model.UnityAsset;
import java.io.File;

public sealed interface TreeEntry permits TreeEntry.DirectoryEntry, TreeEntry.AssetEntry {
  String getFullPath();

  String getDisplayName();

  record DirectoryEntry(String path) implements TreeEntry {
    @Override
    public String getFullPath() {
      return path;
    }

    @Override
    public String getDisplayName() {
      String name = new File(path).getName();
      if (name.isEmpty() && path.length() > 1) {
        return path.substring(0, path.length() - 1);
      }
      return name;
    }
  }

  record AssetEntry(UnityAsset asset) implements TreeEntry {
    @Override
    public String getFullPath() {
      return asset.assetPath();
    }

    @Override
    public String getDisplayName() {
      return new File(asset.assetPath()).getName();
    }
  }
}
