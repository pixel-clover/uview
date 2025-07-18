package com.uview.core;

import static org.junit.jupiter.api.Assertions.*;

import com.uview.io.PackageIO;
import com.uview.model.UnityAsset;
import com.uview.model.UnityPackage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PackageManagerTest {

  private final UnityPackage capturedPackage = new UnityPackage();
  @TempDir Path tempDir;
  private PackageManager packageManager;
  private Path sourceFile;
  private Path sourceFile2;

  @BeforeEach
  void setUp() throws IOException {
    // Create a mock PackageIO that works with our in-memory package object
    PackageIO mockIo =
        new PackageIO() {
          @Override
          public UnityPackage load(File f) {
            return new UnityPackage(); // Return a fresh package on load
          }

          @Override
          public void save(UnityPackage p, File f) {
            // Instead of writing to disk, capture the state for assertion
            capturedPackage.clear();
            p.getAssets().values().forEach(capturedPackage::addAsset);
          }
        };
    packageManager = new PackageManager(mockIo);
    sourceFile = tempDir.resolve("my-test-file.txt");
    Files.write(sourceFile, "test".getBytes(StandardCharsets.UTF_8));
    sourceFile2 = tempDir.resolve("another-file.log");
    Files.write(sourceFile2, "log data".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void addAssetMarksPackageAsModified() throws IOException {
    String assetPath = "Assets/MyFile.txt";
    assertFalse(packageManager.isModified());

    packageManager.addAsset(sourceFile, assetPath);

    assertTrue(packageManager.isModified(), "Package should be modified after adding an asset.");
    assertEquals(1, packageManager.getAssets().size());
  }

  @Test
  void removeAssetMarksPackageAsModifiedAndRemovesAsset() throws IOException {
    String assetPath = "Assets/MyFile.txt";
    packageManager.addAsset(sourceFile, assetPath);
    // Reset modified flag to test remove operation in isolation
    packageManager.savePackage(new File("dummy.unitypackage"));
    assertFalse(packageManager.isModified());
    assertEquals(1, packageManager.getAssets().size());

    packageManager.removeAsset(assetPath);

    assertTrue(packageManager.isModified(), "Package should be modified after removing an asset.");
    assertEquals(0, packageManager.getAssets().size(), "Asset count should be 0 after removal.");
  }

  @Test
  void savePackageResetsModifiedFlag() throws IOException {
    // Use the valid 'sourceFile' instead of Path.of(".")
    packageManager.addAsset(sourceFile, "Assets/dummy.txt");
    assertTrue(packageManager.isModified());

    packageManager.savePackage(new File("dummy.unitypackage"));

    assertFalse(packageManager.isModified(), "Package should not be modified after saving.");
    assertEquals(1, capturedPackage.getAssets().size(), "Captured package should have one asset.");
  }

  @Test
  void extractAssetsWritesToCorrectLocation() throws IOException {
    String assetPath = "Assets/folder/MyFile.txt";
    packageManager.addAsset(sourceFile, assetPath);
    UnityAsset asset = packageManager.getAssets().iterator().next();
    Path outputDir = tempDir.resolve("output");

    packageManager.extractAssets(List.of(asset), outputDir, "Assets/folder/");

    Path extractedFile = outputDir.resolve("MyFile.txt");
    assertTrue(Files.exists(extractedFile));
    assertEquals("test", Files.readString(extractedFile));
  }

  @Test
  void getFilteredAssetsReturnsCorrectSubset() throws IOException {
    packageManager.addAsset(sourceFile, "Assets/Textures/stone.png");
    packageManager.addAsset(sourceFile2, "Assets/Scripts/Player/PlayerController.cs");
    packageManager.addAsset(sourceFile2, "Assets/Audio/stone_hit.wav");

    Collection<UnityAsset> results = packageManager.getFilteredAssets("stone");
    assertEquals(2, results.size());
    assertTrue(results.stream().anyMatch(a -> a.assetPath().contains("stone.png")));
    assertTrue(results.stream().anyMatch(a -> a.assetPath().contains("stone_hit.wav")));

    Collection<UnityAsset> csResults = packageManager.getFilteredAssets(".cs");
    assertEquals(1, csResults.size());
    assertEquals(
        "Assets/Scripts/Player/PlayerController.cs", csResults.iterator().next().assetPath());
  }
}
