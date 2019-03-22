#!/bin/bash

cd `dirname $0`

rm ../../src/main/java-gen/com/beanit/josistack/internal/acse/asn1/*

jasn1-compiler -o "../../src/main/java-gen/" -p "com.beanit.josistack.internal.acse" -f iso-acse-layer.asn -dv
