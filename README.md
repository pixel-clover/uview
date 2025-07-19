<div align="center">
  <picture>
    <img alt="UView Logo" src="logo.svg" height="40%" width="40%">
  </picture>
<br>

<h2>UView</h2>

[![Tests](https://img.shields.io/github/actions/workflow/status/pixel-clover/uview/tests.yml?label=tests&style=flat&labelColor=282c34&logo=github)](https://github.com/pixel-clover/uview/actions/workflows/tests.yml)
[![Code Coverage](https://img.shields.io/codecov/c/github/pixel-clover/uview?label=coverage&style=flat&labelColor=282c34&logo=codecov)](https://codecov.io/gh/pixel-clover/uview)
[![Code Quality](https://img.shields.io/codefactor/grade/github/pixel-clover/uview?label=quality&style=flat&labelColor=282c34&logo=codefactor)](https://www.codefactor.io/repository/github/pixel-clover/uview)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue?style=flat&labelColor=282c34&logo=open-source-initiative)](LICENSE)
[![Release](https://img.shields.io/github/release/pixel-clover/uview.svg?label=release&style=flat&labelColor=282c34&logo=github)](https://github.com/pixel-clover/uview/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/pixel-clover/uview/total?label=downloads&style=flat&labelColor=282c34&logo=github)](https://github.com/pixel-clover/uview/releases)

A cross platform tool to view and modify `.unitypackage` files without Unity

</div>

---

UView is a Java application for viewing and working with Unity package files without using the Unity Editor.
It allows you to view, extract, and modify contents of `.unitypackage` files without needing to import them into a Unity project first.

### Features

- Runs everywhere Java is supported (Windows, macOS, Linux)
- Let you, vies, extract, and modify Unity package files

### Getting Started

To run UView, you need to have Java 17 or later installed on your system.
Typically, you can use your system's package manager to install Java, or you can download it from the [Adoptium](https://adoptium.net/)
website.

You can download the latest JAR release of UView from the [releases page](https://github.com/pixel-clover/uview/releases).
After downloading, you can run UView using the following command:

```bash
java -jar uview-<version>.jar
```

Replace `<version>` with the actual version number of the JAR file you downloaded.

### Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for details on how to make contributions to this project.

### License

UView is available under Apache License, Version 2.0 ([LICENSE](LICENSE)).
