# Aurora Online Game - JavaME Client 构建说明

## 环境要求 (本机开发)
- JDK 1.7+ (`javac`, `jar`) — 路径已内嵌在build.xml中
- Apache Ant 1.9+ — 用于执行build.xml
- JavaME SDK 3.4 (可选，仅`ant run`模拟器目标需要)

## 环境要求 (其他设备 / CI)
> **无需安装JavaME SDK**  
> 所有必要文件已内嵌到项目中：
> - `lib/`        — JavaME核心JAR (编译期classpath)
> - `tools/`      — `preverify.exe` + 依赖DLL (预验证字节码)
> - `docs/`       — JavaME API文档 ZIP

只需要：
- JDK 1.7 (提供`javac`和`jar`)
- Apache Ant

## 目录结构

```
1/
├── client/
│   ├── build.xml                  # Ant构建脚本
│   ├── src/
│   │   └── com/aurora/game/
│   │       └── AuroraGameMIDlet.java   # 主MIDlet入口
│   ├── res/                       # 资源文件 (图片/音频等)
│   ├── lib/                       # JavaME核心JAR (从SDK提取)
│   │   ├── midp_2.1.jar           # MIDP 2.1 API
│   │   ├── cldc_1.1.jar           # CLDC 1.1 API
│   │   ├── midp_2.0.jar
│   │   ├── cldc_1.0.jar
│   │   ├── jsr082_1.1.jar         # Bluetooth API
│   │   ├── jsr120_1.1.jar         # Wireless Messaging API
│   │   ├── jsr135_1.2.jar         # Mobile Media API (MMAPI)
│   │   ├── jsr179_1.0.jar         # Location API
│   │   ├── jsr184_1.1.jar         # Mobile 3D Graphics
│   │   ├── jsr205_2.0.jar         # Wireless Messaging API 2.0
│   │   └── jsr75_1.0.jar          # PDA Optional Packages
│   ├── docs/                      # JavaME API文档 (ZIP格式)
│   │   ├── midp-2.0.zip
│   │   ├── cldc-1.1.zip
│   │   └── ...
│   ├── build/                     # 构建中间产物 (gitignore)
│   │   ├── classes/               # javac编译输出
│   │   └── preverified/           # preverify输出
│   └── dist/                      # 最终发布产物
│       ├── AuroraGame-1.0.0.jar
│       └── AuroraGame-1.0.0.jad
├── server/                        # 服务端 (待实现)
└── tools/                         # 构建工具 (从SDK提取)
    ├── preverify.exe              # CLDC字节码预验证工具
    ├── jadtool.exe                # JAD工具
    ├── msvcp110.dll
    ├── msvcr110.dll
    └── vccorlib110.dll
```

## 构建命令

```bash
cd client

# 完整构建 (clean + compile + preverify + package)
ant all

# 单步执行
ant compile      # 仅编译
ant preverify    # 编译 + 预验证
ant package      # 编译 + 预验证 + 打包JAR/JAD
ant clean        # 清理构建产物

# 在模拟器运行 (需要本地安装JavaME SDK 3.4)
ant run
```

## JavaME规范

| 规范 | 版本 |
|------|------|
| MIDP | 2.1  |
| CLDC | 1.1  |
| Java Source | 1.3 |
| Java Target | 1.3 |

## 关键JAR说明

| 文件 | 用途 |
|------|------|
| `midp_2.1.jar` | MIDP 2.1核心API: MIDlet, Display, Canvas, GameCanvas等 |
| `cldc_1.1.jar` | CLDC 1.1核心API: java.lang, java.io, java.util等 |
| `jsr135_1.2.jar` | 多媒体API: 音频/视频播放 |
| `jsr082_1.1.jar` | 蓝牙API |
| `jsr120_1.1.jar` | 无线消息API (SMS/MMS) |

## 网络通信 (在线游戏)

JavaME使用 `javax.microedition.io.Connector` 进行网络连接：

```java
// HTTP连接
HttpConnection conn = (HttpConnection) Connector.open("http://server:8080/api");

// Socket连接 (TCP)  
SocketConnection sock = (SocketConnection) Connector.open("socket://server:9090");
```

相关API均在 `midp_2.1.jar` 中。
