package de.tabmates.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TabMatesApplication

fun main(args: Array<String>) {
    runApplication<TabMatesApplication>(*args)
}
