#!/bin/sh
exec java -Xshare:auto -XX:TieredStopAtLevel=1 -jar /app/FiscalCode.jar
