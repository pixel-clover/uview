package io.github.pixelclover.uview.cli;

import io.github.pixelclover.uview.core.PackageManager;
import io.github.pixelclover.uview.io.PackageIO;
import io.github.pixelclover.uview.model.UnityAsset;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/** Headless commands for inspecting and extracting Unity packages. */
public final class CommandLine {
  private static final String USAGE =
      """
      Usage: java -jar uview.jar [command]
        list <package>                 List asset paths in sorted order
        extract <package> <directory>  Extract asset contents into a new directory
        --help                         Show this help
      With no arguments, UView opens the desktop application.
      """;

  private CommandLine() {}

  /**
   * Runs a command without starting the desktop application.
   *
   * @param args Command and positional arguments.
   * @param out Standard output for command results.
   * @param err Standard error for failures.
   * @return 0 on success, 1 for an I/O failure, or 2 for invalid usage.
   */
  public static int run(String[] args, PrintStream out, PrintStream err) {
    if (args.length == 1 && (args[0].equals("--help") || args[0].equals("-h"))) {
      out.print(USAGE);
      return 0;
    }
    boolean list = args.length == 2 && args[0].equals("list");
    boolean extract = args.length == 3 && args[0].equals("extract");
    if (!list && !extract) {
      err.print(USAGE);
      return 2;
    }
    try {
      PackageIO io = new PackageIO();
      var assets = io.load(Path.of(args[1]).toFile()).getAssets().values();
      if (list) {
        assets.stream().map(UnityAsset::assetPath).sorted().forEach(out::println);
      } else {
        Path output = Path.of(args[2]);
        Files.createDirectory(output);
        new PackageManager(io).extractAssets(assets, output, null);
      }
      return 0;
    } catch (IOException | InvalidPathException e) {
      err.println("Error: " + e.getMessage());
      return 1;
    }
  }
}
