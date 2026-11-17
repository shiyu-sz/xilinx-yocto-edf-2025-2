# HDMI framebuffer（Zynq XC7Z020）

当前 BSP 使用 Xilinx DRM 的 fbdev 接口提供 `/dev/fb0`。默认输出
1280×720、60 Hz、RGB565（16 bpp），PS 串口仍为控制台。
`/dev/fb` 不是本工程创建的设备名，应用应打开 `/dev/fb0`。

移植参考：`../petalinux2020-1-acz702` 中的
`1a8ec47ef62b96a97e756bdc7402ec70efc47237`（HDMI 显示）和
`75bb9a494b7197bd5d59fb1fe63b63da1864803f`（当前 RGB565 数据路径）。
沿用参考工程的 `sil,sii9022` 驱动绑定，芯片位于 PS I2C1 地址 `0x39`。

## 显示路径和适配

```text
DDR RGB565 framebuffer
  -> AXI VDMA MM2S（0x43000000，DMA 通道 0）
  -> rgb565to888 -> AXI4-Stream to Video Out -> 板级 RGB 接口
  -> SII9022 -> HDMI

axi_dynclk（0x43c10000）-> 像素时钟
VTC（0x43c20000）     -> 视频时序
PS I2C1              -> SII9022 配置和显示器 EDID
```

BSP 所在目录：`sources/meta-amd-edf/meta-amd-edf-bsp/`。

- `recipes-kernel/linux/files/hdmi-display.cfg` 启用 DRM、VTC、PL display、
  SII9022、fbdev、DMA 和 64 MiB CMA。
- `0001-clk-port-digilent-axi-dynclk-to-linux-6.12.patch` 移植 Digilent
  动态时钟驱动，保留硬件 BUFR 除以 5 的逻辑，增加锁定超时。
- `0002-drm-xlnx-connect-sii9022-and-enable-fbdev-io.patch` 提供 HDMI encoder
  component，加入 Linux 6.12 的 Xilinx component 匹配列表，补齐 fbdev 的
  `read`、`write` 和 GEM DMA `mmap` 操作。当前 VTC 驱动已有像素时钟设置逻辑。
- `recipes-bsp/device-tree/files/zynq-xc7z020-hdmi.dtsi` 描述显示链路，
  选用 `RG16`（RGB565），通过 `video=HDMI-A-1:1280x720-16@60D` 设置默认模式。
- 镜像安装 `fb-test`，可用 mmap 或 write 显示八色彩条。

当前 Vivado 工程已包含上述 IP，无需为这次软件适配重新修改硬件。

## 编译和启动

在仓库根目录运行：

```sh
source ./edf-init-build-env build
bitbake edf-linux-disk-image
```

输出目录为 `build/tmp/deploy/images/zynq-xc7z020-custom/`。使用新生成的
`.rootfs.wic` 更新 SD 卡，连接 HDMI 显示器后启动。

**需要一起更新 BOOT.bin。** 当前 EDF Zynq 启动脚本把 U-Boot 的 control DT
传给 Linux，因此 BSP 同时为 `device-tree` 和 `uboot-device-tree` 加入 HDMI
设备树片段；只替换 `zImage` 或独立 `system.dtb` 不能保证启动时使用新节点。
整盘镜像会包含相应启动文件和内核。

上板检查：

```sh
cat /proc/cmdline
zcat /proc/config.gz | grep -E 'CONFIG_(COMMON_CLK_DGLNT_DYNCLK|DRM_XLNX|DRM_SII902X|DRM_FBDEV_EMULATION|FB=)'
dmesg | grep -Ei 'drm|fb0|sii902|dynclk|vtc|vdma|cma'
cat /proc/fb
ls -l /dev/fb0
cat /sys/class/graphics/fb0/virtual_size
cat /sys/class/graphics/fb0/bits_per_pixel
cat /sys/class/drm/card*-HDMI-A-*/status
```

预期位深为 `16`，可见画面为 1280×720。Xilinx fbdev 默认分配双倍虚拟高度，
因此 `virtual_size` 可能是 `1280,1440`，这是供翻页使用的内存。

## 显示彩条

```sh
# 映射显存后写入彩条，覆盖当前可见画面。
sudo fb-test

# 通过设备文件的 write 路径写入同样的彩条。
sudo fb-test --write
```

两种方式均应显示白、黄、青、绿、紫、红、蓝、黑八条竖向色带。
测试工具读取 fbdev ioctl 信息，按实际 `line_length`、`xoffset` 和 `yoffset`
写入，而不是假设显存每行都紧密排列。

应用程序可通过 `FBIOGET_FSCREENINFO` 和 `FBIOGET_VSCREENINFO` 获取显存大小、
行跨度和 RGB 位域，用 `mmap(MAP_SHARED)` 或 `write/pwrite` 更新像素。
当前 RGB565 编码为红色 `0xf800`、绿色 `0x07e0`、蓝色 `0x001f`；
Zynq 的小端内存中红色的两个字节为 `00 f8`。

直接写原始图像时，文件必须匹配实际分辨率、RGB565 格式和行跨度。
若命令使用普通 shell 的 `>` 重定向，`sudo` 不会自动提升重定向权限，
可改用 `sudo sh -c 'cat image.rgb565 > /dev/fb0'`。

## 排查

- **没有 `/dev/fb0`：** 先确认运行的新内核及 BOOT.bin，查看
  `/sys/firmware/devicetree/base/pl-bus/hdmi-pl-disp` 和
  `/sys/firmware/devicetree/base/axi/i2c@e0005000/hdmi-bridge@39` 是否存在。
  再检查日志中 SII9022、VTC 和 PL display 是否成功 probe。
- **I2C 芯片未响应：** 检查板上芯片型号、供电、复位及 PS I2C1 接线。
  这里按参考工程使用 `0x39`，不声明没有证据的复位 GPIO。
- **已有 fb0 但黑屏：** 执行 `sudo fb-test`，核对 HDMI 输入源、显示器支持的
  模式、bitstream 和 VDMA MM2S 中断。`/proc/interrupts` 中对应 SPI 30
  （硬件中断 ID 62）的计数应在扫描期间增长。
- **图像颜色或行错位：** 确认图像为 RGB565，并使用 ioctl 返回的行跨度；
  RGB888/RGBA8888 数据不能直接写入本工程的 16 位显存。
- **彩条被其他程序覆盖：** 停止占用同一 DRM 显示设备的图形程序后再测试。

需要修改分辨率时，更改上述 DTS 的 `video=` 参数并重新生成启动镜像；
所选模式必须在板上像素时钟、VDMA 和显示器支持范围内。
软件构建和设备树检查无法替代实际开发板上的 HDMI 收发验证。
