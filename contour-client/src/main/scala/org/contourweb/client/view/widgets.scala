package org.contourweb.client.view

import scala.collection.mutable.HashSet
import org.contourweb.common.model.model
import com.google.gwt.event.dom.client.KeyPressEvent
import com.google.gwt.user.client.ui.TextBox
import reactive._


trait widgets { this: model =>

  type WidgetBuilder <: WidgetBuilderBase

  def WidgetBuilders(observing: Observing): WidgetBuilders


trait WidgetBuilderBase {
  val bindCss: String
}


trait WidgetBuilders {
  implicit protected def observing: Observing
  
  def TextBox(text: Var[String],
              onChange: String=>Unit = null,
              onEnter: =>Unit = (),
              onKeyPress: KeyPressEvent=>Unit = null,
              bind: String = "",
              init: TextBox=>Unit = null
             ): WidgetBuilder

  def Label(text: Signal[String],
            rendered: Signal[Boolean] = Val(true),
            css: Signal[String] = Val(""),
            bind: String = ""): WidgetBuilder
  
  def Button(onClick: =>Unit = (),
             text: String = "",
             dependentCss: Signal[String] = Val(""),
             bind: String = ""): WidgetBuilder
  
  def VerticalPanel(rendered: Signal[Boolean] = Val(true), bind: String = "")(children: WidgetBuilder*): WidgetBuilder

  def HorizontalPanel(css: Signal[String] = Val(""), bind: String = "")(children: WidgetBuilder*): WidgetBuilder

  def FlexTable[T](seqSignal: SeqSignal[T],
                   rendered: Signal[Boolean] = Val(true),
                   cellPadding: Signal[Int] = Val(0),
                   headerCss: Signal[String] = Val(""),
                   bind: String = "")
                  (columns: T=>List[FlexTableColumn])
                  : WidgetBuilder
                  
  def Column(title: Signal[String], css: Signal[String] = Val(""))(widget: =>WidgetBuilder) = {
    new FlexTableColumn(title, css, () => widget)
  }
  
  case class FlexTableColumn(title: Signal[String], css: Signal[String], val widget: ()=>WidgetBuilder)


  def TextBoxValid(field: AnyModel#StringField,
                   onChange: String=>Unit = null,
                   onEnter: =>Unit = (),
                   onKeyPress: KeyPressEvent=>Unit = null,
                   bind: String = "",
                   init: TextBox=>Unit = null
                  )(implicit form: Form): WidgetBuilder

  def Form(onSubmit: =>Unit)(child: Form=>WidgetBuilder) = child(new Form(() => onSubmit))

  class Form(onSubmit: ()=>Unit) {
    private val modelSet = HashSet[AnyModel]()
    private val errors = Var[Map[Option[AnyField],List[String]]](Map())

    def onSubmitErrors: Signal[Seq[String]] = onSubmitErrors0
    private val onSubmitErrors0: Var[Seq[String]] = Var(Nil)
    
    def addModelToValidateOnSubmit(model: AnyModel) {
      modelSet += model
    }
    
    def submit: Boolean = {
      val valid = validate
      if (valid) {
        try {
          onSubmit()
        } catch {
          case ValidationErrors(messages) =>
          onSubmitErrors0() = messages
        }
      }
      valid
    }
    
    def validate: Boolean = {
      val errorMap = modelSet.flatMap(_.validate.asInstanceOf[Map[Option[AnyField],List[String]]]).toMap
      errors() = errorMap
      errorMap.isEmpty
    }
    
    def validationErrorsByField(chosenField: AnyModel#Field): Signal[List[String]] =
      errors.map{ errorMap =>
        val errorList = errorMap.collect{case (Some(field), errors) if field == chosenField => errors}.headOption
        errorList getOrElse Nil
      }
  }


  implicit def valueToSignal[T](value: T): Signal[T] = Val(value) 
  implicit def fieldToSignal[F<:AnyField](field: F): Signal[field.V] = Val(field.is)
}


}
