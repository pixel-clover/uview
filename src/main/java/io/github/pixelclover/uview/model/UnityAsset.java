package io.github.pixelclover.uview.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a single asset within a Unity package. This record is immutable. The byte arrays for
 * content are defensively copied upon construction and retrieval to maintain immutability.
 *
 * @param guid The unique identifier for the asset.
 * @param assetPath The full path of the asset within the project (e.g.,
 *     "Assets/Scripts/Player.cs").
 * @param content The binary content of the asset file. Null if the asset is a directory.
 * @param metaContent The binary content of the associated .meta file.
 * @param previewContent The binary content of the asset's preview image, if any.
 */
public record UnityAsset(
    String guid, String assetPath, byte[] content, byte[] metaContent, byte[] previewContent) {

  /**
   * Canonical constructor that creates a deep copy of the byte array parameters.
   *
   * @throws NullPointerException if guid or assetPath are null.
   */
  public UnityAsset {
    Objects.requireNonNull(guid);
    Objects.requireNonNull(assetPath);
    content = content == null ? null : Arrays.copyOf(content, content.length);
    metaContent = metaContent == null ? null : Arrays.copyOf(metaContent, metaContent.length);
    previewContent =
        previewContent == null ? null : Arrays.copyOf(previewContent, previewContent.length);
  }

  /**
   * Creates a new {@link UnityAsset} with a randomly generated GUID.
   *
   * @param assetPath The asset's path.
   * @param content The asset's content.
   * @param metaContent The asset's meta file content.
   * @param previewContent The asset's preview content.
   * @return A new {@link UnityAsset} instance.
   */
  public static UnityAsset createNew(
      String assetPath, byte[] content, byte[] metaContent, byte[] previewContent) {
    String newGuid = java.util.UUID.randomUUID().toString().replace("-", "");
    return new UnityAsset(newGuid, assetPath, content, metaContent, previewContent);
  }

  /**
   * Returns a defensive copy of the asset's content.
   *
   * @return A copy of the content byte array, or null if it's a directory.
   */
  @Override
  public byte[] content() {
    return content == null ? null : Arrays.copyOf(content, content.length);
  }

  /**
   * Returns a defensive copy of the asset's meta content.
   *
   * @return A copy of the meta content byte array, or null if it doesn't exist.
   */
  @Override
  public byte[] metaContent() {
    return metaContent == null ? null : Arrays.copyOf(metaContent, metaContent.length);
  }

  /**
   * Returns a defensive copy of the asset's preview content.
   *
   * @return A copy of the preview content byte array, or null if it doesn't exist.
   */
  @Override
  public byte[] previewContent() {
    return previewContent == null ? null : Arrays.copyOf(previewContent, previewContent.length);
  }

  /**
   * Checks if this asset represents a directory. In a unitypackage, directories are represented as
   * assets that have no "asset" file entry, meaning their content is null.
   *
   * @return true if the asset's content is null, false otherwise.
   */
  public boolean isDirectory() {
    // The most reliable way to determine if an asset is a directory is by checking if it
    // has content. A file asset will always have a non-null (though possibly empty) content array.
    return content == null;
  }

  /**
   * Compares this asset to another object for equality. Two assets are considered equal if their
   * GUIDs are equal.
   *
   * @param o The object to compare against.
   * @return true if the objects are both UnityAssets and have the same GUID.
   */
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

  /**
   * Returns the hash code for this asset, based solely on its GUID.
   *
   * @return The hash code of the GUID.
   */
  @Override
  public final int hashCode() {
    return Objects.hash(guid);
  }

  /**
   * Returns a string representation of the asset, including its path and GUID.
   *
   * @return A string summary of the asset.
   */
  @Override
  public String toString() {
    return "UnityAsset{" + "assetPath='" + assetPath + '\'' + ", guid='" + guid + '\'' + '}';
  }
}
