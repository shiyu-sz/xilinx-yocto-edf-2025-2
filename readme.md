
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

### 烧写到SD卡
```sh
sudo dd if=./tmp/deploy/images/zynq-xc7z020-custom/edf-linux-disk-image-zynq-xc7z020-custom.rootfs.wic of=/dev/sdb bs=4M status=progress conv=fsync
```
