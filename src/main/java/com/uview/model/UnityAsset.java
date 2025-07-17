package com.uview.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/** Represents a single asset (file or directory) within a Unity package. */
public class UnityAsset {
  private final String guid;
  private final String assetPath;
  private final byte[] content;
  private final byte[] metaContent;
  private final byte[] previewContent;

  /**
   * Constructs a new UnityAsset with a randomly generated GUID.
   *
   * @param assetPath The full path of the asset within the Unity project structure.
   * @param content The binary content of the asset file. Null for directories.
   * @param metaContent The content of the corresponding .meta file.
   * @param previewContent The content of the preview.png file, if it exists.
   */
  public UnityAsset(String assetPath, byte[] content, byte[] metaContent, byte[] previewContent) {
    this.guid = UUID.randomUUID().toString().replace("-", "");
    this.assetPath = assetPath;
    this.content = content == null ? null : Arrays.copyOf(content, content.length);
    this.metaContent = metaContent == null ? null : Arrays.copyOf(metaContent, metaContent.length);
    this.previewContent =
        previewContent == null ? null : Arrays.copyOf(previewContent, previewContent.length);
  }

  /**
   * Constructs a new UnityAsset with a specific GUID. Used when loading from an existing package.
   *
   * @param guid The existing GUID of the asset.
   * @param assetPath The full path of the asset.
   * @param content The binary content of the asset.
   * @param metaContent The content of the .meta file.
   * @param previewContent The content of the preview.png file.
   */
  UnityAsset(
      String guid, String assetPath, byte[] content, byte[] metaContent, byte[] previewContent) {
    this.guid = guid;
    this.assetPath = assetPath;
    this.content = content == null ? null : Arrays.copyOf(content, content.length);
    this.metaContent = metaContent == null ? null : Arrays.copyOf(metaContent, metaContent.length);
    this.previewContent =
        previewContent == null ? null : Arrays.copyOf(previewContent, previewContent.length);
  }

  public String getGuid() {
    return guid;
  }

  public String getAssetPath() {
    return assetPath;
  }

  public byte[] getContent() {
    return content == null ? null : Arrays.copyOf(content, content.length);
  }

  public byte[] getMetaContent() {
    return metaContent == null ? null : Arrays.copyOf(metaContent, metaContent.length);
  }

  public byte[] getPreviewContent() {
    return previewContent == null ? null : Arrays.copyOf(previewContent, previewContent.length);
  }

  public boolean isDirectory() {
    return content == null && assetPath.endsWith("/");
  }

  @Override
  public boolean equals(Object o) {
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
  public int hashCode() {
    return Objects.hash(guid);
  }

  @Override
  public String toString() {
    return "UnityAsset{" + "assetPath='" + assetPath + '\'' + ", guid='" + guid + '\'' + '}';
  }
}
