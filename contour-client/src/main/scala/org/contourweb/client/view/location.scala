package org.contourweb.client.view

import java.util.logging.Logger
import org.contourweb.common.model.model


trait location { this: view =>
  private[location] val log = Logger.getLogger("location")


trait LocatedView {
  def uri: List[String] = Nil
}


}
