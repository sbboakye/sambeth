package com.sbboakye.engine.config

case class DBConfig(
    nThreads: Int,
    driver: String,
    host: String,
    port: Int,
    database: String,
    user: String,
    password: String
)
