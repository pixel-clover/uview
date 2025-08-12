package io.github.pixelclover.uview.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UnityAssetTest {

  @Test
  void constructor_shouldThrowExceptionForNullGuid() {
    assertThrows(
        NullPointerException.class,
        () -> new UnityAsset(null, "path", new byte[0], new byte[0], new byte[0]));
  }

  @Test
  void constructor_shouldThrowExceptionForNullAssetPath() {
    assertThrows(
        NullPointerException.class,
        () -> new UnityAsset("guid", null, new byte[0], new byte[0], new byte[0]));
  }

  @Test
  void constructor_shouldPerformDefensiveCopy() {
    byte[] content = {1, 2, 3};
    byte[] metaContent = {4, 5, 6};
    byte[] previewContent = {7, 8, 9};

    UnityAsset asset = new UnityAsset("guid", "path", content, metaContent, previewContent);

    // Modify the original arrays
    content[0] = 99;
    metaContent[0] = 99;
    previewContent[0] = 99;

    // Check that the asset's content is unchanged
    assertFalse(Arrays.equals(content, asset.content()));
    assertFalse(Arrays.equals(metaContent, asset.metaContent()));
    assertFalse(Arrays.equals(previewContent, asset.previewContent()));
  }

  @Test
  void getters_shouldReturnDefensiveCopies() {
    UnityAsset asset =
        new UnityAsset("guid", "path", new byte[] {1}, new byte[] {2}, new byte[] {3});

    // Modify the arrays returned by the getters
    asset.content()[0] = 99;
    asset.metaContent()[0] = 99;
    asset.previewContent()[0] = 99;

    // Check that the asset's internal arrays are unchanged
    assertEquals(1, asset.content()[0]);
    assertEquals(2, asset.metaContent()[0]);
    assertEquals(3, asset.previewContent()[0]);
  }

  @Test
  void createNew_shouldCreateAssetWithValidGuid() {
    UnityAsset asset = UnityAsset.createNew("path", new byte[0], new byte[0], new byte[0]);
    assertNotNull(asset.guid());
    assertEquals(32, asset.guid().length());
  }

  @Test
  void isDirectory_shouldReturnTrueForNullContent() {
    UnityAsset dir = new UnityAsset("guid", "path", null, new byte[0], new byte[0]);
    assertTrue(dir.isDirectory());
  }

  @Test
  void isDirectory_shouldReturnFalseForNonNullContent() {
    UnityAsset file = new UnityAsset("guid", "path", new byte[0], new byte[0], new byte[0]);
    assertFalse(file.isDirectory());
  }

  @Test
  void equals_shouldBeTrueForSameGuid() {
    UnityAsset asset1 = new UnityAsset("guid1", "path1", new byte[0], null, null);
    UnityAsset asset2 = new UnityAsset("guid1", "path2", null, new byte[0], null);
    assertEquals(asset1, asset2);
  }

  @Test
  void equals_shouldBeFalseForDifferentGuid() {
    UnityAsset asset1 = new UnityAsset("guid1", "path", new byte[0], null, null);
    UnityAsset asset2 = new UnityAsset("guid2", "path", new byte[0], null, null);
    assertNotEquals(asset1, asset2);
  }

  @Test
  void equals_shouldBeFalseForNull() {
    UnityAsset asset1 = new UnityAsset("guid1", "path", new byte[0], null, null);
    assertNotEquals(null, asset1);
  }

  @Test
  void equals_shouldBeFalseForDifferentClass() {
    UnityAsset asset1 = new UnityAsset("guid1", "path", new byte[0], null, null);
    assertNotEquals("a string", asset1);
  }

  @Test
  void hashCode_shouldBeSameForSameGuid() {
    UnityAsset asset1 = new UnityAsset("guid1", "path1", new byte[0], null, null);
    UnityAsset asset2 = new UnityAsset("guid1", "path2", null, new byte[0], null);
    assertEquals(asset1.hashCode(), asset2.hashCode());
  }

  @Test
  void toString_shouldReturnCorrectFormat() {
    UnityAsset asset = new UnityAsset("guid", "path", null, null, null);
    assertEquals("UnityAsset{assetPath='path', guid='guid'}", asset.toString());
  }
}
