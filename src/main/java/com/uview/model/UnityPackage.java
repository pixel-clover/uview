package com.uview.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/** Represents the contents of a .unitypackage file, managing the collection of assets. */
public class UnityPackage {
  private final Map<String, UnityAsset> assetsByGuid = new HashMap<>();

  /**
   * Loads assets from a .unitypackage file into memory.
   *
   * @param packageFile The .unitypackage file to load.
   * @throws IOException If an I/O error occurs.
   */
  public void load(File packageFile) throws IOException {
    assetsByGuid.clear();
    Map<String, Map<String, byte[]>> rawData = new HashMap<>();

    try (FileInputStream fis = new FileInputStream(packageFile);
        GZIPInputStream gzipIn = new GZIPInputStream(fis);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

      TarArchiveEntry entry;
      while ((entry = tarIn.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }

        java.nio.file.Path path = java.nio.file.Path.of(entry.getName());
        String guid = path.getParent().toString();
        String fileName = path.getFileName().toString();
        byte[] data = tarIn.readAllBytes();

        rawData.computeIfAbsent(guid, k -> new HashMap<>()).put(fileName, data);
      }
    }

    for (Map.Entry<String, Map<String, byte[]>> entry : rawData.entrySet()) {
      String guid = entry.getKey();
      Map<String, byte[]> files = entry.getValue();
      if (!files.containsKey("pathname")) {
        continue;
      }

      String pathname = new String(files.get("pathname"), StandardCharsets.UTF_8).trim();
      UnityAsset asset =
          new UnityAsset(
              guid,
              pathname,
              files.get("asset"),
              files.get("asset.meta"),
              files.get("preview.png"));
      assetsByGuid.put(guid, asset);
    }
  }

  /**
   * Saves the current collection of assets to a .unitypackage file.
   *
   * @param packageFile The file to save to.
   * @throws IOException If an I/O error occurs.
   */
  public void save(File packageFile) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(packageFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        GZIPOutputStream gzipOut = new GZIPOutputStream(bos);
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut)) {

      tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

      for (UnityAsset asset : assetsByGuid.values()) {
        String guidDir = asset.getGuid() + "/";
        tarOut.putArchiveEntry(new TarArchiveEntry(guidDir));
        tarOut.closeArchiveEntry();

        byte[] pathnameBytes = asset.getAssetPath().getBytes(StandardCharsets.UTF_8);
        writeEntry(tarOut, guidDir + "pathname", pathnameBytes);

        if (asset.getContent() != null) {
          writeEntry(tarOut, guidDir + "asset", asset.getContent());
        }
        if (asset.getMetaContent() != null) {
          writeEntry(tarOut, guidDir + "asset.meta", asset.getMetaContent());
        }
        if (asset.getPreviewContent() != null) {
          writeEntry(tarOut, guidDir + "preview.png", asset.getPreviewContent());
        }
      }
    }
  }

  private void writeEntry(TarArchiveOutputStream tarOut, String name, byte[] data)
      throws IOException {
    TarArchiveEntry entry = new TarArchiveEntry(name);
    entry.setSize(data.length);
    tarOut.putArchiveEntry(entry);
    tarOut.write(data);
    tarOut.closeArchiveEntry();
  }

  /**
   * Returns a copy of the current assets in the package.
   *
   * @return A map of assets by their GUID.
   */
  public Map<String, UnityAsset> getAssets() {
    return new HashMap<>(assetsByGuid);
  }

  /**
   * Adds a new asset to the package.
   *
   * @param asset The asset to add.
   */
  public void addAsset(UnityAsset asset) {
    assetsByGuid.put(asset.getGuid(), asset);
  }

  /**
   * Removes an asset from the package by its path.
   *
   * @param assetPath The path of the asset to remove.
   */
  public void removeAssetByPath(String assetPath) {
    Optional<String> guidToRemove =
        assetsByGuid.values().stream()
            .filter(asset -> asset.getAssetPath().equals(assetPath))
            .map(UnityAsset::getGuid)
            .findFirst();
    guidToRemove.ifPresent(assetsByGuid::remove);
  }
}
