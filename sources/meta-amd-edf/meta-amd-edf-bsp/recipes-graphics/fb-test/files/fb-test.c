// SPDX-License-Identifier: MIT
#define _POSIX_C_SOURCE 200809L
#include <errno.h>
#include <fcntl.h>
#include <linux/fb.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <sys/mman.h>
#include <unistd.h>

static const unsigned char colors[][3] = {
    {255, 255, 255}, {255, 255, 0}, {0, 255, 255}, {0, 255, 0},
    {255, 0, 255}, {255, 0, 0}, {0, 0, 255}, {0, 0, 0},
};

int main(int argc, char **argv)
{
    struct fb_fix_screeninfo fix;
    struct fb_var_screeninfo var;
    const char *device = "/dev/fb0";
    unsigned char *mapping = MAP_FAILED;
    uint16_t *row = NULL;
    int use_write = 0, fd, ret = EXIT_FAILURE;

    if (argc > 1 && strcmp(argv[1], "--write") == 0) {
        use_write = 1;
        --argc;
        ++argv;
    }
    if (argc > 2 || (argc == 2 && argv[1][0] == '-')) {
        fprintf(stderr, "Usage: fb-test [--write] [/dev/fb0]\n");
        return EXIT_FAILURE;
    }
    if (argc == 2)
        device = argv[1];

    fd = open(device, O_RDWR);
    if (fd < 0) {
        perror(device);
        return EXIT_FAILURE;
    }
    if (ioctl(fd, FBIOGET_FSCREENINFO, &fix) < 0 ||
        ioctl(fd, FBIOGET_VSCREENINFO, &var) < 0) {
        perror("framebuffer information");
        goto out;
    }
    if (fix.type != FB_TYPE_PACKED_PIXELS || fix.visual != FB_VISUAL_TRUECOLOR ||
        var.bits_per_pixel != 16 || var.red.length != 5 ||
        var.green.length != 6 || var.blue.length != 5 ||
        var.red.offset != 11 || var.green.offset != 5 || var.blue.offset != 0 ||
        var.red.msb_right || var.green.msb_right || var.blue.msb_right ||
        !var.xres || !var.yres ||
        ((uint64_t)var.xoffset + var.xres) * 2 > fix.line_length ||
        ((uint64_t)var.yoffset + var.yres) * fix.line_length > fix.smem_len) {
        fprintf(stderr, "Expected a packed RGB565 framebuffer with valid stride and size\n");
        goto out;
    }

    printf("%s: %ux%u, RGB565, stride=%u, virtual=%ux%u, %s\n",
           device, var.xres, var.yres, fix.line_length,
           var.xres_virtual, var.yres_virtual, use_write ? "write" : "mmap");
    row = malloc((size_t)var.xres * sizeof(*row));
    if (!row) {
        perror("row allocation");
        goto out;
    }
    for (uint32_t x = 0; x < var.xres; ++x) {
        const unsigned char *rgb = colors[(uint64_t)x * 8 / var.xres];

        row[x] = ((rgb[0] >> 3) << 11) |
                 ((rgb[1] >> 2) << 5) | (rgb[2] >> 3);
    }
    if (!use_write) {
        mapping = mmap(NULL, fix.smem_len, PROT_READ | PROT_WRITE,
                       MAP_SHARED, fd, 0);
        if (mapping == MAP_FAILED) {
            perror("framebuffer mmap");
            goto out;
        }
    }
    for (uint32_t y = 0; y < var.yres; ++y) {
        size_t offset = ((size_t)y + var.yoffset) * fix.line_length +
                        (size_t)var.xoffset * 2;
        size_t length = (size_t)var.xres * 2;

        if (use_write) {
            size_t done = 0;

            while (done < length) {
                ssize_t n = pwrite(fd, (unsigned char *)row + done,
                                   length - done, offset + done);

                if (n < 0 && errno == EINTR)
                    continue;
                if (n <= 0) {
                    if (!n)
                        errno = EIO;
                    perror("framebuffer write");
                    goto out;
                }
                done += (size_t)n;
            }
        } else {
            memcpy(mapping + offset, row, length);
        }
    }
    ret = EXIT_SUCCESS;
out:
    if (mapping != MAP_FAILED)
        munmap(mapping, fix.smem_len);
    free(row);
    close(fd);
    return ret;
}
