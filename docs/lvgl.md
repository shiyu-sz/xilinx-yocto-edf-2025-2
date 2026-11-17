# LVGL 9.1（Zynq XC7Z020）

镜像安装 `lvgl` 库和 `lvgl-demo-fb` 控件演示程序。演示程序为 `/usr/bin/lvgl`，
使用现有 HDMI `/dev/fb0`，像素格式为 RGB565（16 bpp）。

## 配方和配置

上游配方位于 `sources/meta-openembedded/meta-oe/recipes-graphics/lvgl/`，
版本为 9.1.0。板级配置位于
`sources/meta-amd-edf/meta-amd-edf-bsp/recipes-graphics/lvgl/`：

- `lvgl_9.1.0.bbappend`：库使用 `fbdev` 后端和 16 位色深。
- `lvgl-demo-fb_9.1.0.bbappend`：演示程序使用相同显示配置。
- 镜像的 `edf-linux-disk-image.bbappend` 将库和演示程序加入 rootfs。

演示程序还带有 CMake 修复：仅为启用的后端查找 DRM/SDL 依赖，
因此 framebuffer 配置不需要安装这些显示库。

这些显示配置仅对 `zynq-xc7z020-custom` 生效。演示配方自带 LVGL 源码，
单独编译库配方不会改变演示程序内的配置，两者需分别配置。
下载使用上游配方锁定提交的源码归档，避免获取完整 Git 历史；原配方补丁保留。

## 手动准备源码

主库固定版本的下载地址：

<https://codeload.github.com/lvgl/lvgl/tar.gz/e1c0b21b2723d391b885de4b2ee5cc997eccca91>

不需要解压，将完整文件保存为：

```text
/home/sy/downloads_edf_25.11.1/downloads/lvgl-e1c0b21b2723d391b885de4b2ee5cc997eccca91.tar.gz
```

演示程序源码归档也需放在同一目录，下载地址为：

<https://codeload.github.com/lvgl/lv_port_linux_frame_buffer/tar.gz/dccc6a1ca48372aa993dbea7a8e17dec6f42df6a>

保存为：

```text
lvgl-demo-dccc6a1ca48372aa993dbea7a8e17dec6f42df6a.tar.gz
```

主库归档的 SHA256 已固定在两个 `.bbappend` 中：

```text
9074c31aa4ec77382b1a055a4d97081e565c96e530d0886acc798f047cc6ba63
```

手动下载后可使用 `tar -tzf` 检查归档完整性，并用 `sha256sum` 核对上述校验值。

## 编译

在仓库根目录执行：

```sh
source ./edf-init-build-env build
bitbake lvgl lvgl-demo-fb
bitbake edf-linux-disk-image
```

当前构建目录的 `build/conf/local.conf` 允许联网：

```bitbake
BB_NO_NETWORK = "0"
```

下载缓存仍使用 `${HOME}/downloads_edf_25.11.1/downloads`。完成所需构建后，
可将该值改为 `"1"`，恢复离线构建。创建新构建目录所用的
`sources/meta-amd-edf/conf/templates/default/local.conf.sample` 保持离线默认值 `"1"`。
若以后增加软件包或更换版本，还需要补齐相应源码和依赖缓存。

新 SD 卡镜像位于：

```text
build/tmp/deploy/images/zynq-xc7z020-custom/edf-linux-disk-image-zynq-xc7z020-custom.rootfs.wic
```

使用新镜像更新 SD 卡。HDMI 链路、启动文件和显示排查见
[HDMI framebuffer](hdmi-framebuffer.md)。

## 在开发板运行

连接 HDMI 显示器，从串口或 SSH 执行：

```sh
ls -l /dev/fb0
cat /sys/class/graphics/fb0/bits_per_pixel
sudo fb-test
sudo env LV_VIDEO_CARD=/dev/fb0 /usr/bin/lvgl
```

位深应为 `16`。先确认彩条正常，再运行控件演示。演示程序前台运行，
自动轮播控件界面，按 Ctrl+C 退出；退出后最后一帧可能留在屏幕上。

该示例先验证显示，鼠标或触摸交互需要另行创建设备对应的 LVGL 输入实例。
不要同时运行其他写入同一 framebuffer 的程序。

## 自定义应用

应用配方使用 `DEPENDS = "lvgl"`，通过 Yocto sysroot 获取目标平台的库和头文件。
应用负责显示初始化、时钟、UI 创建及定期调用 `lv_timer_handler()`。
需要交互时再接入对应的 evdev 输入设备。将应用包加入镜像即可随系统交付。
