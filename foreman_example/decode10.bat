SET count = 1
FOR /L %%G IN (1,1,9) DO call decode1.bat %1 %2 0%%G

call decode1.bat %1 %2 10