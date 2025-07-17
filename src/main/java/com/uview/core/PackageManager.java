package com.uview.core;

import com.uview.io.PackageIO;
import com.uview.model.UnityAsset;
import com.uview.model.UnityPackage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PackageManager {
  private static final Logger LOGGER = LogManager.getLogger(PackageManager.class);
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

  public void addAsset(Path sourceFile, String assetPath) throws IOException {
    byte[] content = Files.readAllBytes(sourceFile);
    String metaGuid = java.util.UUID.randomUUID().toString().replace("-", "");
    String metaContent = String.format("fileFormatVersion: 2\nguid: %s\n", metaGuid);
    UnityAsset newAsset = UnityAsset.createNew(assetPath, content, metaContent.getBytes(), null);
    activePackage.addAsset(newAsset);
    isModified = true;
    LOGGER.info("Staged asset {} for addition", assetPath);
  }

  public void removeAsset(String assetPath) {
    activePackage.removeAssetByPath(assetPath);
    isModified = true;
    LOGGER.info("Staged asset {} for removal", assetPath);
  }

  public boolean isModified() {
    return isModified;
  }

  public void extractAssets(Collection<UnityAsset> assets, Path outputDir) throws IOException {
    if (!Files.exists(outputDir)) {
      Files.createDirectories(outputDir);
    }
    for (UnityAsset asset : assets) {
      if (asset.isDirectory()) {
        continue;
      }
      Path targetPath = outputDir.resolve(asset.assetPath());
      Files.createDirectories(targetPath.getParent());
      assert asset.content() != null;
      Files.write(targetPath, asset.content());
    }
  }
}
