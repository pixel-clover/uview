package com.uview.model;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnityPackage {
  private static final Logger LOGGER = LogManager.getLogger(UnityPackage.class);
  private final Map<String, UnityAsset> assetsByGuid = new HashMap<>();
  private final Map<String, String> pathToGuid = new HashMap<>();

  private static String getString(byte[] pathnameBytes) {
    String pathname = new String(pathnameBytes, StandardCharsets.UTF_8);

    // DEFINITIVE FIX: Based on the logs, we now know the exact problem.
    // 1. Remove all control characters, which gets rid of the newline (\n).
    //    This turns "File.cs\n00" into "File.cs00".
    pathname = pathname.replaceAll("\\p{Cntrl}", "");

    // 2. Now that the newline is gone, check for and remove the literal "00" suffix.
    if (pathname.endsWith("00")) {
      pathname = pathname.substring(0, pathname.length() - 2);
    }
    return pathname;
  }

  public void loadFromRawData(Map<String, Map<String, byte[]>> rawData) {
    clear();
    for (Map.Entry<String, Map<String, byte[]>> entry : rawData.entrySet()) {
      String guid = entry.getKey();
      Map<String, byte[]> files = entry.getValue();
      if (!files.containsKey("pathname")) {
        continue;
      }

      byte[] pathnameBytes = files.get("pathname");
      if (pathnameBytes == null) {
        continue;
      }

      String pathname = getString(pathnameBytes);

      UnityAsset asset =
          new UnityAsset(
              guid,
              pathname,
              files.get("asset"),
              files.get("asset.meta"),
              files.get("preview.png"));
      addAsset(asset);
    }
  }

  public void clear() {
    assetsByGuid.clear();
    pathToGuid.clear();
  }

  public Map<String, UnityAsset> getAssets() {
    return Collections.unmodifiableMap(assetsByGuid);
  }

  public void addAsset(UnityAsset asset) {
    assetsByGuid.put(asset.guid(), asset);
    pathToGuid.put(asset.assetPath(), asset.guid());
  }

  public void removeAssetByPath(String assetPath) {
    String guid = pathToGuid.remove(assetPath);
    if (guid != null) {
      assetsByGuid.remove(guid);
    }
  }
}
