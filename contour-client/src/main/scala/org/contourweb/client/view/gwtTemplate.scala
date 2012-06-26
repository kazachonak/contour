package org.contourweb.client.view
import java.util.logging.Logger
import com.google.gwt.dom.client.Element
import com.google.gwt.query.client.GQuery.$
import com.google.gwt.user.client.DOM
import com.google.gwt.user.client.ui.HTMLPanel
import com.google.gwt.user.client.ui.Widget


trait gwtTemplate { this: gwtWidgets =>
  private[gwtTemplate] val log = Logger.getLogger("gwtTemplate")


object Template {
  def byId(id: String) = {
    if (id == "") {
      NoTemplate
    } else {
      val panel = new HTMLPanel(DOM.getElementById(id).getInnerHTML)
      new SimpleTemplate(panel.getElement)
    }
  }
}

trait Template {
  def element: Element
   // TODO: Return childBuilder.W in Scala 2.10
  def buildChild(childBuilder: WidgetBuilder): Widget
   // TODO: Return childBuilder.W in Scala 2.10
  def buildChildInsideHtml(childBuilder: WidgetBuilder): Widget
  def html: HTMLPanel 
  def copy: Template
}


object NoTemplate extends Template {
  def copy = this
  def buildChild(childBuilder: WidgetBuilder) = childBuilder.build(this)
  def buildChildInsideHtml(childBuilder: WidgetBuilder) = requiresRealTemplateError
  def html = requiresRealTemplateError
  def element = requiresRealTemplateError
  private def requiresRealTemplateError =
    throw new IllegalStateException("Some widget requires HTML template to be set on the View to be used")
}

case class SimpleTemplate(element: Element) extends Template {

  def copy = new SimpleTemplate(element.cloneNode(true).cast[Element])
  
  def buildChild(childBuilder: WidgetBuilder) = {
    val childElement = byCssSelector(childBuilder.bindCss, element)
    childBuilder.build(new SimpleTemplate(childElement))
  }
  
  lazy val html = new HTMLPanel(element.toString)
  
  def buildChildInsideHtml(childBuilder: WidgetBuilder) = {
    val childElement = byCssSelector(childBuilder.bindCss, html.getElement)
    val childWidget = childBuilder.build(new SimpleTemplate(childElement))
    // TODO: Seems like there is GWT bug in this method. No logical attach?
    html.addAndReplaceElement(childWidget, childElement)
    childWidget
  }

  private def byCssSelector(selector: String, el: Element): Element = {
    val found = $(selector, el).get()
    if (found.getLength > 0)
      found.getItem(0)
    else
      throw new IllegalStateException("Element '"+selector+"' not found inside of '"+element.getNodeName+"': "+element.getInnerHTML)
  }
} 
  
}
