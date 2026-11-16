
### 编译
source ./edf-init-build-env build
bitbake edf-linux-disk-image

离线配置保存在 `sources/meta-amd-edf/conf/templates/default/local.conf.sample`，
删除 `build` 后重新初始化仍然生效。源码和缓存使用
`${HOME}/downloads_edf_25.11.1/downloads` 和 `${HOME}/downloads_edf_25.11.1/sstate-cache`。
XSCT 直接使用 `${HOME}/Xilinx_Install/2025.2/Vitis` 中已安装的工具，
不需要下载 `xsct_2025.2.tar.xz`；`BB_NO_NETWORK = "1"` 保持禁网。

XSCT 配方版本固定为 2025.2，目标配方通过构建依赖提前安装 Yocto 的 tar-native，
构建、安装、ptest 文件安装和打包阶段均优先使用它，
避免本机 tar 1.34 在 pseudo 下报 `unknown directory` / `Bad address`。
`DEPENDS` 和 `PACKAGE_DEPENDS` 中均保留 `tar-replacement-native`：前者用于
构建和安装阶段，后者确保 packagegroup 等配方在打包时也不会被移除该工具。
XSCT 设备树生成还需要 `device-tree-xlnx` 的 2025.2 源码；它和
`meta-xilinx-tools` layer、Vitis 内置的 `system-device-tree-xlnx` 是不同的源码。
将 `device-tree-xlnx-xlnx_rel_v2025.2.zip` 保存在项目根目录下的
`downloads/` 中。BSP layer 中的
`recipes-bsp/device-tree/*.bbappend` 会让 Linux 和 U-Boot 的设备树配方
直接使用该本地 ZIP，不再获取 `device-tree-xlnx` Git 仓库。

### 更新XSA文件
gen-machine-conf \
  --soc-family zynq \
  --hw-description ../vivado_project/design_1_wrapper.xsa \
  --machine-name zynq-xc7z020-custom \
  --config-dir ../sources/meta-amd-edf/meta-amd-edf-bsp/conf \
  --localconf conf/local.conf \
  --xsct-tool $HOME/Xilinx_Install/2025.2/Vitis

### PL 扩展串口：AXI UART16550

当前 `design_1_wrapper.xsa` 已包含 `axi_uart16550_0`，使用 Linux 自带的
8250/16550 驱动（`drivers/tty/serial/8250/8250_of.c`），无需修改驱动 C 源码。
检查现有部署产物时，`system.dtb` 已包含完整串口节点，但 `zImage` 中的
`CONFIG_SERIAL_8250` 未启用。

BSP 中的 `recipes-kernel/linux/linux-xlnx_%.bbappend` 为
`zynq-xc7z020-custom` 添加 `files/axi-uart16550.cfg`，内建
`CONFIG_SERIAL_8250=y` 和 `CONFIG_SERIAL_OF_PLATFORM=y`，并保留 4 个端口槽位。
当前 `serial1` 别名需要编号 1 的槽位，不能将端口数设为 1。
PS 控制台继续使用 `ttyPS0`，PL 串口用于普通收发。

当前 XSA 和已生成设备树的关键参数如下，无需重复添加节点：

```dts
/* 已生成节点的关键属性摘录，位于 /pl-bus 下。 */
serial@43c00000 {
    compatible = "xlnx,xps-uart16550-2.00.a", "ns16550a";
    reg = <0x43c00000 0x10000>;
    reg-offset = <0x1000>;
    reg-shift = <2>;
    clock-frequency = <100000000>;
    interrupt-parent = <&intc>;
    interrupts = <0 29 4>;
};
```

`reg-offset` 对应 IP 内 UART 寄存器从 `0x1000` 开始，`reg-shift = <2>`
对应寄存器间隔 4 字节。当前 IP 未启用外部 XIN，波特率基准为 AXI 的
100 MHz；不要使用参数中未启用的外部时钟值 25 MHz。
中断连接为 `ip2intc_irpt -> xlconcat_0/In0 -> IRQ_F2P[0]`，对应
GIC SPI 29（硬件中断 ID 61），高电平触发。

在项目根目录编译并检查最终内核配置：

```sh
source ./edf-init-build-env build
bitbake edf-linux-disk-image
```

使用新镜像启动开发板后执行：

```sh
zcat /proc/config.gz | grep -E 'CONFIG_SERIAL_(8250|OF_PLATFORM)'
dmesg | grep -E 'ttyS|43c0|16550'
cat /proc/tty/driver/serial
```

按当前 `/aliases/serial1`，预计设备为 `/dev/ttyS1`。以启动日志中
`16550A` 和 `0x43c01000` 对应的设备名为准；仅出现 `ttyS*` 文件不能证明硬件已识别。

当前引脚约束为 TX=`K14`、RX=`L15`，电平标准 `LVCMOS33`。
连接 3.3 V USB-TTL 时，板端 TX 接适配器 RX，板端 RX 接适配器 TX，并共地。
设置适配器为 115200、8N1、关闭流控；板端运行：

```sh
stty -F /dev/ttyS1 115200 cs8 -cstopb -parenb -crtscts clocal cread -ixon -ixoff raw -echo
printf 'PL UART test\r\n' > /dev/ttyS1
cat /dev/ttyS1
```

最后一条命令用于接收适配器发来的数据，按 Ctrl+C 退出。
也可以断开适配器，将板端 TX/RX 短接进行回环：先在一个终端运行
`cat /dev/ttyS1`，再在另一个终端运行上述 `printf`。
若日志未识别设备，先检查实际加载的 DTB、PL bitstream、时钟和复位；
若发送正常但接收无响应，检查接线并对照 `/proc/interrupts` 确认 UART 中断是否增长。
Vivado 设计变更后需重新导出含 bitstream 的 XSA，再执行上面的
`gen-machine-conf` 命令更新硬件描述，重新构建镜像。

### 烧写到SD卡
sudo dd if=./tmp/deploy/images/zynq-xc7z020-custom/edf-linux-disk-image-zynq-xc7z020-custom.rootfs.wic of=/dev/sdb bs=4M status=progress conv=fsync
