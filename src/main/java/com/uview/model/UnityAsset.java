package com.uview.model;

import java.util.Arrays;
import java.util.Objects;

public record UnityAsset(
    String guid, String assetPath, byte[] content, byte[] metaContent, byte[] previewContent) {

  public UnityAsset {
    Objects.requireNonNull(guid);
    Objects.requireNonNull(assetPath);
    content = content == null ? null : Arrays.copyOf(content, content.length);
    metaContent = metaContent == null ? null : Arrays.copyOf(metaContent, metaContent.length);
    previewContent =
        previewContent == null ? null : Arrays.copyOf(previewContent, previewContent.length);
  }

  public static UnityAsset createNew(
      String assetPath, byte[] content, byte[] metaContent, byte[] previewContent) {
    String newGuid = java.util.UUID.randomUUID().toString().replace("-", "");
    return new UnityAsset(newGuid, assetPath, content, metaContent, previewContent);
  }

  @Override
  public byte[] content() {
    return content == null ? null : Arrays.copyOf(content, content.length);
  }

  @Override
  public byte[] metaContent() {
    return metaContent == null ? null : Arrays.copyOf(metaContent, metaContent.length);
  }

  @Override
  public byte[] previewContent() {
    return previewContent == null ? null : Arrays.copyOf(previewContent, previewContent.length);
  }

  /**
   * Returns true if this asset represents a directory.
   *
   * @return true if the asset has no content, false otherwise.
   */
  public boolean isDirectory() {
    // The most reliable way to determine if an asset is a directory is by checking if it
    // has content. A file asset will always have a non-null (though possibly empty) content array.
    return content == null;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UnityAsset that = (UnityAsset) o;
    return guid.equals(that.guid);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(guid);
  }

  @Override
  public String toString() {
    return "UnityAsset{" + "assetPath='" + assetPath + '\'' + ", guid='" + guid + '\'' + '}';
  }
}
