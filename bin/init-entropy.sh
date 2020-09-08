#!/bin/sh
# SPDX-License-Identifier: ISC 
# SPDX-Copyright: 2020 FreightTrust and Clearing

echo -e "generating entropy..."
sudo rngd -r /dev/urandom
#gpg --gen-key
cat /dev/random | rngtest -c 1000 >> rngtest.txt
cat /proc/sys/kernel/random/entropy_avail  >> entropyavail.txt
echo -e "entropy script complete results produced in .txt files"
