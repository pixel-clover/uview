package com.uview.model;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UnityPackage {
  private final Map<String, UnityAsset> assetsByGuid = new HashMap<>();
  private final Map<String, String> pathToGuid = new HashMap<>();

  public void loadFromRawData(Map<String, Map<String, byte[]>> rawData) {
    clear();
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
