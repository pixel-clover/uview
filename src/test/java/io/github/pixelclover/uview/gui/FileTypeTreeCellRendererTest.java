package io.github.pixelclover.uview.gui;

import static org.junit.jupiter.api.Assertions.*;

import io.github.pixelclover.uview.gui.tree.TreeEntry;
import io.github.pixelclover.uview.model.UnityAsset;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import org.junit.jupiter.api.Test;

class FileTypeTreeCellRendererTest {
  @Test
  void rendererUsesEntryTypeRatherThanLeafStatusAndResetsReusedCells() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      var renderer = new FileTypeTreeCellRenderer();
      var tree = new JTree();
      UnityAsset folder = UnityAsset.createNew("Assets/Folder", null, null, null);
      UnityAsset file = UnityAsset.createNew("Assets/Player.cs", new byte[0], null, null);
      for (Object entry :
          new Object[] {
            new TreeEntry.DirectoryEntry("Assets/Folder/"),
            new TreeEntry.AssetEntry(folder),
            "Folder"
          }) {
        renderer.getTreeCellRendererComponent(
            tree, new DefaultMutableTreeNode(entry), false, false, true, 0, false);
        assertEquals("Folder", renderer.getText());
        assertSame(IconManager.getFolderIcon(), renderer.getIcon());
      }
      renderer.getTreeCellRendererComponent(
          tree,
          new DefaultMutableTreeNode(new TreeEntry.AssetEntry(file)),
          true,
          false,
          false,
          1,
          true);
      assertEquals("Player.cs", renderer.getText());
      assertSame(IconManager.getIconForFile("Player.cs"), renderer.getIcon());
    });
  }
}
