package com.hdu.miaoshaserver.error;

public interface CommonError {

    int getErrCode();

    String getErrMsg();

    CommonError setErrMsg(String errMsg);
}
