package mpc4s.protocol.codec

import codecs._

package object implicits
    extends HListLineCodec
    with CoproductLineCodec
    with linecodec
    with syntax.linecodec
