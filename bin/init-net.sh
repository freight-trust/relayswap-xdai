#!/bin/sh
# SPDX-License-Identifier: MIT
# Copyright 2020 - FreightTrust and Clearing Corporation
# This is the `NETWORK WARNING BANNER` 
# This MUST be displayed upon ACCESSING ANY COMPUTING RESOURCES
# This script generates a timestamp (local) and saves it as `.env_network_banner`
#
echo -e "\033[31;3m████████████████████████████████████████████████████████████████████████████████████████████"
echo -e "\033[31;3m██ THIS IS A NOTICE OF MONITORING OF THE FREIGHT TRUST NETWORK (FTN) INFORMATION SYSTEMS  ██"
echo -e "\033[31;3m████████████████████████████████████████████████████████████████████████████████████████████"
sleep 2
echo -e "\033[31;3m████████████████████████████████████████████████████████████████████████████████████████████"
echo -e "\033[31;3m██   BY CONNECTING TO AND/OR USING THIS 'NETWORK' YOU CERTIFY THAT YOU AGREE TO ABIDE     ██"
echo -e "\033[31;3m██      BY THE RULES OF BEHAVIOR AND THE OMNIBUS RULEBOOK v5.0.0 & WARNING BANNER.        ██"
echo -e "\033[31;3m████████████████████████████████████████████████████████████████████████████████████████████"
sleep 3
# generate local timestamp & save as env_network_banner
now=$(date '+%Y-%m-%d %H:%M:%S')
