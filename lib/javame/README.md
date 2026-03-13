# Java ME JARs

把 Java ME SDK 中的 API JAR（例如包含 `javax.microedition.*` 的 JAR）放到本目录，VS Code 会自动把它们加入编译类路径。

示例：将 `midp.jar`、`cldc.jar` 或供应商提供的 `javame-api.jar` 复制到此处。

添加 Javadoc：如果你的 SDK 在 `D:\Java_ME_platform_SDK_3.4`（或其他路径），可以运行 `scripts\install_javadoc.bat`，会把 SDK 的 Javadoc（如 `midp-2.0.zip`、`cldc-1.1.zip` 等）解压到本目录下的 `javadoc/`。解压后，使用 VS Code Java Projects 视图为对应 JAR 附加 Javadoc 以便 IDE 与 AI 能索引注释。

命令示例（在项目根目录运行）：
```
scripts\install_javadoc.bat
```

注意：脚本使用 PowerShell 的 `Expand-Archive`，需要在 Windows 环境下运行。
