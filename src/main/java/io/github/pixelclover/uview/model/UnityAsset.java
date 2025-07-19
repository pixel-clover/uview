package io.github.pixelclover.uview.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a single asset within a Unity package. An asset can be a file (like a script or
 * texture) or a directory. This record is immutable; its byte array fields are defensively copied
 * to prevent external modification.
 *
 * @param guid The unique identifier for the asset (e.g., "e8c5a5e3a3e2c4b4f8d9a8c7b6a5e4d3").
 * @param assetPath The full path of the asset within the project (e.g.,
 *     "Assets/Scripts/Player.cs").
 * @param content The binary content of the asset file. This is {@code null} if the asset is a
 *     directory.
 * @param metaContent The binary content of the associated .meta file.
 * @param previewContent The binary content of the asset's preview image, if any.
 */
public record UnityAsset(
    String guid, String assetPath, byte[] content, byte[] metaContent, byte[] previewContent) {

  /**
   * Canonical constructor for UnityAsset. It creates deep copies of the byte array parameters to
   * maintain immutability.
   *
   * @throws NullPointerException if guid or assetPath are null.
   */
  public UnityAsset {
    Objects.requireNonNull(guid);
    Objects.requireNonNull(assetPath);
    content = (content != null) ? Arrays.copyOf(content, content.length) : null;
    metaContent = (metaContent != null) ? Arrays.copyOf(metaContent, metaContent.length) : null;
    previewContent =
        (previewContent != null) ? Arrays.copyOf(previewContent, previewContent.length) : null;
  }

  /**
   * Creates a new {@link UnityAsset} with a randomly generated GUID. This is useful when adding a
   * new file to the package.
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
   * @return A copy of the content byte array, or {@code null} if it's a directory.
   */
  @Override
  public byte[] content() {
    return (content != null) ? Arrays.copyOf(content, content.length) : null;
  }

  /**
   * Returns a defensive copy of the asset's meta content.
   *
   * @return A copy of the meta content byte array, or {@code null} if it doesn't exist.
   */
  @Override
  public byte[] metaContent() {
    return (metaContent != null) ? Arrays.copyOf(metaContent, metaContent.length) : null;
  }

  /**
   * Returns a defensive copy of the asset's preview content.
   *
   * @return A copy of the preview content byte array, or {@code null} if it doesn't exist.
   */
  @Override
  public byte[] previewContent() {
    return (previewContent != null) ? Arrays.copyOf(previewContent, previewContent.length) : null;
  }

  /**
   * Checks if this asset represents a directory. In a .unitypackage, directories are typically
   * represented as assets that have a {@code null} content field.
   *
   * @return {@code true} if the asset's content is null, {@code false} otherwise.
   */
  public boolean isDirectory() {
    // The most reliable way to determine if an asset is a directory is by checking if it
    // has content. A file asset will always have a non-null (though possibly empty) content array.
    return content == null;
  }

  /**
   * Compares this asset to another object for equality. Two assets are considered equal if their
   * GUIDs are equal, as the GUID is the asset's unique identity.
   *
   * @param o The object to compare against.
   * @return {@code true} if the objects are both UnityAssets and have the same GUID.
   */
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

  /**
   * Returns the hash code for this asset, based solely on its GUID.
   *
   * @return The hash code of the GUID.
   */
  @Override
  public int hashCode() {
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
