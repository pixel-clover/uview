package io.github.pixelclover.uview.core;

import io.github.pixelclover.uview.io.PackageIO;
import io.github.pixelclover.uview.model.UnityAsset;
import io.github.pixelclover.uview.model.UnityPackage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PackageManager {
  private static final Logger LOGGER = LogManager.getLogger(PackageManager.class);
  private static final long MAX_ASSET_SIZE_BYTES = 512 * 1024 * 1024; // 512 MB limit
  private final PackageIO packageIO;
  private UnityPackage activePackage = new UnityPackage();
  private boolean isModified = false;

  public PackageManager(PackageIO packageIO) {
    this.packageIO = packageIO;
  }

  public void createNew() {
    activePackage.clear();
    isModified = false;
  }

  public void loadPackage(File packageFile) throws IOException {
    activePackage = packageIO.load(packageFile);
    isModified = false;
    LOGGER.info("Loaded package: {}", packageFile.getAbsolutePath());
  }

  public void savePackage(File packageFile) throws IOException {
    packageIO.save(activePackage, packageFile);
    isModified = false;
    LOGGER.info("Saved package: {}", packageFile.getAbsolutePath());
  }

  public Collection<UnityAsset> getAssets() {
    return activePackage.getAssets().values();
  }

  /**
   * Filters the assets based on a search query.
   *
   * @param query The text to search for in asset paths.
   * @return A collection of matching assets.
   */
  public Collection<UnityAsset> getFilteredAssets(String query) {
    if (query == null || query.trim().isEmpty()) {
      return getAssets(); // Return all assets if query is empty
    }
    String lowerCaseQuery = query.toLowerCase();
    return getAssets().stream()
        .filter(asset -> asset.assetPath().toLowerCase().contains(lowerCaseQuery))
        .collect(Collectors.toList());
  }

  public Collection<UnityAsset> getAssetsUnderPath(String pathPrefix) {
    return getAssets().stream()
        .filter(asset -> asset.assetPath().startsWith(pathPrefix))
        .collect(Collectors.toList());
  }

  public void addAsset(Path sourceFile, String assetPath) throws IOException {
    if (Files.size(sourceFile) > MAX_ASSET_SIZE_BYTES) {
      throw new IOException(
          String.format(
              "File is too large (%.1f MB). Maximum allowed size is %d MB.",
              Files.size(sourceFile) / (1024.0 * 1024.0), MAX_ASSET_SIZE_BYTES / (1024 * 1024)));
    }
    byte[] content = Files.readAllBytes(sourceFile);
    String metaGuid = java.util.UUID.randomUUID().toString().replace("-", "");
    String metaContent = String.format("fileFormatVersion: 2\nguid: %s\n", metaGuid);
    UnityAsset newAsset = UnityAsset.createNew(assetPath, content, metaContent.getBytes(), null);
    activePackage.addAsset(newAsset);
    isModified = true;
    LOGGER.info("Staged asset {} for addition", assetPath);
  }

  public void updateAssetContent(String assetPath, byte[] newContent) {
    UnityAsset oldAsset = activePackage.getAssetByPath(assetPath);
    if (oldAsset == null) {
      LOGGER.warn("Attempted to update content for non-existent asset: {}", assetPath);
      return;
    }
    UnityAsset updatedAsset =
        new UnityAsset(
            oldAsset.guid(),
            oldAsset.assetPath(),
            newContent,
            oldAsset.metaContent(),
            oldAsset.previewContent());
    activePackage.addAsset(updatedAsset);
    isModified = true;
    LOGGER.info("Updated content for asset {}", assetPath);
  }

  public void updateAssetMeta(String assetPath, byte[] newMetaContent) {
    UnityAsset oldAsset = activePackage.getAssetByPath(assetPath);

    if (oldAsset == null) {
      LOGGER.warn("Attempted to update meta for non-existent asset: {}", assetPath);
      return;
    }

    // Create a new asset record with the updated meta content
    UnityAsset updatedAsset =
        new UnityAsset(
            oldAsset.guid(),
            oldAsset.assetPath(),
            oldAsset.content(),
            newMetaContent,
            oldAsset.previewContent());

    activePackage.addAsset(updatedAsset); // Overwrites the old asset due to same GUID
    isModified = true;
    LOGGER.info("Updated metadata for asset {}", assetPath);
  }

  public void removeAsset(String assetPath) {
    activePackage.removeAssetByPath(assetPath);
    isModified = true;
    LOGGER.info("Staged asset {} for removal", assetPath);
  }

  public void removeDirectory(String pathPrefix) {
    // Ensure the prefix ends with a slash to avoid matching "Assets/Tex" with "Assets/Texture"
    String normalizedPrefix = pathPrefix.endsWith("/") ? pathPrefix : pathPrefix + "/";
    List<String> pathsToRemove =
        getAssets().stream()
            .map(UnityAsset::assetPath)
            .filter(p -> p.startsWith(normalizedPrefix))
            .collect(Collectors.toList());

    if (!pathsToRemove.isEmpty()) {
      pathsToRemove.forEach(activePackage::removeAssetByPath);
      isModified = true;
      LOGGER.info(
          "Staged directory {} and its {} contents for removal", pathPrefix, pathsToRemove.size());
    }
  }

  public boolean isModified() {
    return isModified;
  }

  public void extractAssets(Collection<UnityAsset> assets, Path outputDir, String pathPrefixToStrip)
      throws IOException {
    if (!Files.exists(outputDir)) {
      Files.createDirectories(outputDir);
    }
    for (UnityAsset asset : assets) {
      if (asset.content() == null) {
        continue;
      }

      String relativePath = asset.assetPath();
      if (pathPrefixToStrip != null && relativePath.startsWith(pathPrefixToStrip)) {
        relativePath = relativePath.substring(pathPrefixToStrip.length());
      }

      if (relativePath.isEmpty()) {
        continue;
      }

      Path targetPath = outputDir.resolve(relativePath);
      Files.createDirectories(targetPath.getParent());
      Files.write(targetPath, asset.content());
    }
  }
}
