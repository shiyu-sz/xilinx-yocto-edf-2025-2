# OV2640 摄像头（Zynq XC7Z020）

参考 `../petalinux2020-1-acz702` 的
`75bb9a494b7197bd5d59fb1fe63b63da1864803f`，将摄像头配置移植到 EDF 的
Linux 6.12。当前默认采集 **800×480、RGB565**，V4L2 fourcc 为 `RGBP`。
默认使用多平面采集 API，但 RGB565 只有一个内存平面。

## 硬件和驱动

当前 XSA 的数据链路为：

```text
OV2640 DVP（每个 RGB565 像素先高字节、后低字节）
  -> Camera_Data_0（两字节拼接、RGB888 展开）
  -> v_vid_in_axi4s_0 -> rgb888to565_0
  -> AXI VDMA S2MM（0x43000000，DMA 通道 1）-> DDR -> /dev/videoN

PS I2C0 -> OV2640 SCCB，7 位地址 0x30
FCLK0 100 MHz -> clk_wiz_0 -> 摄像头 XCLK 24 MHz
```

HDMI 使用同一 VDMA 的 MM2S 通道 0，两边使用各自的缓冲区。
固定的 DVP 解包和 RGB 转换逻辑没有软件寄存器，因此媒体图中直接连接
OV2640 subdev 与 VIPP 采集端。

当前硬件把 `axi_dynclk_0/LOCKED_O` 接到 `Camera_Data_0/Rst_n` 和
`clk_wiz_0/resetn`。设备树在摄像头节点为动态时钟设置 74.25 MHz 初值，
驱动通过额外的 `pl-reset` 时钟引用保持其运行，使摄像头不依赖 HDMI
显示器连接或手动执行 `modetest` 才解除复位。**采集期间不要切换 HDMI
显示模式**：动态时钟重新锁定会复位摄像头前端，应先停止采集再切换模式。
摄像头的 RESETB、PWDN 由 PL 分别固定为高、低，不声明不存在的 GPIO。

BSP 修改位于 `sources/meta-amd-edf/meta-amd-edf-bsp/`：

- `recipes-kernel/linux/files/ov2640-camera.cfg` 启用 OV2640、Xilinx VIPP、
  V4L2 subdev、Media Controller 和 DMA 连续内存支持，沿用 64 MiB CMA。
- `0003-media-enable-ov2640-rgb565-vdma-capture.patch` 添加 800×480 传感器
  窗口及 RGB565 默认值、DVP 格式映射，补齐多平面高度、平面数量和初始
  DMA 裁剪尺寸，同时处理上述 PL 复位时钟依赖。
- `recipes-bsp/device-tree/files/zynq-xc7z020-ov2640.dtsi` 描述 I2C 传感器、
  时钟和 VDMA 采集媒体图，同时加入 Linux 和 U-Boot 两套设备树。
- 镜像安装 `v4l-utils` 和 `media-ctl`，提供 `v4l2-ctl`、`media-ctl`。

以上配置仅对 `zynq-xc7z020-custom` 生效。当前 PL 按 RGB565 解释输入，
请使用下面的 RGB565 模式测试；把传感器改成 YUV 会使颜色或数据布局错误。

## 编译与更新

在仓库根目录执行：

```sh
source ./edf-init-build-env build
bitbake edf-linux-disk-image
```

输出为 `build/tmp/deploy/images/zynq-xc7z020-custom/` 中的
`edf-linux-disk-image-zynq-xc7z020-custom.rootfs.wic`。
使用新整盘镜像更新 SD 卡，然后重启开发板。

**必须同步更新 BOOT.bin、内核和根文件系统。** 此工程的启动脚本向 Linux
传递 U-Boot control DT，只更新独立 `system.dtb` 无法保证新节点生效。
整盘镜像包含对应文件和当前 XSA 的 bitstream。

## 使用 v4l2-ctl 测试

在开发板执行，以下以 `/dev/media0`、`/dev/video0` 为例。若存在其他媒体
设备，按枚举结果替换设备编号。

```sh
sudo dmesg | grep -Ei 'ov2640|xilinx.*video|vdma|cma|dynclk'
v4l2-ctl --list-devices
sudo media-ctl -d /dev/media0 -p
sudo v4l2-ctl -d /dev/video0 --all
sudo v4l2-ctl -d /dev/video0 --list-formats-ext
sudo v4l2-ctl -d /dev/video0 \
  --set-fmt-video=width=800,height=480,pixelformat=RGBP \
  --get-fmt-video
```

预期日志识别出 OV2640 产品 ID；媒体图包含 `ov2640 ...-0030` 和
`ov2640-vipp output 0`，两者之间的链路为 enabled。
传感器 pad 格式为 `RGB565_2X8_BE/800x480`，采集格式为 `RGBP`，
`Number of planes` 为 `1`，`Bytes per Line` 为 `1600`，`Size Image` 为
`768000`。总线先发高字节，VDMA 写入 Zynq 小端内存后为 RGB565 小端文件，
这两种格式名称对应链路的不同位置。

先连续采集 60 帧，验证缓冲区和中断可以持续工作：

```sh
sudo timeout 30 v4l2-ctl -d /dev/video0 \
  --stream-mmap=4 --stream-count=60 --stream-poll
```

再跳过前 10 个已采集缓冲区，只保存一帧：

```sh
sudo timeout 30 v4l2-ctl -d /dev/video0 \
  --stream-mmap=4 --stream-skip=10 --stream-count=1 \
  --stream-to=/tmp/ov2640-800x480.rgb565 --stream-poll
wc -c /tmp/ov2640-800x480.rgb565
```

预期文件大小为 `768000` 字节，即 `800 × 480 × 2`。`Camera_Data_0` 本身
也会丢弃最初约 10 帧，开始采集时稍作等待属于正常现象。命令退出码应为 0；
`timeout` 返回 124 表示在 30 秒内未完成采集，需要按下面的步骤排查。

将原始文件复制到装有 FFmpeg 的电脑后，可查看图像或转换成 PNG：

```sh
ffplay -f rawvideo -pixel_format rgb565le -video_size 800x480 ov2640-800x480.rgb565
ffmpeg -f rawvideo -pixel_format rgb565le -video_size 800x480 \
  -i ov2640-800x480.rgb565 -frames:v 1 ov2640.png
```

## 排查

- **没有 `/dev/videoN`：** 检查是否使用新 BOOT.bin，运行中的设备树是否有
  `camera@30` 和 `ov2640-vipp`。检查 `dmesg` 中传感器识别和 VIPP probe
  信息；传感器注册失败会使媒体链路无法完成。
- **Product ID error / I2C 不响应：** 检查摄像头供电、排线、PS I2C0 的
  SCCB 接线、24 MHz XCLK、RESETB 高和 PWDN 低；核对动态时钟锁定情况。
  摄像头应由驱动绑定，勿在采集中强制用 I2C 工具访问传感器寄存器。
- **STREAMON 返回 Invalid argument：** 用 `media-ctl -p` 检查传感器 pad
  是否仍是 `RGB565_2X8_BE/800x480`，并重新设置采集格式为 `RGBP/800x480`。
  若之前修改过 sensor pad，可按实际 entity 名恢复，例如：

  ```sh
  sudo media-ctl -d /dev/media0 \
    --set-v4l2 '"ov2640 0-0030":0 [fmt:RGB565_2X8_BE/800x480 field:none]'
  ```

- **采集超时：** 查看 `/proc/interrupts` 中 VDMA S2MM 中断是否增长，
  用 ILA 检查 PCLK、Href、Vsync、AXIS `tvalid/tready/tlast/tuser`，核对
  当前加载的 bitstream 与仓库 XSA 一致。不要在采集时改变 HDMI 模式。
- **分配缓冲区失败：** 检查 `/proc/meminfo` 的 `CmaTotal/CmaFree`，减少
  占用连续内存的应用后重试。

构建和设备树验证无法替代板上采集测试；是否持续出帧、图像颜色是否正确，
需要以上述命令及实际画面确认。
