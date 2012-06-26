package org.contourweb.client.view

import org.contourweb.common.model.model
import com.google.gwt.event.dom.client.ChangeEvent
import com.google.gwt.event.dom.client.ChangeHandler
import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.dom.client.ClickHandler
import com.google.gwt.event.dom.client.KeyCodes
import com.google.gwt.event.dom.client.KeyPressEvent
import com.google.gwt.event.dom.client.KeyPressHandler
import com.google.gwt.event.dom.client.KeyUpEvent
import com.google.gwt.event.dom.client.KeyUpHandler
import com.google.gwt.user.client.ui.Button
import com.google.gwt.user.client.ui.FlexTable
import com.google.gwt.user.client.ui.HorizontalPanel
import com.google.gwt.user.client.ui.Label
import com.google.gwt.user.client.ui.TextBox
import com.google.gwt.user.client.ui.VerticalPanel
import com.google.gwt.user.client.ui.Widget
import reactive._
import com.google.gwt.user.client.ui.UIObject


trait gwtWidgets extends widgets with gwtTemplate { this: model =>
  private[gwtWidgets] val log = java.util.logging.Logger.getLogger("gwtWidgets")

  type WidgetBuilder = GwtWidgetBuilder

  def WidgetBuilders(observing: Observing) = new GwtWidgetBuilders(observing)


trait GwtWidgetBuilder extends WidgetBuilderBase {
  type W <: Widget
  def build(template: Template): W
}

  
class GwtWidgetBuilders(obs: Observing) extends WidgetBuilders {
  implicit protected val observing = obs
    
  def Widget(widget: Widget) = new WidgetBuilder {
    type W = Widget
    val bindCss = ""
    def build(template: Template) = widget
  }

  def TextBox(text: Var[String],
              onChange: String=>Unit,
              onEnter: =>Unit,
              onKeyPress: KeyPressEvent=>Unit,
              bind: String,
              init: TextBox => Unit) = {
    newTextBox(text, Option(onChange), () => onEnter, Option(onKeyPress), bind, Option(init))
  }
  
  private def newTextBox(text: Var[String],
                         onChange: Option[String=>Unit],
                         onEnter: ()=>Unit,
                         onKeyPressF: Option[KeyPressEvent=>Unit],
                         bind: String,
                         init: Option[TextBox => Unit]) =
    new WidgetBuilder {
      type W = TextBox
      val bindCss = bind
      def build(template: Template) = {

        def bindToText(textBox: TextBox) {
          text.foreach(t => if (t != textBox.getText) textBox.setText(t))
          textBox.addKeyUpHandler(new KeyUpHandler {
            def onKeyUp(event: KeyUpEvent) {
              val t = textBox.getText
              if (t != text.now)
                text() = t
            }
          })
        }

        def addEventHandlers(textBox: TextBox) {
          onChange.foreach( f =>
            textBox.addChangeHandler(new ChangeHandler {
              def onChange(event: ChangeEvent) {
                f(textBox.getText)
              }
            })
          )
          textBox.addKeyPressHandler(new KeyPressHandler {
            def onKeyPress(event: KeyPressEvent) {
              onKeyPressF.foreach(_(event))
              if (event.getCharCode == KeyCodes.KEY_ENTER) {
                onEnter()
              }
            }
          })
        }
        
        val textBox = template match {
          case SimpleTemplate(element) => new TextBox(template.element) {}
          case NoTemplate => new TextBox
        }
        bindToText(textBox)
        addEventHandlers(textBox)
        init.foreach(_(textBox))
        textBox
    }
  }
  
  def Label(text: Signal[String], rendered: Signal[Boolean], css: Signal[String], bind: String) = new WidgetBuilder {
    type W = Label
    val bindCss = bind
    def build(template: Template) = {
      val label = template match {
        case SimpleTemplate(element) => new Label(template.element) {}
        case NoTemplate => new Label
      }
      
      text.foreach(label.setText)

      rendered.foreach{ isVisible =>
        label.setVisible(isVisible)
      }
      startUpdatingCssClass(css, label)
      
      label
    }
  }

  def Button(onClick: =>Unit,
             text: String,
             dependentCss: Signal[String],
             bind: String) = {
    newButton(() => onClick, text, dependentCss, bind)
  }
  
  private def newButton(onClickF: ()=>Unit,
                        text: String,
                        dependentCss: Signal[String],
                        bind: String = "") =
    new WidgetBuilder {
      type W = Button
      val bindCss = bind
      def build(template: Template) = {
        val button = new Button(text)
        button.addClickHandler(new ClickHandler {
          def onClick(event: ClickEvent) = onClickF()
        })
        startUpdatingCssClass[Button](dependentCss, button, _.addStyleDependentName, _.removeStyleDependentName)
        button
      }
    }

  def VerticalPanel(rendered: Signal[Boolean], bind: String)(children: WidgetBuilder*) = new WidgetBuilder {
    type W = VerticalPanel
    val bindCss = bind
    def build(template: Template) = {
      // TODO: If template tag has attributes, use them or report warnings. Everywhere.
      val panel = new VerticalPanel
      children.foreach{ child =>
        panel.add(template.buildChild(child))
      }
      rendered.foreach{ isVisible =>
        panel.setVisible(isVisible)
      }
      panel
    }
  }
  
  def HorizontalPanel(css: Signal[String], bind: String)(children: WidgetBuilder*) = new WidgetBuilder {
    type W = HorizontalPanel
    val bindCss = bind
    def build(template: Template): HorizontalPanel = {
      val panel = new HorizontalPanel
      children.foreach{ child =>
        panel.add(template.buildChild(child))
      }
      startUpdatingCssClass(css, panel)
      panel
    }
  }

  def FlexTable[T](seqSignal: SeqSignal[T],
                   rendered: Signal[Boolean],
                   cellPadding: Signal[Int],
                   css: Signal[String],
                   headerCss: Signal[String],
                   bind: String)
                  (columns: T=>List[FlexTableColumn]) = new WidgetBuilder {
    type W = FlexTable
    val bindCss = bind
    def build(template: Template) = {
      
      def startUpdatingCellCssClass(cssClass: Signal[String], row: Int, column: Int, flex: FlexTable) {
        startUpdatingCssClass[FlexTable](cssClass, flex,
               flex => flex.getCellFormatter.addStyleName(row, column, _),
               flex => flex.getCellFormatter.removeStyleName(row, column, _))
      }
      
      def setupHeader(flex: FlexTable) {
        startUpdatingCssClass(css, flex)
        try {
          startUpdatingCssClass[FlexTable](headerCss, flex,
                                           flex => flex.getRowFormatter.addStyleName(0, _),
                                           flex => flex.getRowFormatter.removeStyleName(0, _))
          columns(null.asInstanceOf[T]).zipWithIndex.foreach{case (column, colNum) =>
            column.title.foreach(title => flex.setText(0, colNum, title))
            startUpdatingCellCssClass(column.css, 0, colNum, flex)
          }
        } catch {
          case e: NullPointerException =>
            throw new IllegalStateException("Seems like you use FlexTable element in Column header definition." +
                  " It is impossible, because no element is available during header construction.", e)
        }
      }
      
      def setupBody(flex: FlexTable) {
        for {
          (row, rowNum) <- seqSignal.now.zipWithIndex
          (column, colNum) <- columns(row).zipWithIndex
        }{
          flex.setWidget(rowNum+1, colNum, template.buildChild(column.widget()))
        }        
      }
      
      def handleUpdate(delta: SeqDelta[T,T], flex: FlexTable): Unit = delta match {
        case Include(index, item) =>
          for ((column, colNum) <- columns(item).zipWithIndex) {
            flex.setWidget(index+1, colNum, template.buildChild(column.widget()))
            // TODO: There should be Observing for each row, which is automatically removed with the row
            // (and removes all its listeners), so that memory is freed more eagerly in some cases
            startUpdatingCellCssClass(column.css, index+1, colNum, flex)
          }
        case Update(index, oldItem, item) =>
          for ((column, colNum) <- columns(item).zipWithIndex) {
            flex.setWidget(index+1, colNum, template.buildChild(column.widget()))
          }
        case Remove(index, _) =>
          flex.removeRow(index+1)
        case Batch(ms @ _*) =>
          ms.foreach(handleUpdate(_, flex))
      }
      
      val flex = new FlexTable
      setupHeader(flex)
      setupBody(flex)
      seqSignal.deltas.foreach(handleUpdate(_, flex))

      rendered.foreach{ v =>
        flex.setVisible(v)
      }
      cellPadding.foreach{ v =>
        flex.setCellPadding(v)
      }
      
      flex
    }
  }


  def TextBoxValid(field: AnyModel#StringField,
                   onChange: String=>Unit,
                   onEnter: =>Unit,
                   onKeyPress: KeyPressEvent=>Unit,
                   bind: String,
                   init: TextBox => Unit
                  )(implicit form: Form) = {

    form.addModelToValidateOnSubmit(field.model)
    val fieldErrors = form.validationErrorsByField(field).map(_.mkString(", "))

    def initTextBox(textBox: TextBox) {
      fieldErrors.change.foreach(_ => textBox.selectAll)
      init(textBox)
    }

    VerticalPanel()(
      TextBox(field.asVar, onChange, onEnter, onKeyPress, bind, initTextBox),
      Label(fieldErrors)
    )
  }


  def startUpdatingCssClass(cssClass: Signal[String], widget: UIObject) {
    startUpdatingCssClass[UIObject](cssClass, widget, _.addStyleName, _.removeStyleName)
  }
  
  def startUpdatingCssClass[W](cssClass: Signal[String],
                               widget: W,
                               adder: W=>String=>Unit,
                               remover: W=>String=>Unit) {
    cssClass.foldLeft(""){ (oldClass, newClass) =>
      if (oldClass != "") remover(widget)(oldClass)
      if (newClass != "") adder(widget)(newClass)
      newClass
    }
  }
}
 
  
}
