package com.sbboakye.engine.domain

case class Schedule(
    cronExpression: String,
    timezone: String
)
