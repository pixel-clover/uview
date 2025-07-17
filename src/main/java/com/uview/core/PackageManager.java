package com.uview.core;

import com.uview.model.UnityAsset;
import com.uview.model.UnityPackage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Manages high-level operations on Unity packages, acting as a bridge between UI and model. */
public class PackageManager {
  private static final Logger LOGGER = LogManager.getLogger(PackageManager.class);
  private final UnityPackage activePackage = new UnityPackage();
  private boolean isModified = false;

  /** Creates a new, empty package in memory. */
  public void createNew() {
    activePackage.getAssets().clear();
    isModified = false;
  }

  /**
   * Loads a package from a file.
   *
   * @param packageFile The file to load.
   * @throws IOException If an I/O error occurs.
   */
  public void loadPackage(File packageFile) throws IOException {
    activePackage.load(packageFile);
    isModified = false;
    LOGGER.info("Loaded package: {}", packageFile.getAbsolutePath());
  }

  /**
   * Saves the current package to a file.
   *
   * @param packageFile The file to save to.
   * @throws IOException If an I/O error occurs.
   */
  public void savePackage(File packageFile) throws IOException {
    File tempFile = Files.createTempFile("uview-", ".unitypackage").toFile();
    activePackage.save(tempFile);
    Files.move(tempFile.toPath(), packageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    isModified = false;
    LOGGER.info("Saved package: {}", packageFile.getAbsolutePath());
  }

  /**
   * Gets all assets currently loaded.
   *
   * @return A collection of assets.
   */
  public Collection<UnityAsset> getAssets() {
    return activePackage.getAssets().values();
  }

  /**
   * Adds a new asset from a source file.
   *
   * @param sourceFile The file on disk to add.
   * @param assetPath The target path inside the package.
   * @throws IOException If an I/O error occurs reading the source file.
   */
  public void addAsset(Path sourceFile, String assetPath) throws IOException {
    byte[] content = Files.readAllBytes(sourceFile);
    String metaGuid = java.util.UUID.randomUUID().toString().replace("-", "");
    String metaContent = String.format("fileFormatVersion: 2\nguid: %s\n", metaGuid);

    UnityAsset newAsset = new UnityAsset(assetPath, content, metaContent.getBytes(), null);
    activePackage.addAsset(newAsset);
    isModified = true;
    LOGGER.info("Staged asset {} for addition from {}", assetPath, sourceFile);
  }

  /**
   * Removes an asset by its path.
   *
   * @param assetPath The path of the asset to remove.
   */
  public void removeAsset(String assetPath) {
    activePackage.removeAssetByPath(assetPath);
    isModified = true;
    LOGGER.info("Staged asset {} for removal", assetPath);
  }

  /**
   * Checks if the package has been modified since it was last loaded or saved.
   *
   * @return True if modified, false otherwise.
   */
  public boolean isModified() {
    return isModified;
  }

  /**
   * Extracts a collection of assets to a directory.
   *
   * @param assets The assets to extract.
   * @param outputDir The directory to extract to.
   * @throws IOException If an I/O error occurs.
   */
  public void extractAssets(Collection<UnityAsset> assets, Path outputDir) throws IOException {
    if (!Files.exists(outputDir)) {
      Files.createDirectories(outputDir);
    }
    for (UnityAsset asset : assets) {
      if (asset.isDirectory()) {
        continue;
      }
      Path targetPath = outputDir.resolve(asset.getAssetPath());
      Files.createDirectories(targetPath.getParent());
      Files.write(targetPath, asset.getContent());
    }
  }
}
