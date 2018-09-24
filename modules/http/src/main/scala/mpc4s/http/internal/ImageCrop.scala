package mpc4s.http.internal

import javax.imageio.ImageIO
import java.awt.{Image}
import java.awt.image.BufferedImage
import java.nio.file.Path
import cats.effect.Sync
import cats.implicits._

/** Resize and crop images.
  *
  * Images are resized preserving their aspect ratio and then cropped
  * to create a squared image. It uses standard Java utilities.
  */
object ImageCrop {

  def resize[F[_]: Sync](img: BufferedImage, size: Int): F[BufferedImage] =
    Sync[F].delay(cropSquare(img, size))

  /** Loads the given image file into memory.
    */
  def loadImage[F[_]: Sync](file: Path): F[BufferedImage] =
    Sync[F].delay(ImageIO.read(file.toFile))

  /** Write the given image to the given file in JPEG format.
    *
    * The image should be created with `TYPE_3BYTE_BGR`.
    */
  def writeJpeg[F[_]: Sync](img: BufferedImage, out: Path): F[Unit] =
    Sync[F].delay(ImageIO.write(img, "jpg", out.toFile)).map(_ => ())

  /** Loads the file `in` in memory and resizes the image (if necessary)
    * to be `size x size`. The result is written to file `out`.
    */
  def resizeFile[F[_]: Sync](in: Path, out: Path, size: Int): F[Unit] =
    loadImage(in.toAbsolutePath).
      flatMap(img => resize(img, size)).
      flatMap(img => writeJpeg(img, out.toAbsolutePath))

  private sealed trait Crop
  private object Crop {
    case object None extends Crop
    case object XAxis extends Crop
    case object YAxis extends Crop
  }

  private def cropSquare(img: BufferedImage, size: Int): BufferedImage = {
    val (w, h) = (img.getWidth, img.getHeight)
    if (w == h) {
      if (w <= size) img
      else scaleTo(img, size, Crop.None)
    } else if (w > h) {
      // w > size && h > size   => scale by w/size and crop at x-axis
      // if one is <= size, we know that h < w, so create hxh image and crop at x-axis
      if (w > size && h > size) scaleTo(img, size, Crop.XAxis)
      else crop(img, Crop.XAxis)
    } else { // w < h
      // w > size && h > size   => scale by h/size and crop at y-axis
      // if one is <= size, we know that w<h, so create a wxw image and crop at y-axis
      if (w > size && h > size) scaleTo(img, size, Crop.YAxis)
      else crop(img, Crop.YAxis)
    }
  }

  private def scaleTo(img: BufferedImage, size: Int, crop: Crop): BufferedImage = {
    val (w, h) = (img.getWidth, img.getHeight)
    val factor =
      if (w > h) size.toDouble / h.toDouble
      else size.toDouble / w.toDouble

    val width = if (h > w) size else (w * factor).toInt
    val height = if (w > h) size else (h * factor).toInt
    val large = math.max(width, height)

    val tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
    val resized = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR)
    val g2 = resized.createGraphics

    crop match {
      case Crop.None =>
        g2.drawImage(tmp, 0, 0, null)
        g2.dispose

      case Crop.XAxis =>
        g2.drawImage(tmp, -1 * (large - size) / 2, 0, null)
        g2.dispose

      case Crop.YAxis =>
        g2.drawImage(tmp, 0, -1 * (large - size) / 2, null)
        g2.dispose
    }

    resized
  }

  private def crop(img: BufferedImage, crop: Crop): BufferedImage = {
    val size = math.min(img.getHeight, img.getWidth)
    val large = math.max(img.getHeight, img.getWidth)

    crop match {
      case Crop.None =>
        img

      case Crop.XAxis =>
        val tmp = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR)
        val g2 = tmp.createGraphics
        g2.drawImage(img, (large - size) / -2, 0, null)
        g2.dispose
        tmp

      case Crop.YAxis =>
        val tmp = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR)
        val g2 = tmp.createGraphics
        g2.drawImage(img, 0, (large - size) / -2, null)
        g2.dispose
        tmp
    }
  }
}
