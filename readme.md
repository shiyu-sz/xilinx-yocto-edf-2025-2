
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

### 更新sxa文件
gen-machine-conf \
  --soc-family zynq \
  --hw-description ../vivado_project/design_1_wrapper.xsa \
  --machine-name zynq-xc7z020-custom \
  --config-dir ../sources/meta-amd-edf/meta-amd-edf-bsp/conf \
  --localconf conf/local.conf \
  --xsct-tool $HOME/Xilinx_Install/2025.2/Vitis
