package com.xjeffrose.xio.server;

public enum XioEvent {
  BLOCK_READ,
  UNBLOCK_READ,
  BLOCK_READCOMPLETE,
  UNBLOCK_READCOMPLETE,
  BLOCK_WRITE,
  UNBLOCK_WRITE,
  BLOCK_ACTIVE,
  UNBLOCK_ACTIVE,
  BLOCK_INACTIVE,
  UNBLOCK_INACTIVE,
  REQUEST_SENT,
  RESPONSE_RECIEVED,
  REQUEST_ERROR,
  RESPONSE_ERROR,
  REQUEST_SUCCESS,
  RESPONSE_SUCCESS,
  RATE_LIMIT,
  BLOCK_REQ_POLICY_BASED,
  BLOCK_REQ_BEHAVIORAL_BASED
}
