<div align="center">
  <picture>
    <img alt="UView Logo" src="src/main/resources/logo.svg" height="20%" width="20%">
  </picture>
<br>

<h2>UView</h2>

[![Tests](https://img.shields.io/github/actions/workflow/status/pixel-clover/uview/tests.yml?label=tests&style=flat&labelColor=282c34&logo=github)](https://github.com/pixel-clover/uview/actions/workflows/tests.yml)
[![Code Coverage](https://img.shields.io/codecov/c/github/pixel-clover/uview?label=coverage&style=flat&labelColor=282c34&logo=codecov)](https://codecov.io/gh/pixel-clover/uview)
[![Code Quality](https://img.shields.io/codefactor/grade/github/pixel-clover/uview?label=quality&style=flat&labelColor=282c34&logo=codefactor)](https://www.codefactor.io/repository/github/pixel-clover/uview)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue?style=flat&labelColor=282c34&logo=open-source-initiative)](LICENSE)
[![Release](https://img.shields.io/github/release/pixel-clover/uview.svg?label=release&style=flat&labelColor=282c34&logo=github)](https://github.com/pixel-clover/uview/releases/latest)

A cross-platform tool for viewing and modifying Unity package files

</div>

---

UView is a lightweight Java application that lets you view, extract, and modify the contents of `*.unitypackage` files without importing
them
into Unity Editor.

In many situations, users just want to check what's inside a Unity package, grab a few assets, or make small changes without
launching the Unity Editor that could be slow and somewhat cumbersome.
UView helps them do just that by providing a simple graphical interface to quickly browse and edit contents of Unity packages.

### Features

* Works on Windows, macOS, Linux, or any system with Java 21 or later installed
* Supports viewing the contents of `*.unitypackage` files
* Supports extracting specific assets without Unity
* Supports viewing and editing assets such as C# scripts, shaders, and prefabs
* Supports viewing and editing `*.meta` files associated with assets
* Supports adding or removing assets from the package

> [!IMPORTANT]
> This project is still in early stages of development and may be buggy and not support all Unity package features.
> Please use the [Issues Page](https://github.com/pixel-clover/uview/issues) to report bugs or request features.

---

### Getting Started

To run UView, you need to have Java 21 or later installed on your system.
If you don't have Java installed, you can download it from the [Adoptium](https://adoptium.net/) website or use your system's package
manager.

Assuming you have Java installed, download the latest JAR release of UView from the
[Releases Page](https://github.com/pixel-clover/uview/releases).
After downloading, you can start UView from your terminal or console using the following command:

```bash
java -jar uview-<version>.jar
````

Replace `<version>` with the version you downloaded.

### Screenshots

<div align="center">
  <img alt="Main Window" src="docs/assets/screenshots/0.1.1/asset-window.png" width="80%">
</div>

<details>
<summary>Show more screenshots</summary>

<div align="center">
  <img alt="C# Code" src="docs/assets/screenshots/0.1.1/start-window.png" width="80%">
  <img alt="C# Code" src="docs/assets/screenshots/0.1.0-b.2/code-viewer.png" width="80%">
  <img alt="Image View" src="docs/assets/screenshots/0.1.0-b.2/image-viewer.png" width="80%">
</div>

</details>

---

### Contributing

Contributions are welcome!
See [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute to this project.

### Acknowledgements

UView is built using a lot of amazing open-source libraries, including:

- [FlatLaf](https://www.formdev.com/flatlaf/) for the nice and modern UI.
- [RSyntaxTextArea](https://bobbylight.github.io/RSyntaxTextArea/) for syntax highlighting.
- [Apache Commons Compress](https://commons.apache.org/proper/commons-compress/) for handling archive files.

Box logo is from [SVG Repo](https://www.svgrepo.com/svg/366323/package-inspect).

### License

UView is available under the Apache License, Version 2.0 (see [LICENSE](LICENSE)).
