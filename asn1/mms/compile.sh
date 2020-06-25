#!/bin/bash

cd `dirname $0`

rm -r ../../src/main/java-gen/com/beanit/iec61850bean/internal/mms/asn1/*

asn1bean-compiler -o "../../src/main/java-gen" -p "com.beanit.iec61850bean.internal.mms" -f mms.asn -dv
