package com.uview.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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
}
