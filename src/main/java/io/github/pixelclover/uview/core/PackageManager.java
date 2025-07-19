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

/**
 * Manages the state of an active Unity package. This class acts as a controller, handling all
 * business logic related to creating, loading, modifying, and saving packages. It maintains an
 * "isModified" flag to track unsaved changes.
 */
public class PackageManager {
  private static final Logger LOGGER = LogManager.getLogger(PackageManager.class);
  private static final long MAX_ASSET_SIZE_BYTES = 512 * 1024 * 1024; // 512 MB limit
  private final PackageIO packageIO;
  private UnityPackage activePackage = new UnityPackage();
  private boolean isModified = false;

  /**
   * Constructs a PackageManager with a given PackageIO handler.
   *
   * @param packageIO The I/O handler for reading and writing package files.
   */
  public PackageManager(PackageIO packageIO) {
    this.packageIO = packageIO;
  }

  /** Creates a new, empty package, discarding any existing active package data. */
  public void createNew() {
    activePackage.clear();
    isModified = false;
  }

  /**
   * Loads a package from the specified file, replacing the currently active package.
   *
   * @param packageFile The .unitypackage file to load.
   * @throws IOException If an error occurs during file loading.
   */
  public void loadPackage(File packageFile) throws IOException {
    activePackage = packageIO.load(packageFile);
    isModified = false;
    LOGGER.info("Loaded package: {}", packageFile.getAbsolutePath());
  }

  /**
   * Saves the active package to the specified file.
   *
   * @param packageFile The file to save the package to.
   * @throws IOException If an error occurs during file saving.
   */
  public void savePackage(File packageFile) throws IOException {
    packageIO.save(activePackage, packageFile);
    isModified = false;
    LOGGER.info("Saved package: {}", packageFile.getAbsolutePath());
  }

  /**
   * Gets an unmodifiable collection of all assets in the active package.
   *
   * @return A collection of {@link UnityAsset} objects.
   */
  public Collection<UnityAsset> getAssets() {
    return activePackage.getAssets().values();
  }

  /**
   * Filters the assets based on a search query. The search is case-insensitive and checks for the
   * query's presence in the asset path.
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

  /**
   * Gets all assets located under a specific directory path.
   *
   * @param pathPrefix The directory path to search within (e.g., "Assets/Scripts/").
   * @return A collection of assets under the given path.
   */
  public Collection<UnityAsset> getAssetsUnderPath(String pathPrefix) {
    return getAssets().stream()
        .filter(asset -> asset.assetPath().startsWith(pathPrefix))
        .collect(Collectors.toList());
  }

  /**
   * Adds a new asset to the package from a source file on disk.
   *
   * @param sourceFile The file on the local filesystem to add.
   * @param assetPath The target path for the asset within the package (e.g., "Assets/MyFile.txt").
   * @throws IOException If the file is too large or cannot be read.
   */
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

  /**
   * Updates the content of an existing asset.
   *
   * @param assetPath The path of the asset to update.
   * @param newContent The new binary content for the asset.
   */
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

  /**
   * Updates the metadata (.meta file) of an existing asset.
   *
   * @param assetPath The path of the asset to update.
   * @param newMetaContent The new binary content for the asset's meta file.
   */
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

  /**
   * Removes an asset from the package.
   *
   * @param assetPath The path of the asset to remove.
   */
  public void removeAsset(String assetPath) {
    activePackage.removeAssetByPath(assetPath);
    isModified = true;
    LOGGER.info("Staged asset {} for removal", assetPath);
  }

  /**
   * Removes a directory and all assets contained within it.
   *
   * @param pathPrefix The path of the directory to remove (e.g., "Assets/UnwantedFolder").
   */
  public void removeDirectory(String pathPrefix) {
    // Ensure the prefix ends with a slash to avoid matching "Assets/Tex" with "Assets/Texture"
    String normalizedPrefix = pathPrefix.endsWith("/") ? pathPrefix : pathPrefix + "/";
    List<String> pathsToRemove =
        getAssets().stream()
            .map(UnityAsset::assetPath)
            .filter(p -> p.startsWith(normalizedPrefix))
            .toList();

    if (!pathsToRemove.isEmpty()) {
      pathsToRemove.forEach(activePackage::removeAssetByPath);
      isModified = true;
      LOGGER.info(
          "Staged directory {} and its {} contents for removal", pathPrefix, pathsToRemove.size());
    }
  }

  /**
   * Checks if the active package has been modified since it was last saved or loaded.
   *
   * @return true if there are unsaved changes, false otherwise.
   */
  public boolean isModified() {
    return isModified;
  }

  /**
   * Extracts a collection of assets to a specified directory on the filesystem.
   *
   * @param assets The collection of assets to extract.
   * @param outputDir The destination directory.
   * @param pathPrefixToStrip A common path prefix to remove from the asset paths, preserving the
   *     relative structure within the output directory.
   * @throws IOException If an error occurs during file writing.
   */
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
