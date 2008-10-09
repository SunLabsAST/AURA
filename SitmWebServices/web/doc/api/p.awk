# script that takes artist list and makes
# 2 columns suitable for plotting
BEGIN {FS="\"";}
NF >= 6 { print ++count, $6}
