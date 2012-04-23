#!/bin/bash

while getopts ":ac" opt; do
  case $opt in
    a)
	scp -i ~/.ssh/kirillkuvaldin.pem build.xml ubuntu@snappersserver.emerginggames.com:snappersbackend/
	scp -i ~/.ssh/kirillkuvaldin.pem -r lib/* ubuntu@snappersserver.emerginggames.com:snappersbackend/lib/
	scp -i ~/.ssh/kirillkuvaldin.pem -r config/* ubuntu@snappersserver.emerginggames.com:snappersbackend/config/
      ;;
    c)
	scp -i ~/.ssh/kirillkuvaldin.pem -r config/* ubuntu@snappersserver.emerginggames.com:snappersbackend/config/
      ;;
    \?)
      ;;
  esac
done

scp -i ~/.ssh/kirillkuvaldin.pem -r build/jar/* ubuntu@snappersserver.emerginggames.com:snappersbackend/build/jar/
