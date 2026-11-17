# Use the same pinned commits and patches as the upstream recipe, without Git history.
SRC_URI:remove = " \
    git://github.com/lvgl/lv_port_linux_frame_buffer.git;protocol=https;branch=master;name=demo \
    git://github.com/lvgl/lvgl;protocol=https;branch=master;name=lvgl;subdir=git/lvgl \
"
SRC_URI:prepend = " \
    https://codeload.github.com/lvgl/lv_port_linux_frame_buffer/tar.gz/${SRCREV_demo};downloadfilename=lvgl-demo-${SRCREV_demo}.tar.gz;name=demo;subdir=git;striplevel=1 \
    https://codeload.github.com/lvgl/lvgl/tar.gz/${SRCREV_lvgl};downloadfilename=lvgl-${SRCREV_lvgl}.tar.gz;name=lvgl;subdir=git/lvgl;striplevel=1 \
"
SRC_URI[demo.sha256sum] = "01c1669b5bea2f7f907b757aae1d8fa278613d3c5b38be44078524efdf42dd72"
SRC_URI[lvgl.sha256sum] = "9074c31aa4ec77382b1a055a4d97081e565c96e530d0886acc798f047cc6ba63"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append = " file://0001-cmake-only-find-enabled-display-backends.patch"
EXTRA_OECMAKE:append = " -DLV_PORT_USE_DRM=${LVGL_CONFIG_USE_DRM} -DLV_PORT_USE_SDL=${LVGL_CONFIG_USE_SDL}"

# Use the existing HDMI fbdev path instead of the recipe's default DRM backend.
PACKAGECONFIG:zynq-xc7z020-custom = "fbdev"
LVGL_CONFIG_LV_COLOR_DEPTH:zynq-xc7z020-custom = "16"
