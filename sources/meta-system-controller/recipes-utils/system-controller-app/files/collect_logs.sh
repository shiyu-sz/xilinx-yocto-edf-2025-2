#! /bin/bash

#
# Copyright (c) 2023 - 2026 Advanced Micro Devices, Inc.  All rights reserved.
#
# SPDX-License-Identifier: MIT
#

set +e

SCAPP_DIR="/usr/share/system-controller-app"
SCAPP_LOGDIR="${SCAPP_DIR}/.sc_app/log"

rm -rf "${SCAPP_LOGDIR}" 2> /dev/null
mkdir -p "${SCAPP_LOGDIR}"

BOARD=$(sc_app -c board 2>/dev/null) || BOARD="unknown"
SN=$(sc_app -c geteeprom -t onboard -v summary 2>/dev/null | grep 'Board Serial Number' | awk '{print $4}')
[ -z "${SN}" ] && SN="unknown"

if [ "$BOARD" = "unknown" ] || [ "$SN" = "unknown" ]; then
    LOGNAME=log_$(date +"%d_%m_%Y-%H_%M")
else
    LOGNAME=log_${BOARD}_${SN}_$(date +"%d_%m_%Y-%H_%M")
fi

LOGDIR="${SCAPP_LOGDIR}"/"${LOGNAME}"
mkdir -p "${LOGDIR}"

#
# Collect logs
#
cp -ar /var/volatile/log "${LOGDIR}"/volatile_log
journalctl > "${LOGDIR}"/journal.log
journalctl -u system_controller > "${LOGDIR}"/sc_appd.log
dmesg > "${LOGDIR}"/dmesg.log
ps aux > "${LOGDIR}"/pslist.log
rpm -qa > "${LOGDIR}"/installed_packages.log
/usr/bin/version_info.sh > "${LOGDIR}"/version_info.log

for I in summary all common board multirecord; do
    sc_app -c geteeprom -t onboard -v "$I" >> "${LOGDIR}"/eeprom.log
done

EEPROM_PATHS=(/sys/bus/i2c/devices/*/eeprom_cc*/nvmem)
if [ -e "${EEPROM_PATHS[0]}" ]; then
    EEPROM="$(echo "${EEPROM_PATHS[0]}" | sed 's/_cc[0-9a-z/]*//')"
    dd if="${EEPROM}" of="${LOGDIR}"/eeprom.bin bs=1 count=256 2> /dev/null || true
fi

fw_printenv > "${LOGDIR}"/uboot.env || true

# Collect device tree
dtc -I fs -O dts /proc/device-tree -o "${LOGDIR}"/device_tree.log 2> /dev/null  || true

# Collect power logs
raft-pm-cmd powerlog --outfile "${LOGDIR}/power.log" 1> /dev/null 2> /dev/null || true

#
# Create a tarfile of logs
#
cd "${SCAPP_LOGDIR}"
tar zcf "${LOGNAME}".tar.gz "${LOGNAME}"
rm -rf "${LOGNAME}"

#
# Make the tarfile visible to the GUI
#
if cd /usr/share/scweb; then
    rm -rf ./static/tmp 2> /dev/null
    mkdir -p ./static/tmp
    ln -s "${SCAPP_LOGDIR}/${LOGNAME}".tar.gz ./static/tmp/.
    echo "./static/tmp/${LOGNAME}.tar.gz"
else
    echo "${SCAPP_LOGDIR}/${LOGNAME}.tar.gz"
fi
