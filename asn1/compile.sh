#!/bin/bash

cd `dirname $0`

rm ../src/main/java-gen/com/beanit/josistack/internal/acse/asn1/*
asn1bean-compiler -o "../src/main/java-gen/" -p "com.beanit.josistack.internal.acse" -f iso-acse-layer.asn -dv

rm ../src/main/java-gen/com/beanit/josistack/internal/presentation/asn1/*
asn1bean-compiler -o "../src/main/java-gen/" -p "com.beanit.josistack.internal.presentation" -f iso-presentation-layer.asn -dv

rm -r ../src/main/java-gen/com/beanit/iec61850bean/internal/mms/asn1/*
asn1bean-compiler -o "../src/main/java-gen" -p "com.beanit.iec61850bean.internal.mms" -f mms.asn -dv
