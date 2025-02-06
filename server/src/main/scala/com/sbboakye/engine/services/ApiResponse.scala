package com.sbboakye.engine.services

sealed trait ApiResponse

object ApiResponse:
  final case class Success[T](data: T)  extends ApiResponse
  final case class Error(error: String) extends ApiResponse
