package com.uview.core;

import static org.junit.jupiter.api.Assertions.*;

import com.uview.io.PackageIO;
import com.uview.model.UnityPackage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PackageManagerTest {

  private final UnityPackage capturedPackage = new UnityPackage();
  @TempDir Path tempDir;
  private PackageManager packageManager;
  private Path sourceFile;

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
  void savePackageResetsModifiedFlag() throws IOException {
    // Use the valid 'sourceFile' instead of Path.of(".")
    packageManager.addAsset(sourceFile, "Assets/dummy.txt");
    assertTrue(packageManager.isModified());

    packageManager.savePackage(new File("dummy.unitypackage"));

    assertFalse(packageManager.isModified(), "Package should not be modified after saving.");
    assertEquals(1, capturedPackage.getAssets().size(), "Captured package should have one asset.");
  }
}
