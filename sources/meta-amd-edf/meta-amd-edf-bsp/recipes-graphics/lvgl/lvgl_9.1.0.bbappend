# Fetch the upstream recipe's pinned commit without its full Git history.
SRC_URI:remove = "git://github.com/lvgl/lvgl;protocol=https;branch=master"
SRC_URI:prepend = "https://codeload.github.com/lvgl/lvgl/tar.gz/${SRCREV};downloadfilename=lvgl-${SRCREV}.tar.gz;name=lvgl;subdir=git;striplevel=1 "
SRC_URI[lvgl.sha256sum] = "9074c31aa4ec77382b1a055a4d97081e565c96e530d0886acc798f047cc6ba63"

# Match the board's HDMI framebuffer: /dev/fb0, RGB565.
PACKAGECONFIG:zynq-xc7z020-custom = "fbdev"
LVGL_CONFIG_LV_COLOR_DEPTH:zynq-xc7z020-custom = "16"
