package com.sbboakye.engine.config

import pureconfig.ConfigReader

case class DBConfig(
    nThreads: Int,
    driver: String,
    host: String,
    port: Int,
    database: String,
    user: String,
    password: String
) derives ConfigReader
