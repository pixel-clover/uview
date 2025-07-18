package com.uview.io;

import com.uview.model.UnityAsset;
import com.uview.model.UnityPackage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

public class PackageIO {

  public UnityPackage load(File packageFile) throws IOException {
    UnityPackage unityPackage = new UnityPackage();
    Map<String, Map<String, byte[]>> rawData = new HashMap<>();

    try (FileInputStream fis = new FileInputStream(packageFile);
        GZIPInputStream gzipIn = new GZIPInputStream(fis);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

      TarArchiveEntry entry;
      while ((entry = tarIn.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }

        // FIX: Clean the raw entry name from the tar header itself to remove any padding.
        String entryName = entry.getName();
        int nullIndex = entryName.indexOf(0);
        if (nullIndex != -1) {
          entryName = entryName.substring(0, nullIndex);
        }
        entryName = entryName.replace('\\', '/');

        int lastSlashIndex = entryName.lastIndexOf('/');
        if (lastSlashIndex == -1) {
          continue;
        }

        String guid = entryName.substring(0, lastSlashIndex);
        String fileName = entryName.substring(lastSlashIndex + 1);

        byte[] data = tarIn.readAllBytes();
        rawData.computeIfAbsent(guid, k -> new HashMap<>()).put(fileName, data);
      }
    }

    unityPackage.loadFromRawData(rawData);
    return unityPackage;
  }

  public void save(UnityPackage unityPackage, File packageFile) throws IOException {
    File tempFile = Files.createTempFile("uview-", ".unitypackage").toFile();

    try (FileOutputStream fos = new FileOutputStream(tempFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        GZIPOutputStream gzipOut = new GZIPOutputStream(bos);
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut)) {

      tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

      for (UnityAsset asset : unityPackage.getAssets().values()) {
        String guidDir = asset.guid() + "/";
        tarOut.putArchiveEntry(new TarArchiveEntry(guidDir));
        tarOut.closeArchiveEntry();

        byte[] pathnameBytes = asset.assetPath().getBytes(StandardCharsets.UTF_8);
        writeEntry(tarOut, guidDir + "pathname", pathnameBytes);

        if (asset.content() != null) {
          writeEntry(tarOut, guidDir + "asset", asset.content());
        }
        if (asset.metaContent() != null) {
          writeEntry(tarOut, guidDir + "asset.meta", asset.metaContent());
        }
      }
    }

    Files.move(tempFile.toPath(), packageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
  }

  private void writeEntry(TarArchiveOutputStream tarOut, String name, byte[] data)
      throws IOException {
    TarArchiveEntry entry = new TarArchiveEntry(name);
    entry.setSize(data.length);
    tarOut.putArchiveEntry(entry);
    tarOut.write(data);
    tarOut.closeArchiveEntry();
  }
}
