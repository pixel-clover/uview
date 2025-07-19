package io.github.pixelclover.uview.gui.tree;

import io.github.pixelclover.uview.model.UnityAsset;
import java.io.File;

/**
 * A sealed interface representing an entry in the package tree view. This allows the tree to hold
 * different types of objects (like actual assets vs. implicitly created directories) while
 * providing a common API for retrieving path and display information.
 */
public sealed interface TreeEntry permits TreeEntry.DirectoryEntry, TreeEntry.AssetEntry {
  /**
   * Gets the full path of the tree entry.
   *
   * @return The full path string.
   */
  String getFullPath();

  /**
   * Gets the name to be displayed in the tree.
   *
   * @return The display name string.
   */
  String getDisplayName();

  /**
   * Represents an implicit directory in the tree structure. These are path components that do not
   * correspond to an actual asset in the package but are needed to form the hierarchy.
   *
   * @param path The full path of the directory.
   */
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

  /**
   * Represents an actual {@link UnityAsset} from the package. This can be either a file or a
   * directory that exists as a discrete entry in the package data.
   *
   * @param asset The underlying UnityAsset object.
   */
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
