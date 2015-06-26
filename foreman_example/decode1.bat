java -jar -Xms1024m -Xmx1024m QualnetTraceParser.jar 12 61 58 true 0.2 %1_%3m_seed%2_%3m.trace foreman_160frm_1000_wo9.trace
BitStreamExtractorStaticd foreman_160frm_1000.264 %1_%3m_seed%2_%3m.264 -et %1_%3m_seed%2_%3m_bs.btr
H264AVCDecoderLibTestStaticd %1_%3m_seed%2_%3m.264 %1_%3m_seed%2_%3m.yuv -ec 2
PSNRStaticd 352 288 foreman_cif.yuv %1_%3m_seed%2_%3m.yuv > %1_%3m_seed%2_%3m.txt

call cutLastLine %1_%3m_seed%2_%3m.txt >> psnr.txt

md %1_%3m_seed%2_%3m
move %1_%3m_seed%2_%3m* ./%1_%3m_seed%2_%3m