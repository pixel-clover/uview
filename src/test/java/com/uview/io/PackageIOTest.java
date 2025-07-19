package com.uview.io;

import static org.junit.jupiter.api.Assertions.*;

import com.uview.model.UnityAsset;
import com.uview.model.UnityPackage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PackageIOTest {

  @TempDir Path tempDir;

  private PackageIO packageIO;
  private File testPackageFile;

  @BeforeEach
  void setUp() throws IOException {
    packageIO = new PackageIO();
    testPackageFile = tempDir.resolve("test.unitypackage").toFile();
  }

  private void createTestTarGz(
      String guid, String path, String content, String meta, File outputFile) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(outputFile);
        GZIPOutputStream gzipOut = new GZIPOutputStream(fos);
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut)) {

      tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
      String guidDir = guid + "/";

      // Directory entry
      tarOut.putArchiveEntry(new TarArchiveEntry(guidDir));
      tarOut.closeArchiveEntry();

      // Pathname entry
      byte[] pathnameBytes = path.getBytes(StandardCharsets.UTF_8);
      TarArchiveEntry pathnameEntry = new TarArchiveEntry(guidDir + "pathname");
      pathnameEntry.setSize(pathnameBytes.length);
      tarOut.putArchiveEntry(pathnameEntry);
      tarOut.write(pathnameBytes);
      tarOut.closeArchiveEntry();

      // Asset entry
      byte[] assetBytes = content.getBytes(StandardCharsets.UTF_8);
      TarArchiveEntry assetEntry = new TarArchiveEntry(guidDir + "asset");
      assetEntry.setSize(assetBytes.length);
      tarOut.putArchiveEntry(assetEntry);
      tarOut.write(assetBytes);
      tarOut.closeArchiveEntry();

      // Meta entry
      byte[] metaBytes = meta.getBytes(StandardCharsets.UTF_8);
      TarArchiveEntry metaEntry = new TarArchiveEntry(guidDir + "asset.meta");
      metaEntry.setSize(metaBytes.length);
      tarOut.putArchiveEntry(metaEntry);
      tarOut.write(metaBytes);
      tarOut.closeArchiveEntry();
    }
  }

  @Test
  void load_shouldReadPackageCorrectly() throws IOException {
    String guid = "e8c5a5e3a3e2c4b4f8d9a8c7b6a5e4d3";
    String assetPath = "Assets/Scripts/Player.cs";
    String assetContent = "public class Player {}";
    String metaContent = "fileFormatVersion: 2";

    createTestTarGz(guid, assetPath, assetContent, metaContent, testPackageFile);

    UnityPackage loadedPackage = packageIO.load(testPackageFile);
    assertEquals(1, loadedPackage.getAssets().size());
    UnityAsset loadedAsset = loadedPackage.getAssetByPath(assetPath);

    assertNotNull(loadedAsset);
    assertEquals(guid, loadedAsset.guid());
    assertEquals(assetPath, loadedAsset.assetPath());
    assertArrayEquals(assetContent.getBytes(StandardCharsets.UTF_8), loadedAsset.content());
    assertArrayEquals(metaContent.getBytes(StandardCharsets.UTF_8), loadedAsset.metaContent());
  }

  @Test
  void saveAndLoad_shouldPreserveData() throws IOException {
    UnityPackage originalPackage = new UnityPackage();
    String guid = "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6";
    String assetPath = "Assets/Textures/stone.png";
    byte[] assetContent = new byte[] {1, 2, 3, 4, 5};
    byte[] metaContent = "guid: a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6".getBytes();

    UnityAsset asset = new UnityAsset(guid, assetPath, assetContent, metaContent, null);
    originalPackage.addAsset(asset);

    packageIO.save(originalPackage, testPackageFile);

    UnityPackage loadedPackage = packageIO.load(testPackageFile);
    assertEquals(1, loadedPackage.getAssets().size());
    UnityAsset loadedAsset = loadedPackage.getAssetByPath(assetPath);

    assertNotNull(loadedAsset);
    assertEquals(asset.guid(), loadedAsset.guid());
    assertEquals(asset.assetPath(), loadedAsset.assetPath());
    assertArrayEquals(asset.content(), loadedAsset.content());
    assertArrayEquals(asset.metaContent(), loadedAsset.metaContent());
  }

  @Test
  void load_withEmptyFile_shouldReturnEmptyPackage() throws IOException {
    testPackageFile.createNewFile();
    // Gzip needs at least a header to be valid, so we create a valid empty gzip file.
    try (GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(testPackageFile))) {
      // Write nothing
    }

    UnityPackage loadedPackage = packageIO.load(testPackageFile);
    assertTrue(loadedPackage.getAssets().isEmpty());
  }
}
