#!/bin/bash

curl - s https: //api.github.com/repos/freight-trust/relayswap-demo/releases/latest | grep "relayswap-0.1.0.jar" | cut -d : -f 2,3| tr -d \" | wget -qi -
