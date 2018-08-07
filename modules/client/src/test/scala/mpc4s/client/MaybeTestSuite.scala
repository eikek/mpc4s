package mpc4s.client

import minitest._
import minitest.api.Void

trait MaybeTestSuite extends SimpleTestSuite {

  def disabled: Boolean

  override def test(name: String)(f: => Void): Unit = {
    if (!disabled) {
      super.test(name)(f)
    }
  }
}
