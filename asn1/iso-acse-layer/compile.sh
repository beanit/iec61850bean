#!/bin/sh

rm ../../src/main/java-gen/org/openmuc/josistack/internal/acse/asn1/*

jasn1-compiler -o "../../src/main/java-gen/" -p "com.beanit.josistack.internal.acse" -f iso-acse-layer.asn
