package com.hps.simulator.profile;

public class TerminalProfile {

    private String name;
    private String termId;
    private String outletNo;
    private String termAddr;
    private String mcc;
    private String termData;

    // optionnels
    private String tmk;
    private String tpk;
    private String acquirerId;
    private String paramFile;
    private String trnFile;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    public String getOutletNo() {
        return outletNo;
    }

    public void setOutletNo(String outletNo) {
        this.outletNo = outletNo;
    }

    public String getTermAddr() {
        return termAddr;
    }

    public void setTermAddr(String termAddr) {
        this.termAddr = termAddr;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getTermData() {
        return termData;
    }

    public void setTermData(String termData) {
        this.termData = termData;
    }

    public String getTmk() {
        return tmk;
    }

    public void setTmk(String tmk) {
        this.tmk = tmk;
    }

    public String getTpk() {
        return tpk;
    }

    public void setTpk(String tpk) {
        this.tpk = tpk;
    }

    public String getAcquirerId() {
        return acquirerId;
    }

    public void setAcquirerId(String acquirerId) {
        this.acquirerId = acquirerId;
    }

    public String getParamFile() {
        return paramFile;
    }

    public void setParamFile(String paramFile) {
        this.paramFile = paramFile;
    }

    public String getTrnFile() {
        return trnFile;
    }

    public void setTrnFile(String trnFile) {
        this.trnFile = trnFile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}