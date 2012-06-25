package org.contourweb.common.model

import reflect.Manifest
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import java.util.Calendar
import java.util.Date


trait fields { this: model =>

  // Those dependencies have different implementations for client and server
  def NumberFormat(template: String): NumberFormat
  def DecimalNumberFormat: NumberFormat
  
  trait NumberFormat {
    def format(number: Double): String
  }
  

trait MetaModelFields { meta: MetaModel =>


trait BasicFields { model: M =>
  
  trait RequiredField extends Field {
    type V = T
    def isOptional = false
    
    def value: Option[T] = Some(is)
    def value(option: Option[T]) = option match {
      case Some(t) => apply(t)
      case None => apply(defaultValue)
    }
  }
  
  trait OptionalField extends Field {
    type V = Option[T]
    def isOptional = true
    def defaultValue: V = None
    
    def value: Option[T] = is
    def value(option: Option[T]) = apply(option)
  }


  class StringField extends StringFieldBase with RequiredField {
    def defaultValue = ""
  }
  
  class StringOptionField extends StringFieldBase with OptionalField
  
  trait StringFieldBase extends Field {
    type T = String
    def maxLength = 1024
  }
  
  
  class BooleanField extends BooleanFieldBase with RequiredField {
    def defaultValue = false
  }
  
  class BooleanOptionField extends BooleanFieldBase with OptionalField
  
  trait BooleanFieldBase extends Field {
    type T = Boolean
  }


  class DoubleField extends DoubleFieldBase with RequiredField {
    def defaultValue = .0
  }

  class DoubleOptionField extends DoubleFieldBase with OptionalField
  
  trait DoubleFieldBase extends Field {
    type T = Double

    def format = ""

    // TODO: Cache it by default
    def numberFormat = if (format == "") DecimalNumberFormat
                       else              NumberFormat(format)

    override def toString = value.map(numberFormat.format) getOrElse ""
  }
}


}


// TODO: Make a generic implicit in Scala 2.10
implicit def fieldToValue(field: AnyModel#StringField): String = field.is
implicit def fieldToValue(field: AnyModel#BooleanField): Boolean = field.is
implicit def fieldToValue(field: AnyModel#DoubleField): Double = field.is


}
