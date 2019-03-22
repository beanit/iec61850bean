#!/bin/bash

cd `dirname $0`

rm -r ../../src/main/java-gen/com/beanit/openiec61850/internal/mms/asn1/*

jasn1-compiler -o "../../src/main/java-gen" -p "com.beanit.openiec61850.internal.mms" -f mms.asn -dv
