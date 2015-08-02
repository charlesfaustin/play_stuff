package s3

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.BasicAWSCredentials
import scala.util.{Success, Failure}
import scala.concurrent._
import java.util.concurrent.atomic.AtomicInteger
import play.api.libs.concurrent.Execution.Implicits._

object s3helper {
  val bucketName = "cfgplaytest"
  val s3region = "eu-west-1"

  //MOVE AWS CREDENTIALS INTO SCALA FILE OUTSIDE OF SOURCE CONTROL
  val AWS_ACCESS_KEY = ""
  val AWS_SECRET_KEY = ""
  //maybe put the below into own object, use in function
  val placeToMoveFile = "/Users/Charles/seven/hey/public/up/"

  val yourAWSCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
  val amazonS3Client = new AmazonS3Client(yourAWSCredentials)

  def fileRename(filename:String):String = filename.replace(" ","_").replace("'","")
  
  def allSucceed[T](fs: List[scala.concurrent.Future[T]]) = {
    val remaining = new AtomicInteger(fs.length)

    val p = promise[T]

    fs foreach {
      _ onComplete {
        case s @ Success(_) => {
          if (remaining.decrementAndGet() == 0) {
            // Arbitrarily return the final success
            p tryComplete s
          }
        }
        case f @ Failure(_) => {
          p tryComplete f
        }
      }
    }

    p.future
  }

  


}

