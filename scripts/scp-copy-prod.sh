#!/bin/bash

scp -P10822 ../biller-web/build/libs/*.war  root@37.187.153.76:/var/lib/tomcat7/
