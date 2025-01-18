package com.transit.train.dto;

import lombok.Getter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

@Getter
public class TrainDto {

    private Integer beginRow;

    private Integer endRow;

    private Integer curPage;

    private Integer pageRow;

    private Integer totalCount;

    private Integer rowNum;

    private Integer selectedCount;

    private String subwayId;

    private String subwayNm;

    private String updnLine;

    private String trainLineNm;

    private String subwayHeading;

    private String statnFid;

    private String statnTid;

    private String statnId;

    private String statnNm;

    private String trainCo;

    private String trnsitCo;

    private String ordkey;

    private String subwayList;

    private String statnList;

    private String btrainSttus;

    private Integer barvlDt;

    private String btrainNo;

    private String bstatnId;

    private String bstatnNm;

    private String recptnDt;

    private String arvlMsg2;

    private String arvlMsg3;

    private Integer arvlCd;

    private Integer lstcarAt;

    public SubwayTrainData toSubwayTrainData() {
        return SubwayTrainData
                .newBuilder()
                .setBeginRow(beginRow)
                .setEndRow(endRow)
                .setCurPage(curPage)
                .setPageRow(pageRow)
                .setTotalCount(totalCount)
                .setRowNum(rowNum)
                .setSelectedCount(selectedCount)
                .setSubwayId(subwayId)
                .setSubwayNm(subwayNm)
                .setUpdnLine(updnLine)
                .setTrainLineNm(trainLineNm)
                .setSubwayHeading(subwayHeading)
                .setStatnFid(statnFid)
                .setStatnTid(statnTid)
                .setStatnId(statnId)
                .setStatnNm(statnNm)
                .setTrainCo(trainCo)
                .setTrnsitCo(trainCo)
                .setOrdkey(ordkey)
                .setSubwayList(subwayList)
                .setStatnList(statnList)
                .setBtrainSttus(btrainSttus)
                .setBarvlDt(barvlDt)
                .setBtrainNo(btrainNo)
                .setBstatnId(bstatnId)
                .setBstatnNm(bstatnNm)
                .setRecptnDt(recptnDt)
                .setArvlMsg2(arvlMsg2)
                .setArvlMsg3(arvlMsg3)
                .setArvlCd(arvlCd)
                .setLstcarAt(lstcarAt)
                .build();
    }
}
