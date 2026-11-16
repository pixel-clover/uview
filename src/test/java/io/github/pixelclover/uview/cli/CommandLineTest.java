package io.github.pixelclover.uview.cli;

import static org.junit.jupiter.api.Assertions.*;

import io.github.pixelclover.uview.io.PackageIO;
import io.github.pixelclover.uview.model.UnityAsset;
import io.github.pixelclover.uview.model.UnityPackage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandLineTest {
  @TempDir Path tempDir;
  private final ByteArrayOutputStream output = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errors = new ByteArrayOutputStream();
  private Path packageFile;

  @BeforeEach
  void createPackage() throws IOException {
    packageFile = tempDir.resolve("My Package.unitypackage");
    UnityPackage pkg = new UnityPackage();
    pkg.addAsset(new UnityAsset("z", "Assets/雪.txt", new byte[] {0, -1}, null, null));
    pkg.addAsset(new UnityAsset("a", "Assets/Scripts/Player.cs", new byte[] {1, 2}, null, null));
    pkg.addAsset(new UnityAsset("folder", "Assets/Empty", null, new byte[] {3}, null));
    new PackageIO().save(pkg, packageFile.toFile());
  }

  private int run(String... args) {
    return CommandLine.run(
        args,
        new PrintStream(output, true, StandardCharsets.UTF_8),
        new PrintStream(errors, true, StandardCharsets.UTF_8));
  }

  @ParameterizedTest
  @ValueSource(strings = {"--help", "-h"})
  void helpSucceedsWithoutOpeningAPackage(String flag) {
    assertEquals(0, run(flag));
    assertTrue(output.toString(StandardCharsets.UTF_8).contains("list <package>"));
    assertTrue(output.toString(StandardCharsets.UTF_8).contains("extract <package>"));
    assertEquals(0, errors.size());
  }

  @Test
  void invalidCommandsAndArgumentCountsReturnUsageOnStderr() {
    String[][] invalid = {
      {},
      {"unknown"},
      {"list"},
      {"extract", "file"},
      {"list", "file", "extra"},
      {"--help", "extra"},
      {"extract", "file", "dir", "extra"}
    };
    for (String[] args : invalid) {
      errors.reset();
      assertEquals(2, run(args));
      assertTrue(errors.toString(StandardCharsets.UTF_8).startsWith("Usage:"));
      assertEquals(0, output.size());
    }
  }

  @Test
  void listWritesOnlySortedPathsIncludingDirectories() {
    assertEquals(0, run("list", packageFile.toString()));
    assertEquals(
        "Assets/Empty\nAssets/Scripts/Player.cs\nAssets/雪.txt\n",
        output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"));
    assertEquals(0, errors.size());
  }

  @Test
  void extractPreservesPathsAndBinaryContentWithoutConsoleNoise() throws IOException {
    Path destination = tempDir.resolve("extracted assets");
    assertEquals(0, run("extract", packageFile.toString(), destination.toString()));
    assertArrayEquals(new byte[] {0, -1}, Files.readAllBytes(destination.resolve("Assets/雪.txt")));
    assertArrayEquals(
        new byte[] {1, 2}, Files.readAllBytes(destination.resolve("Assets/Scripts/Player.cs")));
    assertFalse(Files.exists(destination.resolve("Assets/Empty")));
    assertEquals(0, output.size());
    assertEquals(0, errors.size());
  }

  @Test
  void extractRefusesExistingDirectoriesAndPreservesFiles() throws IOException {
    Path destination = Files.createDirectory(tempDir.resolve("existing"));
    Path original = destination.resolve("keep.txt");
    Files.writeString(original, "keep");
    assertEquals(1, run("extract", packageFile.toString(), destination.toString()));
    assertEquals("keep", Files.readString(original));
    assertFalse(Files.exists(destination.resolve("Assets")));
    assertTrue(errors.toString(StandardCharsets.UTF_8).startsWith("Error:"));
  }

  @Test
  void missingOrCorruptPackagesFailWithoutCreatingOutput() throws IOException {
    Path destination = tempDir.resolve("output");
    assertEquals(1, run("extract", tempDir.resolve("missing").toString(), destination.toString()));
    Files.writeString(packageFile, "invalid gzip data");
    assertEquals(1, run("extract", packageFile.toString(), destination.toString()));
    assertFalse(Files.exists(destination));
    assertEquals(0, output.size());
    assertTrue(errors.toString(StandardCharsets.UTF_8).startsWith("Error:"));
  }

  @Test
  void invalidPathsProduceAnErrorInsteadOfAStackTrace() {
    assertEquals(1, run("list", "invalid\0path"));
    assertEquals(0, output.size());
    assertTrue(errors.toString(StandardCharsets.UTF_8).startsWith("Error:"));
  }

  @Test
  void emptyPackagesCanBeListedAndExtracted() throws IOException {
    new PackageIO().save(new UnityPackage(), packageFile.toFile());
    assertEquals(0, run("list", packageFile.toString()));
    Path destination = tempDir.resolve("empty");
    assertEquals(0, run("extract", packageFile.toString(), destination.toString()));
    assertTrue(Files.isDirectory(destination));
    try (var files = Files.list(destination)) {
      assertEquals(0, files.count());
    }
    assertEquals(0, output.size());
    assertEquals(0, errors.size());
  }

  @Test
  void extractionRejectsEscapingPaths() throws IOException {
    UnityPackage pkg = new UnityPackage();
    pkg.addAsset(new UnityAsset("bad", "../outside.txt", new byte[] {1}, null, null));
    new PackageIO().save(pkg, packageFile.toFile());
    assertEquals(1, run("extract", packageFile.toString(), tempDir.resolve("output").toString()));
    assertFalse(Files.exists(tempDir.resolve("outside.txt")));
    assertTrue(errors.toString(StandardCharsets.UTF_8).contains("outside the output directory"));
  }
}
