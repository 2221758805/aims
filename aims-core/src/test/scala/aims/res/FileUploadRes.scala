package aims.res

import java.io.{ FileOutputStream, File }

import aims.core.RestletResult
import aims.http.AttachmentsUploadRestlet
import aims.model.Event
import aims.routing.PatternMatcher
import akka.actor.ActorSystem
import akka.http.model.{ StatusCodes, HttpResponse }
import akka.stream.ActorFlowMaterializer

/**
 * Component:
 * Description:
 * Date: 15/3/10
 * @author Andy Ai
 */
class FileUploadRes(implicit val materializer: ActorFlowMaterializer, implicit val system: ActorSystem) extends AttachmentsUploadRestlet {

  import system.dispatcher

  override def attachmentsPattern(): PatternMatcher = "files"

  override def saveAttachments(event: Event): RestletResult = {
    event.formData.get.parts.runForeach({ bodyPart ⇒
      try {
        val file = File.createTempFile("upload_test", bodyPart.filename.get)
        val outputStream = new FileOutputStream(file)
        bodyPart.entity.dataBytes.runForeach({ byteString ⇒
          outputStream.write(byteString.toByteBuffer.array())
        }).onComplete(_ ⇒ outputStream.close())
      } catch {
        case e: Throwable ⇒
          e.printStackTrace()
          HttpResponse(StatusCodes.InternalServerError)
      }
    })
  }
}
