package mpc4s.client

import scala.concurrent.ExecutionContext
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, ThreadFactory}

final class ThreadPools(namePrefix: String) {

  implicit val ecGlobal = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool(new ThreadFactory() {
    private val counter = new AtomicLong(0)
    def newThread(r: Runnable) = {
      val t = new Thread(r, s"${namePrefix}-${counter.getAndIncrement}")
      t.setDaemon(true)
      t
    }
  }))

  implicit val ACG = AsynchronousChannelGroup.withThreadPool(ecGlobal) // http.server requires a group

}

object ThreadPools {

  def apply(namePrefix: String): ThreadPools =
    new ThreadPools(namePrefix)
}
