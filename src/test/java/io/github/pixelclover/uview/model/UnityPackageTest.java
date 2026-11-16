package io.github.pixelclover.uview.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UnityPackageTest {

  // Test the private static method `getString` via reflection
  private String callGetString(byte[] bytes) throws Exception {
    Method method = UnityPackage.class.getDeclaredMethod("getString", byte[].class);
    method.setAccessible(true);
    return (String) method.invoke(null, bytes);
  }

  @Test
  void getString_shouldHandleNormalPath() throws Exception {
    String path = "Assets/Scripts/MyFile.cs";
    assertEquals(path, callGetString(path.getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void getString_shouldTrimTrailingNewlineAndNulls() throws Exception {
    String dirtyPath = "Assets/MyFile.cs\n\0\0";
    String expectedPath = "Assets/MyFile.cs";
    assertEquals(expectedPath, callGetString(dirtyPath.getBytes(StandardCharsets.US_ASCII)));
  }

  @Test
  void getString_shouldHandleUnityBugWithDoubleZeroSuffix() throws Exception {
    String buggyPath = "Assets/MyFile.cs00";
    String expectedPath = "Assets/MyFile.cs";
    assertEquals(expectedPath, callGetString(buggyPath.getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void getString_shouldHandleNewlineAndDoubleZeroSuffix() throws Exception {
    String dirtyAndBuggyPath = "Assets/MyFile.cs\n00";
    String expectedPath = "Assets/MyFile.cs";
    assertEquals(expectedPath, callGetString(dirtyAndBuggyPath.getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void addAndGetAsset_shouldWorkCorrectly() {
    UnityPackage pkg = new UnityPackage();
    UnityAsset asset =
        UnityAsset.createNew("Assets/test.txt", new byte[0], new byte[0], new byte[0]);

    pkg.addAsset(asset);

    assertEquals(1, pkg.getAssets().size());
    assertEquals(asset, pkg.getAssetByPath("Assets/test.txt"));
    assertEquals(asset, pkg.getAssets().get(asset.guid()));
  }

  @Test
  void removeAssetByPath_shouldRemoveAsset() {
    UnityPackage pkg = new UnityPackage();
    UnityAsset asset =
        UnityAsset.createNew("Assets/test.txt", new byte[0], new byte[0], new byte[0]);
    pkg.addAsset(asset);
    assertEquals(1, pkg.getAssets().size());

    pkg.removeAssetByPath("Assets/test.txt");
    assertTrue(pkg.getAssets().isEmpty());
    assertNull(pkg.getAssetByPath("Assets/test.txt"));
  }

  @Test
  void loadingRawDataClearsOldAssetsAndIgnoresMissingPathnames() {
    UnityPackage pkg = new UnityPackage();
    pkg.addAsset(UnityAsset.createNew("Assets/old.txt", new byte[0], null, null));
    Map<String, byte[]> nullPath = new HashMap<>();
    nullPath.put("pathname", null);
    pkg.loadFromRawData(
        Map.of(
            "missing", Map.of("asset", new byte[] {1}),
            "null-path", nullPath,
            "valid",
                Map.of(
                    "pathname",
                    "Assets/雪.txt\n\0".getBytes(StandardCharsets.UTF_8),
                    "asset",
                    new byte[] {2},
                    "asset.meta",
                    new byte[] {3},
                    "preview.png",
                    new byte[] {4})));
    assertNull(pkg.getAssetByPath("Assets/old.txt"));
    assertEquals(1, pkg.getAssets().size());
    UnityAsset loaded = pkg.getAssetByPath("Assets/雪.txt");
    assertNotNull(loaded);
    assertEquals("valid", loaded.guid());
    assertArrayEquals(new byte[] {2}, loaded.content());
    assertArrayEquals(new byte[] {3}, loaded.metaContent());
    assertArrayEquals(new byte[] {4}, loaded.previewContent());
  }

  @Test
  void assetViewCannotBeModifiedAndClearResetsPathLookup() {
    UnityPackage pkg = new UnityPackage();
    UnityAsset asset = UnityAsset.createNew("Assets/file.txt", new byte[0], null, null);
    pkg.addAsset(asset);
    assertThrows(UnsupportedOperationException.class, () -> pkg.getAssets().clear());
    pkg.removeAssetByPath("missing");
    assertSame(asset, pkg.getAssetByPath(asset.assetPath()));
    pkg.clear();
    assertTrue(pkg.getAssets().isEmpty());
    assertNull(pkg.getAssetByPath(asset.assetPath()));
  }
}
