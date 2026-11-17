FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# AXI UART16550 in the custom Zynq PL design uses the generic 8250 OF driver.
SRC_URI:append:zynq-xc7z020-custom = " file://axi-uart16550.cfg"

# RGB565 VDMA scanout through the board's SII9022 HDMI transmitter.
SRC_URI:append:zynq-xc7z020-custom = " \
    file://hdmi-display.cfg \
    file://0001-clk-port-digilent-axi-dynclk-to-linux-6.12.patch \
    file://0002-drm-xlnx-connect-sii9022-and-enable-fbdev-io.patch \
"

# OV2640 DVP -> fixed RGB565 PL pipeline -> VDMA S2MM capture.
SRC_URI:append:zynq-xc7z020-custom = " \
    file://ov2640-camera.cfg \
    file://0003-media-enable-ov2640-rgb565-vdma-capture.patch \
"
