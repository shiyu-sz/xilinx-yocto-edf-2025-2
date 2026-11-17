SUMMARY = "RGB565 framebuffer color bars for the custom Zynq board"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://fb-test.c"
S = "${WORKDIR}"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} -Wall -Wextra -o fb-test fb-test.c
}

do_install() {
    install -Dm 0755 fb-test ${D}${bindir}/fb-test
}
