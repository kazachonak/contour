package org.contourweb.client.view
import org.contourweb.common.model.model
import com.google.gwt.core.client.EntryPoint
import com.google.gwt.event.logical.shared.ValueChangeEvent
import com.google.gwt.event.logical.shared.ValueChangeHandler
import com.google.gwt.user.client.History
import com.google.gwt.user.client.ui.Panel
import com.google.gwt.user.client.ui.RootPanel


trait gwtView extends view with gwtWidgets { this: model =>
  private[gwtView] val log = java.util.logging.Logger.getLogger("gwtView")


trait GwtWindow extends Window {
  protected def panel: Panel
  
  protected def showViewImpl(view: View) {
    panel.clear
    val widget = view.widget.build(Template.byId(view.template))
    panel.add(widget)
  }
}


trait GwtRootWindow extends RootWindow
                       with GwtWindow
                       with EntryPoint {

  protected def panel = RootPanel.get
  
  def onModuleLoad {
    showView
  }
  
  History.addValueChangeHandler(new ValueChangeHandler[String] {
    def onValueChange(event: ValueChangeEvent[String]) {
      // TODO
    }
  })
  
  def createAndShowView(uri: List[String]) {
    // TODO
  }
  
  override def navigateTo(other: View) {
    super.navigateTo(other)
    History.newItem(other.uri.mkString("/"), false)
  }
}


}
