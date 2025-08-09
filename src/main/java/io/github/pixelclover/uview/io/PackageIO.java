package io.github.pixelclover.uview.io;

import io.github.pixelclover.uview.model.UnityAsset;
import io.github.pixelclover.uview.model.UnityPackage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

/** Handles reading from and writing to .unitypackage files. */
public class PackageIO {

  /**
   * Loads a {@link UnityPackage} from a .unitypackage file. It reads the gzipped tar archive,
   * extracts the raw data for each asset, and populates a new UnityPackage object.
   *
   * @param packageFile The .unitypackage file to load.
   * @return The loaded {@link UnityPackage} object.
   * @throws IOException If an I/O error occurs while reading the file.
   */
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

        // The raw entry name from the tar header can have trailing null characters.
        // Clean it to get a proper path.
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

  /**
   * Saves a {@link UnityPackage} to a .unitypackage file. It creates a temporary gzipped tar
   * archive, writes all assets to it, and then replaces the destination file.
   *
   * @param unityPackage The {@link UnityPackage} to save.
   * @param packageFile The destination .unitypackage file.
   * @throws IOException If an I/O error occurs while writing the file.
   */
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
