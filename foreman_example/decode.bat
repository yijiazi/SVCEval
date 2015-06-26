java -jar QualnetTraceParser.jar 12 61 58 true 0.2 %1_01m_seed%2_01m.trace football_y00_v98_160frms_1000_rf1_wo9_.trace
BitStreamExtractorStaticd football_y00_v98_160frms_1000_rf1.264 %1_01m_seed%2_01m.264 -et %1_01m_seed%2_01m_bs.tr
H264AVCDecoderLibTestStaticd %1_01m_seed%2_01m.264 %1_01m_seed%2_01m.yuv -ec 2
PSNRStaticd 352 288 football_420_352x288_60.yuv %1_01m_seed%2_01m.yuv > %1_01m_seed%2_01m.txt
cutLastLine %1_01m_seed%2_01m.txt >> psnr.txt

BitStreamExtractorStaticd football_y00_v98_160frms_1000_rf1.264 %1_02m_seed%2_02m.264 -et %1_02m_seed%2_02m_bs.tr
H264AVCDecoderLibTestStaticd %1_02m_seed%2_02m.264 %1_02m_seed%2_02m.yuv -ec 2
PSNRStaticd 352 288 football_420_352x288_60.yuv %1_02m_seed%2_02m.yuv > %1_02m_seed%2_02m.txt
cutLastLine %1_02m_seed%2_02m.txt >> psnr.txt

BitStreamExtractorStaticd football_y00_v98_160frms_1000_rf1.264 %1_03m_seed%2_03m.264 -et %1_03m_seed%2_03m_bs.tr
H264AVCDecoderLibTestStaticd %1_03m_seed%2_03m.264 %1_03m_seed%2_03m.yuv -ec 2
PSNRStaticd 352 288 football_420_352x288_60.yuv %1_03m_seed%2_03m.yuv > %1_03m_seed%2_03m.txt

BitStreamExtractorStaticd football_y00_v98_160frms_1000_rf1.264 %1_04m_seed%2_04m.264 -et %1_04m_seed%2_04m_bs.tr
H264AVCDecoderLibTestStaticd %1_04m_seed%2_04m.264 %1_04m_seed%2_04m.yuv -ec 2
PSNRStaticd 352 288 football_420_352x288_60.yuv %1_04m_seed%2_04m.yuv > %1_04m_seed%2_04m.txt

BitStreamExtractorStaticd football_y00_v98_160frms_1000_rf1.264 %1_05m_seed%2_05m.264 -et %1_05m_seed%2_05m_bs.tr
H264AVCDecoderLibTestStaticd %1_05m_seed%2_05m.264 %1_05m_seed%2_05m.yuv -ec 2
PSNRStaticd 352 288 football_420_352x288_60.yuv %1_05m_seed%2_05m.yuv > %1_05m_seed%2_05m.txt

BitStreamExtractorStaticd football_y00_v98_160frms_1000_rf1.264 %1_06m_seed%2_06m.264 -et %1_06m_seed%2_06m_bs.tr
H264AVCDecoderLibTestStaticd %1_06m_seed%2_06m.264 %1_06m_seed%2_06m.yuv -ec 2
PSNRStaticd 352 288 football_420_352x288_60.yuv %1_06m_seed%2_06m.yuv > %1_06m_seed%2_06m.txt

BitStreamExtractorStaticd football_y00_v98_160frms_1000_rf1.264 %1_07m_seed%2_07m.264 -et %1_07m_seed%2_07m_bs.tr
H264AVCDecoderLibTestStaticd %1_07m_seed%2_07m.264 %1_07m_seed%2_07m.yuv -ec 2
PSNRStaticd 352 288 football_420_352x288_60.yuv %1_07m_seed%2_07m.yuv > %1_07m_seed%2_07m.txt

BitStreamExtractorStaticd football_y00_v98_160frms_1000_rf1.264 %1_08m_seed%2_08m.264 -et %1_08m_seed%2_08m_bs.tr
H264AVCDecoderLibTestStaticd %1_08m_seed%2_08m.264 %1_08m_seed%2_08m.yuv -ec 2
PSNRStaticd 352 288 football_420_352x288_60.yuv %1_08m_seed%2_08m.yuv > %1_08m_seed%2_08m.txt

BitStreamExtractorStaticd football_y00_v98_160frms_1000_rf1.264 %1_09m_seed%2_09m.264 -et %1_09m_seed%2_09m_bs.tr
H264AVCDecoderLibTestStaticd %1_09m_seed%2_09m.264 %1_09m_seed%2_09m.yuv -ec 2
PSNRStaticd 352 288 football_420_352x288_60.yuv %1_09m_seed%2_09m.yuv > %1_09m_seed%2_09m.txt

BitStreamExtractorStaticd football_y00_v98_160frms_1000_rf1.264 %1_10m_seed%2_10m.264 -et %1_10m_seed%2_10m_bs.tr
H264AVCDecoderLibTestStaticd %1_10m_seed%2_10m.264 %1_10m_seed%2_10m.yuv -ec 2
PSNRStaticd 352 288 football_420_352x288_60.yuv %1_10m_seed%2_10m.yuv > %1_10m_seed%2_10m.txt

