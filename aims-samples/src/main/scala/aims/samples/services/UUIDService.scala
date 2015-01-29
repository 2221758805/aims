package aims.samples.services

import java.util.UUID

/**
 * Component:
 * Description:
 * Date: 2014/12/17
 * @author Andy Ai
 */
class UUIDService extends Service {

  override def pattern(): String = {
    "/uuid/gen"
  }

  override def handler: Handler = {
    case _ ⇒ UUID.randomUUID().toString
  }
}
