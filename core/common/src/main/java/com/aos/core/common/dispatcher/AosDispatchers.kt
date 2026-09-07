package com.aos.core.common.dispatcher

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val aosDispatcher: AosDispatchers)

enum class AosDispatchers {
    Default,
    IO,
    Main
}
