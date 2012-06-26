package org.contourweb.client.view

import com.google.gwt.core.client.EntryPoint
import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.dom.client.ClickHandler
import com.google.gwt.user.client.ui.Button
import com.google.gwt.user.client.Window
import com.google.gwt.user.client.ui.RootPanel
import scala.collection.mutable.ListBuffer
import scala.collection.mutable
import com.google.gwt.core.client.GWT
import com.google.gwt.core.client.RunAsyncCallback
import com.google.gwt.user.client.History
import com.google.gwt.user.client.HistoryListener
import com.google.gwt.user.client.ui.FlowPanel
import com.google.gwt.user.client.ui.TextBox
import com.google.gwt.event.dom.client.ChangeHandler
import com.google.gwt.event.dom.client.ChangeEvent
import com.google.gwt.user.client.ui.HTML
import com.google.gwt.user.client.ui.Widget
import com.google.gwt.user.client.ui.SimplePanel
import scala.collection.mutable.Buffer
import com.google.gwt.user.client.ui.Panel
import com.google.gwt.user.client.ui.HTMLPanel
import scala.collection.generic.Growable
import com.google.gwt.user.client.ui.VerticalPanel
import com.google.gwt.user.client.ui.FlexTable
import com.google.gwt.event.dom.client.KeyPressEvent
import com.google.gwt.event.dom.client.KeyPressHandler
import com.google.gwt.user.client.ui.HorizontalPanel
import com.google.gwt.user.client.ui.Label
import reactive._
import com.google.gwt.user.client.ui.CheckBox
import org.contourweb.common.model.model
import java.util.logging.Logger
import com.google.gwt.user.client.Element


trait view extends location with widgets { this: model =>
  private[view] val log = Logger.getLogger("view")


trait Window {
  def main: View
  
  private var view: View = main
  
  def navigateTo(other: View) {
    require(!other.isShown, "This view have been shown already")
    view.removeAllListeners
    view = other
    showView
  }
  
  protected def showView {
    showViewImpl(view)
    view.window = Some(this)
    view.onLoad
  }
  
  protected def showViewImpl(view: View)
  // TODO: Implement non-GWT-specific templating for in-JVM testing purposes
}


abstract class View(val template: String = "") extends LocatedView with Observing {
  private[view] var window: Option[Window] = None
  
  def isShown = window.isDefined
  
  def show(other: View) {
    require(window.isDefined, "View can't be shown. No associated window found.")
    window.get.navigateTo(other)
  }
  
  def onLoad {}

  val Widgets = WidgetBuilders(this)

  val widget: WidgetBuilder
}


trait RootWindow extends Window {
  rootWindow = Some(this)
}

private[view] var rootWindow: Option[RootWindow] = None

def RootWindow: RootWindow = {
  assert(rootWindow.isDefined, "RootWindow is not created")
  rootWindow.get
}


}
