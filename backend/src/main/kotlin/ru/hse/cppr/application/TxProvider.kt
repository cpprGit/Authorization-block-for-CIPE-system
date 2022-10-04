package ru.hse.cppr.application

import arrow.Kind
import arrow.core.left
import arrow.core.right
import arrow.fx.IO
import arrow.fx.typeclasses.Concurrent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jooq.Configuration
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.io.Closeable
import javax.sql.DataSource

class TxProvider<F> constructor(val MA: Concurrent<F>, val source: DataSource) {

    fun <A> tx(f: (Configuration) -> A): Kind<F, A> {

        return MA.async { cb ->
            val deferred = GlobalScope.async(Dispatchers.IO) {
                try {
                    DSL.using(source, SQLDialect.POSTGRES).transaction { configuration ->
                        cb(f(configuration).right())
                    }
                } catch (t: Throwable) {
                    cb(t.left())
                }
            }

            MA.later(deferred::cancel)
        }
    }

    fun release() {
        if (source is Closeable) {
            source.close()
        }
    }
}
