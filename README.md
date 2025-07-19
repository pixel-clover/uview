<div align="center">
  <picture>
    <img alt="UView Logo" src="logo.svg" height="20%" width="20%">
  </picture>
<br>

<h2>UView</h2>

[![Tests](https://img.shields.io/github/actions/workflow/status/pixel-clover/uview/tests.yml?label=tests&style=flat&labelColor=282c34&logo=github)](https://github.com/pixel-clover/uview/actions/workflows/tests.yml)
[![Code Coverage](https://img.shields.io/codecov/c/github/pixel-clover/uview?label=coverage&style=flat&labelColor=282c34&logo=codecov)](https://codecov.io/gh/pixel-clover/uview)
[![Code Quality](https://img.shields.io/codefactor/grade/github/pixel-clover/uview?label=quality&style=flat&labelColor=282c34&logo=codefactor)](https://www.codefactor.io/repository/github/pixel-clover/uview)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue?style=flat&labelColor=282c34&logo=open-source-initiative)](LICENSE)
[![Release](https://img.shields.io/github/release/pixel-clover/uview.svg?label=release&style=flat&labelColor=282c34&logo=github)](https://github.com/pixel-clover/uview/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/pixel-clover/uview/total?label=downloads&style=flat&labelColor=282c34&logo=github)](https://github.com/pixel-clover/uview/releases)

A cross-platform tool for viewing and modifying Unity package files

</div>

---

UView is a Java application for viewing and working with Unity package files without using the Unity Editor.
It allows you to view, extract, and modify the contents of `.unitypackage` files without needing to import them into a Unity project first.

### Features

- **Cross-Platform**: Runs on Windows, macOS, and Linux.
- **View & Extract**: Browse the file hierarchy of a package and extract files and folders.
- **Asset Editing**: Edit text-based assets like C# scripts, shaders, and JSON files with syntax highlighting.
- **Meta File Editing**: View and edit the `.meta` files for any asset, including folders.
- **Package Modification**: Add new files to or remove existing files from the package.
- **Modern UI**: A clean, tabbed interface for managing multiple packages.

### Getting Started

To run UView, you need to have Java 17 or later installed on your system.
You can download Java from the [Adoptium](https://adoptium.net/) website.

Download the latest JAR release of UView from the [Releases Page](https://github.com/pixel-clover/uview/releases).
After downloading, you can run UView from your terminal or console using the following command:

```bash
java -jar uview-<version>.jar
````

Replace `<version>` with the version you downloaded.

---

### Contributing

Contributions are welcome!
See [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute to this project.

### Acknowledgements

UView is built using these amazing open-source libraries:

- [FlatLaf](https://www.formdev.com/flatlaf/) for the modern look and feel.
- [RSyntaxTextArea](https://bobbylight.github.io/RSyntaxTextArea/) for syntax highlighting.
- [Apache Commons Compress](https://commons.apache.org/proper/commons-compress/) for archive handling.

### Logo

Box logo is from [SVG Repo](https://www.svgrepo.com/svg/366323/package-inspect).

### License

UView is available under the Apache License, Version 2.0 ([LICENSE](https://www.google.com/search?q=LICENSE)).
