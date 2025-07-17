package com.uview.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.uview.model.UnityAsset;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PackageManagerTest {

  private PackageManager packageManager;
  @TempDir Path tempDir;
  private Path testPackageFile;
  private Path sourceFile;

  @BeforeEach
  void setUp() throws IOException {
    packageManager = new PackageManager();
    testPackageFile = tempDir.resolve("test.unitypackage");
    sourceFile = tempDir.resolve("my-test-file.txt");
    Files.write(sourceFile, "Hello, UView!".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void addAssetAndSavePackage() throws IOException {
    String assetPath = "Assets/MyFile.txt";
    packageManager.addAsset(sourceFile, assetPath);

    assertTrue(packageManager.isModified(), "Package should be modified after adding an asset.");

    packageManager.savePackage(testPackageFile.toFile());
    assertFalse(packageManager.isModified(), "Package should not be modified after saving.");
    assertTrue(Files.exists(testPackageFile), "Package file should be created.");

    // Now load it back and verify
    PackageManager loader = new PackageManager();
    loader.loadPackage(testPackageFile.toFile());
    Collection<UnityAsset> assets = loader.getAssets();

    assertEquals(1, assets.size(), "There should be one asset in the loaded package.");
    UnityAsset loadedAsset = assets.iterator().next();
    assertEquals(assetPath, loadedAsset.getAssetPath());
    assertArrayEquals("Hello, UView!".getBytes(StandardCharsets.UTF_8), loadedAsset.getContent());
  }

  @Test
  void removeAsset() throws IOException {
    // First, add and save an asset
    packageManager.addAsset(sourceFile, "Assets/FileToRemove.txt");
    packageManager.savePackage(testPackageFile.toFile());

    // Load the package, remove the asset, and save again
    PackageManager modifier = new PackageManager();
    modifier.loadPackage(testPackageFile.toFile());
    modifier.removeAsset("Assets/FileToRemove.txt");
    modifier.savePackage(testPackageFile.toFile());

    // Load again and verify it's gone
    PackageManager verifier = new PackageManager();
    verifier.loadPackage(testPackageFile.toFile());
    assertTrue(verifier.getAssets().isEmpty(), "Package should be empty after asset removal.");
  }
}
