SET count = 1
FOR /L %%G IN (1,1,9) DO call decode1_FEC.bat %1 %2 0%%G

call decode1_FEC.bat %1 %2 10