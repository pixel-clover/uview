package io.github.pixelclover.uview.gui;

import static org.junit.jupiter.api.Assertions.*;

import io.github.pixelclover.uview.gui.tree.TreeEntry;
import io.github.pixelclover.uview.model.UnityAsset;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import org.junit.jupiter.api.Test;

class TreeModelBuilderTest {
  @Test
  void emptyInputProducesAnEmptyRoot() {
    assertEquals(0, TreeModelBuilder.build(null).getChildCount());
    assertEquals(0, TreeModelBuilder.build(List.of()).getChildCount());
  }

  @Test
  void nestedAssetsShareDirectoriesAndPreserveTheirIdentity() {
    UnityAsset first = UnityAsset.createNew("Assets/Scripts/Player.cs", new byte[0], null, null);
    UnityAsset second = UnityAsset.createNew("Assets/Scripts/Enemy.cs", new byte[0], null, null);
    DefaultMutableTreeNode root = TreeModelBuilder.build(List.of(first, second));

    assertEquals(1, root.getChildCount());
    DefaultMutableTreeNode assets = (DefaultMutableTreeNode) root.getChildAt(0);
    TreeEntry assetsEntry =
        assertInstanceOf(TreeEntry.DirectoryEntry.class, assets.getUserObject());
    assertEquals("Assets/", assetsEntry.getFullPath());
    assertEquals("Assets", assetsEntry.getDisplayName());
    assertEquals(1, assets.getChildCount());
    DefaultMutableTreeNode scripts = (DefaultMutableTreeNode) assets.getChildAt(0);
    assertEquals(2, scripts.getChildCount());
    for (int i = 0; i < 2; i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) scripts.getChildAt(i);
      TreeEntry.AssetEntry entry =
          assertInstanceOf(TreeEntry.AssetEntry.class, child.getUserObject());
      assertSame(List.of(first, second).get(i), entry.asset());
      assertEquals(entry.asset().assetPath(), entry.getFullPath());
      assertTrue(entry.getDisplayName().endsWith(".cs"));
    }
  }

  @Test
  void explicitFolderKeepsMetadataAndChildrenRegardlessOfInputOrder() {
    UnityAsset folder = UnityAsset.createNew("Assets/Folder", null, new byte[] {1}, null);
    UnityAsset child = UnityAsset.createNew("Assets/Folder/file.txt", new byte[0], null, null);
    for (List<UnityAsset> order : List.of(List.of(folder, child), List.of(child, folder))) {
      DefaultMutableTreeNode root = TreeModelBuilder.build(order);
      DefaultMutableTreeNode assets = (DefaultMutableTreeNode) root.getChildAt(0);
      assertEquals(1, assets.getChildCount());
      DefaultMutableTreeNode directory = (DefaultMutableTreeNode) assets.getChildAt(0);
      TreeEntry.AssetEntry entry =
          assertInstanceOf(TreeEntry.AssetEntry.class, directory.getUserObject());
      assertSame(folder, entry.asset());
      assertArrayEquals(new byte[] {1}, entry.asset().metaContent());
      assertEquals(1, directory.getChildCount());
    }
  }

  @Test
  void normalizesWindowsPathsAndDoesNotDuplicateAssets() {
    UnityAsset asset = UnityAsset.createNew("Assets\\Scripts\\Player.cs", new byte[0], null, null);
    DefaultMutableTreeNode root = TreeModelBuilder.build(List.of(asset, asset));
    DefaultMutableTreeNode assets = (DefaultMutableTreeNode) root.getChildAt(0);
    DefaultMutableTreeNode scripts = (DefaultMutableTreeNode) assets.getChildAt(0);
    assertEquals(1, scripts.getChildCount());
    TreeEntry.AssetEntry entry =
        (TreeEntry.AssetEntry) ((DefaultMutableTreeNode) scripts.getChildAt(0)).getUserObject();
    assertSame(asset, entry.asset());
  }

  @Test
  void topLevelAssetsDoNotCreateExtraDirectories() {
    UnityAsset asset = UnityAsset.createNew("readme.txt", new byte[0], null, null);
    DefaultMutableTreeNode root = TreeModelBuilder.build(List.of(asset));
    assertEquals(1, root.getChildCount());
    TreeEntry.AssetEntry entry =
        (TreeEntry.AssetEntry) ((DefaultMutableTreeNode) root.getChildAt(0)).getUserObject();
    assertEquals("readme.txt", entry.getDisplayName());
  }
}
