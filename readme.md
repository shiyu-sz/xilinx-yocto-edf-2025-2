
### 编译
```sh
source ./edf-init-build-env build
bitbake edf-linux-disk-image
```

### 更新XSA文件
```sh
gen-machine-conf \
  --soc-family zynq \
  --hw-description ../vivado_project/design_1_wrapper.xsa \
  --machine-name zynq-xc7z020-custom \
  --config-dir ../sources/meta-amd-edf/meta-amd-edf-bsp/conf \
  --localconf conf/local.conf \
  --xsct-tool $HOME/Xilinx_Install/2025.2/Vitis
```

### 测试串口
```sh
stty -F /dev/ttyS1 115200 cs8 -cstopb -parenb -crtscts clocal cread -ixon -ixoff raw -echo
printf 'PL UART test\r\n' > /dev/ttyS1
cat /dev/ttyS1
```

### HDMI Framebuffer
```sh
modetest -M xlnx -s 34:1280x720-60@RG16
sudo fb-test
```

### LVGL 9.1 控件演示
镜像配置已加入 LVGL 9.1.0 库和演示程序，使用 HDMI `/dev/fb0`、RGB565。
首次编译需按下方说明准备完整源码归档。
```sh
sudo env LV_VIDEO_CARD=/dev/fb0 /usr/bin/lvgl
```
构建、联网开关和配置见 [LVGL 使用说明](docs/lvgl.md)。

### OV2640 摄像头
默认 800×480、RGB565（`RGBP`）。更新完整 SD 镜像后在开发板测试：
```sh
v4l2-ctl --list-devices
sudo media-ctl -d /dev/media0 -p
sudo v4l2-ctl -d /dev/video0 --set-fmt-video=width=800,height=480,pixelformat=RGBP --get-fmt-video
sudo timeout 30 v4l2-ctl -d /dev/video0 --stream-mmap=4 --stream-count=60 --stream-poll
sudo timeout 30 v4l2-ctl -d /dev/video0 --stream-mmap=4 --stream-skip=10 --stream-count=1 --stream-to=/tmp/ov2640.rgb565 --stream-poll
wc -c /tmp/ov2640.rgb565
```
单帧预期为 768000 字节；设备编号按枚举结果选择。采集时勿切换 HDMI 显示模式。
详见 [摄像头接线、驱动适配和排查](docs/ov2640-camera.md)。

### 烧写到SD卡
```sh
sudo dd if=./tmp/deploy/images/zynq-xc7z020-custom/edf-linux-disk-image-zynq-xc7z020-custom.rootfs.wic of=/dev/sdb bs=4M status=progress conv=fsync
```
