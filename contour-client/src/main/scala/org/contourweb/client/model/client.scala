package org.contourweb.client.model

import java.util.logging.Logger
import org.contourweb.common.model.model
import com.google.gwt.i18n.client.{NumberFormat => GwtNumberFormat}


trait client { this: model =>
  private[client] val log = Logger.getLogger("client")

  def isClient = true

  def NumberFormat(template: String) = new ClientNumberFormat(GwtNumberFormat.getFormat(template)) 
  def DecimalNumberFormat = new ClientNumberFormat(GwtNumberFormat.getDecimalFormat)

  class ClientNumberFormat(target: GwtNumberFormat) extends NumberFormat {
    def format(number: Double) = target.format(number)
  }


}
