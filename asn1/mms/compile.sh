#!/bin/sh

rm -r ../../src/main/java-gen/*

jasn1-compiler -o "../../src/main/java-gen" -p "org.openmuc.openiec61850.internal.mms" -f mms.asn
